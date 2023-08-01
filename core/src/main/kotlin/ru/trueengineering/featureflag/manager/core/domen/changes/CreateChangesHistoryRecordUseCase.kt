package ru.trueengineering.featureflag.manager.core.domen.changes

import org.springframework.security.access.prepost.PreAuthorize
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag

interface CreateChangesHistoryRecordUseCase {

    @PreAuthorize(
        "(#command.environmentId == null && (hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'CREATE_FLAG')" +
                "|| hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'DELETE_FLAG'))) ||" +
                "hasPermission(#command.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')"
    )
    fun execute(command: CreateChangesHistoryRecordCommand): ChangesHistoryRecord

}

data class CreateChangesHistoryRecordCommand(
    val action: ChangeAction,
    val projectId: Long,
    val featureUid: String,
    val environmentId: Long? = null,
    val featureChanges: FeatureChanges? = null,
    val creationInfo: FeatureFlag? = null
)