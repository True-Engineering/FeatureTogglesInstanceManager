package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InstanceJpaRepository : CrudRepository<InstanceEntity, Long> {
}