package ru.trueengineering.featureflag.manager.ports.service

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.project.AddNewProjectToOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.CreateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectProperties
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditProjectRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditUserProjectRoleCommand
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationQuery
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationUserCase
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleCommand
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectUserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectUsersDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserFilterResponseDto
import ru.trueengineering.featureflag.manager.ports.service.mapper.ProjectMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ProjectUserMapper

@Service
class ProjectService(
    private val projectMapper: ProjectMapper,
    private val projectUserMapper: ProjectUserMapper,
    private val addNewProjectToOrganizationUseCase: AddNewProjectToOrganizationUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val searchProjectByIdUseCase: SearchProjectUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val searchInvitationUserCase: FetchInvitationUserCase,
    private val setDefaultProjectUseCase: SetDefaultProjectUseCase,
    private val updateUserEnvironmentRoleUseCase: UpdateUserEnvironmentRoleUseCase,
    private val fetchMembersForProjectUseCase: FetchMembersForProjectUseCase,
    private val deleteUserFromProjectUseCase: DeleteUserFromProjectUseCase,
    private val editProjectRoleUseCase: EditProjectRoleUseCase
) {

    fun create(
        projectName: String,
        properties: ProjectProperties,
        organizationId: Long
    ) =
        projectMapper.convertToDto(
            addNewProjectToOrganizationUseCase.execute(CreateProjectCommand(projectName, organizationId, properties))
        )

    fun update(
        projectName: String,
        properties: ProjectProperties,
        projectId: Long, organizationId: Long
    ) =
        projectMapper.convertToDto(
            updateProjectUseCase.execute(UpdateProjectCommand(projectName, projectId, organizationId, properties))
        )

    fun delete(projectId: Long) =
        deleteProjectUseCase.execute(DeleteProjectCommand(projectId))

    fun searchById(projectId: Long) =
        projectMapper.convertToDto(searchProjectByIdUseCase.search(SearchProjectByIdQuery(projectId)))

    fun inviteToProject(projectId: Long): String {
        return searchInvitationUserCase.search(FetchInvitationQuery(projectId)).id.toString()
    }

    fun setProjectDefault(projectId: Long, isDefault: Boolean): Any {
        return setDefaultProjectUseCase.execute(
            SetDefaultProjectCommand(
                projectId = projectId,
                isDefault = isDefault
            )
        )
    }

    fun editEnvironmentRoleForMember(
        organizationId: Long,
        projectId: Long,
        userId: Long,
        environmentId: Long,
        role: UserRole?
    ): ProjectUserDto {
        return projectUserMapper.convertToDto(
            updateUserEnvironmentRoleUseCase.execute(
                UpdateUserEnvironmentRoleCommand(organizationId, projectId, userId, environmentId, role)
            )
        )
    }

    fun editProjectRoleForMember(
        organizationId: Long,
        projectId: Long,
        userId: Long,
        role: CustomRole
    ): ProjectUserDto {
        return projectUserMapper.convertToDto(
            editProjectRoleUseCase.execute(
                EditUserProjectRoleCommand(organizationId, projectId, userId, role)
            )
        )
    }


    fun getMembers(organizationId: Long, projectId: Long): ProjectUsersDto {
        return ProjectUsersDto(
            projectUserMapper.convertToDtoList(
                fetchMembersForProjectUseCase.search(
                    FetchMembersForProjectQuery(projectId)
                )
            )
        )
    }

    fun getMembersByPattern(organizationId: Long, projectId: Long, pattern: String, pageable: Pageable): UserFilterResponseDto {
        val projectUserList = projectUserMapper.convertToDtoList(fetchMembersForProjectUseCase.search(
            FetchMembersForProjectQuery(projectId)
        ))
        val usersByPattern = projectUserList.map { it.user }.filter { pattern in it.userName || pattern in it.email}

        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(usersByPattern.size)

        val page = if (start < usersByPattern.size) {
            PageImpl(usersByPattern.subList(start, end), pageable, usersByPattern.size.toLong())
        } else {
            PageImpl(emptyList<UserDto>(), pageable, usersByPattern.size.toLong())
        }

        return UserFilterResponseDto(
            resultList = page.content,
            page = page.number,
            pageSize = page.size,
            totalPages = page.totalPages,
            totalElements = page.totalElements
        )
    }

    fun deleteUser(organizationId: Long, projectId: Long, userId: Long): Any {
        return deleteUserFromProjectUseCase.execute(DeleteUserFromProjectCommand(projectId, userId))
    }
}