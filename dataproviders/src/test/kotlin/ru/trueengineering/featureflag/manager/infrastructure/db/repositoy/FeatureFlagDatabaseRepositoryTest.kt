package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEnvironmentStateEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.FeatureFlagEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.FeatureFlagEnvironmentEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PROJECT = "Cool project"

private const val FEATURE = "feature"

internal class FeatureFlagDatabaseRepositoryTest {

    private val featureFlagJpaRepository: FeatureFlagJpaRepository = mockk()
    private val featureFlagEntityMapper: FeatureFlagEntityMapper = mockk()
    private val featureFlagEnvironmentEntityMapper: FeatureFlagEnvironmentEntityMapper = mockk()
    private val projectEntityMapper: ProjectEntityMapper = mockk()

    private val uut: FeatureFlagDatabaseRepository =
        FeatureFlagDatabaseRepository(
            featureFlagJpaRepository, featureFlagEntityMapper,
            projectEntityMapper
        )

    @Test
    fun getFeatureFlagsForProject() {
        val featureFlagEntities = listOf(FeatureFlagEntity(FEATURE))
        every { featureFlagJpaRepository.searchDistinctByProjectId(1) } returns
                featureFlagEntities
        val featureFlagList = listOf(FeatureFlag(FEATURE))
        every { featureFlagEntityMapper.convertToDomainList(featureFlagEntities) } returns featureFlagList
        val actual = uut.getFeatureFlagsForProject(1L)
        assertEquals(featureFlagList, actual)
    }

    @Test
    fun createOrEdit() {
        val project = Project(1L, PROJECT)
        val featureFlag = FeatureFlag(FEATURE)
        val flagEnvironmentStateEntity = FeatureFlagEnvironmentStateEntity(true)
        val featureFlagEntity = FeatureFlagEntity(FEATURE).apply { environments.add(flagEnvironmentStateEntity) }
        val projectEntity = ProjectEntity(PROJECT)
        val savedFeatureFlagEntity = FeatureFlagEntity("saved.feature")
        val savedFeatureFlag = FeatureFlag("saved.feature")

        every { projectEntityMapper.convertToEntity(project) } returns projectEntity
        every { featureFlagEntityMapper.convertToEntity(featureFlag) } returns featureFlagEntity
        every { featureFlagJpaRepository.save(featureFlagEntity) } returns savedFeatureFlagEntity
        every { featureFlagEntityMapper.convertToDomain(savedFeatureFlagEntity) } returns savedFeatureFlag
        val actual = uut.createOrEdit(featureFlag, project)
        assertEquals(projectEntity, featureFlagEntity.project)
        assertEquals(savedFeatureFlag, actual)
    }

    @Test
    fun deleteFeatureFlag() {
        val entity = FeatureFlagEntity(FEATURE).apply {
            id = 1
            project = ProjectEntity("project").apply { id = 1 }
        }
        val slot = slot<FeatureFlagEntity>()
        every { featureFlagJpaRepository.searchActiveByUidAndProjectId(FEATURE, 1) } returns entity
        every { featureFlagJpaRepository.deleteFeatureEnvironmentByFeatureFlagId(1) } just Runs
        every { featureFlagJpaRepository.save(capture(slot)) } answers { firstArg() }

        uut.deleteFeatureFlag(FEATURE, 1)

        verify { featureFlagJpaRepository.deleteFeatureEnvironmentByFeatureFlagId(1) }
        verify { featureFlagJpaRepository.save(any()) }
        assertTrue(slot.captured.removed)
    }

    @Test
    fun getByUidAndProjectId() {
        val featureFlagEntity = FeatureFlagEntity(FEATURE)
        every { featureFlagJpaRepository.searchActiveByUidAndProjectId(FEATURE, 1) } returns featureFlagEntity
        val expected = FeatureFlag(FEATURE)
        every { featureFlagEntityMapper.convertToDomain(featureFlagEntity) } returns expected
        val actual = uut.getActiveByUidAndProjectId(FEATURE, 1L)
        assertEquals(expected, actual)
    }

    @Test
    fun getByUidAndProjectIdNotFound() {
        val featureFlagEntity = FeatureFlagEntity(FEATURE)
        every { featureFlagJpaRepository.searchActiveByUidAndProjectId(FEATURE, 1) } returns null
        val expected = FeatureFlag(FEATURE)
        every { featureFlagEntityMapper.convertToDomain(featureFlagEntity) } returns expected
        val actual = uut.getActiveByUidAndProjectId(FEATURE, 1L)
        assertNull(actual)
    }

    @Test
    fun deleteFeatureEnvironment() {
        every { featureFlagJpaRepository.deleteFeatureEnvironmentById(1) } just Runs

        uut.deleteFeatureEnvironment(1)
        verify { featureFlagJpaRepository.deleteFeatureEnvironmentById(1) }
    }

    @Test
    fun getFeatureFlagsByPattern() {
        val pageable = PageRequest.of(0, 10)
        val featureFlagEntity = FeatureFlagEntity("test123")
        val page = PageImpl(listOf(featureFlagEntity))
        val featureFlag = FeatureFlag("test123")

        every { featureFlagJpaRepository.searchByPattern(1, "test", pageable) } returns page
        every { featureFlagEntityMapper.convertToDomain(featureFlagEntity) } returns featureFlag

        val actual = uut.getFeatureFlagsByPattern(1, "test", pageable)

        assertEquals(PageImpl(listOf(featureFlag)), actual)
    }
}