package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteProjectUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'DELETE')")
    fun execute(command: DeleteProjectCommand) : Any

}

data class DeleteProjectCommand(val projectId: Long)