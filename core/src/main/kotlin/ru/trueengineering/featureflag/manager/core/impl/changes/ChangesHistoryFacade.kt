package ru.trueengineering.featureflag.manager.core.impl.changes

import org.springframework.data.domain.Page
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordCommand
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordUseCase
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryCommand
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentRepository
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectFacade
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.user.UserFacade
import java.time.Instant

open class ChangesHistoryFacade(
    private val projectFacade: ProjectFacade,
    private val userFacade: UserFacade,
    private val environmentRepository: EnvironmentRepository,
    private val featureFlagRepository: FeatureFlagRepository,
    private val changesHistoryRepository: ChangesHistoryRepository
) : CreateChangesHistoryRecordUseCase, GetProjectChangesHistoryUseCase {

    override fun execute(command: CreateChangesHistoryRecordCommand): ChangesHistoryRecord {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val user = userFacade.search()
        val environment =
            if (command.environmentId == null) null else environmentRepository.getById(command.environmentId)
        val featureFlag = featureFlagRepository.getDespiteRemovedByUidAndProjectId(command.featureUid, command.projectId)
        return changesHistoryRepository.create(
            ChangesHistoryRecord(
                action = command.action,
                project = project,
                user = user,
                environment = environment,
                featureFlag = featureFlag!!,
                created = Instant.now(),
                featureChanges = command.featureChanges,
                creationInfo = command.creationInfo
            )
        )
    }

    override fun execute(command: GetProjectChangesHistoryCommand): Page<ChangesHistoryRecord> {
        return changesHistoryRepository.getProjectChangesHistory(
            command.projectId,
            command.featureFlagUid,
            command.userId,
            command.start,
            command.end,
            command.tag,
            command.pageable
        )
    }

}
