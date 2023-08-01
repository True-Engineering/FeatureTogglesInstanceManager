package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface SynchronizePortalsUseCase {

    @PreAuthorize(
        "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'CREATE_FLAG') &&" +
                "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'UPLOAD_ENVIRONMENT') && " +
                "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'DELETE_FLAG')"
    )
    fun execute(command: SynchronizePortalsCommand): List<FeatureFlag>

}

data class SynchronizePortalsCommand(
    val projectId: Long,
    val featureFlagStates: List<FeatureFlag>,
    val needToDelete: Boolean
)