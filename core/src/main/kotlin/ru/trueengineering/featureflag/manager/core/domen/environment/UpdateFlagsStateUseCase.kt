package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface UpdateFlagsStateUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: UpdateFlagsStateCommand)

}

data class UpdateFlagsStateCommand(
    val projectId: Long,
    val environmentId: Long,
    val featureFlagsStates: Map<String, Boolean>
)