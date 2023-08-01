package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface UpdateEnvironmentUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: UpdateEnvironmentCommand): Environment

}

data class UpdateEnvironmentCommand(val environmentId: Long, val name: String, val projectId: Long)