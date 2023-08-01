package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationCommand
import ru.trueengineering.featureflag.manager.ports.rest.OrganizationId
import ru.trueengineering.featureflag.manager.ports.service.OrganizationService

@RestController
@RequestMapping("/api/organization")
class OrganizationController(val organizationService: OrganizationService) {

    @Operation(summary = "Create new organization")
    @PostMapping
    fun registryNewOrganization(
        @RequestBody request: CreateOrganizationRequest
    ) =
        ResponseEntity.ok(organizationService.create(CreateOrganizationCommand(request.name)))

    @Operation(summary = "Delete organization")
    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(
        @Parameter(description = "Organization id")
        @OrganizationId @PathVariable organizationId: Long
    ): ResponseEntity<Any> {
        organizationService.delete(DeleteOrganizationCommand(organizationId))
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Get information about specified organization")
    @GetMapping("/{organizationId}")
    fun getOrganization(
        @Parameter(description = "Organization id")
        @OrganizationId @PathVariable organizationId: Long) =
        ResponseEntity.ok(organizationService.searchById(organizationId))

    @Operation(summary = "Get information about all organization")
    @GetMapping("/all")
    fun getAllOrganizations() : HttpEntity<List<OrganizationDto>> = ResponseEntity.ok(organizationService.fetchAll())


    @Operation(summary = "Get all members for specified Organization")
    @GetMapping("/{organizationId}/members")
    fun getMembers(
            @Parameter(description = "Organization id", example = "1")
            @OrganizationId @PathVariable organizationId: Long,
    ): OrganizationUsersDto {
        return organizationService.getMembers(organizationId)
    }

    @Operation(summary = "Delete member from the specified Organization")
    @DeleteMapping("/{organizationId}/members/{userId}")
    fun deleteMember(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "User id", example = "1")
        @PathVariable userId: Long
    ): ResponseEntity<Any> {
        organizationService.deleteUser(organizationId, userId)
        return ResponseEntity.noContent().build()
    }

}

@Schema(description = "Model for create organization request")
data class CreateOrganizationRequest(
    @field:Schema(description = "Organization name", example = "Cool company")
    val name: String
)

data class OrganizationDto(
    @field:Schema(description = "Organization id")
    val id: Long,
    @field:Schema(description = "Organization name", example = "Cool company")
    val name: String,
    @field:Schema(description = "List of organization projects")
    val projects: List<ProjectDto>? = ArrayList(),
    @field:Schema(description = "List of permissions of current user for current organization")
    var permissions: Set<String>? = HashSet(),
    @field:Schema(description = "Members count", example = "15")
    var membersCount: Int? = null
)

@Schema(description = "Model for users from organization")
data class OrganizationUsersDto(
        val users: List<OrganizationUserDto>
)

@Schema(description = "Model for users in organization")
data class OrganizationUserDto(
    @field:Schema(description = "User")
        val user: UserDto,
    @field:Schema(description = "Projects roles")
        val projects: List<ProjectUserDto>
)