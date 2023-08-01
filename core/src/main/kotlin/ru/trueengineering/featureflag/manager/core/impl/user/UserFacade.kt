package ru.trueengineering.featureflag.manager.core.impl.user

import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserByEmailQuery
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUsersByEmailListQuery
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

class UserFacade(
    private val userRepository: UserRepository
) :
    FetchCurrentUserUseCase,
    CreateUserUseCase,
    FetchUserUseCase,
    SetDefaultProjectUseCase {

    override fun search(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal
        if (principal !is User) throw ServiceException(ErrorCode.USER_NOT_FOUND)
        principal.authorities = authentication.authorities.map { it.authority }.toList()
        return principal
    }

    override fun execute(command: CreateUserCommand): User {
        return userRepository.createUser(
            User(
                userName = command.name,
                email = command.email,
                status = command.status,
                authorities = command.authorities
            )
        )
    }

    override fun execute(command: FetchUserByEmailQuery) = userRepository.getByEmail(command.email)

    override fun execute(command: FetchUsersByEmailListQuery): List<User> =
        userRepository.getByEmailList(command.emails)

    override fun searchUserCount(command: FetchUsersByEmailListQuery): Int =
        userRepository.getCountByEmailList(command.emails)

    override fun searchById(userId: Long) = userRepository.getById(userId)

    override fun execute(command: SetDefaultProjectCommand) {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
            .apply { defaultProjectId = if (command.isDefault) command.projectId else null }
        userRepository.updateUser(user)
    }
}