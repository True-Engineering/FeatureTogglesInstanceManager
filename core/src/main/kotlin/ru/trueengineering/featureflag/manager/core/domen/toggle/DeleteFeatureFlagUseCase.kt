package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteFeatureFlagUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'DELETE_FLAG')")
    fun execute(command: DeleteFeatureFlagCommand) : Any

}

data class DeleteFeatureFlagCommand(val uuid: String, val projectId: Long)