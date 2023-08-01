package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentProperties
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsForProject
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateCommand
import ru.trueengineering.featureflag.manager.ports.rest.EnvironmentId
import ru.trueengineering.featureflag.manager.ports.rest.ProjectId
import ru.trueengineering.featureflag.manager.ports.service.EnvironmentService
import java.time.Instant
import java.time.OffsetDateTime
import java.util.EnumMap

@RestController
@RequestMapping("/api/environment/{projectId}")
class EnvironmentController(
    val environmentService: EnvironmentService
) {

    @Operation(summary = "Create new environment for specified project")
    @PostMapping
    fun registryNewEnvironment(
        @RequestBody request: CreateOrUpdateEnvironmentRequest,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long
    ): ResponseEntity<EnvironmentInfoDto> =
        ResponseEntity.ok(environmentService.create(CreateEnvironmentCommand(request.name, projectId)))

    @Operation(summary = "Create or revoke environment Token")
    @PostMapping("/{environmentId}/token")
    fun createEnvironmentToken(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long
    ): ResponseEntity<String> =
        ResponseEntity.ok(environmentService.createToken(CreateEnvironmentTokenCommand(environmentId)))

    @Operation(summary = "Update environment for specified project")
    @PutMapping("/{environmentId}")
    fun updateEnvironment(
        @RequestBody request: CreateOrUpdateEnvironmentRequest,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long
    ): ResponseEntity<EnvironmentInfoDto> =
        ResponseEntity.ok(
            environmentService.update(UpdateEnvironmentCommand(environmentId, request.name, projectId))
        )

    @Operation(summary = "Delete environment for specified project")
    @DeleteMapping("/{environmentId}")
    fun deleteEnvironment(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long
    ): ResponseEntity<Any> {
        environmentService.delete(DeleteEnvironmentCommand(environmentId))
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @Deprecated(message = "Use instead method with environmentId", level = DeprecationLevel.WARNING)
    @Operation(summary = "Delete environment agent instance")
    @DeleteMapping("/instance/{instanceId}")
    fun deleteInstance(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Instance id", example = "1")
        @PathVariable instanceId: Long
    ): ResponseEntity<Any> {
        environmentService.deleteInstance(DeleteInstanceCommand(projectId, 0, instanceId))
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @Operation(summary = "Delete environment agent instance")
    @DeleteMapping("/{environmentId}/instance/{instanceId}")
    fun deleteInstance(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long,
        @Parameter(description = "Instance id", example = "1")
        @PathVariable instanceId: Long
    ): ResponseEntity<Any> {
        environmentService.deleteInstance(DeleteInstanceCommand(projectId, environmentId, instanceId))
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @Operation(summary = "Get information about all environments for specified project")
    @GetMapping("/all")
    fun getAllEnvironmentsForProject(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long
    ): HttpEntity<List<EnvironmentInfoDto>> =
        ResponseEntity.ok(environmentService.findAllForProject(FetchAllEnvironmentsForProject(projectId)))

    @Operation(summary = "Compare two environments and get lists of need to enable and disable")
    @GetMapping("/compare")
    fun getCompareEnvironmentsState(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Id of environment with which we want to compare", example = "1")
        @EnvironmentId @RequestParam from: Long,
        @Parameter(description = "Id of environment we want to compare and apply changes", example = "1")
        @EnvironmentId @RequestParam to: Long
    ): HttpEntity<GetCompareEnvironmentsStateResponse> =
        ResponseEntity.ok(environmentService.getCompareEnvironmentsState(
            GetCompareEnvironmentsStateCommand(projectId, from, to))
        )

    @Operation(summary = "Update environment's states after compare")
    @PostMapping("/compare/update/{environmentId}")
    fun updateFlagsState(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Id of environment which will be updated after compare", example = "1")
        @EnvironmentId @PathVariable environmentId: Long,
        @Parameter(description = "Map of feature uid and it's state")
        @RequestBody request: UpdateFlagsStateRequest
    ): ResponseEntity<Any> {
        environmentService.updateFlagsState(
            UpdateFlagsStateCommand(
                projectId,
                environmentId,
                request.featureFlagsStates
            )
        )
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/freeze/{environmentId}")
    fun freezeEnvironment(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long,
        @RequestParam endTime: String
    ): ResponseEntity<EnvironmentInfoDto> = ResponseEntity.ok(environmentService.freezeEnvironment(
        FreezeEnvironmentCommand(projectId, environmentId, OffsetDateTime.parse(endTime))))

    @PostMapping("/unfreeze/{environmentId}")
    fun unfreezeEnvironment(
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long
    ): ResponseEntity<EnvironmentInfoDto> = ResponseEntity.ok(
        environmentService.unfreezeEnvironment(UnfreezeEnvironmentCommand(projectId, environmentId)))
}

@Schema(description = "Model for create or update environment request")
data class CreateOrUpdateEnvironmentRequest(
    @field:Schema(description = "Environment name", example = "DEV")
    val name: String
)

@Schema(description = "Model for environment information")
data class EnvironmentInfoDto(
    @field:Schema(description = "Environment id", example = "1")
    val id: Long,
    @field:Schema(description = "Environment name", example = "DEV")
    val name: String,
    @field:Schema(
        description = "List of instances of current environment. " +
                "For example, there can be two instance of production environment"
    )
    val instances: List<InstanceDto>,
    val authKeyExist: Boolean,
    @field:Schema(description = "Environment status", example = "ACTIVE")
    var status: EnvironmentConnectionStatus? = EnvironmentConnectionStatus.NOT_CONNECTED,
    @field:Schema(description = "List of permissions of current user for current environment")
    var permissions: MutableSet<String>? = HashSet(),
    @field:Schema(description = "List of emails")
    var emails: List<EmailDto>,
    @field:Schema(description = "Environment custom properties")
    var properties: EnvironmentProperties = EnumMap(EnvironmentPropertiesClass::class.java),
)

@Schema(description = "Model for emails")
data class EmailDto(
    @field:Schema(description = "Email", example = "asd@test.ru")
    val email: String
)

data class InstanceDto(
    @field:Schema(description = "Instance id", example = "1")
    val id: Long,
    @field:Schema(description = "Instance name", example = "prod-1")
    val name: String,
    @field:Schema(description = "Instance update date", example = "2022-04-22T12:00:00+03:00")
    val updated: Instant,
    @field:Schema(description = "Instance status", example = "ACTIVE")
    val status: InstanceConnectionStatus
)

data class GetCompareEnvironmentsStateResponse(
    @field:Schema(description = "Feature flags which enabled on first env but not on second")
    val enable: List<FeatureFlagDto>,
    @field:Schema(description = "Feature flags which disabled on first env but not on second")
    val disable: List<FeatureFlagDto>
)

data class UpdateFlagsStateRequest(
    @field:Schema(description = "feature states", example = "{'cool.feature', true}")
    val featureFlagsStates: Map<String, Boolean>
)