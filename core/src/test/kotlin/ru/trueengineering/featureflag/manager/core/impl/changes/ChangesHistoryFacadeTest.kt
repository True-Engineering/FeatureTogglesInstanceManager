package ru.trueengineering.featureflag.manager.core.impl.changes

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction.ENABLE
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordCommand
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentRepository
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectFacade
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.user.UserFacade
import java.time.Instant

internal class ChangesHistoryFacadeTest{

    private val projectFacade: ProjectFacade = mockk()
    private val userFacade: UserFacade= mockk()
    private val environmentRepository: EnvironmentRepository = mockk()
    private val featureFlagRepository: FeatureFlagRepository = mockk()
    private val changesHistoryRepository: ChangesHistoryRepository = mockk()

    private val uut = ChangesHistoryFacade(
        projectFacade,
        userFacade,
        environmentRepository,
        featureFlagRepository,
        changesHistoryRepository
    )

    @Test
    fun createChangesHistoryRecord() {
        val project = Project(1, name = "Project")
        val user = User("name", "email")
        val environment = Environment(1, name = "environment")
        val featureFlag = FeatureFlag("uid")
        val changesHistoryRecord = ChangesHistoryRecord(
            ENABLE,
            null,
            project,
            user,
            environment,
            featureFlag,
            Instant.now()
        )

        every { projectFacade.search(SearchProjectByIdQuery(1)) } returns project
        every { userFacade.search() } returns user
        every { environmentRepository.getById(1) } returns environment
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId("uid", 1) } returns featureFlag
        every { changesHistoryRepository.create(any()) } returns changesHistoryRecord

        uut.execute(CreateChangesHistoryRecordCommand(ENABLE, 1, "uid", 1))
        verify { changesHistoryRepository.create(any()) }
    }

    @Test
    fun getProjectChangesHistory() {
        val command = GetProjectChangesHistoryCommand(
            projectId = 1L,
            featureFlagUid = "abc",
            userId = 2L,
            start = Instant.MIN,
            end = Instant.MAX,
            tag = "test",
            pageable = PageRequest.of(0, 10)
        )
        val expectedPage = PageImpl<ChangesHistoryRecord>(listOf(mockk()))
        every { changesHistoryRepository.getProjectChangesHistory(any(), any(), any(), any(), any(), any(), any()) } returns expectedPage

        val result = uut.execute(command)

        verify { changesHistoryRepository.getProjectChangesHistory(1L, "abc", 2L, Instant.MIN, Instant.MAX, "test", PageRequest.of(0, 10)) }
        assertEquals(expectedPage, result)
    }
}