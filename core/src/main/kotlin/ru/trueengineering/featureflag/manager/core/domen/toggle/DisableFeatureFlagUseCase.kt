package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface DisableFeatureFlagUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: DisableFeatureFlagCommand) : Any

}

data class DisableFeatureFlagCommand(val uuid: String, val projectId: Long, val environmentId: Long)