package ru.trueengineering.featureflag.manager.authorization

import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_ENV
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_PROJECT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ENVIRONMENT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ORGANIZATION
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_PROJECT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.UPLOAD_ENVIRONMENT

const val ADMIN_ROLE = "ROLE_FEATURE_FLAGS_ADMIN"

enum class CustomRole(
    val organizationPermissions: Set<CustomPermission>,
    val projectPermissions: Set<CustomPermission>,
    val environmentPermissions: Set<CustomPermission>,
    val needClearLowLayerEntityPermissions: Boolean
) {
    ADMIN(
        organizationPermissions = setOf(READ_ORGANIZATION, EDIT, READ_MEMBERS, EDIT_MEMBERS, CREATE_PROJECT, DELETE),
        projectPermissions = setOf(
            READ_PROJECT,
            EDIT,
            READ_MEMBERS,
            EDIT_MEMBERS,
            DELETE_FLAG,
            CREATE_FLAG,
            CREATE_ENV,
            DELETE,
            UPLOAD_ENVIRONMENT
        ),
        environmentPermissions = setOf(READ_ENVIRONMENT, EDIT, DELETE),
        true
    ),
    MEMBER(
        organizationPermissions = setOf(READ_ORGANIZATION),
        projectPermissions = setOf(READ_PROJECT, CREATE_FLAG),
        environmentPermissions = setOf(READ_PROJECT),
        false
    ),
    NO_ACCESS(setOf(), setOf(), setOf(), true)
    ;

    companion object {

        fun defineRoleByOrganizationPermissions(permissions: Collection<CustomPermission>): CustomRole {
            return defineRole(permissions, CustomRole::organizationPermissions)
        }

        fun defineRoleByProjectPermissions(permissions: Collection<CustomPermission>): CustomRole {
            return defineRole(permissions, CustomRole::projectPermissions)
        }


        fun defineRoleByEnvironmentPermissions(permissions: Collection<CustomPermission>): CustomRole {
            return defineRole(permissions, CustomRole::environmentPermissions)
        }

        private fun defineRole(
            actualPermissions: Collection<CustomPermission>,
            expectedPermissions: CustomRole.() -> Set<CustomPermission>
        ): CustomRole {
            for (projectRole in values()) {
                if (actualPermissions.containsAll(projectRole.expectedPermissions()))
                    return projectRole
            }
            return NO_ACCESS
        }

    }
}