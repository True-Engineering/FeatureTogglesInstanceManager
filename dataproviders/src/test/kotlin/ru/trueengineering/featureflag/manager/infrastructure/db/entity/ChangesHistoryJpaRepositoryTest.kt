package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.test.context.jdbc.Sql
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChangesHistoryJpaRepositoryTest(
    @Autowired val projectJpaRepository: ProjectJpaRepository,
    @Autowired val userJpaRepository: UserJpaRepository,
    @Autowired val featureFlagJpaRepository: FeatureFlagJpaRepository,
    @Autowired val environmentJpaRepository: EnvironmentJpaRepository,
    @Autowired override var uut: ChangesHistoryJpaRepository
): JpaRepositoryBaseTest<ChangesHistoryJpaRepository>() {

    @Test
    @Sql("/changes_history_dataset.sql")
    fun shouldCreateNew() {
        val entity = ChangesHistoryEntity(ChangeAction.CREATE).apply {
            id = 1
            project = projectJpaRepository.getById(1)!!
            user = userJpaRepository.findById(1).get()
            featureFlag = featureFlagJpaRepository.findById(1).get()
            environment = null
        }
        val actualEntity = uut.save(entity)
        assertEquals(ChangeAction.CREATE, actualEntity.action)
        assertEquals(1, actualEntity.project.id)
        assertEquals(1, actualEntity.featureFlag.id)
        assertEquals(null, actualEntity.environment)
        assertNotNull(actualEntity.id)
        assertNotNull(actualEntity.created)
        assertNotNull(actualEntity.updated)
    }

    @Test
    @Sql("/changes_history_dataset.sql")
    fun shouldGetAllProjectChangesHistory() {
        val entity1 = ChangesHistoryEntity(ChangeAction.CREATE).apply {
            id = 1
            project = projectJpaRepository.getById(1)!!
            user = userJpaRepository.findById(1).get()
            featureFlag = featureFlagJpaRepository.findById(1).get()
            environment = null
        }
        uut.save(entity1)
        val entity2 = ChangesHistoryEntity(ChangeAction.CREATE).apply {
            id = 2
            project = projectJpaRepository.getById(2)!!
            user = userJpaRepository.findById(1).get()
            featureFlag = featureFlagJpaRepository.findById(1).get()
            environment = null
        }
        uut.save(entity2)
        val entity3 = ChangesHistoryEntity(ChangeAction.ENABLE).apply {
            id = 3
            project = projectJpaRepository.getById(1)!!
            user = userJpaRepository.findById(1).get()
            featureFlag = featureFlagJpaRepository.findById(1).get()
            environment = environmentJpaRepository.getById(1)
        }
        uut.save(entity3)
        val specification = Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            val projectFlagJoin = root.join<ProjectEntity, ChangesHistoryEntity>("project")
            criteriaBuilder.equal(projectFlagJoin.get<Long>("id"), 1L)
        }
        val actual = uut.findAll(specification, PageRequest.of(0, 10))
        assertThat(actual).hasSize(3)
        assertThat(actual.content[1].action).isEqualTo(ChangeAction.CREATE)
        assertThat(actual.content[1].featureFlag.id).isEqualTo(1)
        assertThat(actual.content[2].action).isEqualTo(ChangeAction.ENABLE)
        assertThat(actual.content[2].featureFlag.id).isEqualTo(1)
    }
}