package ru.trueengineering.featureflag.manager.core.impl.organization

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.CreateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUsersByEmailListQuery
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.validator.ProjectValidatorHandler

private const val ORGANIZATION_NAME = "True"
private const val ORGANIZATION_NAME_TWO = "False"
private const val PROJECT_NAME = "PROJECT_NAME"

internal class OrganizationFacadeTest {

    private val organizationRepository: OrganizationRepository = mockk()
    private val fetchCurrentUserUseCase: FetchCurrentUserUseCase = mockk()
    private val featureFlagRepository: FeatureFlagRepository = mockk()
    private val fetchMembersForProjectUseCase: FetchMembersForProjectUseCase = mockk()
    private val permissionService: IPermissionService = mockk()
    private val fetchUserUseCase: FetchUserUseCase = mockk()
    private val projectValidatorHandler: ProjectValidatorHandler = mockk()

    private val uut = OrganizationFacade(
        organizationRepository, featureFlagRepository, fetchMembersForProjectUseCase,
        permissionService, fetchUserUseCase, projectValidatorHandler
    )

    @BeforeEach
    fun init() {
    }

    @Test
    fun create() {
        every { organizationRepository.findByName(ORGANIZATION_NAME) } returns null
        val organization = Organization(1, ORGANIZATION_NAME)
        every { organizationRepository.create(ORGANIZATION_NAME) } returns organization
        val actual = uut.execute(CreateOrganizationCommand(ORGANIZATION_NAME))
        assertEquals(organization, actual)
    }

    @Test
    fun addNewProject() {
        val command = CreateProjectCommand(PROJECT_NAME, 1L)
        val project = Project(name = PROJECT_NAME, environments = mutableListOf())
        val savedProject = Project(1L, PROJECT_NAME, mutableListOf())

        every { projectValidatorHandler.validateOrThrow(project) } returns project
        every { organizationRepository.addNewProject(eq(1L), eq(project)) } returns savedProject
        every { featureFlagRepository.getFeatureFlagsCountForProject(1L) } returns 0
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L)) } returns listOf()
        every {
            permissionService.isGrantedPermissionForCurrentUser(
                project,
                CustomPermission.READ_MEMBERS
            )
        } returns true
        every {
            permissionService.isGrantedPermissionForCurrentUser(
                savedProject,
                CustomPermission.READ_MEMBERS
            )
        } returns true

        val actual = uut.execute(command)
        verify { projectValidatorHandler.validateOrThrow(project) }
        kotlin.test.assertEquals(savedProject, actual)
    }

    @Test
    fun addNewProjectNotValid() {
        val command = CreateProjectCommand(PROJECT_NAME, 1L)
        val project = Project(name = PROJECT_NAME, environments = mutableListOf())

        every { projectValidatorHandler.validateOrThrow(project) } throws ServiceException(ErrorCode.UNABLE_TO_SAVE_PROJECT)

        val actualException = assertThrows<ServiceException> { uut.execute(command) }
        verify { projectValidatorHandler.validateOrThrow(project) }
        kotlin.test.assertEquals(actualException.errorCode, ErrorCode.UNABLE_TO_SAVE_PROJECT)
    }

    @Test
    fun delete() {
        every { organizationRepository.findById(1) } returns Organization(1, ORGANIZATION_NAME)
        every { organizationRepository.removeById(1) } just Runs
        uut.execute(DeleteOrganizationCommand(1L))
        verify { organizationRepository.removeById(1) }
    }

    @Test
    fun searchOne() {
        val project1 = Project(id = 1L, name = "Project1")
        val project2 = Project(id = 2L, name = "Project2")
        val organization = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        every { fetchCurrentUserUseCase.search() } returns
                User(userName = "name", email = "email", defaultProjectId = 1)
        every { organizationRepository.findById(1L) } returns organization
        every { featureFlagRepository.getFeatureFlagsCountForProject(1L) } returns 10
        every { featureFlagRepository.getFeatureFlagsCountForProject(2L) } returns 20
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L)) } returns listOf(mockk(), mockk())
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(2L)) } returns listOf(
            mockk(),
            mockk(),
            mockk()
        )
        every {
            permissionService.isGrantedPermissionForCurrentUser(
                project1,
                CustomPermission.READ_MEMBERS
            )
        } returns true
        every {
            permissionService.isGrantedPermissionForCurrentUser(
                project2,
                CustomPermission.READ_MEMBERS
            )
        } returns false
        val actual = uut.search(SearchOrganizationByIdQuery(1L))
        assertEquals(organization, actual)
        assertEquals(10, organization.projects[0].featureFlagsCount)
        assertEquals(2, organization.projects[0].membersCount)
        assertEquals(20, organization.projects[1].featureFlagsCount)
        assertEquals(null, organization.projects[1].membersCount)
    }

    @Test
    fun searchAll() {
        val project1 = Project(id = 1L, name = "Project1")
        val project2 = Project(id = 2L, name = "Project2")
        val organization1 = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        val organization2 = Organization(2, ORGANIZATION_NAME_TWO, projects = listOf())
        every { featureFlagRepository.getFeatureFlagsCountForProject(1L) } returns 10
        every { featureFlagRepository.getFeatureFlagsCountForProject(2L) } returns 20
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L)) } returns listOf(mockk(), mockk())
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(2L)) } returns listOf(mockk(), mockk(), mockk())
        every { permissionService.isGrantedPermissionForCurrentUser(project1, CustomPermission.READ_MEMBERS) } returns true
        every { permissionService.isGrantedPermissionForCurrentUser(project2, CustomPermission.READ_MEMBERS) } returns false
        every { fetchCurrentUserUseCase.search() } returns
                User(userName = "name", email ="email", defaultProjectId = 1)
        every { organizationRepository.findAll() } returns listOf(organization1, organization2)
        val actual = uut.search()
        assertEquals(organization1, actual[0])
        assertEquals(organization2, actual[1])
    }

    @Test
    fun searchMembers() {
        val project1 = Project(id = 1L, name = "Project1")
        val project2 = Project(id = 2L, name = "Project2")
        val organization = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        every { organizationRepository.findById(1L) } returns organization
        val emails = setOf("email1", "email2", "email3", "email4")
        every {
            permissionService.getUsersByEntity(
                organization,
                listOf(CustomPermission.READ_ORGANIZATION, CustomPermission.EDIT)
            )
        } returns emails
        val user1 = User("name1", "email1")
        val user2 = User("name2", "email2")
        val user3 = User("name3", "email3")
        val user4 = User("name4", "email4")
        val userList = listOf(user1, user2, user3, user4)
        every { fetchUserUseCase.execute(FetchUsersByEmailListQuery(emails.toList())) } returns userList
        val projectOneUsers = listOf(
            ProjectUser(
                projectId = 1L,
                projectName = "Project1",
                user1,
                CustomRole.ADMIN,
                listOf(EnvironmentRole(1L, "env", UserRole.EDITOR))
            ),
            ProjectUser(projectId = 1L, projectName = "Project1", user2, CustomRole.MEMBER, emptyList())
        )
        val projectTwoUsers = listOf(
            ProjectUser(projectId = 2L, projectName = "Project2", user2, CustomRole.MEMBER, emptyList()),
            ProjectUser(projectId = 2L, projectName = "Project2", user3, CustomRole.ADMIN, emptyList())
        )
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L)) } returns projectOneUsers
        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(2L)) } returns projectTwoUsers

        val actual = uut.search(FetchMembersForOrganizationQuery(1L))

        assertThat(actual).isNotNull.isNotEmpty.hasSize(4)
        assertThat(actual).extracting<User> { it.user }.containsExactly(user1, user2, user3, user4)
        assertThat(actual[0].projects).hasSize(1).extracting<CustomRole> { it.projectRole }
            .containsExactly(CustomRole.ADMIN)
        assertThat(actual[0].projects).hasSize(1).extracting<Long> { it.projectId }.containsExactly(1L)
        assertThat(actual[0].projects[0].environmentPermissions).hasSize(1).extracting<String> { it.environment }
            .containsExactly("env")
        assertThat(actual[0].projects[0].environmentPermissions).hasSize(1).extracting<UserRole> { it.environmentRole }
            .containsExactly(UserRole.EDITOR)
        assertThat(actual[1].projects).hasSize(2).extracting<CustomRole> { it.projectRole }
            .containsExactly(CustomRole.MEMBER, CustomRole.MEMBER)
        assertThat(actual[1].projects).hasSize(2).extracting<Long> { it.projectId }.containsExactly(1L, 2L)
        assertThat(actual[2].projects).hasSize(1).extracting<CustomRole> { it.projectRole }
            .containsExactly(CustomRole.ADMIN)
        assertThat(actual[2].projects).hasSize(1).extracting<Long> { it.projectId }.containsExactly(2L)
        assertThat(actual[3].projects).isEmpty()

    }

    @Test
    fun searchMemberCount() {
        val emails = setOf("email1", "email2", "email3", "email4")
        every { permissionService.getUsersByEntity(any(), any()) } returns emails
        every { fetchUserUseCase.searchUserCount(FetchUsersByEmailListQuery(emails.toList())) } returns 3
        every { permissionService.isGrantedPermissionForCurrentUser(any(), CustomPermission.READ_MEMBERS) } returns true

        val project1 = Project(id = 1L, name = "Project1")
        val project2 = Project(id = 2L, name = "Project2")
        val organization = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        every { organizationRepository.findById(1L) } returns organization

        assertThat(uut.searchMembersCount(FetchMembersForOrganizationQuery(1))).isEqualTo(3)
    }

    @Test
    fun searchMemberThrow() {
        val emails = setOf("email1", "email2", "email3", "email4")
        every { permissionService.getUsersByEntity(any(), any()) } returns emails
        every { fetchUserUseCase.searchUserCount(FetchUsersByEmailListQuery(emails.toList())) } throws
            java.lang.RuntimeException("Some error")
        every { permissionService.isGrantedPermissionForCurrentUser(any(), CustomPermission.READ_MEMBERS) } returns true

        val project1 = Project(id = 1L, name = "Project1")
        val project2 = Project(id = 2L, name = "Project2")
        val organization = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        every { organizationRepository.findById(1L) } returns organization

        assertThat(uut.searchMembersCount(FetchMembersForOrganizationQuery(1))).isNull()
    }

    @Test
    fun deleteUser() {
        val environment = Environment(id = 55, name = "env")
        val project1 = Project(id = 1L, name = "Project1", environments = mutableListOf(environment))
        val project2 = Project(id = 2L, name = "Project2")
        val organization = Organization(1, ORGANIZATION_NAME, projects = listOf(project1, project2))
        val user = User(id = 33, userName = "name", email = "email", defaultProjectId = 1)
        every { fetchUserUseCase.searchById(33) } returns
                user

        every { organizationRepository.findById(1L) } returns organization
        every { permissionService.clearPermissionsForUser(any(), user = user) } just Runs

        uut.execute(DeleteOrganizationUserCommand(33, 1))
        verify { permissionService.clearPermissionsForUser(environment, user = user) }
        verify { permissionService.clearPermissionsForUser(project1, user = user) }
        verify { permissionService.clearPermissionsForUser(project2, user = user) }
        verify { permissionService.clearPermissionsForUser(organization, user = user) }
    }
}