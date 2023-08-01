package ru.trueengineering.featureflag.manager.core.domen.user

import org.springframework.security.acls.model.Permission
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission

enum class UserRole {
    EDITOR,
    VIEWER,
    NO_ACCESS;

    companion object {

        fun getEnvironmentPermissionByRole(permissionName: UserRole?): List<Permission> {
            if (EDITOR == permissionName) return listOf(CustomPermission.EDIT)
            if (VIEWER == permissionName)  return listOf(CustomPermission.READ_ENVIRONMENT)
            return emptyList()
        }

        fun getRoleByPermissions(permissions: List<Permission>): UserRole {
            val permissionsSet = HashSet(permissions)
            if (permissionsSet.contains(CustomPermission.EDIT)) {
                return EDITOR
            }
            return if (permissionsSet.contains(CustomPermission.READ_ENVIRONMENT) ||
                    permissionsSet.contains(CustomPermission.READ_ORGANIZATION) ||
                    permissionsSet.contains(CustomPermission.READ_PROJECT)) VIEWER else NO_ACCESS
        }
    }
}
