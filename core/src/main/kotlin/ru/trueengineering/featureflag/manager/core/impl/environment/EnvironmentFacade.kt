package ru.trueengineering.featureflag.manager.core.impl.environment

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.environment.CompareLists
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsForProject
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsOfProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FindByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.SearchEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.environment.scheduler.DelayUnfreezeService
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectRepository
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.user.UserFacade
import ru.trueengineering.featureflag.manager.core.utils.HashUtils
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Calendar

open class EnvironmentFacade(
    private val environmentRepository: EnvironmentRepository,
    private val featureFlagRepository: FeatureFlagRepository,
    private val projectRepository: ProjectRepository,
    private val userFacade: UserFacade,
    private val delayUnfreezeService: DelayUnfreezeService
) : DeleteEnvironmentUseCase, FetchAllEnvironmentsOfProjectUseCase, UpdateEnvironmentUseCase,
    CreateEnvironmentTokenUseCase, DeleteInstanceUseCase, UpdateFlagsStateUseCase,
    SearchEnvironmentUseCase, GetCompareEnvironmentsStateUseCase,
    FreezeEnvironmentUseCase, UnfreezeEnvironmentUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun fetchProject(projectId: Long): Project {
        return projectRepository.getById(projectId)
    }

    override fun execute(command: UpdateEnvironmentCommand): Environment {
        val environment = environmentRepository.getById(command.environmentId).apply { this.name = command.name }
        val project = fetchProject(command.projectId)
        return environmentRepository.saveEnvironment(environment, project) ?: throw ServiceException(
            ErrorCode.UNABLE_TO_SAVE_ENVIRONMENT,
            "Unable to update environment name '${command.name}' for id '${command.environmentId}'"
        )
    }

    @Transactional
    override fun execute(command: DeleteEnvironmentCommand): Any {
        environmentRepository.remove(command.environmentId)
        return featureFlagRepository.deleteFeatureEnvironment(command.environmentId)
    }

    override fun search(query: FetchAllEnvironmentsForProject): List<Environment> {
        return environmentRepository.getByProjectId(query.projectId)
    }

    open fun createOrUpdateInstance(environment: Environment, agentName: String) {
        val instance: Instance? = environment.instances.firstOrNull { it.name == agentName }
        if (instance != null) {
            environmentRepository.setInstanceStatus(instance, InstanceConnectionStatus.ACTIVE)
        } else {
            environment.id?.let { environmentRepository.createInstance(it, agentName) }
        }
    }

    open fun getEnvironmentByToken(token: String): Environment? {
        val authKeyHash = HashUtils.getHash(token)
        return environmentRepository.findEnvironmentByAuthKeyHash(authKeyHash)
    }

    override fun execute(command: CreateEnvironmentTokenCommand): String {
        val environment = environmentRepository.getById(command.environmentId)
        val token =
            HashUtils.getHash("${environment.id}${environment.name}${environment.authKeyHash}${Calendar.getInstance().timeInMillis}")
        environmentRepository.saveAuthHash(command.environmentId, HashUtils.getHash(token))
        return token
    }

    override fun execute(command: DeleteInstanceCommand) {
        environmentRepository.removeInstance(command.instanceId)
    }

    override fun search(findByIdQuery: FindByIdQuery): Environment {
        return environmentRepository.getById(findByIdQuery.environmentId)
    }

    override fun execute(command: GetCompareEnvironmentsStateCommand): CompareLists {
        val project = projectRepository.getById(command.projectId)
        val featureFlags = featureFlagRepository.getFeatureFlagsForProject(project.id!!)

        val enable = featureFlags.filter { featureFlag ->
            val from = featureFlag.environments.first { it.id == command.from }
            val to = featureFlag.environments.first { it.id == command.to }
            from.enable && !to.enable
        }

        val disable = featureFlags.filter { featureFlag ->
            val from = featureFlag.environments.first { it.id == command.from }
            val to = featureFlag.environments.first { it.id == command.to }
            !from.enable && to.enable
        }

        return CompareLists(enable, disable)
    }

    @Transactional
    override fun execute(command: UpdateFlagsStateCommand) {
        val project = projectRepository.getById(command.projectId)

        command.featureFlagsStates.forEach {
            val feature = featureFlagRepository.getActiveByUidAndProjectId(it.key, project.id!!)

            if (feature != null) {
                feature.environments.first { environment -> environment.id == command.environmentId }.enable = it.value

                val action = if (it.value) ChangeAction.ENABLE else ChangeAction.DISABLE

                featureFlagRepository.createOrEdit(feature, project, action, command.environmentId)
            }
        }
    }

    @Transactional
    override fun execute(command: FreezeEnvironmentCommand): Environment {
        val freezingEnvironments = environmentRepository.getByProjectId(command.projectId)
            .filter { it.properties.containsKey(EnvironmentPropertiesClass.FREEZING_ENABLE) }
            .filter { it.properties[EnvironmentPropertiesClass.FREEZING_ENABLE].toBoolean() }

        if (freezingEnvironments.isNotEmpty()) {
            throw ServiceException(
                ErrorCode.UNABLE_TO_FREEZE_ENVIRONMENT,
                "Unable to freeze environment for id ${command.environmentId} because some environment already freeze"
            )
        }

        if (command.endTime.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw ServiceException(
                ErrorCode.UNABLE_TO_FREEZE_ENVIRONMENT,
                "Unable to freeze environment for id ${command.environmentId} because request time is less than the current time"
            )
        }

        val freezingProperties = mutableMapOf<EnvironmentPropertiesClass, String?>(
            EnvironmentPropertiesClass.FREEZING_ENABLE to true.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to userFacade.search().userName,
            EnvironmentPropertiesClass.FREEZING_END_TIME to command.endTime.toString()
        )

        val frozenEnvironment = environmentRepository.addProperties(command.environmentId, freezingProperties)
        log.info("Environment with id ${command.environmentId} was frozen until ${command.endTime}")

        delayUnfreezeService.delayUnfreeze(command.projectId, command.environmentId, command.endTime)

        return frozenEnvironment
    }

    @Transactional
    override fun execute(command: UnfreezeEnvironmentCommand): Environment {
        val environment = environmentRepository.getById(command.environmentId)

        if (isNotFrozen(environment)) {
            return environment
        }

        val unfreezingProperties = mutableMapOf(
            EnvironmentPropertiesClass.FREEZING_ENABLE to false.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to null,
            EnvironmentPropertiesClass.FREEZING_END_TIME to null
        )

        val unfrozenEnvironment = environmentRepository.addProperties(command.environmentId, unfreezingProperties)
        log.info("Environment with id ${command.environmentId} was unfrozen")
        delayUnfreezeService.deleteUnfreezingJob(command.projectId, command.environmentId)

        return unfrozenEnvironment
    }

    private fun isNotFrozen(environment: Environment): Boolean {
        return environment.properties.containsKey(EnvironmentPropertiesClass.FREEZING_ENABLE) &&
            !environment.properties[EnvironmentPropertiesClass.FREEZING_ENABLE].toBoolean()
    }
}