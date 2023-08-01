package ru.trueengineering.featureflag.manager.core.impl.project

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.auth.BusinessEntityPermissions
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.auth.UserPermissions
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_ENV
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE_FLAG
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.PENDING_APPROVE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ENVIRONMENT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_MEMBERS
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_PROJECT
import ru.trueengineering.featureflag.manager.authorization.impl.BusinessEntityImpl
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUsersByEmailListQuery
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleCommand
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import ru.trueengineering.featureflag.manager.core.impl.validator.ProjectValidatorHandler
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import kotlin.test.assertEquals

private const val PROJECT_NAME = "PROJECT_NAME"
private const val NEW_PROJECT_NAME = "NEW_PROJECT_NAME"

internal class ProjectFacadeTest {

    private val projectRepository: ProjectRepository = mockk()

    private val organizationRepository: OrganizationRepository = mockk()

    private val featureFlagRepository: FeatureFlagRepository = mockk()

    private val permissionService: IPermissionService = mockk()

    private val fetchUserUseCase: FetchUserUseCase = mockk()

    private val fetchCurrentUserUseCase: FetchCurrentUserUseCase = mockk()

    private val projectValidatorHandler: ProjectValidatorHandler = mockk()

    private val uut: ProjectFacade = ProjectFacade(
        projectRepository,
        organizationRepository,
        featureFlagRepository,
        permissionService,
        fetchUserUseCase,
        fetchCurrentUserUseCase,
        projectValidatorHandler
    )

    @Test
    fun delete() {
        val command = DeleteProjectCommand(1L)
        every { projectRepository.deleteProject(1L) } just Runs

        uut.execute(command)
        verify { projectRepository.deleteProject(1L) }
    }

    @Test
    fun update() {
        val command = UpdateProjectCommand(NEW_PROJECT_NAME, 1L, 1L)
        val project = Project(name = PROJECT_NAME)
        every { projectRepository.getById(1L) } returns project
        every { projectRepository.updateName(1L, NEW_PROJECT_NAME) } just Runs
        every { projectRepository.setProperties(1L, mutableMapOf()) } just Runs
        every { projectValidatorHandler.validateOrThrow(project) } returns project

        val actual = uut.execute(command)
        assertEquals(project, actual)
        verify { projectValidatorHandler.validateOrThrow(project) }
    }

    @Test
    fun updateNotValid() {
        val command = UpdateProjectCommand(NEW_PROJECT_NAME, 1L, 1L)
        val project = Project(name = PROJECT_NAME)

        every { projectRepository.getById(1L) } returns project
        every { projectRepository.updateName(1L, NEW_PROJECT_NAME) } just Runs
        every { projectRepository.setProperties(1L, mutableMapOf()) } just Runs
        every { projectValidatorHandler.validateOrThrow(project) } throws ServiceException(ErrorCode.UNABLE_TO_SAVE_PROJECT)

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        verify { projectValidatorHandler.validateOrThrow(project) }
        assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_SAVE_PROJECT)
    }

    @Test
    fun search() {
        val project = Project(name = PROJECT_NAME, environments = mutableListOf(), id = 1L)

        val emails = setOf("email1", "email2", "email3", "email4")
        every {
            permissionService.getUsersByEntity(
                project,
                listOf(PENDING_APPROVE, READ_PROJECT, EDIT)
            )
        } returns emails
        val user1 = User("name1", "email1")
        val user2 = User("name2", "email2")
        val user3 = User("name3", "email3")
        val user4 = User("name4", "email4")
        val userList = listOf(user1, user2, user3, user4)
        every { fetchUserUseCase.execute(FetchUsersByEmailListQuery(emails.toList())) } returns userList
        every { permissionService.getPermissionsForUsers(any(), any()) } returns emptyList()
        every { permissionService.getUserRoleForEntity(eq(project), any()) } returns CustomRole.MEMBER

        every { projectRepository.getById(1L) } returns project
        every { featureFlagRepository.getFeatureFlagsCountForProject(1L) } returns 10L

        val actual = uut.search(SearchProjectByIdQuery(1L))
        assertEquals(project, actual)
        assertEquals(10L, project.featureFlagsCount)
    }

    @Test
    fun activateUser() {
        val user = User(userName = "name", email = "email")
        val project = Project(11L, "project")
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        every { fetchUserUseCase.searchById(1L) } returns user
        every { projectRepository.getById(11L) } returns project
        every { permissionService.getUserRoleForEntity(project, user) } returns CustomRole.NO_ACCESS
        every { permissionService.isGrantedPermission(project, PENDING_APPROVE, user) } returns true
        every { permissionService.clearPermissionsForUser(project, user) } just Runs
        every { permissionService.grantPermissionsForUser(project, listOf(READ_PROJECT, CREATE_FLAG), user) } just Runs

        uut.execute(ActivateUserCommand(1L, 11))
    }

    @Test
    fun activateUserNotFound() {
        every { fetchUserUseCase.searchById(1L) } returns null

        val actualException = assertThrows<ServiceException> { uut.execute(ActivateUserCommand(1L, 11)) }
        assertEquals(actualException.errorCode, ErrorCode.USER_NOT_FOUND)
        assertEquals(actualException.errorMessage, "User with id = 1 not found!")
    }

    @Test
    internal fun createEnvironment() {
        val projectId = 123L
        val environmentId = 234L
        val environmentName = "test env"

        val newEnv = Environment(environmentId, environmentName)
        val newProject = Project(
            projectId,
            "test project",
            environments = mutableListOf(newEnv)
        )

        every { projectRepository.addNewEnvironment(eq(projectId), any()) } just runs
        every { projectRepository.getById(eq(projectId)) } returns newProject

        val actual = uut.execute(CreateEnvironmentCommand(environmentName, projectId))

        assertThat(actual.id).isEqualTo(environmentId)
        assertThat(actual.name).isEqualTo(environmentName)
    }

    @Test
    fun grantUserEditEnvironmentRole() {
        val environmentId = 234L
        val environmentName = "test env"
        val user1 = User("name1", "email1", id = 1L)
        every { fetchUserUseCase.searchById(1L) } returns user1
        val env = Environment(environmentId, environmentName)
        val project = Project(1L, "project", environments = mutableListOf(env))
        every { permissionService.getPermissionsForUser(env, user1) } returns listOf(READ_ENVIRONMENT)
        every {
            permissionService.clearPermissionsForUser(env, user1)
        } just Runs
        every { permissionService.grantPermissionsForUser(env, listOf(EDIT), user1) } just Runs
        //собираем юзера после редактирования прав
        every { permissionService.getPermissionsForUser(listOf(env, project), user1) } returns
                UserPermissions(
                    "name1", listOf(
                        BusinessEntityPermissions(env, listOf(READ_ENVIRONMENT, EDIT)),
                        BusinessEntityPermissions(project, listOf(READ_PROJECT, CREATE_FLAG))
                    )
                )
        every { permissionService.getUserRoleForEntity(project, user1) } returns CustomRole.MEMBER
        every { projectRepository.getById(1L) } returns project

        val actualProjectUser = uut.execute(UpdateUserEnvironmentRoleCommand(1L, 1, 1L, 234L, UserRole.EDITOR))
        assertThat(actualProjectUser.projectName).isEqualTo("project")
        assertThat(actualProjectUser.projectId).isEqualTo(1L)
        assertThat(actualProjectUser.projectRole).isEqualTo(CustomRole.MEMBER)
        assertThat(actualProjectUser.user).isSameAs(user1)
        assertThat(actualProjectUser.environmentPermissions).hasSize(1)
        assertThat(actualProjectUser.environmentPermissions[0]).isEqualTo(
            EnvironmentRole(234L, "test env", UserRole.EDITOR)
        )
    }

    @Test
    fun deleteUserEnvironmentRole() {
        val environmentId = 234L
        val environmentName = "test env"
        val user1 = User("name1", "email1", id = 1L)
        every { fetchUserUseCase.searchById(1L) } returns user1
        val env = Environment(environmentId, environmentName)
        val project = Project(1L, "project", environments = mutableListOf(env))
        every { permissionService.getPermissionsForUser(env, user1) } returns listOf(READ_ENVIRONMENT)
        every {
            permissionService.clearPermissionsForUser(env, user1)
        } just Runs

        //собираем юзера после редактирования прав
        every { permissionService.getPermissionsForUser(listOf(env, project), user1) } returns
                UserPermissions(
                    "name1", listOf(
                        BusinessEntityPermissions(env, listOf()),
                        BusinessEntityPermissions(project, listOf(READ_PROJECT, CREATE_FLAG))
                    )
                )
        every { permissionService.getUserRoleForEntity(project, user1) } returns CustomRole.MEMBER
        every { projectRepository.getById(1L) } returns project

        val actualProjectUser = uut.execute(UpdateUserEnvironmentRoleCommand(1L, 1, 1L, 234L, null))
        assertThat(actualProjectUser.projectName).isEqualTo("project")
        assertThat(actualProjectUser.projectId).isEqualTo(1L)
        assertThat(actualProjectUser.projectRole).isEqualTo(CustomRole.MEMBER)
        assertThat(actualProjectUser.user).isSameAs(user1)
        assertThat(actualProjectUser.environmentPermissions).hasSize(1)
        assertThat(actualProjectUser.environmentPermissions[0]).isEqualTo(
            EnvironmentRole(234L, "test env", UserRole.NO_ACCESS)
        )

        verify(exactly = 0) { permissionService.grantPermissionsForUser(any(), any(), any()) }
    }

    @Test
    fun fetchMembersForProject() {
        val environmentId = 234L
        val environmentName = "test env"

        val env = Environment(environmentId, environmentName)
        val env2 = Environment(235, "test env 2")
        val project = Project(11L, "project", environments = mutableListOf(env, env2))

        every { projectRepository.getById(11L) } returns project
        val emails = setOf("email1", "email2", "email3", "email4")
        every {
            permissionService.getUsersByEntity(
                project,
                listOf(PENDING_APPROVE, READ_PROJECT, EDIT)
            )
        } returns emails
        every { permissionService.getUserRoleForEntity(eq(project), any()) }
            .returnsMany(
                CustomRole.MEMBER, CustomRole.MEMBER, CustomRole.NO_ACCESS, CustomRole.NO_ACCESS, CustomRole.ADMIN
            )
        val user1 = User("name1", "email1")
        val user2 = User("name2", "email2")
        val user3 = User("name3", "email3")
        val user4 = User("name4", "email4")
        val user5 = User("name5", "email5")
        val userList = listOf(user1, user2, user3, user4, user5)
        every { fetchUserUseCase.execute(FetchUsersByEmailListQuery(emails.toList())) } returns userList
        val envBusinessEntity1 = BusinessEntityImpl(env)
        val envBusinessEntity2 = BusinessEntityImpl(env2)
        val projectBusinessEntity = BusinessEntityImpl(project)
        val userPermissions1 = UserPermissions(
            "email1",
            listOf(
                BusinessEntityPermissions(envBusinessEntity1, listOf(READ_ENVIRONMENT)),
                BusinessEntityPermissions(envBusinessEntity2, listOf(READ_ENVIRONMENT)),
                BusinessEntityPermissions(projectBusinessEntity, listOf(READ_PROJECT, CREATE_FLAG))
            )
        )
        val userPermissions2 = UserPermissions(
            "email2",
            listOf(
                BusinessEntityPermissions(envBusinessEntity1, listOf(READ_ENVIRONMENT, EDIT, DELETE)),
                BusinessEntityPermissions(projectBusinessEntity, listOf(READ_PROJECT, CREATE_FLAG))
            )
        )
        val userPermissions3 = UserPermissions(
            "email3", listOf(
                BusinessEntityPermissions(projectBusinessEntity, listOf(PENDING_APPROVE))
            )
        )
        val userPermissions4 =
            UserPermissions("email4", listOf(BusinessEntityPermissions(envBusinessEntity1, emptyList())))
        val userPermissions5 = UserPermissions(
            "email5",
            listOf(
                BusinessEntityPermissions(envBusinessEntity1, listOf(READ_ENVIRONMENT, EDIT)),
                BusinessEntityPermissions(envBusinessEntity2, listOf(READ_ENVIRONMENT, EDIT)),
                BusinessEntityPermissions(
                    projectBusinessEntity, listOf(
                        READ_PROJECT,
                        EDIT,
                        READ_MEMBERS,
                        EDIT_MEMBERS,
                        DELETE_FLAG,
                        CREATE_FLAG,
                        CREATE_ENV,
                        READ_ENVIRONMENT
                    )
                )
            )
        )

        every { permissionService.getPermissionsForUsers(listOf(project, env, env2), userList) } returns
                listOf(userPermissions1, userPermissions2, userPermissions3, userPermissions4, userPermissions5)


        val projectUsers = uut.search(FetchMembersForProjectQuery(11L))
        assertThat(projectUsers).hasSize(5)

        assertThat(projectUsers).extracting<Long> { it.projectId }.containsExactly(11L, 11L, 11L, 11L, 11L)
        assertThat(projectUsers).extracting<String> { it.projectName }
            .containsExactly("project", "project", "project", "project", "project")

        val projectUser1 = projectUsers[0]
        assertThat(projectUser1.environmentPermissions).hasSize(2)
        assertThat(projectUser1.environmentPermissions[0])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env", UserRole.VIEWER)
        assertThat(projectUser1.environmentPermissions[1])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env 2", UserRole.VIEWER)
        assertThat(projectUser1.user.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(projectUser1.projectRole).isEqualTo(CustomRole.MEMBER)

        val projectUser2 = projectUsers[1]
        assertThat(projectUser2.environmentPermissions).hasSize(2)
        assertThat(projectUser2.environmentPermissions[0])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env", UserRole.EDITOR)
        assertThat(projectUser2.environmentPermissions[1])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env 2", UserRole.NO_ACCESS)
        assertThat(projectUser2.user.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(projectUser2.projectRole).isEqualTo(CustomRole.MEMBER)

        val projectUser3 = projectUsers[2]
        assertThat(projectUser3.environmentPermissions).hasSize(2)
        assertThat(projectUser3.environmentPermissions[0])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env", UserRole.NO_ACCESS)
        assertThat(projectUser3.environmentPermissions[1])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env 2", UserRole.NO_ACCESS)
        assertThat(projectUser3.user.status).isEqualTo(UserStatus.PENDING)
        assertThat(projectUser3.projectRole).isEqualTo(CustomRole.NO_ACCESS)

        val projectUser4 = projectUsers[3]
        assertThat(projectUser4.environmentPermissions).hasSize(2)
        assertThat(projectUser4.environmentPermissions[0])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env", UserRole.NO_ACCESS)
        assertThat(projectUser4.environmentPermissions[1])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env 2", UserRole.NO_ACCESS)
        assertThat(projectUser4.projectRole).isEqualTo(CustomRole.NO_ACCESS)

        val projectUser5 = projectUsers[4]
        assertThat(projectUser5.environmentPermissions).hasSize(2)
        assertThat(projectUser5.environmentPermissions[0])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env", UserRole.EDITOR)
        assertThat(projectUser5.environmentPermissions[1])
            .extracting(EnvironmentRole::environment, EnvironmentRole::environmentRole)
            .containsExactly("test env 2", UserRole.EDITOR)
        assertThat(projectUser5.user.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(projectUser5.projectRole).isEqualTo(CustomRole.ADMIN)
    }

    @Test
    internal fun deleteUser() {
        val environmentId = 234L
        val environmentName = "test env"

        val environment = Environment(environmentId, environmentName)
        val user = User(userName = "name", email = "email", id = 1L)
        val project = Project(11L, "project", environments = mutableListOf(environment))

        every { fetchUserUseCase.searchById(1L) } returns user
        every { projectRepository.getById(11L) } returns project
        val userPermissions = UserPermissions(
            "email", listOf(
                BusinessEntityPermissions(
                    project,
                    listOf(READ_MEMBERS, EDIT_MEMBERS)
                ),
                BusinessEntityPermissions(environment, listOf(EDIT))
            )
        )
        every { permissionService.getPermissionsForUser(listOf(project, environment), user) } returns userPermissions
        every { permissionService.clearPermissionsForUser(any(), any()) } just Runs

        uut.execute(DeleteUserFromProjectCommand(11L, 1L))

        val slotProject = mutableListOf<BusinessEntity>()
        verify {
            permissionService.clearPermissionsForUser(capture(slotProject), user)
        }
        verify { permissionService.clearPermissionsForUser(capture(slotProject), user) }

        assertThat(slotProject[0].getBusinessId()).isEqualTo(project.getBusinessId())
        assertThat(slotProject[0].type).isEqualTo(project.type)
        assertThat(slotProject[1].getBusinessId()).isEqualTo(environment.getBusinessId())
        assertThat(slotProject[1].type).isEqualTo(environment.type)
    }

    @Test
    internal fun deleteUserNotFound() {
        val environmentId = 234L
        val environmentName = "test env"

        val environment = Environment(environmentId, environmentName)
        val project = Project(11L, "project", environments = mutableListOf(environment))

        every { projectRepository.getById(11L) } returns project
        every { fetchUserUseCase.searchById(1L) } returns null

        val actualException = assertThrows<ServiceException> { uut.execute(DeleteUserFromProjectCommand(11L, 1L)) }
        assertEquals(actualException.errorCode, ErrorCode.USER_NOT_FOUND)
    }

}