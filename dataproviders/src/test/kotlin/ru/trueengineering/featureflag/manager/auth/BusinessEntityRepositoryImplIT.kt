package ru.trueengineering.featureflag.manager.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.AclService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.config.AuthorizationConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest
import java.security.Principal

@ContextConfiguration(classes = [AuthorizationConfiguration::class, TestRoleDefiner::class])
internal class BusinessEntityRepositoryImplIT(
    @Autowired override var uut: BusinessEntityRepository,
    @Autowired var aclService: AclService
    ) :
    JpaRepositoryBaseTest<BusinessEntityRepository>() {

    private val aclClass: String = "ru.trueengineering.featureflag.manager.core.domen.organization.Organization"

    @BeforeEach
    internal fun setUp() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(Principal { "user" }, null,
                listOf(SimpleGrantedAuthority(ADMIN_ROLE))
            )

    }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should save without parent`() {
        val businessEntity = Organization(10L, "name")
        uut.createBusinessEntity(businessEntity)

        val actual = aclService.readAclById(ObjectIdentityImpl(aclClass, 10L))
        assertThat(actual).isNotNull
        assertThat(actual.parentAcl).isNull()
        assertThat(actual.isEntriesInheriting).isTrue
        assertThat(actual.objectIdentity.type).isEqualTo(aclClass)
        assertThat(actual.objectIdentity.identifier).isEqualTo(10L)
        assertThat(actual.owner).isEqualTo(GrantedAuthoritySid(ADMIN_ROLE))
    }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should save with parent`() {
        val parentEntity = Organization(10L, "name_1")
        val businessEntity = Organization(20L, "name_2")
        uut.createBusinessEntity(parentEntity)

        uut.createBusinessEntity(businessEntity, parentEntity)

        val actual = aclService.readAclById(ObjectIdentityImpl(aclClass, 20L))
        assertThat(actual).isNotNull
        assertThat(actual.parentAcl).isNotNull
        assertThat(actual.isEntriesInheriting).isTrue
        assertThat(actual.objectIdentity.type).isEqualTo(aclClass)
        assertThat(actual.objectIdentity.identifier).isEqualTo(20L)
        assertThat(actual.owner).isEqualTo(GrantedAuthoritySid(ADMIN_ROLE))

        assertThat(actual.parentAcl).isNotNull
        assertThat(actual.parentAcl.isEntriesInheriting).isTrue
        assertThat(actual.parentAcl.objectIdentity.type).isEqualTo(aclClass)
        assertThat(actual.parentAcl.objectIdentity.identifier).isEqualTo(10L)
        assertThat(actual.parentAcl.owner).isEqualTo(GrantedAuthoritySid(ADMIN_ROLE))
    }
}