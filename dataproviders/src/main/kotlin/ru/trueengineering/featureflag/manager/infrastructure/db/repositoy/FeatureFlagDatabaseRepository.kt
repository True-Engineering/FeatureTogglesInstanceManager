package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.event.FeatureFlagNewActionEvent
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.FeatureFlagEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper

@Service
class FeatureFlagDatabaseRepository(
    val featureFlagJpaRepository: FeatureFlagJpaRepository,
    val featureFlagEntityMapper: FeatureFlagEntityMapper,
    val projectEntityMapper: ProjectEntityMapper
) : FeatureFlagRepository {
    override fun getRemovedByUidAndProjectId(uid: String, projectId: Long): FeatureFlag? {
        val featureFlagEntity =
            featureFlagJpaRepository.searchRemovedByUidAndProjectId(uid, projectId)
        return featureFlagEntity?.let(featureFlagEntityMapper::convertToDomain)
    }

    override fun getActiveByUidAndProjectId(uid: String, projectId: Long): FeatureFlag? {
        val featureFlagEntity =
            featureFlagJpaRepository.searchActiveByUidAndProjectId(uid, projectId)
        return featureFlagEntity?.let(featureFlagEntityMapper::convertToDomain)
    }

    override fun getDespiteRemovedByUidAndProjectId(uid: String, projectId: Long): FeatureFlag? {
        val featureFlagEntity =
            featureFlagJpaRepository.searchByUidAndProjectId(uid, projectId)
        return featureFlagEntity?.let(featureFlagEntityMapper::convertToDomain)
    }

    override fun getFeatureFlagsForProject(projectId: Long): List<FeatureFlag> {
        return featureFlagEntityMapper.convertToDomainList(
            featureFlagJpaRepository.searchDistinctByProjectId(projectId)
        )
    }

    override fun getFeatureFlagsCountForProject(projectId: Long): Long {
        return featureFlagJpaRepository.countByProjectId(projectId)
    }

    override fun getFeatureFlagsForEnvironment(environmentId: Long): List<FeatureFlag> {
        return featureFlagEntityMapper.convertToDomainList(
            featureFlagJpaRepository.getFeatureFlagEntityByEnvironmentId(environmentId)
        )
    }

    override fun createOrEdit(
        featureFlag: FeatureFlag,
        project: Project,
        action: ChangeAction?,
        environmentId: Long?,
        changes: FeatureChanges?,
        creationInfo: FeatureFlag?
    ): FeatureFlag {
        val projectEntity = projectEntityMapper.convertToEntity(project)
        val featureFlagEntity = convertToFeatureFlagEntity(featureFlag, projectEntity)

        val savedEntity = featureFlagJpaRepository.save(featureFlagEntity.apply {
            if (action != null) {
                addDomainEvent(FeatureFlagNewActionEvent(
                    action,
                    this.project.id!!,
                    this.uid,
                    environmentId,
                    changes,
                    creationInfo
                ))
            }
        })

        return featureFlagEntityMapper.convertToDomain(savedEntity)
    }

    @Transactional
    override fun deleteFeatureEnvironment(environmentId: Long) {
        featureFlagJpaRepository.deleteFeatureEnvironmentById(environmentId)
    }

    override fun saveAll(features: List<FeatureFlag>, project: Project): List<FeatureFlag> {
        val projectEntity = projectEntityMapper.convertToEntity(project)
        val featureFlagEntities = features.map { convertToFeatureFlagEntity(it, projectEntity) }
        val entities = featureFlagJpaRepository.saveAll(featureFlagEntities)
        return featureFlagEntityMapper.convertToDomainList(entities)
    }

    private fun convertToFeatureFlagEntity(featureFlag: FeatureFlag, projectEntity: ProjectEntity): FeatureFlagEntity {
        return featureFlagEntityMapper.convertToEntity(featureFlag).apply {
            this.project = projectEntity
            this.environments.forEach { it.primaryKey?.featureFlag = this }
        }
    }

    @Transactional
    override fun deleteFeatureFlag(featureFlagUid: String, projectId: Long) {
        val featureFlag = featureFlagJpaRepository.searchActiveByUidAndProjectId(featureFlagUid, projectId)

        if (featureFlag != null) {
            val deleted = featureFlag.apply {
                removed = true
                addDomainEvent(FeatureFlagNewActionEvent(ChangeAction.DELETE, this.project.id!!, this.uid))
            }

            featureFlagJpaRepository.deleteFeatureEnvironmentByFeatureFlagId(deleted.id!!)
            featureFlagJpaRepository.save(deleted)
        }
    }

    override fun activateFeatureFlag(featureFlagUid: String, projectId: Long) {
        val removedFeature = featureFlagJpaRepository.searchRemovedByUidAndProjectId(featureFlagUid, projectId)

        if (removedFeature != null) {
            val restored = removedFeature.apply {
                removed = false
                addDomainEvent(FeatureFlagNewActionEvent(ChangeAction.RESTORE, this.project.id!!, this.uid))
            }

            featureFlagJpaRepository.save(restored)
        }
    }

    override fun getFeatureFlagsForProjectUpdatedBefore(projectId: Long, days: Int): List<FeatureFlag> {
        return featureFlagEntityMapper.convertToDomainList(
            featureFlagJpaRepository.searchByProjectIdAndUpdatedBefore(projectId, days)
        )
    }

    override fun getFeatureFlagsByPattern(
        projectId: Long,
        pattern: String,
        pageable: Pageable
    ): Page<FeatureFlag> {
        return featureFlagJpaRepository.searchByPattern(projectId, pattern, pageable)
            .map(featureFlagEntityMapper::convertToDomain)
    }

    override fun getTags(projectId: Long, pageable: Pageable): Page<String> {
        return featureFlagJpaRepository.getDistinctTags(projectId, pageable)
    }
}