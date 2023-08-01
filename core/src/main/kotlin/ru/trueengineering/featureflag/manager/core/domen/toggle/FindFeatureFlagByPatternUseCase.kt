package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize

interface FindFeatureFlagByPatternUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')")
    fun execute(command: FindFeatureFlagByPatternCommand) : Page<FeatureFlag>

}

data class FindFeatureFlagByPatternCommand(
    val projectId: Long,
    val pattern: String,
    val pageable: Pageable
)