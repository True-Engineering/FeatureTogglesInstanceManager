package ru.trueengineering.featureflag.manager.authorization.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.Acl
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.MutableAclService
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.Permission
import org.springframework.security.acls.model.Sid
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.auth.BusinessEntityPermissions
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.auth.UserPermissions
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import java.security.Principal

@Service
class PermissionService(
    @Autowired val aclService: MutableAclService,
    @Autowired val permissionFilters: List<PermissionFilter>,
    @Autowired val permissionNameEvaluator: PermissionNameEvaluator,
    @Autowired val roleDefiner: RoleDefiner
) : IPermissionService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * TODO m.yastrebov: необходимо позаботиться о многопотчности
     */
    @Transactional
    override fun grantPermissionsForUser(entity: BusinessEntity, permissions: List<Permission>, user: Principal) {
        if (permissions.isEmpty()) {
            return
        }
        val sid: Sid = PrincipalSid(user.name)
        val objectIdentity: ObjectIdentity = objectIdentity(entity)

        val acl: MutableAcl = aclService.readAclById(objectIdentity, listOf(sid)) as MutableAcl
        permissions
            .filter { !permissionExist(acl, it, sid) }
            .forEach { acl.insertAce(acl.entries.size, it, sid, true) }
        aclService.updateAcl(acl)
    }

    override fun grantPermissionsForCurrentUser(entity: BusinessEntity, permissions: List<Permission>) {
        grantPermissionsForUser(entity, permissions, SecurityContextHolder.getContext().authentication)
    }

    override fun clearPermissionsForUser(entity: BusinessEntity, user: Principal) {
        val sid: Sid = PrincipalSid(user.name)
        val objectIdentity: ObjectIdentity = objectIdentity(entity)

        val acl: MutableAcl = aclService.readAclById(objectIdentity, listOf(sid)) as MutableAcl
        for (aceIndex in acl.entries.indices.reversed()) {
            if (acl.entries[aceIndex].sid.equals(sid)) {
                acl.deleteAce(aceIndex)
            }
        }
        aclService.updateAcl(acl)
    }

    override fun getPermissionsForUser(entities: List<BusinessEntity>, user: Principal): UserPermissions =
        getPermissionsForUsers(entities, listOf(user))[0]

    override fun getPermissionsForUser(entity: BusinessEntity, user: Principal): List<Permission> {
        return getPermissionsForUsers(listOf(entity), listOf(user))[0]
            .findByObjectIdentity(entity)
            ?.permissions
            ?.toList() ?: emptyList()
    }

    override fun getUserRoleForEntity(entity: BusinessEntity, user: Principal): CustomRole {
        return roleDefiner.defineRoleForEntity(
            entity, user,
            getPermissionsForUser(entity, user).filterIsInstance<CustomPermission>()
        )
    }

    override fun getUserRoleForEntity(entity: BusinessEntity): CustomRole {
        return roleDefiner.defineRoleForEntity(
            entity, SecurityContextHolder.getContext().authentication,
            getPermissionsForCurrentUser(entity).filterIsInstance<CustomPermission>()
        )

    }

    override fun grantPermissionsForOwner(entity: BusinessEntity, permissions: List<Permission>) {
        if (permissions.isEmpty()) {
            return
        }
        val objectIdentity: ObjectIdentity = objectIdentity(entity)

        val acl: MutableAcl = aclService.readAclById(objectIdentity) as MutableAcl
        val owner = acl.owner
        permissions
            .filter { !permissionExist(acl, it, owner) }
            .forEach { acl.insertAce(acl.entries.size, it, owner, true) }
        aclService.updateAcl(acl)
    }

    override fun getPermissionsForCurrentUser(entity: BusinessEntity): List<Permission> {
        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities.mapNotNull { it.authority }
        val sids = authorities.map { GrantedAuthoritySid(it) }.plus(PrincipalSid(authentication.name))
        val acl = aclService.readAclById(objectIdentity(entity))
        return filter(entity, sids.flatMap { getPermissionsForSid(it, acl) })
    }


    override fun getPermissionsForUsers(
        entities: List<BusinessEntity>,
        users: List<Principal>
    ): List<UserPermissions> {
        if (entities.isEmpty()) {
            return emptyList()
        }
        val sidList: List<PrincipalSid> = users.map { PrincipalSid(it.name) }.toList()
        val objectIdentities = entities.map { objectIdentity(it) }.toList()

        val aclMap = aclService.readAclsById(objectIdentities, sidList)
        users.associateBy({ it.name }, { ArrayList<BusinessEntityPermissions>() })
        val userPermissionsMap: MutableMap<String, MutableList<BusinessEntityPermissions>> =
            users
                .associateBy({ it.name }, { ArrayList<BusinessEntityPermissions>() })
                .toMutableMap()

        aclMap
            .filter { (objectIdentity, _) -> entities.any { it.isSame(objectIdentity) } }
            .forEach { (objectIdentity, acl) ->
                run {
                    val entity = BusinessEntityImpl(objectIdentity)
                    sidList.forEach { sid: PrincipalSid ->
                        userPermissionsMap.computeIfAbsent(sid.principal) { ArrayList() }
                            .add(
                                BusinessEntityPermissions(
                                    entity,
                                    filter(entity, getPermissionsForSid(sid, acl).toList())
                                )
                            )
                    }
                }
            }

        return userPermissionsMap.entries.map { UserPermissions(it.key, it.value) }
    }

    private fun filter(entity: BusinessEntity, permissions: List<Permission>): List<Permission> {
        var filteredPermissions = permissions
        for (permissionFilter in permissionFilters) {
            if (permissionFilter.isApplicable(entity)) {
                filteredPermissions = permissionFilter.filter(filteredPermissions)
            }
        }
        return filteredPermissions
    }

    /**
     * Возвращает все permissions указанного пользователя на указанный объект с учетом наследования
     */
    private fun getPermissionsForSid(sid: Sid, acl: Acl): Set<Permission> {
        val permissions = acl.entries.filter { it.sid.equals(sid) }
            .filter { it.isGranting }
            .map { it.permission }.toHashSet()
        return if (acl.parentAcl == null || !acl.isEntriesInheriting) permissions
        else permissions.plus(getPermissionsForSid(sid, acl.parentAcl))
    }

    override fun isGrantedPermission(entity: BusinessEntity, permission: Permission, user: Principal): Boolean {
        return permissionExist(aclService.readAclById(objectIdentity(entity)), permission, PrincipalSid(user.name))
    }

    private fun isGrantedPermissionForRole(
        entity: BusinessEntity,
        permission: Permission,
        role: GrantedAuthority
    ): Boolean {
        return permissionExist(
            aclService.readAclById(objectIdentity(entity)),
            permission,
            GrantedAuthoritySid(role.authority)
        )
    }

    override fun isGrantedPermissionForCurrentUser(entity: BusinessEntity, permission: Permission): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (isGrantedPermission(entity, permission, authentication)) {
            true
        } else if (authentication.authorities != null && !authentication.authorities.isEmpty()) {
            authentication.authorities.any { isGrantedPermissionForRole(entity, permission, it) }
        } else {
            false
        }
    }

    override fun getUsersByEntity(entity: BusinessEntity, permissions: List<Permission>): Set<String> {
        val permissionsSet = permissions.toHashSet()
        var acl = aclService.readAclById(objectIdentity(entity))
        val sids = HashSet<String>()
        while (acl != null) {
            sids.addAll(acl.entries
                .asSequence()
                .filter { it.isGranting }
                .filter { permissionsSet.contains(it.permission) }
                .map { it.sid }
                .filterIsInstance<PrincipalSid>()
                .map { it.principal })
            if (acl.isEntriesInheriting) acl = acl.parentAcl
            else return sids
        }
        return sids
    }

    override fun getPermissionsNameListForCurrentUser(entity: BusinessEntity): MutableSet<String> {
        return permissionNameEvaluator.getNames(getPermissionsForCurrentUser(entity))
    }

    private fun permissionExist(acl: Acl, permission: Permission, sid: Sid) = try {
        acl.isGranted(listOf(permission), listOf(sid), false)
    } catch (ex: NotFoundException) {
        false
    }

    private fun objectIdentity(entity: BusinessEntity) = ObjectIdentityImpl(entity.type, entity.getBusinessId())
}

