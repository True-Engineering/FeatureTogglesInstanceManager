package ru.trueengineering.featureflag.manager.core.domen.user

import org.springframework.security.access.prepost.PreAuthorize

interface ActivateUserUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT_MEMBERS')")
    fun execute(command: ActivateUserCommand)

}

data class ActivateUserCommand(val userId: Long, val projectId: Long)