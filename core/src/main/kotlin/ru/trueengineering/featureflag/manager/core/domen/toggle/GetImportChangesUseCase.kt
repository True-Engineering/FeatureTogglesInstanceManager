package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface GetImportChangesUseCase {
    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'UPLOAD_ENVIRONMENT')")
    fun execute(command: GetImportChangesCommand): ImportChanges

}

data class GetImportChangesCommand(
    val key: String,
    val projectId: Long,
    val featureFlagStates: List<FeatureFlag>
)

data class ImportChanges(
    val key: String,
    val featureFlagsToAdd: List<FeatureFlag>,
    val featureFlagsToRemove: List<FeatureFlag>,
    val featureFlagsToUpdate: List<Changes>
)

data class Changes(
    val newFeatureFlag: FeatureFlag,
    val currentFeatureFlag: FeatureFlag
)