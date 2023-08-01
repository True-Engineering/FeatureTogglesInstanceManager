package ru.trueengineering.featureflag.manager.core.impl.toggle

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.environment.FindByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.CopyDirection
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentFeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.Strategy
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsCommand
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentFacade
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectFacade
import ru.trueengineering.featureflag.manager.core.impl.validator.CreateFeatureFlagCommandValidatorHandler
import ru.trueengineering.featureflag.manager.core.impl.validator.EditFeatureFlagCommandValidatorHandler
import java.time.Instant
import kotlin.test.assertEquals

private const val TOKEN = "TOKEN"
private const val FEATURE_HASH = "72bcda93949e8004d3966e1abba8f063c803335255d0225062f4239817bf3fb1"
private const val OTHER_HASH = "OTHER_HASH"

private const val AUTH_TOKEN_HASH = "AUTH_TOKEN_HASH"

private const val FEATURE_FLAG = "feature.flag"

private const val TAG = "WEB"

private const val SPRINT = "SPRINT 1"

private const val DEV_ENVIRONMENT_NAME = "DEV"

private const val QA_ENVIRONMENT_NAME = "QA"

private const val UAT_ENVIRONMENT_NAME = "UAT"

private const val PROD_ENVIRONMENT_NAME = "PROD"

private const val NEW_ENVIRONMENT_NAME = "NEW_ENV"

internal class FeatureFlagFacadeTest {

    private val environmentFacade: EnvironmentFacade = mockk()

    private val featureFlagRepository: FeatureFlagRepository = mockk()

    private val projectFacade: ProjectFacade = mockk()

    private val featureFlagPropertyHelper: FeatureFlagPropertyHelper = mockk()

    private val createFeatureFlagCommandValidatorHandler: CreateFeatureFlagCommandValidatorHandler = mockk()

    private val editFeatureFlagCommandValidatorHandler: EditFeatureFlagCommandValidatorHandler = mockk()

    val uut: FeatureFlagFacade = FeatureFlagFacade(
            environmentFacade, projectFacade, featureFlagRepository, featureFlagPropertyHelper,
            createFeatureFlagCommandValidatorHandler, editFeatureFlagCommandValidatorHandler
        )

    @Test
    fun searchShouldReturnTrue() {
        val agentName = "Instance"
        val instance = Instance(1, agentName, Instant.now(), InstanceConnectionStatus.UNAVAILABLE)
        val environment = Environment(1, QA_ENVIRONMENT_NAME, AUTH_TOKEN_HASH, listOf(instance))

        every { environmentFacade.getEnvironmentByToken(TOKEN) } returns environment
        every { environmentFacade.createOrUpdateInstance(environment, agentName) } just Runs
        every { featureFlagRepository.getFeatureFlagsForEnvironment(1) } returns listOf(buildFeatureFlag(FEATURE_FLAG))

        val query = FetchAllFeatureFlagsForAgentQuery(TOKEN, agentName, OTHER_HASH)
        val actual = uut.search(query)
        assertThat(actual.update).isTrue
        assertThat(actual.featureFlags).isNotNull.hasSize(1)
        assertThat(actual.featureFlags.get(0)).isNotNull.extracting(
            EnvironmentFeatureFlag::uid,
            EnvironmentFeatureFlag::enable
        ).containsExactly(
            FEATURE_FLAG,
            true
        )
        assertThat(actual.featureFlags.get(0).flippingStrategy).isNotNull
        assertThat(actual.featureFlags.get(0).customProperties).isEqualTo(
            mapOf(
                Pair("sprint", SPRINT),
                Pair("type", FeatureFlagType.RELEASE.name),
                Pair("tag", TAG)
            )
        )

        verify(exactly = 1) { environmentFacade.createOrUpdateInstance(environment, agentName) }
    }

    @Test
    fun searchShouldReturnFalse() {
        val agentName = "Instance"
        val instance = Instance(1, agentName, Instant.now(), InstanceConnectionStatus.UNAVAILABLE)
        val environment = Environment(1, QA_ENVIRONMENT_NAME, AUTH_TOKEN_HASH, listOf(instance))

        every { environmentFacade.getEnvironmentByToken(TOKEN) } returns environment
        every { environmentFacade.createOrUpdateInstance(environment, agentName) } just Runs
        every { featureFlagRepository.getFeatureFlagsForEnvironment(1) } returns
                listOf(buildFeatureFlag(FEATURE_FLAG))

        val query = FetchAllFeatureFlagsForAgentQuery(TOKEN, agentName, FEATURE_HASH)
        val actual = uut.search(query)
        assertThat(actual.update).isFalse
        assertThat(actual.featureFlags).isNotNull.isEmpty()

        verify(exactly = 1) { environmentFacade.createOrUpdateInstance(environment, agentName) }
    }

    @Test
    fun searchShouldThrowEnvNotFound() {
        val agentName = "Instance"

        every { environmentFacade.getEnvironmentByToken(TOKEN) } returns null
        every { featureFlagRepository.getFeatureFlagsForEnvironment(1) } returns
                listOf(buildFeatureFlag(FEATURE_FLAG))

        val query = FetchAllFeatureFlagsForAgentQuery(TOKEN, agentName, FEATURE_HASH)

        val actualException = assertThrows<ServiceException> { uut.search(query) }
        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find authToken ${TOKEN}")


        verify(exactly = 0) { environmentFacade.createOrUpdateInstance(any(), agentName) }
    }

    @Test
    fun delete() {
        every { featureFlagRepository.deleteFeatureFlag(FEATURE_FLAG, 1) } just Runs
        uut.execute(DeleteFeatureFlagCommand(FEATURE_FLAG, 1))
        verify { featureFlagRepository.deleteFeatureFlag(FEATURE_FLAG, 1) }
    }

    private fun buildFeatureFlag(featureUid: String): FeatureFlag {
        val strategy = Strategy("RequestContextFlippingStrategy", mutableMapOf(Pair("param", "value")))
        val environments = mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
                1,
                QA_ENVIRONMENT_NAME,
                true,
                strategy
            ),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
                2,
                DEV_ENVIRONMENT_NAME,
                false,
                strategy
            ),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
                3,
                UAT_ENVIRONMENT_NAME,
                true,
                strategy
            ),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
                4,
                PROD_ENVIRONMENT_NAME,
                true,
                strategy
            )
        )
        val featureFlag = FeatureFlag(featureUid, environments.toMutableList())
        featureFlag.description = featureUid
        featureFlag.tags = setOf(TAG)
        featureFlag.type = FeatureFlagType.RELEASE
        featureFlag.sprint = SPRINT
        return featureFlag
    }

    @Test
    fun create() {
        val description = "description"
        val projectId = 1L
        val devEnvName = "DEV"
        val qaEnvName = "QA"
        listOf(ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2L, devEnvName))
        val command = CreateFeatureFlagCommand(FEATURE_FLAG, projectId, description)
        val projectEnvironments = mutableListOf(Environment(3, qaEnvName))
        val slot = slot<FeatureFlag>()
        val project = Project(1L, "project", projectEnvironments)

        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.getRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns null
        every { featureFlagRepository.activateFeatureFlag(FEATURE_FLAG, 1L) } just Runs
        val expected = FeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.CREATE, creationInfo = any()) } returns expected
        every { createFeatureFlagCommandValidatorHandler.validateOrThrow(command) } returns command
        val actual = uut.execute(command)

        assertThat(actual).isEqualTo(expected)

        val captured = slot.captured
        assertThat(captured.uid).isEqualTo(FEATURE_FLAG)
        assertThat(captured.description).isEqualTo(description)
        assertThat(captured.environments)
            .hasSize(1)
        assertThat(captured.environments[0].name).isEqualTo(qaEnvName)
        assertThat(captured.environments[0].enable).isFalse
        assertThat(captured.environments[0].strategy).isNull()
    }

    @Test
    fun createNotValid() {
        val command = CreateFeatureFlagCommand("uid", 1)

        every { createFeatureFlagCommandValidatorHandler.validateOrThrow(command) } throws ServiceException(
            ErrorCode.UNABLE_TO_SAVE_PROJECT
        )

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_SAVE_PROJECT)

        verify (exactly = 0) { featureFlagRepository.createOrEdit(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun restore() {
        val removedFeatureFlag = FeatureFlag(FEATURE_FLAG, description = "old description")
        val description = "description"
        val projectId = 1L
        val qaEnvName = "QA"
        val command = CreateFeatureFlagCommand(FEATURE_FLAG, projectId, description)
        val projectEnvironments = mutableListOf(Environment(3, qaEnvName))
        val slot = slot<FeatureFlag>()
        val project = Project(1L, "project", projectEnvironments)

        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.getRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns removedFeatureFlag
        every { featureFlagRepository.activateFeatureFlag(FEATURE_FLAG, 1L) } just Runs
        every { featureFlagRepository.createOrEdit(capture(slot), project, creationInfo = any()) } answers { firstArg() }
        every { createFeatureFlagCommandValidatorHandler.validateOrThrow(command) } returns command

        uut.execute(command)

        val captured = slot.captured
        verify (exactly = 0) { featureFlagRepository.createOrEdit(any(), project, ChangeAction.CREATE) }
        verify (exactly = 1) { featureFlagRepository.createOrEdit(any(), project, null, creationInfo = any()) }
        verify (exactly = 1) { featureFlagRepository.activateFeatureFlag(FEATURE_FLAG, 1L) }
        assertThat(captured.uid).isEqualTo(FEATURE_FLAG)
        assertThat(captured.description).isEqualTo(description)
        assertThat(captured.environments).hasSize(1)
        assertThat(captured.environments[0].name).isEqualTo(qaEnvName)
        assertThat(captured.environments[0].enable).isFalse
        assertThat(captured.environments[0].strategy).isNull()
    }

    @Test
    fun edit() {
        val description = "description"
        val projectId = 1L
        val devEnvName = "DEV"
        val qaEnvName = "QA"
        listOf(ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2L, devEnvName))
        val group = "group"
        val type = FeatureFlagType.RELEASE
        val sprint = "sprint_1"
        val tags = setOf("tag")
        val command = EditFeatureFlagCommand(FEATURE_FLAG, projectId, description, group, type, sprint, tags)
        val projectEnvironments = mutableListOf(Environment(3, qaEnvName))
        val slot = slot<FeatureFlag>()
        val project = Project(1L, "project", projectEnvironments)
        val featureId = 35L
        val savedFeatureFlag = FeatureFlag(FEATURE_FLAG).apply { id = featureId }
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, projectId) } returns savedFeatureFlag
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns
                project
        val expected = FeatureFlag(FEATURE_FLAG)
        every { featureFlagPropertyHelper.getChanges(any(), any()) } returns FeatureChanges()
        every { featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.EDIT, null, FeatureChanges()) } returns expected
        every { editFeatureFlagCommandValidatorHandler.validateOrThrow(command) } returns command

        val actual = uut.execute(command)

        assertThat(actual).isEqualTo(expected)

        val captured = slot.captured
        assertThat(captured.id).isEqualTo(featureId)
        assertThat(captured.uid).isEqualTo(FEATURE_FLAG)
        assertThat(captured.description).isEqualTo(description)
        assertThat(captured.group).isEqualTo(group)
        assertThat(captured.type).isEqualTo(type)
        assertThat(captured.sprint).isEqualTo(sprint)
        assertThat(captured.tags).isEqualTo(tags)
    }

    @Test
    fun editNotValid() {
        val command = EditFeatureFlagCommand("uid", 1)

        every { editFeatureFlagCommandValidatorHandler.validateOrThrow(command) } throws ServiceException(
            ErrorCode.UNABLE_TO_SAVE_PROJECT
        )

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_SAVE_PROJECT)

        verify (exactly = 0) { featureFlagRepository.createOrEdit(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun editFeatureFlagNotFound() {
        val description = "description"
        val projectId = 1L
        val devEnvName = "DEV"
        listOf(ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2L, devEnvName))
        val group = "group"
        val type = FeatureFlagType.RELEASE
        val sprint = "sprint_1"
        val tags = setOf("tag")
        val command = EditFeatureFlagCommand(FEATURE_FLAG, projectId, description, group, type, sprint, tags)

        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, projectId) } returns null
        val project = Project(1L, "Project")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { editFeatureFlagCommandValidatorHandler.validateOrThrow(command) } returns command

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        assertEquals(actualException.errorCode, ErrorCode.FEATURE_FLAG_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find feature $FEATURE_FLAG")
    }

    @Test
    fun enableFeatureFlagEnvironmentExist() {
        val command = EnableFeatureFlagCommand(FEATURE_FLAG, 1L, 2L)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val project = Project(1L, "Project")
        every { environmentFacade.search(FindByIdQuery(2)) } returns Environment(2, "env")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.createOrEdit(any(), any(), ChangeAction.ENABLE, 2L) } returns featureFlag

        uut.execute(command)
        assertThat(featureFlag.environments.first { it.id == 2L }.enable).isTrue
        verify { featureFlagRepository.createOrEdit(featureFlag, project, ChangeAction.ENABLE, 2L) }
    }

    @Test
    fun disableFeatureFlagEnvironmentExist() {
        val command = DisableFeatureFlagCommand(FEATURE_FLAG, 1L, 1L)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val project = Project(1L, "Project")
        every { environmentFacade.search(FindByIdQuery(1)) } returns Environment(1, "env")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.createOrEdit(any(), any(), ChangeAction.DISABLE, 1L) } returns featureFlag

        uut.execute(command)
        assertThat(featureFlag.environments.first { it.id == 1L }.enable).isFalse
        verify { featureFlagRepository.createOrEdit(featureFlag, project, ChangeAction.DISABLE, 1L) }
    }

    @Test
    fun enableFeatureFlagNotFound() {
        val command = EnableFeatureFlagCommand(FEATURE_FLAG, 1L, 1L)
        every { environmentFacade.search(FindByIdQuery(1)) } returns Environment(1, "env")
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns null

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.FEATURE_FLAG_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find feature $FEATURE_FLAG")
    }

    @Test
    fun disableFeatureFlagNotFound() {
        val command = DisableFeatureFlagCommand(FEATURE_FLAG, 1L, 1L)
        every { environmentFacade.search(FindByIdQuery(1)) } returns Environment(1, "env")
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns null

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.FEATURE_FLAG_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find feature $FEATURE_FLAG")
    }

    @Test
    fun enableFeatureFlagEnvironmentNotExist() {
        val command = EnableFeatureFlagCommand(FEATURE_FLAG, 1L, 5L)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val environment = Environment(5, NEW_ENVIRONMENT_NAME)
        val project = Project(1L, "Project", mutableListOf(environment))
        every { environmentFacade.search(FindByIdQuery(5)) } returns environment
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find environment with id 5")
    }

    @Test
    fun disableFeatureFlagEnvironmentNotExist() {
        val command = DisableFeatureFlagCommand(FEATURE_FLAG, 1L, 5L)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val environment = Environment(5, NEW_ENVIRONMENT_NAME)
        val project = Project(1L, "Project", mutableListOf(environment))
        every { environmentFacade.search(FindByIdQuery(5)) } returns environment
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find environment with id 5")
    }

    @Test
    fun disableFeatureFlagEnvironmentNotFound() {
        val command = DisableFeatureFlagCommand(FEATURE_FLAG, 1L, 5L)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val project = Project(1L, "Project")
        every { environmentFacade.search(FindByIdQuery(5)) } throws ServiceException(
            ErrorCode.ENVIRONMENT_NOT_FOUND,
            "Environment not found for id = 5"
        )
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Environment not found for id = 5")
    }

    @Test
    fun enableFeatureFlagEnvironmentFrozen() {
        val command = EnableFeatureFlagCommand(FEATURE_FLAG, 1L, 1L)
        val environment = Environment(1, "env", properties = mutableMapOf(EnvironmentPropertiesClass.FREEZING_ENABLE to "true"))

        every { environmentFacade.search(FindByIdQuery(1)) } returns environment

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_IS_FROZEN)
        assertEquals(actualException.errorMessage, "Unable to enable feature flag with uid ${FEATURE_FLAG} " +
                "because environment with id 1 is frozen")
    }

    @Test
    fun disableFeatureFlagEnvironmentFrozen() {
        val command = DisableFeatureFlagCommand(FEATURE_FLAG, 1L, 1L)
        val environment = Environment(1, "env", properties = mutableMapOf(EnvironmentPropertiesClass.FREEZING_ENABLE to "true"))

        every { environmentFacade.search(FindByIdQuery(1)) } returns environment

        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_IS_FROZEN)
        assertEquals(actualException.errorMessage, "Unable to disable feature flag with uid ${FEATURE_FLAG} " +
                "because environment with id 1 is frozen")
    }

    @Test
    fun changeStrategyFeatureFlagEnvironmentExist() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val command = EditFeatureFlagStrategyCommand(FEATURE_FLAG, 1L, 2L, type, initParams)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val project = Project(1L, "Project")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.createOrEdit(any(), any()) } returns featureFlag
        uut.execute(command)

        val strategy = featureFlag.environments.first { it.id == 2L }.strategy
        assertThat(strategy).isNotNull
        assertThat(strategy!!.type).isEqualTo(type)
        assertThat(strategy.initParams).isEqualTo(initParams)
        verify { featureFlagRepository.createOrEdit(featureFlag, project) }
    }

    @Test
    fun changeStrategyFeatureFlagEnvironmentNotExist() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val command = EditFeatureFlagStrategyCommand(FEATURE_FLAG, 1L, 5L, type, initParams)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val environment = Environment(5, NEW_ENVIRONMENT_NAME)
        val project = Project(1L, "Project", mutableListOf(environment))
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project


        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find environment with id 5")
    }

    @Test
    fun changeStrategyFeatureFlagEnvironmentNotFound() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val command = EditFeatureFlagStrategyCommand(FEATURE_FLAG, 1L, 5L, type, initParams)
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        val project = Project(1L, "Project")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.createOrEdit(any(), any()) } returns featureFlag


        val actualException = assertThrows<ServiceException> { uut.execute(command) }

        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find environment with id 5")
    }

    @Test
    fun changeStrategyFeatureFlagNotFound() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val command = EditFeatureFlagStrategyCommand(FEATURE_FLAG, 1L, 5L, type, initParams)
        every { featureFlagRepository.getActiveByUidAndProjectId(FEATURE_FLAG, 1L) } returns null
        val project = Project(1L, "Project")
        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        assertEquals(actualException.errorCode, ErrorCode.FEATURE_FLAG_NOT_FOUND)
        assertEquals(actualException.errorMessage, "Unable to find feature $FEATURE_FLAG")
    }

    @Test
    fun uploadFeatureFlagsUpdate() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val featureFlag = buildFeatureFlag(FEATURE_FLAG)
        val environmentQa = Environment(1, QA_ENVIRONMENT_NAME)
        val environmentDev = Environment(2, DEV_ENVIRONMENT_NAME)
        val environmentUat = Environment(3, UAT_ENVIRONMENT_NAME)
        val environmentProd = Environment(4, PROD_ENVIRONMENT_NAME)
        val project =
            Project(1L, "Project", mutableListOf(environmentQa, environmentDev, environmentUat, environmentProd))
        val newFeatureFlagEnvironment = ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
            2, DEV_ENVIRONMENT_NAME, true, Strategy(type, initParams)
        )
        val newFeatureFlag = FeatureFlag(uid = FEATURE_FLAG, mutableListOf(newFeatureFlagEnvironment))
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(featureFlag)
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        every { featureFlagRepository.getRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns null
        every { featureFlagPropertyHelper.getChanges(any(), any()) } returns FeatureChanges()
        every {
            featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.EDIT, null, FeatureChanges())
        } answers { firstArg() }

        uut.execute(SynchronizePortalsCommand(1L, listOf(newFeatureFlag), needToDelete = false))

        verify {
            featureFlagRepository.createOrEdit(any(), project, ChangeAction.EDIT, null, FeatureChanges())
        }
        assertThat(slot.captured.environments).hasSize(4)
        assertThat(slot.captured.environments[1].enable).isFalse()
        assertThat(slot.captured.description).isEqualTo(newFeatureFlag.description)
    }

    @Test
    fun uploadFeatureFlagsCreate() {
        val type = "strategy"
        val initParams = mutableMapOf(Pair("param1", "param2"))
        val environment = Environment(2, DEV_ENVIRONMENT_NAME)
        val project = Project(1L, "Project", mutableListOf(environment))
        val newFeatureFlagEnvironment = ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
            2, DEV_ENVIRONMENT_NAME, true, Strategy(type, initParams)
        )
        val newFeatureFLag = FeatureFlag(
            uid = FEATURE_FLAG,
            description = "description",
            type = FeatureFlagType.RELEASE,
            sprint = "sprint1",
            tags = setOf("tag1"),
            environments = mutableListOf(newFeatureFlagEnvironment)
        )
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(eq(SearchProjectByIdQuery(1L))) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf()
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns null
        every { featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.CREATE) } answers { firstArg() }

        uut.execute(SynchronizePortalsCommand(1L, listOf(newFeatureFLag), needToDelete = false))

        assertThat(slot.captured.uid).isEqualTo(FEATURE_FLAG)
        assertThat(slot.captured.description).isEqualTo("description")
        assertThat(slot.captured.type).isEqualTo(FeatureFlagType.RELEASE)
        assertThat(slot.captured.sprint).isEqualTo("sprint1")
        assertThat(slot.captured.tags).containsExactly("tag1")
        assertThat(slot.captured.environments[0].id)
            .isEqualTo(2)
        assertThat(slot.captured.environments[0].name)
            .isEqualTo(DEV_ENVIRONMENT_NAME)
        assertThat(slot.captured.environments[0].enable)
            .isEqualTo(false)
    }

    @Test
    fun synchronizePortalsWithOutDelete() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val featureFlag = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val newFeatureFlag = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(3, DEV_ENVIRONMENT_NAME, true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(4, UAT_ENVIRONMENT_NAME, true)
            )
        )
        val resultFlags = listOf(
            featureFlag,
            FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(3, "Petrov", false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(4, "Smirnov", false)
            )
        ))
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returnsMany listOf(listOf(featureFlag), resultFlags)
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId("FF2", 1L) } returns null
        every { featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.CREATE) } answers { firstArg() }

        val actual = uut.execute(SynchronizePortalsCommand(1L, listOf(newFeatureFlag), needToDelete = false))

        verify(exactly = 0) { featureFlagRepository.deleteFeatureFlag(any(), 1L) }
        verify (exactly = 2) { featureFlagRepository.getFeatureFlagsForProject(1L) }
        assertThat(actual).isEqualTo(resultFlags)
        assertThat(slot.captured.uid).isEqualTo("FF2")
        assertThat(slot.captured.environments.size).isEqualTo(2)
        assertThat(slot.captured.environments[0].name).isEqualTo("Petrov")
        assertThat(slot.captured.environments[0].enable).isEqualTo(false)
        assertThat(slot.captured.environments[1].name).isEqualTo("Smirnov")
        assertThat(slot.captured.environments[1].enable).isEqualTo(false)
    }

    @Test
    fun synchronizeEnvironments() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val FF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val FF2 = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val newFF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, false)
            )
        )
        val newFF2 = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, true)
            )
        )
        val ffList = mutableListOf<FeatureFlag>()

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(FF1, FF2)
        every { featureFlagRepository.createOrEdit(any(), project, any(), any()) } answers {
            ffList.add(firstArg())
            firstArg()
        }

        uut.execute(
            SynchronizeEnvironmentsCommand(
                1L,
                listOf(newFF1, newFF2),
                CopyDirection(UAT_ENVIRONMENT_NAME, listOf("Smirnov"))
            )
        )

        assertThat(ffList.size).isEqualTo(2)
        assertThat(ffList[0].environments).isEqualTo(
            mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        assertThat(ffList[1].environments).isEqualTo(
            mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
    }

    @Test
    fun synchronizePortalsWithDelete() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val featureFlag = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val newFeatureFlag = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(3, DEV_ENVIRONMENT_NAME, true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(4, UAT_ENVIRONMENT_NAME, true)
            )
        )
        val resultFlags = listOf(
            FeatureFlag(
                "FF2", mutableListOf(
                    ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(3, "Petrov", false),
                    ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(4, "Smirnov", false)
                )
            ))
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returnsMany listOf(listOf(featureFlag), resultFlags)
        every { featureFlagRepository.deleteFeatureFlag("FF1", 1L) } just Runs
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId("FF2", 1L) } returns null
        every { featureFlagRepository.createOrEdit(capture(slot), project, ChangeAction.CREATE) } answers { firstArg() }

        val actual = uut.execute(SynchronizePortalsCommand(1L, listOf(newFeatureFlag), needToDelete = true))

        verify(exactly = 1) { featureFlagRepository.deleteFeatureFlag(any(), 1L) }
        verify (exactly = 2) { featureFlagRepository.getFeatureFlagsForProject(1L) }
        assertThat(actual).isEqualTo(resultFlags)
        assertThat(slot.captured.uid).isEqualTo("FF2")
        assertThat(slot.captured.environments.size).isEqualTo(2)
        assertThat(slot.captured.environments[0].name).isEqualTo("Petrov")
        assertThat(slot.captured.environments[0].enable).isEqualTo(false)
        assertThat(slot.captured.environments[1].name).isEqualTo("Smirnov")
        assertThat(slot.captured.environments[1].enable).isEqualTo(false)
    }

    @Test
    fun getImportEnvironmentsTrueValidation() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val featureFlag = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val newFeatureFlag = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, true)
            )
        )

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(featureFlag)

        val actual = uut.execute(GetImportEnvironmentsCommand("key", 1L, listOf(newFeatureFlag)))
        assertThat(actual.key).isEqualTo("key")
        assertThat(actual.envSynchronizedStatus).isEqualTo(true)
        assertThat(actual.srcEnvironments.size).isEqualTo(2)
        assertThat(actual.srcEnvironments[0].name).isEqualTo(DEV_ENVIRONMENT_NAME)
        assertThat(actual.srcEnvironments[1].name).isEqualTo(UAT_ENVIRONMENT_NAME)
        assertThat(actual.destEnvironments.size).isEqualTo(2)
        assertThat(actual.destEnvironments[0].name).isEqualTo("Petrov")
        assertThat(actual.destEnvironments[1].name).isEqualTo("Smirnov")
    }

    @Test
    fun getImportEnvironmentsFalseValidation() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val featureFlag = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val newFeatureFlag = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, true)
            )
        )

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(featureFlag)

        val actual = uut.execute(GetImportEnvironmentsCommand("key", 1L, listOf(newFeatureFlag)))
        assertThat(actual.key).isEqualTo("key")
        assertThat(actual.envSynchronizedStatus).isEqualTo(false)
        assertThat(actual.srcEnvironments.size).isEqualTo(2)
        assertThat(actual.srcEnvironments[0].name).isEqualTo(DEV_ENVIRONMENT_NAME)
        assertThat(actual.srcEnvironments[1].name).isEqualTo(UAT_ENVIRONMENT_NAME)
        assertThat(actual.destEnvironments.size).isEqualTo(2)
        assertThat(actual.destEnvironments[0].name).isEqualTo("Petrov")
        assertThat(actual.destEnvironments[1].name).isEqualTo("Smirnov")
    }

    @Test
    fun getImportChanges() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val FF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            ),
            description = "description",
            sprint = "sprint"
        )
        val FF2 = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val FF3 = FeatureFlag(
            "FF3", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, true)
            )
        )
        val updatedFF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, true)
            ),
            description = "new description",
            sprint = "new sprint"
        )

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(FF1, FF2)

        val actual = uut.execute(GetImportChangesCommand("key", 1L, listOf(FF3, updatedFF1)))
        assertThat(actual.key).isEqualTo("key")
        assertThat(actual.featureFlagsToAdd.size).isEqualTo(1)
        assertThat(actual.featureFlagsToAdd[0].uid).isEqualTo("FF3")
        assertThat(actual.featureFlagsToRemove.size).isEqualTo(1)
        assertThat(actual.featureFlagsToRemove[0].uid).isEqualTo("FF2")
        assertThat(actual.featureFlagsToUpdate.size).isEqualTo(1)
        assertThat(actual.featureFlagsToUpdate[0].currentFeatureFlag.description).isEqualTo("description")
        assertThat(actual.featureFlagsToUpdate[0].currentFeatureFlag.sprint).isEqualTo("sprint")
        assertThat(actual.featureFlagsToUpdate[0].newFeatureFlag.description).isEqualTo("new description")
        assertThat(actual.featureFlagsToUpdate[0].newFeatureFlag.sprint).isEqualTo("new sprint")
        assertThat(actual.featureFlagsToUpdate[0].newFeatureFlag.uid).isEqualTo(actual.featureFlagsToUpdate[0].currentFeatureFlag.uid)
    }


    @Test
    fun restoreFeatureFlag() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val featureFlag = FeatureFlag(
            FEATURE_FLAG, mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returnsMany listOf(listOf(), listOf(featureFlag))
        every { featureFlagRepository.getDespiteRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        every { featureFlagRepository.getRemovedByUidAndProjectId(FEATURE_FLAG, 1L) } returns featureFlag
        every { featureFlagRepository.activateFeatureFlag(FEATURE_FLAG, 1L) }  just Runs
        every { featureFlagRepository.createOrEdit(capture(slot), project) } answers { firstArg() }

        uut.execute(SynchronizePortalsCommand(1L, listOf(featureFlag), false))

        verify (exactly = 1) { featureFlagRepository.activateFeatureFlag(FEATURE_FLAG, 1L) }
        assertThat(slot.captured.uid).isEqualTo(FEATURE_FLAG)
        assertThat(slot.captured.environments).hasSize(2)
        assertThat(slot.captured.environments[0].name).isEqualTo("Petrov")
        assertThat(slot.captured.environments[0].enable).isFalse()
        assertThat(slot.captured.environments[1].name).isEqualTo("Smirnov")
        assertThat(slot.captured.environments[1].enable).isFalse()
    }

    @Test
    fun synchronizeEnvironmentsWithoutPortalsSynchronization() {
        val petrovEnv = Environment(1, "Petrov")
        val smirnovEnv = Environment(2, "Smirnov")
        val project = Project(1L, "Project", mutableListOf(petrovEnv, smirnovEnv))
        val FF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val FF2 = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val newFF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, DEV_ENVIRONMENT_NAME, false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, UAT_ENVIRONMENT_NAME, false)
            )
        )
        val slot = slot<FeatureFlag>()

        every { projectFacade.search(SearchProjectByIdQuery(1L)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(1L) } returns listOf(FF1, FF2)
        every { featureFlagRepository.createOrEdit(capture(slot), project, any(), any()) } answers { firstArg() }

        uut.execute(
            SynchronizeEnvironmentsCommand(
                1L,
                listOf(newFF1),
                CopyDirection(UAT_ENVIRONMENT_NAME, listOf("Smirnov"))
            )
        )

        verify (exactly = 1) { featureFlagRepository.createOrEdit(any(), project, any(), any()) }
        assertThat(slot.captured.uid).isEqualTo("FF1")
        assertThat(slot.captured.environments).isEqualTo(
            mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
    }

    @Test
    fun fetchAllEnabledFeatureFlagsForEnvironment() {
        val project = Project(1, "project")
        val FF1 = FeatureFlag(
            "FF1", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", true),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", true)
            )
        )
        val FF2 = FeatureFlag(
            "FF2", mutableListOf(
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "Petrov", false),
                ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "Smirnov", false)
            )
        )
        val featureFlags = listOf(FF1, FF2)

        every { projectFacade.search(SearchProjectByIdQuery(1)) } returns project
        every { featureFlagRepository.getFeatureFlagsForProjectUpdatedBefore(1, 1) } returns featureFlags

        val actual = uut.execute(FetchAllEnabledFeatureFlagsForEnvironmentCommand(1, 1, 1))

        assertThat(actual.size).isEqualTo(1)
        assertThat(actual[0].uid).isEqualTo("FF1")
    }

    @Test
    fun findFeatureFlagsByPattern() {
        val pageable = PageRequest.of(0, 1)
        val command = FindFeatureFlagByPatternCommand(1, "test", pageable)
        val featureFlag = FeatureFlag("test123")
        val page = PageImpl(listOf(featureFlag))

        every { featureFlagRepository.getFeatureFlagsByPattern(any(), any(), any()) } returns page

        val actual = uut.execute(command)

        verify { featureFlagRepository.getFeatureFlagsByPattern(command.projectId, command.pattern, command.pageable) }
        assertThat(actual).isEqualTo(page)
    }
}