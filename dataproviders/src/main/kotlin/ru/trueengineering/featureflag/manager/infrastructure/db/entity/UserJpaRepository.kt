package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : CrudRepository<UserEntity, Long> {

    /**
     * Return users, if it isn't removed
     */
    @Query("SELECT u FROM UserEntity u where u.removed <> true")
    fun getAllNonRemoved() : List<UserEntity>

    /**
     * Return user, if it isn't removed
     */
    @Query("SELECT u FROM UserEntity u where u.id = :id AND u.removed <> true")
    fun getNonRemovedById(@Param("id") id: Long) : UserEntity?

    /**
     * Return user, if it isn't removed
     */
    @Query("SELECT u FROM UserEntity u where u.email = :email AND u.removed <> true")
    fun getNonRemovedByEmail(@Param("email") email: String) : UserEntity?

    /**
     * Return users, if they aren't removed
     */
    @Query("SELECT u FROM UserEntity u where u.email in :emails AND u.removed <> true")
    fun getAllNonRemovedByEmailList(@Param("emails") emails: List<String>) : List<UserEntity>

    @Query("SELECT COUNT(u) FROM UserEntity u where u.email in :emails AND u.removed <> true")
    fun getNonRemovedCountByEmailList(emails: List<String>): Int

    @Modifying
    @Query("UPDATE UserEntity u set u.removed = true where u.id = :id")
    fun setRemoved(@Param("id") id: Long)

    @Modifying
    @Query("UPDATE UserEntity u set u.status = :status where u.id = :id")
    fun setStatus(@Param("id") id: Long, @Param("status") status: String)

}