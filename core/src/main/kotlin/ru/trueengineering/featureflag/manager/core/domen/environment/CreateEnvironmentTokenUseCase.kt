package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface CreateEnvironmentTokenUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: CreateEnvironmentTokenCommand): String

}

data class CreateEnvironmentTokenCommand(val environmentId: Long)