package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize

interface SearchProjectUseCase {

    @PreAuthorize("hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')" +
            "|| hasPermission(#query.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun search(query: SearchProjectByIdQuery) : Project

}

data class SearchProjectByIdQuery(val projectId: Long)