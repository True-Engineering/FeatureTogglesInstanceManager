package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.ports.service.FeatureFlagService
import ru.trueengineering.lib.logger.autoconfigure.RequestMappingLogger
import java.util.TreeMap

@RestController
@RequestMapping("/api/agent/features")
class AgentFeatureFlagController(val featureFlagService: FeatureFlagService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Get all feature flags for specified environment")
    @GetMapping
    @RequestMappingLogger(false)
    fun getAll(
        authentication: Authentication,
        @RequestHeader("FF-HASH") featureFlagHash: String?
    ): HttpEntity<AgentFeatureFlagResponseDto> {
        log.trace("Agent authentication: cred: ${authentication.credentials},  principal: ${authentication.principal}")
        val token = authentication.credentials as String?
            ?: throw ServiceException(ErrorCode.ACCESS_DENIED, "Unable to find agent token")
        val agentName = authentication.principal as String?
            ?: throw ServiceException(ErrorCode.ACCESS_DENIED, "Unable to find agent name")
        return ResponseEntity.ok(
            featureFlagService.getAgentFeatureFlags(token, agentName,featureFlagHash))
    }
}

@Schema(description = "Model for feature flag")
data class AgentFeatureFlagResponseDto(
    val featureFlagList: List<AgentFeatureFlagDto>,
    val updated: Boolean)

@Schema(description = "Model for agent feature flag")
data class AgentFeatureFlagDto(val uid: String) {
    @field:Schema(description = "Human readable description of feature flag")
    var description: String? = null

    @field:Schema(description = "Feature flag state on current environment")
    var enable: Boolean = false

    @field:Schema(description = "Feature flag group")
    var group: String? = null

    @field:Schema(description = "List of feature flag permissions")
    val permissions: MutableSet<String> = HashSet()

    @field:Schema(description = "Feature flag strategy on current environment")
    var flippingStrategy: AgentFlippingStrategyStrategyDto? = null

    @field:Schema(description = "Custom properties")
    val customProperties: MutableMap<String, String> = TreeMap()
}

@Schema(description = "Model for feature flag strategy")
data class AgentFlippingStrategyStrategyDto(
    @field:Schema(description = "Strategy name", example = "ReleaseDateFlipStrategy")
    val className: String,
    @field:Schema(description = "Map of strategy parameters")
    val initParams: MutableMap<String, String> = TreeMap()
)
