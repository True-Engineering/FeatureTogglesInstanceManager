package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface FetchAllEnvironmentsOfProjectUseCase {

    @PreAuthorize("hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT') || " +
            "hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun search(query: FetchAllEnvironmentsForProject): List<Environment>

}

data class FetchAllEnvironmentsForProject(val projectId: Long)