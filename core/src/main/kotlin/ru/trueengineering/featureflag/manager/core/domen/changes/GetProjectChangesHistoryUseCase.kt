package ru.trueengineering.featureflag.manager.core.domen.changes

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import java.time.Instant


interface GetProjectChangesHistoryUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')")
    fun execute(command: GetProjectChangesHistoryCommand): Page<ChangesHistoryRecord>

}

data class GetProjectChangesHistoryCommand(
    val projectId: Long,
    val pageable: Pageable,
    val featureFlagUid: String? = null,
    val userId: Long? = null,
    val start: Instant? = null,
    val end: Instant? = null,
    val tag: String? = null
)