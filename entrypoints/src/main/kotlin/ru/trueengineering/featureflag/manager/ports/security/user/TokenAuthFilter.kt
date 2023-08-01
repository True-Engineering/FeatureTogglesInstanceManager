package ru.trueengineering.featureflag.manager.ports.security.user

import org.apache.logging.log4j.util.Strings.isNotEmpty
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenAuthFilter(
    private val userSecurityService: UserSecurityService,
    private val tokenProvider: TokenProvider
) :
        OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = tokenProvider.getTokenFromRequest(request)
            if (isNotEmpty(token) && tokenProvider.validateToken(token)) {
                val user = tokenProvider.parseUser(token)
                userSecurityService.authenticate(user)
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }
        filterChain.doFilter(request, response)
    }
}