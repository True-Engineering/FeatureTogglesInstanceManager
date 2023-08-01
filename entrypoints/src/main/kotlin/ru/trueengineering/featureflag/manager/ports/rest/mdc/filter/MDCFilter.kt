package ru.trueengineering.featureflag.manager.ports.rest.mdc.filter

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.ports.rest.mdc.MDCProperty
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MDCFilter(
    private val fetchCurrentUserUseCase: FetchCurrentUserUseCase
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication !is UsernamePasswordAuthenticationToken) {
            return filterChain.doFilter(request, response)
        }
        return MDCContext(fetchCurrentUserUseCase.search()).use {
            filterChain.doFilter(request, response)
        }
    }
}

private class MDCContext(user: User) : MDCProperty("userName", user.userName)