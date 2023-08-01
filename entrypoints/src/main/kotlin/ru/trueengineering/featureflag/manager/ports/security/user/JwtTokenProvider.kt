package ru.trueengineering.featureflag.manager.ports.security.user

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.ports.config.properties.ClientSecurityProperties
import javax.servlet.http.HttpServletRequest

private const val ACCESS_TOKEN_NAME = "X-Forwarded-Access-Token"

class JwtTokenProvider(
    private val jwtParser: JwtParser,
    private val clientSecurityProperties: ClientSecurityProperties
) : TokenProvider {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun buildUser(claims: Claims): User {
        log.trace("Claims - $claims")
        val isAdmin = ((claims.get("realm_access", Map::class.java))?.get("roles") as List<*>?)
                ?.contains(clientSecurityProperties.adminAuthorityRoleName) ?: false
        val name = claims.get(clientSecurityProperties.userNameClaim, String::class.java)
                ?: throw ServiceException(ErrorCode.ACCESS_DENIED, "User name must be not null").also {
                    log.error("User name must be not null! Claims: ${claims.entries}")
                }
        val email = claims.get(clientSecurityProperties.userEmailClaim, String::class.java)
                ?: throw ServiceException(ErrorCode.ACCESS_DENIED, "Email must be not null").also {
                    log.error("Email must be not null! Claims: ${claims.entries}")
                }
        return User(
            userName = name,
            email = email,
            authorities = if (isAdmin) listOf(ADMIN_ROLE) else emptyList()
        )
    }

    override fun validateToken(authToken: String?): Boolean {
        try {
            jwtParser
                    .parseClaimsJwt(authToken)
            return true
        } catch (ex: SignatureException) {
            log.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
        }
        return false
    }

    override fun parseUser(authToken: String?): User {
        return buildUser(parseToken(authToken))
    }

    override fun getTokenFromRequest(request: HttpServletRequest?): String {
        val token = request!!.getHeader(ACCESS_TOKEN_NAME)
        return if (StringUtils.hasText(token)) {
            log.trace("Auth token: $token")
            // Обрезаем часть токена с подписью, так как подпись уже проверена в oauth2-proxy
            val lastDotIndex = token.lastIndexOf('.')
            token.substring(0, lastDotIndex + 1)
        } else ""
    }

    private fun parseToken(token: String?): Claims {
        return jwtParser
                .parseClaimsJwt(token)
                .body
    }
}