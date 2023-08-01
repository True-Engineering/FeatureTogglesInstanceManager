package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeatureFlagJpaRepository : CrudRepository<FeatureFlagEntity, Long> {

    @Query("SELECT * from features f WHERE f.project_id = :projectId AND f.removed = false", nativeQuery = true)
    fun searchDistinctByProjectId(projectId: Long) : List<FeatureFlagEntity>

    @Query("SELECT * from features f WHERE f.feat_uid = :uid AND f.project_id = :projectId AND f.removed = true", nativeQuery = true)
    fun searchRemovedByUidAndProjectId(uid: String, projectId: Long) : FeatureFlagEntity?

    @Query("SELECT * from features f WHERE f.feat_uid = :uid AND f.project_id = :projectId AND f.removed = false", nativeQuery = true)
    fun searchActiveByUidAndProjectId(uid: String, projectId: Long) : FeatureFlagEntity?

    @Query("SELECT * from features f WHERE f.feat_uid = :uid AND f.project_id = :projectId", nativeQuery = true)
    fun searchByUidAndProjectId(uid: String, projectId: Long) : FeatureFlagEntity?

    @Modifying
    @Query("DELETE FROM FeatureFlagEnvironmentStateEntity e WHERE e.primaryKey.environment.id = :environmentId")
    fun deleteFeatureEnvironmentById(@Param("environmentId") environmentId: Long)

    @Modifying
    @Query("DELETE FROM FeatureFlagEnvironmentStateEntity e WHERE e.primaryKey.featureFlag.id = :featureFlagId")
    fun deleteFeatureEnvironmentByFeatureFlagId(featureFlagId: Long)

    @Query("SELECT COUNT(*) FROM features f WHERE f.project_id = :projectId AND f.removed = false", nativeQuery = true)
    fun countByProjectId(projectId: Long) : Long

    @Query("SELECT * from features f join feature_environment_state fs on f.id = fs.feature_id " +
            "where fs.environment_id = :environmentId", nativeQuery = true)
    fun getFeatureFlagEntityByEnvironmentId(environmentId: Long) : List<FeatureFlagEntity>

    @Query("SELECT COUNT(*) FROM feature_environment_state s WHERE s.feature_id = :featureFlagId", nativeQuery = true)
    fun getFeatureEnvironmentStatesSizeByFeatureFlagId(featureFlagId: Long): Long

    @Query("SELECT * from features f " +
            "WHERE f.project_id = :projectId " +
            "AND f.removed = false " +
            "AND f.updated <= NOW() - interval '1 days' * :days",
        nativeQuery = true)
    fun searchByProjectIdAndUpdatedBefore(projectId: Long, days: Int): List<FeatureFlagEntity>

    @Query("SELECT f FROM FeatureFlagEntity f " +
            "WHERE f.project.id = :projectId " +
            "AND (f.uid like %:pattern% or f.description like %:pattern%)")
    fun searchByPattern(projectId: Long, pattern: String, pageable: Pageable): Page<FeatureFlagEntity>
    @Query("SELECT DISTINCT f.tag FROM FeatureFlagEntity f WHERE f.tag != NULL AND f.project.id = :projectId")
    fun getDistinctTags(projectId: Long, pageable: Pageable): Page<String>
}