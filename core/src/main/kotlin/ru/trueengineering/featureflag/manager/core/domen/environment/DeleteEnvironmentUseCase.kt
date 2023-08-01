package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteEnvironmentUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'DELETE')")
    fun execute(command: DeleteEnvironmentCommand) : Any

}

data class DeleteEnvironmentCommand(val environmentId: Long)