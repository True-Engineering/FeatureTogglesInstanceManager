package ru.trueengineering.featureflag.manager.core.domen.user

import org.springframework.security.access.prepost.PreAuthorize
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser

interface UpdateUserEnvironmentRoleUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT_MEMBERS')")
    fun execute(command: UpdateUserEnvironmentRoleCommand): ProjectUser

}

data class UpdateUserEnvironmentRoleCommand(val organizationId: Long,
                                            val projectId: Long,
                                            val userId: Long,
                                            val environmentId: Long,
                                            val role: UserRole? = null)