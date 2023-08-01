package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentProperties
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EmailEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EmailJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.EnvironmentEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper
import java.time.Instant

@Service
class EnvironmentDatabaseRepository(
    val environmentRepository: EnvironmentJpaRepository,
    val emailRepository: EmailJpaRepository,
    val instanceRepository: InstanceJpaRepository,
    val environmentEntityMapper: EnvironmentEntityMapper,
    val projectEntityMapper: ProjectEntityMapper
) : EnvironmentRepository {

    override fun findEnvironmentByAuthKeyHash(authKeyHash: String): Environment? {
        return environmentRepository.findByAuthKeyHash(authKeyHash)?.let(environmentEntityMapper::convertToDomain)
    }

    @Transactional
    override fun setInstanceStatus(instance: Instance, instanceConnectionStatus: InstanceConnectionStatus) {
        instance.id?.let { id ->
            instanceRepository.findById(id).ifPresent {
                it.status = instanceConnectionStatus
                it.updated = Instant.now()
                instanceRepository.save(it)
            }
        }
    }

    override fun saveEnvironment(environment: Environment, project: Project): Environment? {
        val environmentEntity = environmentEntityMapper.convertToEntity(environment)
        val projectEntity = projectEntityMapper.convertToEntity(project)
        environmentEntity.project = projectEntity
        environmentEntity.emails.forEach{ it.environment = environmentEntity}
        val result = environmentRepository.save(environmentEntity)
        return environmentEntityMapper.convertToDomain(result)
    }

    @Transactional
    override fun remove(environmentId: Long) {
        environmentRepository.findById(environmentId)
            .ifPresent { environmentRepository.setRemoved(environmentId) }
    }

    @Transactional
    override fun getById(environmentId: Long): Environment {
        val environment: EnvironmentEntity = environmentRepository.getById(environmentId) ?: throw ServiceException(
            ErrorCode.ENVIRONMENT_NOT_FOUND, "Environment not found for id = '${environmentId}'"
        )
        return environmentEntityMapper.convertToDomain(environment)
    }

    @Transactional
    override fun saveAuthHash(environmentId: Long, authKeyHash: String) {
        return environmentRepository.setAuthKeyHash(environmentId, authKeyHash)
    }

    @Transactional
    override fun createInstance(environmentId: Long, agentName: String): Environment? {
        val environmentEntity: EnvironmentEntity? = environmentRepository.getById(environmentId)?.let {
            val instance = InstanceEntity(agentName)
            instance.environment = it
            it.instances.add(instance)
            environmentRepository.save(it)
        }
        return environmentEntity?.let(environmentEntityMapper::convertToDomain)
    }

    override fun removeInstance(instanceId: Long) {
        instanceRepository.deleteById(instanceId)
    }

    override fun getByProjectId(projectId: Long): List<Environment> {
        return environmentEntityMapper.convertToDomainList(environmentRepository.getAllByProjectId(projectId))
    }

    override fun getByProjectIdAndName(projectId: Long, envName: String): Environment? {
        return environmentRepository.findByProjectIdAndName(projectId, envName)
            ?.let(environmentEntityMapper::convertToDomain)
    }

    override fun saveAll(environments: List<Environment>, project: Project): List<Environment> {
        val projectEntity = projectEntityMapper.convertToEntity(project)
        val entities = environmentEntityMapper.convertToEntityList(environments)
        entities.onEach { env ->
            env.project = projectEntity
            env.instances.onEach { it.environment = env }
        }
        return environmentEntityMapper.convertToDomainList(environmentRepository.saveAll(entities))
    }

    @Transactional
    override fun checkAndUpdateInstanceStatus(instanceOutOfSyncTimeSec: Int) {
        environmentRepository.updateInstanceStatus(instanceOutOfSyncTimeSec)
    }

    @Transactional
    override fun createEmails(environmentId: Long, emails: List<String>) {
        val environmentEntity = environmentRepository.getById(environmentId)

        if (environmentEntity != null) {
            val emailEntities = emails.map { EmailEntity(it) }
            emailEntities.forEach { environmentEntity.addEmail(it) }

            environmentRepository.save(environmentEntity)
        }
    }

    @Transactional
    override fun removeEmails(environmentId: Long, emails: List<String>) {
        val environmentEntity = environmentRepository.getById(environmentId)

        if (environmentEntity != null) {
            val emailEntities = emailRepository.findByEnvironmentIdAndEmailIn(environmentId, emails)
            emailEntities.forEach { environmentEntity.removeEmail(it) }

            environmentRepository.save(environmentEntity)
        }
    }

    override fun updateSettings(environment: Environment, project: Project): Environment {
        val projectEntity = projectEntityMapper.convertToEntity(project)

        val environmentEntity = environmentEntityMapper.convertToEntity(environment).apply {
            this.project = projectEntity
            this.emails.forEach { it.environment = this }
        }

        return environmentEntityMapper.convertToDomain(environmentRepository.save(environmentEntity))
    }

    @Transactional
    override fun addProperties(environmentId: Long, newProperties: EnvironmentProperties): Environment {
        val environmentEntity = environmentRepository.getById(environmentId) ?: throw ServiceException(
            ErrorCode.ENVIRONMENT_NOT_FOUND, "Environment not found for id = '${environmentId}'"
        )

        environmentEntity.properties.putAll(newProperties)

        return environmentEntityMapper.convertToDomain(environmentRepository.save(environmentEntity))
    }
}