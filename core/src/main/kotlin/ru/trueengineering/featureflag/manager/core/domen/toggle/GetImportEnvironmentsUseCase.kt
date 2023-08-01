package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment

interface GetImportEnvironmentsUseCase {
    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'UPLOAD_ENVIRONMENT')")
    fun execute(command: GetImportEnvironmentsCommand): ImportEnvironments

}

data class GetImportEnvironmentsCommand(
    val key: String,
    val projectId: Long,
    val featureFlagStates: List<FeatureFlag>
)

data class ImportEnvironments(
    val key: String,
    val envSynchronizedStatus: Boolean,
    val srcEnvironments: List<Environment>,
    val destEnvironments: List<Environment>
)