package ru.trueengineering.featureflag.manager.authorization.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.security.acls.model.Permission
import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.auth.UserPermissions
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import java.security.Principal
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Wrapper of PermissionService for synchronization work with composite method
 * org.springframework.security.acls.model.MutableAclService#updateAcl() updateAcl
 */
@Primary
@Service
class PermissionSynchronizedProxyService(
    @Autowired val permissionService: IPermissionService,
) : IPermissionService {

    private val lock = ReentrantReadWriteLock()
    private val writeLock = lock.writeLock()
    private val readLock = lock.readLock()

    override fun grantPermissionsForUser(entity: BusinessEntity, permissions: List<Permission>, user: Principal) {
        try {
            writeLock.lock()
            permissionService.grantPermissionsForUser(entity, permissions, user)
        } finally {
            writeLock.unlock()
        }
    }

    override fun grantPermissionsForCurrentUser(entity: BusinessEntity, permissions: List<Permission>) {
        try {
            writeLock.lock()
            permissionService.grantPermissionsForCurrentUser(entity, permissions)
        } finally {
            writeLock.unlock()
        }
    }

    override fun clearPermissionsForUser(entity: BusinessEntity, user: Principal) {
        try {
            writeLock.lock()
            permissionService.clearPermissionsForUser(entity, user)
        } finally {
            writeLock.unlock()
        }
    }

    override fun getPermissionsForUser(entities: List<BusinessEntity>, user: Principal): UserPermissions {
        try {
            readLock.lock()
            return permissionService.getPermissionsForUser(entities, user)
        } finally {
            readLock.unlock()
        }
    }

    override fun getPermissionsForUser(entity: BusinessEntity, user: Principal): List<Permission> {
        try {
            readLock.lock()
            return permissionService.getPermissionsForUser(entity, user)
        } finally {
            readLock.unlock()
        }
    }

    override fun getUserRoleForEntity(entity: BusinessEntity, user: Principal): CustomRole {
        try {
            readLock.lock()
            return permissionService.getUserRoleForEntity(entity, user)
        } finally {
            readLock.unlock()
        }
    }

    override fun getUserRoleForEntity(entity: BusinessEntity): CustomRole {
        try {
            readLock.lock()
            return permissionService.getUserRoleForEntity(entity)
        } finally {
            readLock.unlock()
        }
    }

    override fun grantPermissionsForOwner(entity: BusinessEntity, permissions: List<Permission>) {
        try {
            writeLock.lock()
            permissionService.grantPermissionsForOwner(entity, permissions)
        } finally {
            writeLock.unlock()
        }
    }

    override fun getPermissionsForCurrentUser(entity: BusinessEntity): List<Permission> {
        try {
            readLock.lock()
            return permissionService.getPermissionsForCurrentUser(entity)
        } finally {
            readLock.unlock()
        }
    }

    override fun getPermissionsForUsers(entities: List<BusinessEntity>, users: List<Principal>): List<UserPermissions> {
        try {
            readLock.lock()
            return permissionService.getPermissionsForUsers(entities, users)
        } finally {
            readLock.unlock()
        }
    }

    override fun isGrantedPermission(entity: BusinessEntity, permission: Permission, user: Principal): Boolean {
        try {
            readLock.lock()
            return permissionService.isGrantedPermission(entity, permission, user)
        } finally {
            readLock.unlock()
        }
    }

    override fun isGrantedPermissionForCurrentUser(entity: BusinessEntity, permission: Permission): Boolean {
        try {
            readLock.lock()
            return permissionService.isGrantedPermissionForCurrentUser(entity, permission)
        } finally {
            readLock.unlock()
        }
    }

    override fun getUsersByEntity(entity: BusinessEntity, permissions: List<Permission>): Set<String> {
        try {
            readLock.lock()
            return permissionService.getUsersByEntity(entity, permissions)
        } finally {
            readLock.unlock()
        }
    }

    override fun getPermissionsNameListForCurrentUser(entity: BusinessEntity): MutableSet<String> {
        try {
            readLock.lock()
            return permissionService.getPermissionsNameListForCurrentUser(entity)
        } finally {
            readLock.unlock()
        }
    }
}
