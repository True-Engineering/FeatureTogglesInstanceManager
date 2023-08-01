package ru.trueengineering.featureflag.manager.core.impl.environment

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsForProject
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.environment.scheduler.DelayUnfreezeService
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectRepository
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.user.UserFacade
import ru.trueengineering.featureflag.manager.core.utils.HashUtils
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Calendar
import kotlin.test.assertEquals

private const val TOKEN = "TOKEN"

private const val AUTH_KEY = "f98103e9217f099208569d295c1b276f1821348636c268c854bb2a086e0037cd"

private const val ENV = "ENV"

internal class EnvironmentFacadeTest {

    private val environmentRepository: EnvironmentRepository = mockk()
    private val projectRepository: ProjectRepository = mockk()
    private val featureFlagRepository: FeatureFlagRepository = mockk()
    private val calendar = mockk<Calendar>()
    private val userFacade: UserFacade = mockk()
    private val delayUnfreezeService: DelayUnfreezeService = mockk()

    private val uut: EnvironmentFacade = EnvironmentFacade(
        environmentRepository,
        featureFlagRepository,
        projectRepository,
        userFacade,
        delayUnfreezeService
    )

    @Test
    internal fun updateEnvironment() {
        val projectId = 123L
        val environmentId = 234L
        val environmentName = "test env"
        val newName = "$environmentName additional text"
        val environment = Environment(environmentId, environmentName)
        val project = Project(projectId, "test project")

        every { environmentRepository.getById(environmentId) } returns environment
        every { projectRepository.getById(projectId) } returns project
        every { environmentRepository.saveEnvironment(any(), any()) } returns Environment(
                environmentId, newName
        )
        val actual = uut.execute(UpdateEnvironmentCommand(environmentId, newName, projectId))

        assertThat(actual.id).isEqualTo(environmentId)
        assertThat(actual.name).isEqualTo(newName)
    }

    @Test
    internal fun unableToUpdateEnvironment() {
        val projectId = 123L
        val environmentId = 234L
        val environmentName = "test env"
        val newName = "$environmentName additional text"
        val environment = Environment(environmentId, environmentName)
        val project = Project(projectId, "test project")

        every { environmentRepository.getById(environmentId) } returns environment
        every { projectRepository.getById(projectId) } returns project
        every { environmentRepository.saveEnvironment(any(), any()) } returns null

        val actualException = assertThrows<ServiceException> {
            uut.execute(UpdateEnvironmentCommand(environmentId, newName, projectId))
        }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_SAVE_ENVIRONMENT)
        assertEquals(
                actualException.errorMessage,
                "Unable to update environment name '${newName}' for id '${environmentId}'"
        )

    }

    @Test
    internal fun updateNullEnvironment() {
        val projectId = 123L
        val environmentId = 234L
        val environmentName = "test env"
        val newName = "$environmentName additional text"

        every { environmentRepository.getById(environmentId) } throws  ServiceException(ErrorCode.ENVIRONMENT_NOT_FOUND)

        val actualException = assertThrows<ServiceException> {
            uut.execute(UpdateEnvironmentCommand(environmentId, newName, projectId))
        }
        assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
    }

    @Test
    fun getEnvironmentByToken() {
        val environment = Environment(1, "PROD", "Token Hash", emptyList())
        every { environmentRepository.findEnvironmentByAuthKeyHash(AUTH_KEY) } returns environment
        uut.getEnvironmentByToken(TOKEN)
        verify { environmentRepository.findEnvironmentByAuthKeyHash(AUTH_KEY) }
    }

    @Test
    fun setInstanceActive() {
        val agentName = "Instance"
        val instance = Instance(1, agentName, Instant.now(), InstanceConnectionStatus.UNAVAILABLE)
        val environment = Environment(1, "QA", AUTH_KEY, listOf(instance))

        every { environmentRepository.setInstanceStatus(any(), any()) } just Runs

        uut.createOrUpdateInstance(environment, agentName)

        verify { environmentRepository.setInstanceStatus(instance, InstanceConnectionStatus.ACTIVE) }
    }

    @Test
    fun createInstance() {
        val agentName = "Instance"
        val environment = Environment(1, "QA", AUTH_KEY)

        every { environmentRepository.createInstance(any(), any()) } returns environment

        uut.createOrUpdateInstance(environment, agentName)

        verify { environmentRepository.createInstance(1, agentName) }
    }

    @Test
    internal fun setEnvironmentToken() {
        val token = HashUtils.getHash("1PRODnull123")
        val hash = HashUtils.getHash(token)
        Thread.sleep(2)

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns 123
        every { environmentRepository.getById(any()) } returns Environment(1, "PROD")
        every { environmentRepository.saveAuthHash(any(), any()) } just Runs

        val actualToken = uut.execute(CreateEnvironmentTokenCommand(1))

        assertEquals(token, actualToken)
        verify { environmentRepository.saveAuthHash(1, hash) }
    }

    @Test
    fun searchByProject() {
        val expected = listOf(Environment(1, ENV))
        every { environmentRepository.getByProjectId(1) } returns expected
        val actual = uut.search(FetchAllEnvironmentsForProject(1))
        assertEquals(expected, actual)
    }

    @Test
    fun delete() {
        every { environmentRepository.remove(1) } just Runs
        every { featureFlagRepository.deleteFeatureEnvironment(1) } just Runs

        uut.execute(DeleteEnvironmentCommand(1))
        verify { environmentRepository.remove(1) }
        verify { featureFlagRepository.deleteFeatureEnvironment(1) }
    }

    @Test
    fun getCompareEnvironmentsState() {
        val project = Project(1, "project")
        val FF1 = FeatureFlag("ff1", mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "env1", true),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "env2", false)
        ))
        val FF2 = FeatureFlag("ff2", mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "env1", false),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "env2", true)
        ))
        val FF3 = FeatureFlag("ff3", mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "env1", false),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "env2", false)
        ))
        every { projectRepository.getById(1) } returns project
        every { featureFlagRepository.getFeatureFlagsForProject(project.id!!) } returns listOf(FF1, FF2, FF3)

        val actual = uut.execute(GetCompareEnvironmentsStateCommand(1, 1, 2))

        assertThat(actual.enable.size).isEqualTo(1)
        assertThat(actual.disable.size).isEqualTo(1)
        assertThat(actual.enable[0].uid).isEqualTo("ff1")
        assertThat(actual.disable[0].uid).isEqualTo("ff2")
    }

    @Test
    fun updateFlagsState() {
        val project = Project(1, "project")
        val FF1 = FeatureFlag("ff1", mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "env1", true),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "env2", false)
        ))
        val FF2 = FeatureFlag("ff2", mutableListOf(
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(1, "env1", false),
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(2, "env2", true)
        ))
        val states = mapOf("ff1" to true, "ff2" to false)
        val updatedFlags = mutableListOf<FeatureFlag>()

        every { projectRepository.getById(1) } returns project
        every { featureFlagRepository.getActiveByUidAndProjectId("ff1", 1) } returns FF1
        every { featureFlagRepository.getActiveByUidAndProjectId("ff2", 1) } returns FF2
        every { featureFlagRepository.createOrEdit(any(), project, any(), any()) } answers {
            updatedFlags.add(firstArg())
            firstArg()
        }

        uut.execute(UpdateFlagsStateCommand(1, 2, states))

        verify (exactly = 2) { featureFlagRepository.createOrEdit(any(), project, any(), any()) }
        assertThat(updatedFlags.size).isEqualTo(2)
        assertThat(updatedFlags[0].uid).isEqualTo("ff1")
        assertThat(updatedFlags[0].environments.first { it.id == 1L }.enable).isEqualTo(true)
        assertThat(updatedFlags[0].environments.first { it.id == 2L }.enable).isEqualTo(true)
        assertThat(updatedFlags[1].uid).isEqualTo("ff2")
        assertThat(updatedFlags[1].environments.first { it.id == 1L }.enable).isEqualTo(false)
        assertThat(updatedFlags[1].environments.first { it.id == 2L }.enable).isEqualTo(false)
    }

    @Test
    fun unableToFreezeEnvironmentBecauseOfSomeOneElseFrozen() {
        val environments = listOf(
            Environment(1, "env1"),
            Environment(2, "env2", properties = mutableMapOf(EnvironmentPropertiesClass.FREEZING_ENABLE to true.toString()))
        )
        every { environmentRepository.getByProjectId(1) } returns environments

        val actualException = assertThrows<ServiceException> {
            uut.execute(FreezeEnvironmentCommand(1, 1, OffsetDateTime.now(ZoneOffset.UTC)))
        }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_FREEZE_ENVIRONMENT)
        assertEquals(
            actualException.errorMessage,
            "Unable to freeze environment for id 1 because some environment already freeze"
        )
    }

    @Test
    fun unableToFreezeEnvironmentBecauseOfBadTime() {
        val environments = listOf(
            Environment(1, "env1"),
            Environment(2, "env2")
        )

        every { environmentRepository.getByProjectId(1) } returns environments

        val actualException = assertThrows<ServiceException> {
            uut.execute(FreezeEnvironmentCommand(1, 1, OffsetDateTime.MIN))
        }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_FREEZE_ENVIRONMENT)
        assertEquals(
            actualException.errorMessage,
            "Unable to freeze environment for id 1 because request time is less than the current time"
        )
    }

    @Test
    fun freezeEnvironment() {
        val environments = listOf(
            Environment(1, "env1"),
            Environment(2, "env2")
        )
        val endTime = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(3)


        every { userFacade.search() } returns User("name", "email")
        val freezingProperties = mutableMapOf<EnvironmentPropertiesClass, String?>(
            EnvironmentPropertiesClass.FREEZING_ENABLE to true.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to userFacade.search().userName,
            EnvironmentPropertiesClass.FREEZING_END_TIME to endTime.toString()
        )
        val unfreezingProperties = mutableMapOf(
            EnvironmentPropertiesClass.FREEZING_ENABLE to false.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to null,
            EnvironmentPropertiesClass.FREEZING_END_TIME to null
        )
        val frozenEnvironment = Environment(1, "env1", properties = freezingProperties)
        val unfrozenEnvironment = Environment(1, "env2", properties = unfreezingProperties)

        every { environmentRepository.getByProjectId(1) } returns environments
        every { environmentRepository.addProperties(1, freezingProperties) } returns frozenEnvironment
        every { environmentRepository.getById(1) } returns frozenEnvironment
        every { environmentRepository.addProperties(1, unfreezingProperties) } returns unfrozenEnvironment
        every { delayUnfreezeService.delayUnfreeze(1, 1, endTime) } just Runs

        val actual = uut.execute(FreezeEnvironmentCommand(1, 1,  endTime))

        assertThat(actual).isEqualTo(frozenEnvironment)
        verify (exactly = 1) { environmentRepository.addProperties(1, freezingProperties) }
        verify (exactly = 0) { environmentRepository.addProperties(1, unfreezingProperties) }
        verify (exactly = 1) { delayUnfreezeService.delayUnfreeze(1, 1, endTime) }
    }

    @Test
    fun unfreezeEnvironmentThatNotFrozen() {
        val environment = Environment(1, "env", properties = mutableMapOf(EnvironmentPropertiesClass.FREEZING_ENABLE to false.toString()))
        val unfreezingProperties = mutableMapOf(
            EnvironmentPropertiesClass.FREEZING_ENABLE to false.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to null,
            EnvironmentPropertiesClass.FREEZING_END_TIME to null
        )
        val newEnvironment = Environment(1, "env", properties = unfreezingProperties)

        every { environmentRepository.getById(1) } returns environment
        every { environmentRepository.addProperties(1, unfreezingProperties) } returns newEnvironment

        val actual = uut.execute(UnfreezeEnvironmentCommand(1, 1))

        assertThat(actual).isEqualTo(environment)
    }

    @Test
    fun unfreezeEnvironment() {
        val environment = Environment(1, "env")
        val unfreezingProperties = mutableMapOf(
            EnvironmentPropertiesClass.FREEZING_ENABLE to false.toString(),
            EnvironmentPropertiesClass.FREEZING_USER to null,
            EnvironmentPropertiesClass.FREEZING_END_TIME to null
        )
        val newEnvironment = Environment(1, "env", properties = unfreezingProperties)

        every { environmentRepository.getById(1) } returns environment
        every { environmentRepository.addProperties(1, unfreezingProperties) } returns newEnvironment
        every { delayUnfreezeService.deleteUnfreezingJob(1, 1) } just Runs

        val actual = uut.execute(UnfreezeEnvironmentCommand(1, 1))

        assertThat(actual).isEqualTo(newEnvironment)
    }
}