package ru.trueengineering.featureflag.manager.core.domen.user

import java.util.UUID

interface InviteUserToProjectUseCase {
    fun execute(command: InviteUserToProjectCommand)
}

data class InviteUserToProjectCommand(val invitationUid: UUID)