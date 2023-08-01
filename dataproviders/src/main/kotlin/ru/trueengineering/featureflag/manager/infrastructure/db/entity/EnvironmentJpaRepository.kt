package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EnvironmentJpaRepository : CrudRepository<EnvironmentEntity, Long> {

    @Modifying
    @Query("UPDATE EnvironmentEntity e set e.authKeyHash = :authKeyHash where e.id = :id")
    fun setAuthKeyHash(@Param("id") id: Long, @Param("authKeyHash") authKeyHash: String)

    fun findByAuthKeyHash(authKeyHash: String): EnvironmentEntity?

    @Modifying
    @Query("UPDATE EnvironmentEntity e set e.removed = true where e.id = :id")
    fun setRemoved(@Param("id") id: Long)

    @Query("SELECT e FROM EnvironmentEntity e where e.id = :id AND e.removed <> true")
    fun getById(@Param("id") id: Long): EnvironmentEntity?

    @Query("SELECT e FROM EnvironmentEntity e where e.project.id = :projectId AND e.removed <> true")
    fun getAllByProjectId(@Param("projectId") projectId: Long): List<EnvironmentEntity>

    @Query("SELECT e FROM EnvironmentEntity e where e.project.id = :projectId AND e.name = :name AND e.removed <> true")
    fun findByProjectIdAndName(@Param("projectId") projectId: Long, @Param("name") name: String): EnvironmentEntity?

    @Modifying
    @Query(
        "UPDATE env_instance set status = 'OUT_OF_SYNC' " +
                "where status = 'ACTIVE' and updated < now() - (interval '1 sec' ) * :intervalSec", nativeQuery = true)
    fun updateInstanceStatus(@Param("intervalSec") intervalSec: Int)

}