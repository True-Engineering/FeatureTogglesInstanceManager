package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize

interface FetchAllFeatureFlagsTagsUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')")
    fun execute(command: FetchAllFeatureFlagsTagsCommand) : Page<String>

}

data class FetchAllFeatureFlagsTagsCommand(
    val projectId: Long,
    val pageable: Pageable
)