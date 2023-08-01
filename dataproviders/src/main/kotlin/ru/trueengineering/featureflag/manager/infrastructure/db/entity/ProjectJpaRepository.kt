package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProjectJpaRepository : CrudRepository<ProjectEntity, Long> {

    @Modifying
    @Query("UPDATE ProjectEntity p set p.removed = true where p.id = :id")
    fun setRemoved(@Param("id") id: Long)

    @Modifying
    @Query("UPDATE ProjectEntity p set p.name = :projectName where p.id = :id")
    fun updateName(@Param("projectName") projectName: String, @Param("id") id: Long)

    /**
     * Return project, if it isn't removed
     */
    @Query("SELECT p FROM ProjectEntity p where p.id = :id AND p.removed <> true")
    fun getById(@Param("id") id: Long): ProjectEntity?

    @Query("SELECT p FROM ProjectEntity p where p.name = :name AND p.removed <> true")
    fun findByName(@Param("name") name: String): ProjectEntity?
}