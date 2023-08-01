package ru.trueengineering.featureflag.manager.authorization

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import java.security.Principal

interface RoleDefiner {

    fun defineRoleForEntity(entity: BusinessEntity, user: Principal, permissions: Collection<CustomPermission>): CustomRole

    fun isAdmin(entity: BusinessEntity, user: Principal, permissions: Collection<CustomPermission>): Boolean {
        return defineRoleForEntity(entity, user, permissions) == CustomRole.ADMIN
    }

}