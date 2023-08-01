package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction.CREATE
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ChangesHistoryEntityMapper
import java.time.Instant

internal class ChangesHistoryDatabaseRepositoryTest {

    private val mapper: ChangesHistoryEntityMapper = mockk()
    private val changesHistoryRepository: ChangesHistoryJpaRepository = mockk()

    private val uut = ChangesHistoryDatabaseRepository(mapper, changesHistoryRepository)

    @Test
    fun create() {
        val changesHistoryRecord = ChangesHistoryRecord(
            ChangeAction.CREATE,
            null,
            Project(name = "project"),
            User("name", "email"),
            null,
            FeatureFlag("uid"),
            Instant.now()
        )
        val entity = ChangesHistoryEntity(CREATE).apply {
            id = 1
            project = ProjectEntity("project")
            user = UserEntity("email")
            environment = null
            featureFlag = FeatureFlagEntity("uid")
        }
        every { mapper.convertToEntity(changesHistoryRecord) } returns entity
        every { changesHistoryRepository.save(entity) } returns entity
        every { mapper.convertToDomain(entity) } returns changesHistoryRecord

        val actual = uut.create(changesHistoryRecord)
        assertThat(actual).isEqualTo(changesHistoryRecord)
    }

    @Test
    fun getProjectChangesHistory() {
        val projectId = 1L
        val featureFlagUid = "test"
        val userId = 2L
        val start = Instant.parse("2021-01-01T00:00:00Z")
        val end = Instant.parse("2021-12-31T23:59:59Z")
        val tag = "test"
        val pageable = PageRequest.of(0, 10)

        val entity = ChangesHistoryEntity(CREATE).apply {
            id = 1L
            project = ProjectEntity("project").apply { id = projectId }
            featureFlag = FeatureFlagEntity(featureFlagUid).apply { this.tag = tag }
            user = UserEntity("email").apply { id = userId }
            created = Instant.MAX
        }
        val entityPage = PageImpl(listOf(entity))
        val record = ChangesHistoryRecord(
            id = entity.id,
            project = Project(entity.project.id, entity.project.name),
            featureFlag = FeatureFlag(entity.featureFlag.uid, tags = setOf("test")),
            user = User("name", "email"),
            created = Instant.MAX,
            action = CREATE
        )
        val recordPage = PageImpl(listOf(record))

        every { changesHistoryRepository.findAll(any(), pageable) } returns entityPage
        every { mapper.convertToDomain(entity) } returns record

        val result = uut.getProjectChangesHistory(projectId, featureFlagUid, userId, start, end, tag, pageable)

        assertThat(result).isEqualTo(recordPage)
    }
}