package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailJpaRepository: CrudRepository<EmailEntity, Long> {
    fun findByEnvironmentIdAndEmailIn(environmentId: Long, email: Collection<String>, ): List<EmailEntity>
}