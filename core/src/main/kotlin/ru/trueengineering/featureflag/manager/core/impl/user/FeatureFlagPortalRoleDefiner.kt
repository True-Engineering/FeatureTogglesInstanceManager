package ru.trueengineering.featureflag.manager.core.impl.user

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.authorize.DomainTypeDefinition
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import java.security.Principal

class FeatureFlagPortalRoleDefiner : RoleDefiner {
    override fun defineRoleForEntity(
        entity: BusinessEntity,
        user: Principal,
        permissions: Collection<CustomPermission>
    ): CustomRole {
        return when (entity.type) {
            DomainTypeDefinition.PROJECT.type -> CustomRole.defineRoleByProjectPermissions(permissions)
            DomainTypeDefinition.ORGANIZATION.type -> CustomRole.defineRoleByOrganizationPermissions(permissions)
            DomainTypeDefinition.ENVIRONMENT.type -> CustomRole.defineRoleByEnvironmentPermissions(permissions)
            else -> return CustomRole.NO_ACCESS
        }
    }
}