package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteInstanceUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: DeleteInstanceCommand)

}

data class DeleteInstanceCommand(val projectId: Long, val environmentId: Long, val instanceId: Long)