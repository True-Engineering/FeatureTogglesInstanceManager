package ru.trueengineering.featureflag.manager.ports.security.user

import ru.trueengineering.featureflag.manager.core.domen.user.User
import javax.servlet.http.HttpServletRequest

interface TokenProvider {

    fun validateToken(authToken: String?): Boolean

    fun parseUser(authToken: String?): User

    fun getTokenFromRequest(request: HttpServletRequest?): String

}