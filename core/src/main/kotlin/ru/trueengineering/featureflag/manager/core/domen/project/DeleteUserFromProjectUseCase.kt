package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteUserFromProjectUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT_MEMBERS')")
    fun execute(command: DeleteUserFromProjectCommand)

}

data class DeleteUserFromProjectCommand(val projectId: Long, val userId: Long)