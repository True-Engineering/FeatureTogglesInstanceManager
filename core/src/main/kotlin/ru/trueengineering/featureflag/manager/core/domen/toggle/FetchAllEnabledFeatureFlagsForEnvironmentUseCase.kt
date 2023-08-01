package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface FetchAllEnabledFeatureFlagsForEnvironmentUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'UPLOAD_ENVIRONMENT')")
    fun execute(command: FetchAllEnabledFeatureFlagsForEnvironmentCommand): List<FeatureFlag>

}

data class FetchAllEnabledFeatureFlagsForEnvironmentCommand(
    val projectId: Long,
    val environmentId: Long,
    val days: Int,
)