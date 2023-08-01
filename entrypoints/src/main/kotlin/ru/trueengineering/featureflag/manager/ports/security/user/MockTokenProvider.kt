package ru.trueengineering.featureflag.manager.ports.security.user

import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.core.domen.user.User
import javax.servlet.http.HttpServletRequest

class MockTokenProvider : TokenProvider {

    override fun validateToken(authToken: String?): Boolean {
        return true
    }

    override fun parseUser(authToken: String?): User =
        User(userName = "Mock User", email = "test@trueengineering.ru", authorities = listOf(ADMIN_ROLE))

    override fun getTokenFromRequest(request: HttpServletRequest?): String {
        return "Mock token"
    }
}