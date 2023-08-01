package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectProperties
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.FeatureFlagEnvironmentEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper

@Repository
@Transactional
class ProjectDatabaseRepository(
    val projectRepository: ProjectJpaRepository,
    val projectEntityMapper: ProjectEntityMapper,
    val featureFlagJpaRepository: FeatureFlagJpaRepository,
    val featureFlagEnvironmentEntityMapper: FeatureFlagEnvironmentEntityMapper
) : ProjectRepository {

    override fun deleteProject(projectId: Long) {
        projectRepository.setRemoved(projectId)
    }

    override fun getById(id: Long): Project {
        return projectRepository.getById(id)?.let(projectEntityMapper::convertToDomain)
            ?: throw ServiceException(ErrorCode.PROJECT_NOT_FOUND)

    }

    override fun findByNameOrNull(name: String): Project? {
        return projectRepository.findByName(name)
            ?.let(projectEntityMapper::convertToDomain)
    }

    @Transactional
    override fun addNewEnvironment(projectId: Long, environment: Environment) {
        val projectEntity = (projectRepository.getById(projectId)?.let {
            projectRepository.save(it.addNewEnvironment(EnvironmentEntity(environment.name)))
        }
            ?: throw ServiceException(ErrorCode.PROJECT_NOT_FOUND))

        val environmentEntity = projectEntity.environments.first { it.name == environment.name && !it.removed }

        val featureFlags = featureFlagJpaRepository.searchDistinctByProjectId(projectId)
        featureFlags.forEach {
            val featureFlagEnvEntity = featureFlagEnvironmentEntityMapper
                .convertToEntity(
                    ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
                        id = environmentEntity.id!!.toLong(),
                        name = environmentEntity.name
                    )
                )
            featureFlagEnvEntity.primaryKey!!.featureFlag = it
            it.addNewFeatureFlagEnvironment(featureFlagEnvEntity)
        }
        featureFlagJpaRepository.saveAll(featureFlags)
    }

    override fun updateName(id: Long, projectName: String) {
        projectRepository.updateName(projectName, id)
    }

    override fun setProperties(projectId: Long, properties: ProjectProperties) {
        val projectEntity = projectRepository.getById(projectId)

        if (projectEntity != null) {
            projectEntity.properties = properties
            projectRepository.save(projectEntity)
        }
    }
}