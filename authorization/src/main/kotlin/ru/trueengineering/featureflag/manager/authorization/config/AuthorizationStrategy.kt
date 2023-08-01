package ru.trueengineering.featureflag.manager.authorization.config

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.domain.AclAuthorizationStrategy
import org.springframework.security.acls.domain.AclAuthorizationStrategy.CHANGE_GENERAL
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.Acl
import org.springframework.security.acls.model.Sid
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import ru.trueengineering.featureflag.manager.authorization.impl.BusinessEntityImpl

/**
 * Реализация [AclAuthorizationStrategy], которая выполняет следующие проверки через или:
 * - пользователь является владельцем данного ресурса
 * - пользователь имеет права админа для данного ресурса или родительского ресурса
 * - пользователь является "администратором" системы и имеет роль FEATURE_FLAGS_ADMIN
 */
class AuthorizationStrategy(private val roleDefiner: RoleDefiner) : AclAuthorizationStrategy {

    override fun securityCheck(acl: Acl, changeType: Int) {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().authentication == null
            || !SecurityContextHolder.getContext().authentication.isAuthenticated
        ) {
            throw AccessDeniedException("Authenticated principal required to operate with ACLs")
        }

        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities.map { it.authority }.toSet()

        // Check if authorized by virtue of ACL ownership
        val currentUser: Sid = createCurrentUser(authentication)
        if (currentUser == acl.owner) {
            return
        }

        // check if user is admin
        if (authorities.contains(ADMIN_ROLE)) {
            return
        }

        when (changeType) {
            /**
             * - установка родителя
             * - добавление какому-либо пользователю права
             * - удаление у пользователя права
             * - редактирование у пользователя права
             */
            CHANGE_GENERAL -> checkPermissionForEditPermissions(acl, currentUser, authentication)
            /**
             * - при установке нового владельца доменного объекта
             * - при вызове [org.springframework.security.acls.model.AuditableAcl.updateAuditing]
             */
            else ->
                throw AccessDeniedException("You do not have permissions for current operation " +
                        "CHANGE_OWNERSHIP, CHANGE_AUDITING")
        }
    }

    private fun checkPermissionForEditPermissions(acl: Acl, currentUser: Sid, authentication: Authentication) {
        var currentAcl: Acl? = acl;
        while (currentAcl != null) {
            val permissions = currentAcl.entries
                .filter { it.sid.equals(currentUser) }
                .map { it.permission as CustomPermission }
                .toSet()
            val userHasAnyAdminPermission = roleDefiner
                .isAdmin(BusinessEntityImpl(acl.objectIdentity), authentication, permissions)
            if (userHasAnyAdminPermission) {
                return
            }
            currentAcl = currentAcl.parentAcl
        }

        throw AccessDeniedException("You do not have permissions for current operation")
    }

    private fun createCurrentUser(authentication: Authentication?): Sid {
        return PrincipalSid(authentication)
    }
}