package ru.trueengineering.featureflag.manager.ports.security.agent

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import java.io.IOException
import java.util.Base64
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val AGENT_NAME_DELIMITER = ":"

@Component
class AgentTokenAuthFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        request.getHeader("Agent-Authorization")
            ?.removePrefix("Bearer ")
            ?.let { String(Base64.getDecoder().decode(it)) }
            ?.also { token ->
                if (!token.contains(AGENT_NAME_DELIMITER)) {
                    throw org.springframework.security.access.AccessDeniedException(ErrorCode.ACCESS_DENIED.name)
                }
                val authInfo = token.split(AGENT_NAME_DELIMITER)
                val agentName = authInfo[0]
                val agentToken = authInfo[1]

                val authentication = PreAuthenticatedAuthenticationToken(
                        agentName, agentToken, listOf(SimpleGrantedAuthority("AGENT")))
                SecurityContextHolder.getContext().authentication = authentication
            }

        filterChain.doFilter(request, response)
    }

}