package ru.trueengineering.featureflag.manager.authorization.annotation

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE

object AspectUtils {

    fun executeWithAdminRole(lambda: () -> Any?): Any? {
        return applyAdminRoleInner(lambda)
    }

    private fun applyAdminRoleInner(lambda: () -> Any?): Any? {
        val currentUser = SecurityContextHolder.getContext()?.authentication
        return if (currentUser == null) {
            try {
                val ctx: SecurityContext = SecurityContextHolder.createEmptyContext()
                ctx.authentication = UsernamePasswordAuthenticationToken(
                    "system",
                    "system",
                    mutableListOf(SimpleGrantedAuthority(ADMIN_ROLE))
                )
                SecurityContextHolder.setContext(ctx)
                lambda()
            } finally {
                SecurityContextHolder.clearContext()
            }

        } else {
            return try {
                val adminAuthorities = currentUser.authorities.plus(SimpleGrantedAuthority(ADMIN_ROLE))
                val admin =
                    UsernamePasswordAuthenticationToken(
                        currentUser.principal,
                        currentUser.credentials,
                        adminAuthorities
                    )
                SecurityContextHolder.getContext().authentication = admin
                lambda()
            } finally {
                SecurityContextHolder.getContext().authentication = currentUser
            }
        }

    }
}