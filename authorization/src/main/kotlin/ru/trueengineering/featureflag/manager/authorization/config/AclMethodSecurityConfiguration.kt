package ru.trueengineering.featureflag.manager.authorization.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.AclPermissionCacheOptimizer
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.acls.domain.AclAuthorizationStrategy
import org.springframework.security.acls.domain.ConsoleAuditLogger
import org.springframework.security.acls.domain.DefaultPermissionFactory
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy
import org.springframework.security.acls.domain.EhCacheBasedAclCache
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl
import org.springframework.security.acls.domain.PermissionFactory
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.acls.model.AclService
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.springframework.security.acls.model.PermissionGrantingStrategy
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.util.Assert
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import java.util.Objects
import javax.sql.DataSource


@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
@Import(AclBeanConfiguration::class)
open class AclMethodSecurityConfiguration : GlobalMethodSecurityConfiguration() {

    @Autowired
    private lateinit var aclService: AclService
    @Autowired
    private lateinit var permissionFactory: PermissionFactory
    @Autowired
    private lateinit var objectIdentityRetrievalStrategy: ObjectIdentityRetrievalStrategy

    override fun createExpressionHandler(): MethodSecurityExpressionHandler? {

        val permissionEvaluator = AclPermissionEvaluator(aclService)
        permissionEvaluator.setPermissionFactory(permissionFactory)
        permissionEvaluator.setObjectIdentityRetrievalStrategy(objectIdentityRetrievalStrategy)

        val aclPermissionCacheOptimizer = AclPermissionCacheOptimizer(aclService)
        aclPermissionCacheOptimizer.setObjectIdentityRetrievalStrategy(objectIdentityRetrievalStrategy)

        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(permissionEvaluator)
        expressionHandler.setPermissionCacheOptimizer(aclPermissionCacheOptimizer)
        return expressionHandler
    }
}

@Configuration
class AclBeanConfiguration {


    @Autowired
    private val dataSource: DataSource? = null
    @Bean
    fun aclService(
        @Autowired lookupStrategy: LookupStrategy,
        @Autowired aclCache: AclCache
    ): JdbcMutableAclService? {
        val jdbcMutableAclService = JdbcMutableAclService(dataSource, lookupStrategy, aclCache)
        jdbcMutableAclService.setAclClassIdSupported(true)
        // необходимые настройки для PostgreSQL
        // https://docs.spring.io/spring-security/reference/servlet/appendix/database-schema.html#_postgresql
        jdbcMutableAclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))")
        jdbcMutableAclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))")
        return jdbcMutableAclService
    }

    @Bean
    fun lookupStrategy(
        @Autowired aclAuthorizationStrategy: AclAuthorizationStrategy,
        @Autowired aclCache: AclCache
    ): LookupStrategy? {
        val basicLookupStrategy =
            BasicLookupStrategy(dataSource, aclCache, aclAuthorizationStrategy, ConsoleAuditLogger())
        basicLookupStrategy.setAclClassIdSupported(true);
        basicLookupStrategy.setPermissionFactory(permissionFactory())
        return basicLookupStrategy
    }
    @Bean
    fun permissionFactory(): PermissionFactory {
        return DefaultPermissionFactory(CustomPermission::class.java)
    }

    @Bean
    fun aclCache(
        @Autowired aclAuthorizationStrategy: AclAuthorizationStrategy
    ): AclCache? {
        return EhCacheBasedAclCache(
            Objects.requireNonNull(aclEhCacheFactoryBean().getObject()),
            permissionGrantingStrategy(),
            aclAuthorizationStrategy
        )
    }

    @Bean
    fun aclEhCacheFactoryBean(): EhCacheFactoryBean {
        val ehCacheFactoryBean = EhCacheFactoryBean()
        ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject()!!)
        ehCacheFactoryBean.setCacheName("aclCache")
        return ehCacheFactoryBean
    }

    @Bean
    fun aclCacheManager(): EhCacheManagerFactoryBean {
        return EhCacheManagerFactoryBean()
    }

    @Bean
    fun permissionGrantingStrategy(): PermissionGrantingStrategy? {
        return DefaultPermissionGrantingStrategy(ConsoleAuditLogger())
    }

    @Bean
    fun aclAuthorizationStrategy(@Autowired roleDefiner: RoleDefiner): AclAuthorizationStrategy? {
        return AuthorizationStrategy(roleDefiner)
    }

    @Bean fun objectIdentityRetrievalStrategy() = object : ObjectIdentityRetrievalStrategyImpl() {
        override fun getObjectIdentity(domainObject: Any?): ObjectIdentity {
            Assert.notNull(domainObject, "object cannot be null")
            domainObject as BusinessEntity
            return createObjectIdentity(domainObject.getBusinessId(), domainObject.getType())
        }
    }
}
