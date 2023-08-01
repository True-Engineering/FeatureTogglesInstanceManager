package ru.trueengineering.featureflag.manager.ports.rest.mdc.filter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class VersionFilter(
        @Value("\${APP_VERSION:unknown}") private val portalVersion: String
): OncePerRequestFilter() {
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        response.addHeader("FF-PORTAL-VERSION", portalVersion)
        filterChain.doFilter(request, response)
    }
}