package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.OrganizationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.OrganizationJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.OrganizationEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper

@Service
class OrganizationDatabaseRepository(
    val organizationJpaRepository: OrganizationJpaRepository,
    val organizationEntityMapper: OrganizationEntityMapper,
    val projectEntityMapper: ProjectEntityMapper
) : OrganizationRepository {

    override fun findAll(): List<Organization> {
        return organizationEntityMapper.convertToDomainList(organizationJpaRepository.getAllNonRemoved())
    }

    override fun findById(organizationId: Long): Organization {
        return organizationEntityMapper.convertToDomain(findOrganizationEntity(organizationId))

    }

    private fun findOrganizationEntity(organizationId: Long): OrganizationEntity {
        return organizationJpaRepository.getNonRemovedById(organizationId) ?: throw ServiceException(
            ErrorCode.ORGANIZATION_NOT_FOUND, "Organization not found"
        )
    }

    override fun findByName(name: String): Organization? {
        val organization = organizationJpaRepository.findFirstByName(name).orElse(null) ?: return null
        return organizationEntityMapper.convertToDomain(organization)
    }

    @Transactional
    override fun removeById(organizationId: Long) {
        organizationJpaRepository.setRemoved(organizationId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    override fun create(name: String): Organization {
        organizationJpaRepository.findFirstByName(name)
            .ifPresent { throw ServiceException(ErrorCode.ORGANIZATION_ALREADY_EXIST) }
        return organizationEntityMapper.convertToDomain(organizationJpaRepository.save(OrganizationEntity(name)))
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [ServiceException::class])
    override fun addNewProject(organizationId: Long, project: Project): Project {
        val organizationEntity = organizationJpaRepository.save(
            findOrganizationEntity(organizationId).addNewProject(projectEntityMapper.convertToEntity(project))
        )
        val projectEntity = organizationEntity.projects
            .first { it.name == project.name }
        return projectEntityMapper.convertToDomain(projectEntity)
    }
}