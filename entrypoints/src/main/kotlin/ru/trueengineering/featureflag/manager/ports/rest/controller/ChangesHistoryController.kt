package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.Difference
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternCommand
import ru.trueengineering.featureflag.manager.ports.rest.OrganizationId
import ru.trueengineering.featureflag.manager.ports.rest.ProjectId
import ru.trueengineering.featureflag.manager.ports.service.ChangesHistoryService
import ru.trueengineering.featureflag.manager.ports.service.FeatureFlagService
import ru.trueengineering.featureflag.manager.ports.service.ProjectService
import java.text.SimpleDateFormat
import java.time.Instant

const val SECONDS_PER_DAY = (60 * 60 * 24).toLong()

@RestController
@RequestMapping("/api/history/{organizationId}/{projectId}")
class ChangesHistoryController(
    val changesHistoryService: ChangesHistoryService,
    val projectService: ProjectService,
    val featureFlagService: FeatureFlagService
) {
    @GetMapping
    fun getChangesHistory(
        @PathVariable @OrganizationId organizationId: Long,
        @PathVariable @ProjectId projectId: Long,
        @RequestParam page: Int,
        @RequestParam (defaultValue = "3") pageSize: Int,
        @RequestParam (name = "featureFlagId", required = false) featureFlagUid: String?,
        @RequestParam (required = false) userId: Long?,
        @RequestParam (required = false) start: String?,
        @RequestParam (required = false) end: String?,
        @RequestParam (required = false) tag: String?,
    ): HttpEntity<ChangesHistoryResponseDto> {
        return ResponseEntity.ok(changesHistoryService.getProjectChangesHistory(
            GetProjectChangesHistoryCommand(
                projectId,
                PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("created"))),
                featureFlagUid,
                userId,
                if (start != null) SimpleDateFormat("dd.MM.yyyy").parse(start).toInstant() else start,
                if (end != null) SimpleDateFormat("dd.MM.yyyy").parse(end).toInstant().plusSeconds(SECONDS_PER_DAY) else end,
                tag
            )))
    }

    @GetMapping("/filter/user")
    fun filterUser(
        @PathVariable @OrganizationId organizationId: Long,
        @PathVariable @ProjectId projectId: Long,
        @RequestParam page: Int,
        @RequestParam (defaultValue = "10") pageSize: Int,
        @RequestParam template: String
    ): HttpEntity<UserFilterResponseDto> {
        return ResponseEntity.ok(
            projectService.getMembersByPattern(
                organizationId,
                projectId,
                template,
                PageRequest.of(page, pageSize)
            )
        )
    }

    @GetMapping("/filter/feature")
    fun filterFeatureFlag(
        @PathVariable @OrganizationId organizationId: Long,
        @PathVariable @ProjectId projectId: Long,
        @RequestParam page: Int,
        @RequestParam (defaultValue = "10") pageSize: Int,
        @RequestParam template: String
    ): HttpEntity<FeatureFlagFilterResponseDto> {
        return ResponseEntity.ok(featureFlagService.findFeatureFlagsByPattern(
            FindFeatureFlagByPatternCommand(
                projectId,
                template,
                PageRequest.of(page, pageSize)
            )))
    }

    @GetMapping("/filter/tag")
    fun filterTag(
        @PathVariable @OrganizationId organizationId: Long,
        @PathVariable @ProjectId projectId: Long,
        @RequestParam page: Int,
        @RequestParam (defaultValue = "5") pageSize: Int
    ): HttpEntity<TagFilterResponseDto> {
        return ResponseEntity.ok(featureFlagService.getFeatureFlagsTags(projectId, PageRequest.of(page, pageSize)))
    }
}

@Schema(description = "Model for changes history")
data class ChangesHistoryDto(
    @field:Schema(description = "Changes History id", example = "1")
    val id: Long,
    @field:Schema(description = "Changes History action", example = "CREATE")
    val action: ChangeAction,
    @field:Schema(description = "User name")
    val userName: String,
    @field:Schema(description = "Feature Flag")
    val featureFlag: FeatureFlagDto,
    @field:Schema(description = "Environment name (maybe null)")
    val environment: String? = null,
    @field:Schema(description = "Changes")
    var changes: FeatureChangesDto = mutableMapOf(),
    @field:Schema(description = "Creation info")
    var creationInfo: FeatureFlagDto? = null,
    @field:Schema(description = "Time of history record")
    val time: Instant
)

@Schema(description = "Model for get all project history response")
data class ChangesHistoryResponseDto(
    @field:Schema(description = "List of changes history records")
    val changesHistory: List<ChangesHistoryDto>,
    @field:Schema(description = "Page size", example = "3")
    val pageSize: Int,
    @field:Schema(description = "Number of page", example = "1")
    val page: Int,
    @field:Schema(description = "Count of total pages", example = "20")
    val totalPages: Int,
    @field:Schema(description = "Count of total elements", example = "100")
    val totalElements: Long
)

@Schema(description = "Model for user filter response")
data class UserFilterResponseDto(
    @field:Schema(description = "List of users")
    val resultList: List<UserDto>,
    @field:Schema(description = "Page size", example = "3")
    val pageSize: Int,
    @field:Schema(description = "Number of page", example = "1")
    val page: Int,
    @field:Schema(description = "Count of total pages", example = "20")
    val totalPages: Int,
    @field:Schema(description = "Count of total elements", example = "100")
    val totalElements: Long
)

@Schema(description = "Model for featureFlags filter response")
data class FeatureFlagFilterResponseDto(
    @field:Schema(description = "List of featureFlags")
    val resultList: List<FeatureFlagDto>,
    @field:Schema(description = "Page size", example = "3")
    val pageSize: Int,
    @field:Schema(description = "Number of page", example = "1")
    val page: Int,
    @field:Schema(description = "Count of total pages", example = "20")
    val totalPages: Int,
    @field:Schema(description = "Count of total elements", example = "100")
    val totalElements: Long
)

@Schema(description = "Model for filter tag response")
data class TagFilterResponseDto(
    @field:Schema(description = "List of tags")
    val resultList: List<String>,
    @field:Schema(description = "Page size", example = "3")
    val pageSize: Int,
    @field:Schema(description = "Number of page", example = "1")
    val page: Int,
    @field:Schema(description = "Count of total pages", example = "20")
    val totalPages: Int,
    @field:Schema(description = "Count of total elements", example = "100")
    val totalElements: Long
)

typealias FeatureChangesDto = MutableMap<String, Difference>