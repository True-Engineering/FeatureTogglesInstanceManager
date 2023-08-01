package ru.trueengineering.featureflag.manager.ports.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.project.AddNewProjectToOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditProjectRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationUserCase
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectUserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserSettings
import ru.trueengineering.featureflag.manager.ports.service.mapper.ProjectMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ProjectUserMapper

class ProjectServiceTest {
    private val projectMapper: ProjectMapper = mockk()
    private val projectUserMapper: ProjectUserMapper = mockk()
    private val addNewProjectToOrganizationUseCase: AddNewProjectToOrganizationUseCase = mockk()
    private val deleteProjectUseCase: DeleteProjectUseCase = mockk()
    private val searchProjectByIdUseCase: SearchProjectUseCase = mockk()
    private val updateProjectUseCase: UpdateProjectUseCase = mockk()
    private val searchInvitationUserCase: FetchInvitationUserCase = mockk()
    private val setDefaultProjectUseCase: SetDefaultProjectUseCase = mockk()
    private val updateUserEnvironmentRoleUseCase: UpdateUserEnvironmentRoleUseCase = mockk()
    private val fetchMembersForProjectUseCase: FetchMembersForProjectUseCase = mockk()
    private val deleteUserFromProjectUseCase: DeleteUserFromProjectUseCase = mockk()
    private val editProjectRoleUseCase: EditProjectRoleUseCase = mockk()

    private val uut = ProjectService(
        projectMapper,
        projectUserMapper,
        addNewProjectToOrganizationUseCase,
        deleteProjectUseCase,
        searchProjectByIdUseCase,
        updateProjectUseCase,
        searchInvitationUserCase,
        setDefaultProjectUseCase,
        updateUserEnvironmentRoleUseCase,
        fetchMembersForProjectUseCase,
        deleteUserFromProjectUseCase,
        editProjectRoleUseCase
    )

    @Test
    fun getMembersByPattern() {
        val pageable = PageRequest.of(0, 10)
        val user = User("name", "email")
        val user2 = User("name1", "email2")
        val userDto = UserDto("name", "email", userSettings = UserSettings(defaultProjectId = null))
        val userDto2 = UserDto("name2", "email2", userSettings = UserSettings(defaultProjectId = null))
        val projectUser = ProjectUser(1, "project", user, CustomRole.MEMBER, listOf())
        val projectUser2 = ProjectUser(1, "project", user2, CustomRole.MEMBER, listOf())
        val projectUserDto = ProjectUserDto(1, "project", CustomRole.MEMBER, userDto, listOf())
        val projectUserDto2 = ProjectUserDto(1, "project", CustomRole.MEMBER, userDto2, listOf())
        val projectUsers = listOf(projectUser, projectUser2)
        val projectUsersDto = listOf(projectUserDto, projectUserDto2)
        val pattern = "name"
        val pattern2 = "2"

        every { fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1)) } returns projectUsers
        every { projectUserMapper.convertToDtoList(projectUsers) } returns projectUsersDto

        val actual = uut.getMembersByPattern(1, 1, pattern, pageable)
        val actual2 = uut.getMembersByPattern(1, 1, pattern2, pageable)

        assertThat(actual.totalElements).isEqualTo(2)
        assertThat(actual2.totalElements).isEqualTo(1)
    }
}