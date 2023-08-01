package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize

interface FetchAllFeatureFlagsForProjectUseCase {

    @PreAuthorize("hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')" +
            "|| hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun search(query: FetchAllFeatureFlagsForProjectQuery) : List<FeatureFlag>

}

data class FetchAllFeatureFlagsForProjectQuery(val projectId: Long)