package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import javax.persistence.EntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeatureFlagJpaRepositoryTest(
        @Autowired override var uut: FeatureFlagJpaRepository,
        @Autowired var entityManager: EntityManager
) : JpaRepositoryBaseTest<FeatureFlagJpaRepository>() {

    @Test
    @Sql("/feature_flag_dataset.sql", "/update_id_sequences.sql")
    internal fun shouldCreateNewFeature() {
        val projectEntity = ProjectEntity("Super test project 1").apply { id = 1 }
        projectEntity.organization = OrganizationEntity("Org").apply { id = 1 }
        val featureFlagEntity = FeatureFlagEntity("new.super.cool.feature.enabled")
                .apply {
                    project = projectEntity
                    val featureFlagEnvironmentStateEntity = FeatureFlagEnvironmentStateEntity(true)
                    val environmentEntity = EnvironmentEntity("TEST")
                    environmentEntity.id = 1
                    val featureFlagEnvironmentStatePK = FeatureFlagEnvironmentStatePK(environmentEntity)
                    featureFlagEnvironmentStatePK.featureFlag = this
                    featureFlagEnvironmentStateEntity.primaryKey = featureFlagEnvironmentStatePK
                    environments.add(featureFlagEnvironmentStateEntity)
                }

        val actualEntity = uut.save(featureFlagEntity)
        assertEquals("new.super.cool.feature.enabled", actualEntity.uid)
        assertNotNull(actualEntity.project)
        assertEquals("Super test project 1", actualEntity.project.name)
        assertNotNull(actualEntity.created)
        assertNotNull(actualEntity.updated)

        assertEquals(1, actualEntity.environments.size)
        assertEquals("TEST", actualEntity.environments.first().primaryKey!!.environment.name)

        val envCount = entityManager.createNativeQuery(
                "select count(*) from feature_environment_state " +
                        "where feature_id = (select id from features where feat_uid = 'new.super.cool.feature.enabled') " +
                        "and environment_id = 1")
                .singleResult.toString()

        assertEquals("1", envCount)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldDeleteFeature() {
        val feature = uut.findById(1)
        assertTrue(feature.isPresent)

        uut.delete(feature.get())

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldDeleteById() {
        val feature = uut.findById(1)
        assertTrue(feature.isPresent)

        uut.deleteById(1)

        assertFalse(uut.findById(1).isPresent)

        val envCount = entityManager.createNativeQuery(
                "select count(*) from feature_environment_state " +
                        "where feature_id = 1 and environment_id = 1")
                .singleResult.toString()

        assertEquals("0", envCount)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldFetchAll() {
        val projects = uut.findAll()
        assertEquals(3, projects.count())
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldSearchByProject() {
        val entities = uut.searchDistinctByProjectId(1)
        assertNotNull(entities)
        assertEquals(2, entities.count())
        assertEquals("cool.feature.enabled", entities[0].uid)
        assertEquals("second.cool.feature.enabled", entities[1].uid)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldSearchByUidAndProject() {
        val entity = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        assertNotNull(entity)
        assertEquals("cool.feature.enabled", entity.uid)
        assertEquals("TEST", entity.environments.first().primaryKey!!.environment.name)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun deleteFeatureEnvironmentById() {
        uut.deleteFeatureEnvironmentById(1)
        val entity = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        assertEquals(entity?.environments?.size, 0)
    }


    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun getFeatureFlagEntityByEnvironmentId() {
        val entites = uut.getFeatureFlagEntityByEnvironmentId(1)
        assertEquals(entites.size, 2)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldCountActiveFeatures() {
        assertTrue(uut.countByProjectId(1).toInt() == 2)
        val feature = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        uut.save(feature!!.apply { removed = true })
        assertTrue(uut.countByProjectId(1).toInt() == 1)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldRemoveFeature() {
        val feature = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        assertTrue(feature != null)

        uut.save(feature.apply { removed = true })

        assertFalse(uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1) != null)
        assertTrue(uut.searchByUidAndProjectId("cool.feature.enabled", 1) != null)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldDeleteFeatureEnvironment() {
        val feature = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        assertTrue(feature != null)
        val featureId = feature.id

        assertTrue(featureId?.let { uut.getFeatureEnvironmentStatesSizeByFeatureFlagId(it) } == 1L)
        featureId?.let { uut.deleteFeatureEnvironmentByFeatureFlagId(it) }
        assertTrue(featureId?.let { uut.getFeatureEnvironmentStatesSizeByFeatureFlagId(it) } == 0L)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldRemoveFeatureAndDeleteFeatureEnvironments() {
        val feature = uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1)
        assertTrue(feature != null)
        val featureId = feature.id
        assertTrue(featureId?.let { uut.getFeatureEnvironmentStatesSizeByFeatureFlagId(it) } == 1L)

        featureId?.let { uut.deleteFeatureEnvironmentByFeatureFlagId(it) }
        uut.save(feature.apply { removed = true })

        assertTrue(featureId?.let { uut.getFeatureEnvironmentStatesSizeByFeatureFlagId(it) } == 0L)
        assertFalse(uut.searchActiveByUidAndProjectId("cool.feature.enabled", 1) != null)
        assertTrue(uut.searchByUidAndProjectId("cool.feature.enabled", 1) != null)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun shouldSearchFeatureFlagsByProjectIdUpdatedBefore() {
        val updatedBefore1DayFirstProject = uut.searchByProjectIdAndUpdatedBefore(1, 1)
        val updatedBefore1DaySecondProject = uut.searchByProjectIdAndUpdatedBefore(2, 1)
        assertTrue(updatedBefore1DayFirstProject.size == 2)
        assertTrue(updatedBefore1DaySecondProject.size == 1)

        val updatedBefore2DaysFirstProject = uut.searchByProjectIdAndUpdatedBefore(1, 2)
        val updatedBefore2DaysSecondProject = uut.searchByProjectIdAndUpdatedBefore(2, 2)
        assertTrue(updatedBefore2DaysFirstProject.size == 1)
        assertTrue(updatedBefore2DaysSecondProject.size == 1)

        val updatedBefore3DaysFirstProject = uut.searchByProjectIdAndUpdatedBefore(1, 3)
        val updatedBefore3DaysSecondProject = uut.searchByProjectIdAndUpdatedBefore(2, 3)
        assertTrue(updatedBefore3DaysFirstProject.isEmpty())
        assertTrue(updatedBefore3DaysSecondProject.size == 1)

        val updatedBefore4DaysFirstProject = uut.searchByProjectIdAndUpdatedBefore(1, 4)
        val updatedBefore4DaysSecondProject = uut.searchByProjectIdAndUpdatedBefore(2, 4)
        assertTrue(updatedBefore4DaysFirstProject.isEmpty())
        assertTrue(updatedBefore4DaysSecondProject.isEmpty())
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun searchByPattern() {
        val pageable = PageRequest.of(0, 10)
        val findByPatternFeature = uut.searchByPattern(1L, "feature", pageable)
        val findByPatternSecond = uut.searchByPattern(1L, "second", pageable)

        assertEquals(2, findByPatternFeature.totalElements)
        assertEquals(1, findByPatternSecond.totalElements)
    }

    @Test
    @Sql("/feature_flag_dataset.sql")
    internal fun getDistinctTags() {
        val pageable = PageRequest.of(0, 1)

        val actual = uut.getDistinctTags(1, pageable)

        assertEquals(2, actual.totalElements)
        assertEquals(2, actual.totalPages)
    }
}