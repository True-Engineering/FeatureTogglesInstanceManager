package ru.trueengineering.featureflag.manager.core.domen.user

import org.springframework.security.access.prepost.PreAuthorize
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser

interface EditProjectRoleUseCase {

    @PreAuthorize("" +
            "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT_MEMBERS') " +
            "&&" +
            "hasPermission(#command.organizationId, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'READ_ORGANIZATION')")
    fun execute(command: EditUserProjectRoleCommand): ProjectUser

}

data class EditUserProjectRoleCommand(val organizationId: Long, val projectId: Long, val userId: Long, val projectRole: CustomRole)