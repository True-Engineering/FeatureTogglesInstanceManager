package ru.trueengineering.featureflag.manager.core.domen.authorize

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.acls.model.Permission
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_ENV
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_PROJECT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.PENDING_APPROVE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ENVIRONMENT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ORGANIZATION
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_PROJECT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.UPLOAD_ENVIRONMENT
import ru.trueengineering.featureflag.manager.authorization.impl.PermissionFilter
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project

enum class DomainTypeDefinition(
    private val clazz: Class<*>,
    private val permissions: Set<CustomPermission>
) : PermissionFilter {
    ORGANIZATION(
        Organization::class.java,
        setOf(READ_ORGANIZATION, CREATE_PROJECT, EDIT, READ_MEMBERS, EDIT_MEMBERS, DELETE)
    ),
    PROJECT(
        Project::class.java,
        setOf(
            PENDING_APPROVE,
            READ_PROJECT,
            EDIT,
            CREATE_ENV,
            DELETE,
            READ_MEMBERS,
            EDIT_MEMBERS,
            CREATE_FLAG,
            DELETE_FLAG,
            UPLOAD_ENVIRONMENT
        )
    ),
    ENVIRONMENT(
        Environment::class.java,
        setOf(EDIT, DELETE, READ_ENVIRONMENT)
    );

    companion object {}

    val type: String = this.clazz.name

    override fun isApplicable(entity: BusinessEntity): Boolean {
        return entity.type == type
    }

    override fun isAllowed(permission: Permission): Boolean {
        return permissions.any { permission.mask == it.mask }
    }
}

@Configuration
class PermissionFilterConfiguration {
    @Bean
    fun organizationPermissionFilter(): PermissionFilter {
        return DomainTypeDefinition.ORGANIZATION
    }

    @Bean
    fun projectPermissionFilter(): PermissionFilter {
        return DomainTypeDefinition.PROJECT
    }

    @Bean
    fun environmentPermissionFilter(): PermissionFilter {
        return DomainTypeDefinition.ENVIRONMENT
    }

}