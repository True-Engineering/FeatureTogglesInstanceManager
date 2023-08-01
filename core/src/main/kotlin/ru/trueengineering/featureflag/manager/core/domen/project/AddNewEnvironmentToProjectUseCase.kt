package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface AddNewEnvironmentToProjectUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'CREATE_ENV')")
    fun execute(command: CreateEnvironmentCommand) : Environment

}

data class CreateEnvironmentCommand(val name: String, val projectId: Long)