package ru.trueengineering.featureflag.manager.core.impl.environment

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.event.EnvironmentCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

internal class EnvironmentEventHandlerTest {

    private val repository: BusinessEntityRepository = mockk()
    private val permissionService: IPermissionService = mockk()
    private val environmentRepository: EnvironmentRepository = mockk()
    private val uut = EnvironmentEventHandler(repository, environmentRepository, permissionService)

        private val allPermission = listOf(
            CustomPermission.READ_ENVIRONMENT,
            CustomPermission.EDIT,
            CustomPermission.DELETE
        )

    @Test
    fun handle() {
        val environment = Environment(id = 1, name = "env")
        val project = Project(id = 1, name = "project", environments = mutableListOf(environment))
        val environmentCreatedEvent = EnvironmentCreatedEvent("env", 1, project)
        every { environmentRepository.getByProjectIdAndName(1, "env") } returns environment
        every { repository.createBusinessEntity(environment, project) } just Runs
        every { permissionService.grantPermissionsForCurrentUser(environment, allPermission) } just Runs
        every { permissionService.grantPermissionsForOwner(environment, allPermission) } just Runs
        uut.handle(environmentCreatedEvent)
    }

    @Test
    fun handleThrow() {
        val project = Project(id = 1, name = "project")
        val environmentCreatedEvent = EnvironmentCreatedEvent("env", 1, project)
        every { environmentRepository.getByProjectIdAndName(1, "env") } returns null

        val actualException =
            org.junit.jupiter.api.assertThrows<ServiceException> { uut.handle(environmentCreatedEvent) }
        kotlin.test.assertEquals(actualException.errorCode, ErrorCode.ENVIRONMENT_NOT_FOUND)
        kotlin.test.assertEquals(actualException.errorMessage, "Unable to create acl, " +
                "environment with name env is not found in project with id 1!"
        )
    }
}