package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

interface ChangesHistoryJpaRepository:
    CrudRepository<ChangesHistoryEntity, Long>,
    JpaSpecificationExecutor<ChangesHistoryEntity> {}
