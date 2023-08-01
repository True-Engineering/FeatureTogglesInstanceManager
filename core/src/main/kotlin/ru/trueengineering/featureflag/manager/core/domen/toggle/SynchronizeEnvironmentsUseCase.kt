package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface SynchronizeEnvironmentsUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'UPLOAD_ENVIRONMENT')")
    fun execute(command: SynchronizeEnvironmentsCommand): List<FeatureFlag>

}

data class SynchronizeEnvironmentsCommand(
    val projectId: Long,
    val featureFlagStates: List<FeatureFlag>,
    val copyDirection: CopyDirection
)

data class CopyDirection(
    val src: String,
    val dest: List<String>
)