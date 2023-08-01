package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface FetchAllFeatureFlagsForAgentUseCase {

    @PreAuthorize("hasAuthority('AGENT')")
    fun search(query: FetchAllFeatureFlagsForAgentQuery): EnvironmentFeatureFlagsWithUpdateStatus

}

data class FetchAllFeatureFlagsForAgentQuery(val token: String, val agentName: String, val featureFlagHash: String?)