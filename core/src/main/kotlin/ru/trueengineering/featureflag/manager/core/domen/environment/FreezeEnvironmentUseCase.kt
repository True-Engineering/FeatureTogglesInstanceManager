package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize
import java.time.OffsetDateTime

interface FreezeEnvironmentUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT') && " +
            "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun execute(command: FreezeEnvironmentCommand): Environment

}

data class FreezeEnvironmentCommand(
    val projectId: Long,
    val environmentId: Long,
    val endTime: OffsetDateTime
)