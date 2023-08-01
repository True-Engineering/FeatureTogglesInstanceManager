package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize

interface FetchMembersForProjectUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_MEMBERS') ||" +
            "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT_MEMBERS')")
    fun search(command: FetchMembersForProjectQuery): List<ProjectUser>

}

data class FetchMembersForProjectQuery(val projectId: Long)