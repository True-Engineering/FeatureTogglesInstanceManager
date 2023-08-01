package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface EnableFeatureFlagUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: EnableFeatureFlagCommand) : Any
}

data class EnableFeatureFlagCommand(val uuid: String, val projectId: Long, val environmentId: Long)