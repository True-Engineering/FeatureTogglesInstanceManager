package ru.trueengineering.featureflag.manager.authorization.config.impl

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import java.security.Principal

class TestRoleDefiner: RoleDefiner {
    override fun defineRoleForEntity(
        entity: BusinessEntity,
        user: Principal,
        permissions: Collection<CustomPermission>
    ): CustomRole {
        return if (permissions.contains(CustomPermission.EDIT)) {
            return CustomRole.ADMIN
        } else {
            return CustomRole.MEMBER
        }
    }
}