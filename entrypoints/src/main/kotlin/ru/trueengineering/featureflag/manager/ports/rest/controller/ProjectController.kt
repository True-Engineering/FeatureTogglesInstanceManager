package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectProperties
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.ports.rest.OrganizationId
import ru.trueengineering.featureflag.manager.ports.rest.ProjectId
import ru.trueengineering.featureflag.manager.ports.service.ProjectService
import java.util.EnumMap

@RestController
@RequestMapping("/api/project/{organizationId}")
class ProjectController(val projectService: ProjectService) {

    @Operation(summary = "Create new project for specified organization")
    @PostMapping
    fun registryNewProject(
            @RequestBody request: CreateOrUpdateProjectRequest,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long
    ) = ResponseEntity.ok(projectService.create(request.name, request.properties, organizationId))

    @Operation(summary = "Update project for specified organization")
    @PutMapping("/{projectId}")
    fun updateProject(
            @RequestBody request: CreateOrUpdateProjectRequest,
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long
    ) = ResponseEntity.ok(projectService.update(request.name, request.properties, projectId, organizationId))

    @Operation(summary = "Mark project as default for current user")
    @PatchMapping("/{projectId}/default")
    fun setProjectDefault(
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
            @RequestBody setDefaultProjectRequest: SetDefaultProjectRequest
    ) = ResponseEntity.ok(projectService.setProjectDefault(projectId, setDefaultProjectRequest.defaultProject))

    @Operation(summary = "Delete project for specified organization")
    @DeleteMapping("/{projectId}")
    fun deleteProject(
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long
    ): ResponseEntity<Any> {
        projectService.delete(projectId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Get information about specified project")
    @GetMapping("/{projectId}")
    fun getProject(
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long
    ) = ResponseEntity.ok(projectService.searchById(projectId))

    @Operation(summary = "Generate invite link for project")
    @GetMapping("/{projectId}/invite")
    fun invite(
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long
    ) = ResponseEntity.ok(projectService.inviteToProject(projectId))

    @Operation(summary = "Get all members for specified project")
    @GetMapping("/{projectId}/members")
    fun getMembers(
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long
    ): ProjectUsersDto {
        return projectService.getMembers(organizationId, projectId)
    }

    @Operation(summary = "Delete member from the specified project")
    @DeleteMapping("/{projectId}/members/{userId}")
    fun deleteMember(
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "User id", example = "1")
            @PathVariable userId: Long
    ): ResponseEntity<Any> {
        projectService.deleteUser(organizationId, projectId, userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Edit member role for environment in project")
    @PostMapping("/{projectId}/members/{userId}")
    fun editEnvironmentRole(
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "User id", example = "1")
            @PathVariable userId: Long,
            @RequestBody userEnvironmentRole: EditEnvironmentRoleRequest
    ): ResponseEntity<ProjectUserDto> {
        return ResponseEntity.ok(projectService.editEnvironmentRoleForMember(
                organizationId,
                projectId,
                userId,
                userEnvironmentRole.environmentId,
                userEnvironmentRole.environmentRole
        ))
    }

    @Operation(summary = "Edit member role for  project")
    @PostMapping("/{projectId}/members/{userId}/role")
    fun editMemberRole(
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
            @Parameter(description = "Project id", example = "1")
            @ProjectId @PathVariable projectId: Long,
            @Parameter(description = "User id", example = "1")
            @PathVariable userId: Long,
            @RequestBody request: EditProjectRoleRequest
    ): ResponseEntity<ProjectUserDto> {
        return ResponseEntity.ok(projectService.editProjectRoleForMember(
                organizationId,
                projectId,
                userId,
                request.role
        ))
    }

}

@Schema(description = "Model for create project request")
data class CreateOrUpdateProjectRequest(
    @field:Schema(description = "Project name", example = "Cool project")
    val name: String,
    @field:Schema(description = "Project custom properties")
    var properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)

)

@Schema(description = "Model for setting default project request")
data class SetDefaultProjectRequest(
    @field:Schema(description = "Is default", example = "true")
    val defaultProject: Boolean
)

@Schema(description = "Model for project information")
data class ProjectDto(
    @field:Schema(description = "Project id", example = "1")
    val id: Long,
    @field:Schema(description = "Project name", example = "Cool project")
    val name: String,
    @field:Schema(description = "List of environments")
    val environments: List<EnvironmentInfoDto> = ArrayList(),
    @field:Schema(description = "List of permissions of current user for current project")
    var permissions: MutableSet<String>? = HashSet(),
    @field:Schema(description = "Feature flags count", example = "20")
    val featureFlagsCount: Long? = null,
    @field:Schema(description = "Members count", example = "15")
    val membersCount: Long? = null,
    @field:Schema(description = "Project status", example = "ACTIVE")
    var status: EnvironmentConnectionStatus? = null,
    @field:Schema(description = "Project custom properties")
    var properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)
)

@Schema(description = "Model for users from project")
data class ProjectUsersDto(
    val users: List<ProjectUserDto>
)

@Schema(description = "Model for users in project with roles for environment")
data class ProjectUserDto(
    @field:Schema(description = "Project id", example = "1")
    val projectId: Long,
    @field:Schema(description = "Project name", example = "Cool project")
    val projectName: String,
    @field:Schema(
        description = "User role in project",
        example = "ADMIN",
        nullable = true,
        allowableValues = ["ADMIN", "MEMBER", "NO_ACCESS"]
    )
    val projectRole: CustomRole,
    @field:Schema(description = "User")
    val user: UserDto,
    @field:Schema(description = "Environment roles")
    val environmentPermissions: List<EnvironmentRoleDto>,
)

data class EnvironmentRoleDto(
    @field:Schema(description = "Environment id", example = "1")
    val environmentId: Long,
    @field:Schema(description = "Environment name", example = "DEV", nullable = false)
    val environment: String,
    @field:Schema(description = "Role name", example = "EDITOR", nullable = true)
    val environmentRole: UserRole?
)

data class EditEnvironmentRoleRequest(
    @field:Schema(description = "Environment id", example = "1")
    val environmentId: Long,
    @field:Schema(description = "Role name", example = "EDITOR", nullable = true)
    val environmentRole: UserRole?
)

data class EditProjectRoleRequest(
    @field:Schema(description = "New user role", example = "ADMIN")
    val role: CustomRole
)