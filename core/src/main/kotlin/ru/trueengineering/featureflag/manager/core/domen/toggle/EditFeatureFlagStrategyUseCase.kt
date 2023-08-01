package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface EditFeatureFlagStrategyUseCase {

    @PreAuthorize("hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun execute(command: EditFeatureFlagStrategyCommand): FeatureFlag

}

data class EditFeatureFlagStrategyCommand(
    val uuid: String,
    val projectId: Long,
    val environmentId: Long,
    val type: String?,
    val initParams: MutableMap<String, String>?
)