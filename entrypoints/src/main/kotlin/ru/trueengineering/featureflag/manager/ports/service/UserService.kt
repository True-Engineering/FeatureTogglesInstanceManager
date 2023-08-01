package ru.trueengineering.featureflag.manager.ports.service

import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.InviteUserToProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.InviteUserToProjectUseCase
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserDto
import ru.trueengineering.featureflag.manager.ports.service.mapper.UserMapper
import java.util.UUID

@Component
class UserService(
    private val userMapper: UserMapper,
    private val fetchCurrentUserUseCase: FetchCurrentUserUseCase,
    private val activateUserUseCase: ActivateUserUseCase,
    private val inviteUserToProjectUseCase: InviteUserToProjectUseCase
) {
    fun fetchUser(): UserDto {
        return userMapper.convertToDto(fetchCurrentUserUseCase.search())
    }

    fun activate(userId: Long, projectId: Long) {
        activateUserUseCase.execute(ActivateUserCommand(userId, projectId))
    }

    fun inviteUserToProject(invitationUid: UUID) {
        inviteUserToProjectUseCase.execute(InviteUserToProjectCommand(invitationUid))
    }
}
