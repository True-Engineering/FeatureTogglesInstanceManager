package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface UnfreezeEnvironmentUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT') && " +
            "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun execute(command: UnfreezeEnvironmentCommand): Environment

}

data class UnfreezeEnvironmentCommand(
    val projectId: Long,
    val environmentId: Long,
)