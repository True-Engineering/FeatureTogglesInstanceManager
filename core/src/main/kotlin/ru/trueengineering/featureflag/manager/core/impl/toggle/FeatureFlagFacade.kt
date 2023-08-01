package ru.trueengineering.featureflag.manager.core.impl.toggle

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.environment.FindByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.Changes
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.Environment
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentFeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentFeatureFlagsWithUpdateStatus
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentStrategy
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportChanges
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportEnvironments
import ru.trueengineering.featureflag.manager.core.domen.toggle.Strategy
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsUseCase
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentFacade
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectFacade
import ru.trueengineering.featureflag.manager.core.impl.validator.CreateFeatureFlagCommandValidatorHandler
import ru.trueengineering.featureflag.manager.core.impl.validator.EditFeatureFlagCommandValidatorHandler
import ru.trueengineering.featureflag.manager.core.utils.HashUtils

open class FeatureFlagFacade(
    private val environmentFacade: EnvironmentFacade,
    private val projectFacade: ProjectFacade,
    private val featureFlagRepository: FeatureFlagRepository,
    private val featureFlagPropertyHelper: FeatureFlagPropertyHelper,
    private val createFeatureFlagCommandValidatorHandler: CreateFeatureFlagCommandValidatorHandler,
    private val editFeatureFlagCommandValidatorHandler: EditFeatureFlagCommandValidatorHandler
) : CreateFeatureFlagUseCase, DeleteFeatureFlagUseCase,
    DisableFeatureFlagUseCase, EnableFeatureFlagUseCase, FetchAllFeatureFlagsForProjectUseCase,
    FetchAllFeatureFlagsForAgentUseCase, EditFeatureFlagUseCase, EditFeatureFlagStrategyUseCase,
    SynchronizePortalsUseCase, SynchronizeEnvironmentsUseCase, GetImportChangesUseCase,
    GetImportEnvironmentsUseCase, FetchAllEnabledFeatureFlagsForEnvironmentUseCase,
    FindFeatureFlagByPatternUseCase, FetchAllFeatureFlagsTagsUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(command: CreateFeatureFlagCommand): FeatureFlag {
        createFeatureFlagCommandValidatorHandler.validateOrThrow(command)

        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val featureEnvironments = project.environments.map(this::buildFeatureEnvironment).toMutableList()
        val removedFeatureFlag = featureFlagRepository.getRemovedByUidAndProjectId(command.uuid, project.id!!)

        val featureFlag = removedFeatureFlag.also {
            featureFlagRepository.activateFeatureFlag(command.uuid, project.id!!)
        } ?: FeatureFlag(command.uuid)

        featureFlag.apply {
            this.description = command.description
            this.group = command.group
            this.type = command.type
            this.tags = command.tags
            this.sprint = command.sprint
            this.properties = command.properties
            this.environments = featureEnvironments
        }

        val creationInfo = FeatureFlag(
            command.uuid,
            description = command.description,
            type = command.type,
            tags = command.tags,
            sprint = command.sprint,
            properties = command.properties,
            group = command.group
        )

        return when (removedFeatureFlag == null) {
            true -> featureFlagRepository.createOrEdit(featureFlag, project, ChangeAction.CREATE, creationInfo = creationInfo)
            false -> featureFlagRepository.createOrEdit(featureFlag, project, creationInfo = creationInfo)
        }
    }

    @Transactional
    override fun execute(command: EditFeatureFlagCommand): FeatureFlag {
        editFeatureFlagCommandValidatorHandler.validateOrThrow(command)

        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val savedFeature = featureFlagRepository.getActiveByUidAndProjectId(command.uuid, command.projectId)
            ?: throw ServiceException(ErrorCode.FEATURE_FLAG_NOT_FOUND, "Unable to find feature ${command.uuid}")

        val newFeature = savedFeature.copy(
            description = command.description,
            group = command.group,
            type = command.type,
            tags = command.tags,
            sprint = command.sprint,
            properties = command.properties
        )

        if (needToUpdate(savedFeature, newFeature)) {
            val featureChanges = featureFlagPropertyHelper.getChanges(savedFeature, newFeature)
            return featureFlagRepository.createOrEdit(newFeature, project, ChangeAction.EDIT, changes = featureChanges)
        }

        return savedFeature
    }

    override fun execute(command: EditFeatureFlagStrategyCommand): FeatureFlag {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val featureFlag = featureFlagRepository.getActiveByUidAndProjectId(command.uuid, command.projectId)
            ?: throw ServiceException(ErrorCode.FEATURE_FLAG_NOT_FOUND, "Unable to find feature ${command.uuid}")

        val featureEnvironment = getFeatureEnvironment(featureFlag, command.environmentId)
        if (command.type == null || command.initParams == null) featureEnvironment.strategy = null
        else featureEnvironment.strategy = Strategy(command.type, command.initParams)

        return featureFlagRepository.createOrEdit(featureFlag, project)
    }

    @Transactional
    override fun execute(command: DeleteFeatureFlagCommand): Any {
        return featureFlagRepository.deleteFeatureFlag(command.uuid, command.projectId)
    }

    @Transactional
    override fun execute(command: DisableFeatureFlagCommand): Any {
        val environmentProperties = environmentFacade.search(FindByIdQuery(command.environmentId)).properties
        val freezingEnable = EnvironmentPropertiesClass.FREEZING_ENABLE
        if (environmentProperties.containsKey(freezingEnable) && environmentProperties[freezingEnable].toBoolean()) {
            throw ServiceException(
                ErrorCode.ENVIRONMENT_IS_FROZEN,
                "Unable to disable feature flag with uid ${command.uuid} " +
                        "because environment with id ${command.environmentId} is frozen"
            )
        }

        return updateFeatureFlagState(command.uuid, command.projectId, command.environmentId, false)
    }

    @Transactional
    override fun execute(command: EnableFeatureFlagCommand): Any {
        val environmentProperties = environmentFacade.search(FindByIdQuery(command.environmentId)).properties
        val freezingEnable = EnvironmentPropertiesClass.FREEZING_ENABLE
        if (environmentProperties.containsKey(freezingEnable) && environmentProperties[freezingEnable].toBoolean()) {
            throw ServiceException(
                ErrorCode.ENVIRONMENT_IS_FROZEN,
                "Unable to enable feature flag with uid ${command.uuid} " +
                        "because environment with id ${command.environmentId} is frozen"
            )
        }

        return updateFeatureFlagState(command.uuid, command.projectId, command.environmentId, true)
    }

    override fun search(query: FetchAllFeatureFlagsForAgentQuery): EnvironmentFeatureFlagsWithUpdateStatus {
        val environment = environmentFacade.getEnvironmentByToken(query.token)
            ?: throw ServiceException(ErrorCode.ENVIRONMENT_NOT_FOUND, "Unable to find authToken ${query.token}")

        val environmentId = environment.id ?: throw ServiceException(ErrorCode.ENVIRONMENT_NOT_FOUND)
        val featureFlags = featureFlagRepository.getFeatureFlagsForEnvironment(environmentId)
            .map { buildAgentFeatureFlag(it, environmentId) }

        val updated = checkHash(featureFlags, query.featureFlagHash)

        environmentFacade.createOrUpdateInstance(environment, query.agentName)
        if (updated) {
            log.info("Send new feature flags state to agent with authTokenHash ${HashUtils.getHash(query.token)}")
            return EnvironmentFeatureFlagsWithUpdateStatus(featureFlags, updated)
        }
        return EnvironmentFeatureFlagsWithUpdateStatus(emptyList(), updated)
    }

    override fun search(query: FetchAllFeatureFlagsForProjectQuery): List<FeatureFlag> {
        return featureFlagRepository.getFeatureFlagsForProject(query.projectId)
    }

    override fun execute(command: GetImportEnvironmentsCommand): ImportEnvironments {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val currentFeatureFlags = featureFlagRepository.getFeatureFlagsForProject(project.id!!)

        val envSynchronizedStatus = isSynchronized(currentFeatureFlags, command.featureFlagStates)
        val srcEnvironments = getFeatureFlagsEnvironments(command.featureFlagStates)
        val destEnvironments = getFeatureFlagsEnvironments(currentFeatureFlags)

        return ImportEnvironments(command.key, envSynchronizedStatus, srcEnvironments, destEnvironments)
    }

    override fun execute(command: GetImportChangesCommand): ImportChanges {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val featureFlags = featureFlagRepository.getFeatureFlagsForProject(project.id!!)

        val featureFlagsToAdd = getNewFlags(featureFlags, command.featureFlagStates)
        val featureFlagsToRemove = getExtraFlags(featureFlags, command.featureFlagStates)
        val featureFlagsToUpdate = getUpdatedFlags(featureFlags, command.featureFlagStates)
        featureFlagsToAdd.plus(featureFlagsToRemove).plus(featureFlagsToUpdate.map { it.currentFeatureFlag }
            .plus(featureFlagsToUpdate.map { it.newFeatureFlag })).map { it.environments = ArrayList() }

        return ImportChanges(command.key, featureFlagsToAdd, featureFlagsToRemove, featureFlagsToUpdate)
    }

    /**
     * Синхронизация значений фича флагов между окружениями
     *
     * Выбираются два окружения: источник и назначение. Окружение-источик может быть получен из файла
     * Все значения ФФ на окружении назначении примут такие же значения как на окружении источнике
     * Перед выполнением синхронизации окружений необходимо сделать синхронизацию порталов
     */
    @Transactional
    override fun execute(command: SynchronizeEnvironmentsCommand): List<FeatureFlag> {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val currentFeatureFlags = featureFlagRepository.getFeatureFlagsForProject(project.id!!)

        currentFeatureFlags.map { featureFlag ->
            if (featureFlag.uid in command.featureFlagStates.map { it.uid }) {
                featureFlag.environments.map { environment ->
                    if (environment.name in command.copyDirection.dest) {
                        val matchFeatureFlag = command.featureFlagStates.first { it.uid == featureFlag.uid }
                        val matchEnvironment =
                            matchFeatureFlag.environments.first { it.name == command.copyDirection.src }

                        if (environment.enable != matchEnvironment.enable) {
                            environment.enable = matchEnvironment.enable

                            featureFlagRepository.createOrEdit(
                                featureFlag,
                                project,
                                defineAction(environment.enable),
                                environment.id
                            )
                        }
                    }
                }
            }
        }

        return featureFlagRepository.getFeatureFlagsForProject(project.id!!)
    }

    /**
     * Синхронизация списков фича флагов в рамках нескольких порталов
     *
     * Если задан needToDelete, то удалятся все фича флаги, которых нет в подаваемом списке, иначе они останутся
     * Если в подаваемом списке находится ФФ, который уже есть на текущем портале, то он, если нужно меняет свои поля
     * Если в подаваемом списке находится ФФ, который уже есть на текущем портале, но был удален, то он восстанавливается
     * Новые ФФ создаются на текущем портале с проектными окружениями со значением false
     */
    @Transactional
    override fun execute(command: SynchronizePortalsCommand): List<FeatureFlag> {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val currentFeatureFlags = featureFlagRepository.getFeatureFlagsForProject(project.id!!)

        if (command.needToDelete) {
            val extraFlags = getExtraFlags(currentFeatureFlags, command.featureFlagStates)
            extraFlags.forEach { ff -> execute(DeleteFeatureFlagCommand(ff.uid, project.id!!)) }
        }

        command.featureFlagStates.map { newFeatureFlag ->
            val feature = featureFlagRepository.getDespiteRemovedByUidAndProjectId(newFeatureFlag.uid, project.id!!)

            if (feature != null) {
                updateExistingFeatureFlag(feature, newFeatureFlag, project)
            }
            else {
                createNewFeatureFlagWithProjectEnvironments(newFeatureFlag, project)
            }
        }

        return featureFlagRepository.getFeatureFlagsForProject(project.id!!)
    }

    @Transactional
    override fun execute(command: FetchAllEnabledFeatureFlagsForEnvironmentCommand): List<FeatureFlag> {
        val project = projectFacade.search(SearchProjectByIdQuery(command.projectId))
        val featureFlags = featureFlagRepository.getFeatureFlagsForProjectUpdatedBefore(project.id!!, command.days)

        val enabled = featureFlags.filter { featureFlag ->
            val environment = featureFlag.environments.first { it.id == command.environmentId }
            environment.enable
        }

        return enabled
    }

    override fun execute(command: FindFeatureFlagByPatternCommand): Page<FeatureFlag> {
        return featureFlagRepository.getFeatureFlagsByPattern(
            command.projectId,
            command.pattern,
            command.pageable
        )
    }

    override fun execute(command: FetchAllFeatureFlagsTagsCommand): Page<String> {
        return featureFlagRepository.getTags(command.projectId, command.pageable)
    }

    private fun buildFeatureEnvironment(
        environment:
        ru.trueengineering.featureflag.manager.core.domen.environment.Environment
    ): Environment {
        val environmentId = environment.id ?: throw ServiceException(
            ErrorCode.UNABLE_TO_SAVE_FEATURE_FLAG,
            "Unable to create feature flag environment without id"
        )
        return Environment(environmentId, environment.name)
    }

    private fun updateFeatureFlagState(uuid: String, projectId: Long, environmentId: Long, enable: Boolean) {
        val featureFlag = featureFlagRepository.getActiveByUidAndProjectId(uuid, projectId)
            ?: throw ServiceException(ErrorCode.FEATURE_FLAG_NOT_FOUND, "Unable to find feature $uuid")

        val project = projectFacade.search(SearchProjectByIdQuery(projectId))

        val featureEnvironment = getFeatureEnvironment(featureFlag, environmentId)
        featureEnvironment.enable = enable

        featureFlagRepository.createOrEdit(featureFlag, project, defineAction(enable), environmentId)
    }

    private fun getFeatureEnvironment(featureFlag: FeatureFlag, environmentId: Long) =
        (featureFlag.environments.firstOrNull { environmentId == it.id }
            ?: throw ServiceException(
                ErrorCode.ENVIRONMENT_NOT_FOUND,
                "Unable to find environment with id $environmentId"
            ))

    private fun buildAgentFeatureFlag(featureFlag: FeatureFlag, environmentId: Long): EnvironmentFeatureFlag {
        val environmentFeatureFlag = EnvironmentFeatureFlag(featureFlag.uid).also {
            it.description = featureFlag.description
            it.group = featureFlag.group
            featureFlag.sprint?.let { sprint -> it.customProperties["sprint"] = sprint }
            featureFlag.type?.let { type -> it.customProperties["type"] = type.name }
            featureFlag.tags.firstOrNull()?.let { tag -> it.customProperties["tag"] = tag }
        }
        val environment = featureFlag.environments.firstOrNull { it.id == environmentId }
        environmentFeatureFlag.enable = environment?.enable ?: false
        environmentFeatureFlag.flippingStrategy =
            environment?.strategy?.let { EnvironmentStrategy(it.type, it.initParams) }
        return environmentFeatureFlag
    }

    private fun checkHash(featureFlags: List<EnvironmentFeatureFlag>, agentHash: String?): Boolean {
        return HashUtils.getHash(featureFlags) != agentHash
    }

    private fun getFeatureFlagsEnvironments(featureFlags: List<FeatureFlag>): List<ru.trueengineering.featureflag.manager.core.domen.environment.Environment> {
        if (featureFlags.isEmpty()) {
            return listOf()
        }

        return featureFlags[0].environments.map {
            ru.trueengineering.featureflag.manager.core.domen.environment.Environment(it.id, it.name)
        }
    }

    private fun getNewFlags(
        currentFeatureFlags: List<FeatureFlag>,
        newFeatureFlags: List<FeatureFlag>,
    ): List<FeatureFlag> {
        return newFeatureFlags.filter { it.uid !in currentFeatureFlags.map { ff -> ff.uid } }.map { it.copy() }
    }

    private fun getExtraFlags(
        currentFeatureFlags: List<FeatureFlag>,
        newFeatureFlags: List<FeatureFlag>
    ): List<FeatureFlag> {
        return currentFeatureFlags.filter { it.uid !in newFeatureFlags.map { ff -> ff.uid } }.map { it.copy() }
    }

    private fun getUpdatedFlags(
        currentFeatureFlags: List<FeatureFlag>,
        newFeatureFlags: List<FeatureFlag>
    ): List<Changes> {
        val featureFlagsToUpdate = ArrayList<Changes>()

        newFeatureFlags.forEach { newFeatureFlag ->
            if (newFeatureFlag.uid in currentFeatureFlags.map { it.uid }) {
                val matchFlag = currentFeatureFlags.first { it.uid == newFeatureFlag.uid }
                if (needToUpdate(matchFlag, newFeatureFlag)) {
                    featureFlagsToUpdate.add(Changes(newFeatureFlag.copy(), matchFlag.copy()))
                }
            }
        }

        return featureFlagsToUpdate
    }

    private fun needToUpdate(featureFlag: FeatureFlag, newFeatureFlag: FeatureFlag): Boolean {
        return featureFlag.description != newFeatureFlag.description ||
                featureFlag.group != newFeatureFlag.group ||
                featureFlag.type != newFeatureFlag.type ||
                featureFlag.tags != newFeatureFlag.tags ||
                featureFlag.sprint != newFeatureFlag.sprint ||
                featureFlag.properties != newFeatureFlag.properties
    }

    private fun updateExistingFeatureFlag(
        featureFlag: FeatureFlag,
        newFeatureFlag: FeatureFlag,
        project: Project
    ): FeatureFlag {
        val projectEnvironments = project.environments.map { buildFeatureEnvironment(it) }.toMutableList()

        val removedFeature = featureFlagRepository.getRemovedByUidAndProjectId(featureFlag.uid, project.id!!)
        if (removedFeature != null) {
            featureFlagRepository.activateFeatureFlag(removedFeature.uid, project.id!!)
            removedFeature.environments = projectEnvironments
            featureFlagRepository.createOrEdit(removedFeature, project)
        }

        if (needToUpdate(featureFlag, newFeatureFlag)) {
            val featureChanges = featureFlagPropertyHelper.getChanges(featureFlag, newFeatureFlag)
            val updatedFeatureFlag = featureFlag.copy(
                description = newFeatureFlag.description,
                group = newFeatureFlag.group,
                type = newFeatureFlag.type,
                tags = newFeatureFlag.tags,
                sprint = newFeatureFlag.sprint,
                properties = newFeatureFlag.properties
            )

            if (removedFeature != null) {
                updatedFeatureFlag.environments = removedFeature.environments
            }

            featureFlagRepository.createOrEdit(updatedFeatureFlag, project, ChangeAction.EDIT, changes = featureChanges)
        }
        return featureFlag
    }

    private fun createNewFeatureFlagWithProjectEnvironments(
        featureFlag: FeatureFlag,
        project: Project
    ): FeatureFlag {
        val projectEnvironments = project.environments.map { buildFeatureEnvironment(it) }.toMutableList()
        val featureFlagWithEnvironments = featureFlag.copy(environments = projectEnvironments)
        featureFlagRepository.createOrEdit(featureFlagWithEnvironments, project, ChangeAction.CREATE)
        return featureFlagWithEnvironments
    }

    private fun isSynchronized(
        currentFeatureFlags: List<FeatureFlag>,
        newFeatureFlags: List<FeatureFlag>
    ): Boolean {
        val featureFlagsToAdd = getNewFlags(currentFeatureFlags, newFeatureFlags)
        val featureFlagsToRemove = getExtraFlags(currentFeatureFlags, newFeatureFlags)
        val featureFlagsToUpdate = getUpdatedFlags(currentFeatureFlags, newFeatureFlags)

        return featureFlagsToAdd.plus(featureFlagsToRemove).plus(featureFlagsToUpdate).isEmpty()
    }

    private fun defineAction(enable: Boolean): ChangeAction {
        return if (enable) ChangeAction.ENABLE else ChangeAction.DISABLE
    }
}
