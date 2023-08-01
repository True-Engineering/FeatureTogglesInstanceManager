package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
import org.springframework.web.multipart.MultipartFile
import ru.trueengineering.featureflag.manager.core.domen.toggle.CopyDirection
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagProperties
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import ru.trueengineering.featureflag.manager.ports.exception.RestExceptionHandler.ErrorResponse
import ru.trueengineering.featureflag.manager.ports.rest.EnvironmentId
import ru.trueengineering.featureflag.manager.ports.rest.FeatureFlagId
import ru.trueengineering.featureflag.manager.ports.rest.OrganizationId
import ru.trueengineering.featureflag.manager.ports.rest.ProjectId
import ru.trueengineering.featureflag.manager.ports.service.FeatureFlagService
import java.util.EnumMap

@RestController
@RequestMapping("/api/features/{organizationId}/{projectId}")
class FeatureFlagController(val featureFlagService: FeatureFlagService) {

    @Operation(summary = "Get all feature flags")
    @GetMapping("/")
    fun getAll(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long
    ): HttpEntity<List<FeatureFlagDto>> = ResponseEntity.ok(featureFlagService.fetchAll(projectId))

    @Operation(summary = "Enable feature flag")
    @PostMapping("/enable/{uuid}")
    fun enable(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @RequestParam("environmentId") environmentId: Long
    ): ResponseEntity<Any> = ResponseEntity.ok(featureFlagService.enableFlag(uuid, projectId, environmentId))

    @Operation(summary = "Disable feature flag")
    @PostMapping("/disable/{uuid}")
    fun disable(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @RequestParam("environmentId") environmentId: Long
    ): ResponseEntity<Any> = ResponseEntity.ok(featureFlagService.disableFlag(uuid, projectId, environmentId))

    @Operation(summary = "Update feature flag strategy")
    @PutMapping("/{uuid}/strategy")
    fun updateStrategy(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @RequestParam("environmentId") environmentId: Long,
        @RequestBody strategy: FlippingStrategyStrategyDto?

    ): ResponseEntity<FeatureFlagDto> = ResponseEntity.ok(
        featureFlagService.updateStrategy(
            uuid,
            projectId,
            environmentId,
            strategy?.type,
            strategy?.initParams
        )
    )

    @Operation(summary = "Enable group of feature flags")
    @PostMapping("/groups/{groupName}/enable")
    fun enableGroup(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Group name", example = "Group of cool feature flags")
        @PathVariable groupName: String
    ): ResponseEntity<Any> {
        TODO()
    }

    @Operation(summary = "Disable group of feature flags")
    @PostMapping("/groups/{groupName}/disable")
    fun disableGroup(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Group name", example = "Group of cool feature flags")
        @PathVariable groupName: String
    ): ResponseEntity<Any> {
        TODO()
    }

    @Operation(summary = "Delete feature flag")
    @DeleteMapping("/{uuid}")
    fun deleteFlag(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String
    ): ResponseEntity<Any> = ResponseEntity.ok(featureFlagService.deleteFeatureFlag(uuid, projectId))

    @Operation(
        summary = "Edit feature flag", responses = [
            ApiResponse(
                description = "Sucess", responseCode = "200", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = FeatureFlagDto::class))]
            ),
            ApiResponse(
                description = "Internal error", responseCode = "500", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                description = "Not found", responseCode = "404", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                description = "Bad request", responseCode = "400", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PutMapping("/{uuid}")
    fun editFlag(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String,
        @RequestBody featureFlag: FeatureFlagRequestDto
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(
            featureFlagService.editFlag(
                uuid, projectId,
                featureFlag.description,
                featureFlag.group,
                featureFlag.type,
                featureFlag.sprint,
                featureFlag.tags ?: HashSet(),
                featureFlag.properties
            )
        )
    }

    @Operation(
        summary = "Create feature flag", responses = [
            ApiResponse(
                description = "Sucess", responseCode = "200", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = FeatureFlagDto::class))]
            ),
            ApiResponse(
                description = "Internal error", responseCode = "500", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                description = "Not found", responseCode = "404", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                description = "Bad request", responseCode = "400", content =
                [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/{uuid}")
    fun create(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Feature flag id", example = "cool.feature.enabled")
        @FeatureFlagId @PathVariable uuid: String,
        @RequestBody featureFlag: FeatureFlagRequestDto
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(
            featureFlagService.createFlag(
                uuid, projectId,
                featureFlag.description,
                featureFlag.group,
                featureFlag.type,
                featureFlag.sprint,
                featureFlag.tags ?: HashSet(),
                featureFlag.properties
            )
        )
    }

    @Operation(summary = "Download feature toggles file for current environment")
    @GetMapping("/download")
    fun downloadEnvironmentFeatureFlags(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long
    ): ResponseEntity<Resource> {
        val resource = featureFlagService.getFeatureFlagResourceForProject(projectId)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=feature-toggles.json"
        )
            .body(resource)
    }

    @Operation(summary = "Get information about available environments during environments synchronization")
    @PostMapping("/environments-synchronization/info", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun getImportEnvironments(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "File to be imported")
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ImportEnvironmentsDto> {
        return ResponseEntity.ok(featureFlagService.getImportEnvironments(projectId, file))
    }

    @Operation(summary = "Get information about changes during portal synchronization")
    @PostMapping("/portals-synchronization/info", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun getImportChanges(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "File to be imported")
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ImportChangesDto> {
        return ResponseEntity.ok(featureFlagService.getImportChanges(projectId, file))
    }

    @Operation(summary = "Environment synchronization")
    @PostMapping("/environments-synchronization/upload")
    fun synchronizeEnvironments(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "unique id of file in cache", example = "112aa9a348ee960f8e02e0f354ab201f")
        @RequestParam("key") key: String,
        @RequestBody copyDirectionDto: CopyDirectionDto
    ): ResponseEntity<List<FeatureFlagDto>> {
        return ResponseEntity.ok(
            featureFlagService.synchronizeEnvironments(
                projectId,
                key,
                CopyDirection(copyDirectionDto.src, copyDirectionDto.dest)
            )
        )
    }

    @Operation(summary = "Portals synchronization")
    @PostMapping("/portals-synchronization/upload")
    fun synchronizePortals(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "unique id of file in cache", example = "112aa9a348ee960f8e02e0f354ab201f")
        @RequestParam("key") key: String,
        @Parameter(
            description = "Parameter, which is responsible for whether extra feature flags will be removed or not",
            example = "true"
        )
        @RequestParam("needToDelete") needToDelete: Boolean
    ): ResponseEntity<List<FeatureFlagDto>> {
        return ResponseEntity.ok(featureFlagService.synchronizePortals(projectId, key, needToDelete))
    }

    @Operation(summary = "Get all feature flags enabled for more than N days")
    @GetMapping("{environmentId}/enabled")
    fun getEnabledFeatureFlags(
        @Parameter(description = "Organization id", example = "1")
        @OrganizationId @PathVariable organizationId: Long,
        @Parameter(description = "Project id", example = "1")
        @ProjectId @PathVariable projectId: Long,
        @Parameter(description = "Environment id", example = "1")
        @EnvironmentId @PathVariable environmentId: Long,
        @Parameter(description = "The number of days for how many feature flags were enabled and did not change")
        @RequestParam(name = "days", required = false, defaultValue = "30") days: Int
    ): ResponseEntity<Resource> {
        val resource = featureFlagService.getEnabledFeatureFlagsResource(projectId, environmentId, days)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feature-toggles-enabled.json")
            .body(resource)
    }
}

@Schema(description = "Model for create feature flag request")
data class FeatureFlagRequestDto(
    @field:Schema(description = "Human readable description of feature flag")
    var description: String? = "",

    @field:Schema(description = "Feature flag group")
    var group: String? = "",

    @field:Schema(description = "Feature flag type", example = "RELEASE")
    var type: FeatureFlagType? = null,

    @field:Schema(description = "Set of feature flag tags")
    var tags: Set<String>? = HashSet(),

    @field:Schema(description = "Sprint of feature flag", example = "Sprint 1")
    var sprint: String? = "",

    @field:Schema(description = "Feature flag custom properties")
    var properties: FeatureFlagProperties = EnumMap(FeatureFlagPropertiesClass::class.java)
)

@Schema(description = "Model for feature flag")
class FeatureFlagDto(val uid: String) {
    @field:Schema(description = "Human readable description of feature flag")
    var description: String? = ""

    @field:Schema(description = "Feature flag group")
    var group: String? = ""

    @field:Schema(description = "Feature flag type", example = "RELEASE")
    var type: FeatureFlagType? = null

    @field:Schema(description = "Set of feature flag tags")
    var tags: Set<String>? = HashSet()

    @field:Schema(description = "Sprint of feature flag", example = "Sprint 1")
    var sprint: String? = ""

    @field:Schema(description = "List of environments")
    var environments: List<FeatureFlagEnvironment> = ArrayList()

    @field:Schema(description = "Feature flag custom properties")
    var properties: FeatureFlagProperties = EnumMap(FeatureFlagPropertiesClass::class.java)
}

@Schema(description = "Model for feature flag strategy")
data class FlippingStrategyStrategyDto(
    @field:Schema(description = "Strategy name", example = "ReleaseDateFlipStrategy")
    val type: String,
    @field:Schema(description = "Map of strategy parameters")
    val initParams: MutableMap<String, String> = HashMap()
)

@Schema(description = "Model for feature flag environment state and strategy")
data class FeatureFlagEnvironment(
    @field:Schema(description = "Environment id", example = "1")
    val id: Long,
    @field:Schema(description = "Environments name", example = "DEV")
    val name: String,
    @field:Schema(description = "Feature flag state on current environment")
    var enable: Boolean = false,
    @field:Schema(description = "Feature flag strategy on current environment")
    var flippingStrategy: FlippingStrategyStrategyDto? = null,
    @field:Schema(description = "List of feature flag permissions")
    var permissions: MutableSet<String>? = HashSet()
)

@Schema(description = "Model for information about changes on import")
data class ImportChangesDto(
    @field:Schema(description = "Import file key for cache")
    val key: String,
    @field:Schema(description = "Feature flags to be added")
    val featureFlagsToAdd: List<FeatureFlagDto>,
    @field:Schema(description = "Feature flags that can be removed")
    val featureFlagsToRemove: List<FeatureFlagDto>,
    @field:Schema(description = "Feature flags to be updated ")
    val featureFlagsToUpdate: List<ChangesDto>
)

@Schema(description = "Model for feature flags changes during portals environments")
data class ChangesDto(
    @field:Schema(description = "updated feature flag")
    val newFeatureFlag: FeatureFlagDto,
    @field:Schema(description = "current feature flag")
    val currentFeatureFlag: FeatureFlagDto
)

@Schema(description = "Model for information about available environments on import")
data class ImportEnvironmentsDto(
    @field:Schema(description = "Import file key for cache")
    val key: String,
    @field:Schema(description = "Indicates whether the portals need to be synchronized first")
    val envSynchronizedStatus: Boolean,
    @field:Schema(description = "New environments from file")
    val srcEnvironments: List<EnvironmentInfoDto>,
    @field:Schema(description = "Current environments")
    val destEnvironments: List<EnvironmentInfoDto>
)

@Schema(description = "Model for import route")
data class CopyDirectionDto(
    @field:Schema(description = "Environment from which states will be taken", example = "UAT")
    val src: String,
    @field:Schema(description = "Environments to which the states will be copied", example = "Petrov")
    val dest: List<String>
)