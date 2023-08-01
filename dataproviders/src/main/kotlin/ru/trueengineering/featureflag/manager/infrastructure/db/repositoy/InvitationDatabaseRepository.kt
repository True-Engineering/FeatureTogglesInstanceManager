package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.user.Invitation
import ru.trueengineering.featureflag.manager.core.impl.user.InvitationRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InvitationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InvitationJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.InvitationEntityMapper
import java.util.UUID

@Service
class InvitationDatabaseRepository(
    private val invitationJpaRepository: InvitationJpaRepository,
    private val projectJpaRepository: ProjectJpaRepository,
    private val mapper: InvitationEntityMapper
) : InvitationRepository {

    override fun create(uuid: UUID, projectId: Long): Invitation {
        val projectEntity = projectJpaRepository.getById(projectId)
        return mapper
            .convertToDomain(invitationJpaRepository.save(InvitationEntity(uuid).apply { project = projectEntity!! }))
    }

    override fun findByProject(projectId: Long): Invitation? =
        invitationJpaRepository.findByProjectId(projectId)
            .map { mapper.convertToDomain(it) }
            .orElse(null)

    override fun findById(invitationId: UUID): Invitation? =
        invitationJpaRepository.findById(invitationId)
            .map { mapper.convertToDomain(it) }
            .orElse(null)
}