package ru.trueengineering.featureflag.manager.ports.security.user

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserByEmailQuery
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import java.util.stream.Collectors

@Service
class UserSecurityService(
    private val fetchUserUseCase: FetchUserUseCase,
    private val createUserUseCase: CreateUserUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun authenticate(user: User) {
        var featureFlagUser = fetchUserUseCase.execute(FetchUserByEmailQuery(user.email))
        if (featureFlagUser == null) {
            log.debug("User not found in DB, save new user with email ${user.email}")
            featureFlagUser = createUserUseCase.execute(
                CreateUserCommand(
                    name = user.userName,
                    email = user.email,
                    authorities = user.authorities,
                    status = UserStatus.ACTIVE
                )
            )
        }

        val authorities: List<SimpleGrantedAuthority> = user.authorities?.stream()
            ?.map { role: String? -> SimpleGrantedAuthority(role) }
            ?.collect(Collectors.toList()) ?: emptyList()
        val authentication = UsernamePasswordAuthenticationToken(featureFlagUser, null, authorities)

        SecurityContextHolder.getContext().authentication = authentication
    }

}