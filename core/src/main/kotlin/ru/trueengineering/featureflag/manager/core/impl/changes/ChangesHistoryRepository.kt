package ru.trueengineering.featureflag.manager.core.impl.changes

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import java.time.Instant

interface ChangesHistoryRepository {

    fun create(changesHistoryRecord: ChangesHistoryRecord): ChangesHistoryRecord

    fun getById(id: Long): ChangesHistoryRecord

    fun getProjectChangesHistory(
        projectId: Long,
        featureFlagUid: String?,
        userId: Long?,
        start: Instant?,
        end: Instant?,
        tag: String?,
        pageable: Pageable
    ): Page<ChangesHistoryRecord>

}