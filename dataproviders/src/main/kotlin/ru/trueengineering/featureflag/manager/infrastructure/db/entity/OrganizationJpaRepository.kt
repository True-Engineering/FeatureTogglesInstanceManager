package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrganizationJpaRepository : CrudRepository<OrganizationEntity, Long> {

    fun findFirstByName(name: String): Optional<OrganizationEntity>

    /**
     * Return organizations, if it isn't removed
     */
    @Query("SELECT o FROM OrganizationEntity o where o.removed <> true")
    fun getAllNonRemoved() : List<OrganizationEntity>

    /**
     * Return organization, if it isn't removed
     */
    @Query("SELECT o FROM OrganizationEntity o where o.id = :id AND o.removed <> true")
    fun getNonRemovedById(@Param("id") id: Long) : OrganizationEntity?

    @Modifying
    @Query("UPDATE OrganizationEntity o set o.removed = true where o.id = :id")
    fun setRemoved(@Param("id") id: Long)
}