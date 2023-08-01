package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.impl.changes.ChangesHistoryRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ChangesHistoryEntityMapper
import java.text.SimpleDateFormat
import java.time.Instant

@Service
class ChangesHistoryDatabaseRepository(
    private val mapper: ChangesHistoryEntityMapper,
    private val changesHistoryRepository: ChangesHistoryJpaRepository,
): ChangesHistoryRepository {

    override fun create(changesHistoryRecord: ChangesHistoryRecord): ChangesHistoryRecord {
        val entity = mapper.convertToEntity(changesHistoryRecord)
        return mapper.convertToDomain(changesHistoryRepository.save(entity))
    }

    override fun getById(id: Long): ChangesHistoryRecord {
        return mapper.convertToDomain(changesHistoryRepository.findById(id).orElseThrow())
    }

    override fun getProjectChangesHistory(
        projectId: Long,
        featureFlagUid: String?,
        userId: Long?,
        start: Instant?,
        end: Instant?,
        tag: String?,
        pageable: Pageable
    ): Page<ChangesHistoryRecord> {
        val specification = hasProjectId(projectId)
            .and(hasFeatureFlagUid(featureFlagUid))
            .and(hasUserId(userId))
            .and(hasCreatedBetween(start, end))
            .and(hasTag(tag))

        return changesHistoryRepository
            .findAll(specification, pageable)
            .map(mapper::convertToDomain)
    }

    private fun hasProjectId(projectId: Long): Specification<ChangesHistoryEntity> {
        return Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            val projectJoin = root.join<ProjectEntity, ChangesHistoryEntity>("project")
            criteriaBuilder.equal(projectJoin.get<Long>("id"), projectId)
        }
    }

    private fun hasFeatureFlagUid(featureFlagUid: String?): Specification<ChangesHistoryEntity> {
        return Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            if (featureFlagUid != null) {
                val featureFlagJoin = root.join<FeatureFlagEntity, ChangesHistoryEntity>("featureFlag")
                criteriaBuilder.equal(featureFlagJoin.get<Long>("uid"), featureFlagUid)
            } else {
                criteriaBuilder.conjunction()
            }
        }
    }

    private fun hasUserId(userId: Long?): Specification<ChangesHistoryEntity> {
        return Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            if (userId != null) {
                val userJoin = root.join<UserEntity, ChangesHistoryEntity>("user")
                criteriaBuilder.equal(userJoin.get<Long>("id"), userId)
            } else {
                criteriaBuilder.conjunction()
            }
        }
    }

    private fun hasCreatedBetween(start: Instant?, end: Instant?): Specification<ChangesHistoryEntity> {
        val newStart = start ?: SimpleDateFormat("dd.MM.yyyy").parse("01.01.1970").toInstant()
        val newEnd = end ?: Instant.now()

        return Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            criteriaBuilder.between(root.get("created"), newStart, newEnd)
        }
    }

    private fun hasTag(tag: String?): Specification<ChangesHistoryEntity> {
        return Specification<ChangesHistoryEntity> { root, _, criteriaBuilder ->
            if (tag != null) {
                val featureFlagJoin = root.join<FeatureFlagEntity, ChangesHistoryEntity>("featureFlag")
                criteriaBuilder.equal(featureFlagJoin.get<String>("tag"), tag)
            } else {
                criteriaBuilder.conjunction()
            }
        }
    }
}
