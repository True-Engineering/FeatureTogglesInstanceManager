package ru.trueengineering.featureflag.manager.authorization.impl

import org.springframework.security.acls.model.Permission
import ru.trueengineering.featureflag.manager.auth.BusinessEntity

interface PermissionFilter {

    fun isApplicable(entity: BusinessEntity): Boolean

    fun filter(permissions: List<Permission>): List<Permission> {
        return permissions.filter { isAllowed(it) }
    }

    fun isAllowed(permission: Permission): Boolean
}