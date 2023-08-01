package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Repository
@Transactional
interface InvitationJpaRepository : CrudRepository<InvitationEntity, UUID> {


    fun findByProjectId(projectId: Long): Optional<InvitationEntity>

}