package ru.trueengineering.featureflag.manager.auth

import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.Permission
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import java.security.Principal

interface IPermissionService {

    /**
     * Идемпотентное добавление указанных прав на данный доменный объект указанному пользователю
     *
     * @param entity        бизнес сущность
     * @param permissions   новое разрешение для пользователя, которые нужно добавить
     * @param user          пользователь
     */
    fun grantPermissionsForUser(entity: BusinessEntity, permissions: List<Permission>, user: Principal)

    /**
     * Идемпотентное добавление указанных прав на данный доменный объект текущему пользователю
     *
     * @param entity        бизнес сущность
     * @param permissions   новое разрешение для пользователя, которые нужно добавить
     */
    fun grantPermissionsForCurrentUser(entity: BusinessEntity, permissions: List<Permission>)

    /**
     * Идемпотентное добавление указанного права на данный доменный объект указанному пользователю
     *
     * @param entity        бизнес сущность
     * @param permission    новое разрешение для пользователя, которые нужно добавить
     * @param user          пользователь
     */
    fun grantPermissionForUser(entity: BusinessEntity, permission: Permission, user: Principal) =
        grantPermissionsForUser(entity, listOf(permission), user)

    /**
     * Идемпотентное добавление указанного права на данный доменный объект текущему пользователю
     *
     * @param entity        бизнес сущность
     * @param permission    новое разрешение для пользователя, которые нужно добавить
     */
    fun grantPermissionForCurrentUser(entity: BusinessEntity, permission: Permission) =
        grantPermissionsForCurrentUser(entity, listOf(permission))


    /**
     * Идемпотентное удаление всех прав, привязанных к данной бизнес сущности.
     *
     * @param entity        бизнес сущность
     * @param user          пользователь
     */
    fun clearPermissionsForUser(entity: BusinessEntity, user: Principal)



    /**
     * Получение прав указанного пользователя для переданного списка доменных объектов
     *
     * @implNote            Возвращает только permissions, назначенные непосредственно на пользователя,
     * permissions роли не будут возвращены
     * @param entities      бизнес сущности
     * @param user          пользователь
     */
    fun getPermissionsForUser(entities: List<BusinessEntity>, user: Principal): UserPermissions

    /**
     * Получение прав указанного пользователя для переданного доменного объекта
     *
     * @implNote            Возвращает только permissions, назначенные непосредственно на пользователя,
     * permissions роли не будут возвращены
     * @param entity      бизнес сущность
     * @param user          пользователь
     */
    fun getPermissionsForUser(entity: BusinessEntity, user: Principal): List<Permission>

    /**
     * Определение роли пользователя в рамках бизнес сущности
     * @param entity      бизнес сущность
     * @param user          пользователь
     */
    fun getUserRoleForEntity(entity: BusinessEntity, user: Principal): CustomRole

    /**
     * Определение роли текущего пользователя в рамках бизнес сущности
     * @param entity      бизнес сущность
     */
    fun getUserRoleForEntity(entity: BusinessEntity): CustomRole

    /**
     * Предоставление прав владельцу ресурса
     * @param entity        бизнес сущности
     * @param permissions   разрешение, которое нужно добавить
     */
    fun grantPermissionsForOwner(entity: BusinessEntity, permissions: List<Permission>)

    /**
     * Получение прав текущего пользователя для доменного объекта с учетом наследования
     *
     * @implNote          Возвращает все permissions, включая permissions роли
     * @param entity      бизнес сущность
     */
    fun getPermissionsForCurrentUser(entity: BusinessEntity): List<Permission>

    /**
     * Получение прав для указанных пользователей для переданного списка доменных объектов с учетом наследования.
     *
     * @implNote            Возвращает только permissions, назначенные непосредственно на пользователя,
     * permissions роли не будут возвращены
     * @param entities      бизнес сущности
     * @param users         пользователи
     */
    fun getPermissionsForUsers(entities: List<BusinessEntity>, users: List<Principal>): List<UserPermissions>

    /**
     * Проверяем наличие указанного права у пользователя
     * @param entity        бизнес сущность
     * @param permission    право
     * @param user          пользователь
     */
    fun isGrantedPermission(entity: BusinessEntity, permission: Permission, user: Principal): Boolean

    /**
     * Проверяем наличие указанного права у текущего пользователя
     * @param entity        бизнес сущность
     * @param permission    право
     */
    fun isGrantedPermissionForCurrentUser(entity: BusinessEntity, permission: Permission): Boolean

    /**
     * Возвращает список пользователей, имеющих указанные права на указанный объект с учетом наследования
     * Список содержит sid, т.е. может включать как email, так и роли
     * @param entity        бизнес сущность
     * @param permissions   список прав
     */
    fun getUsersByEntity(entity: BusinessEntity, permissions: List<Permission>): Set<String>

    /**
     * Возвращает список названий прав текущего пользователя для переданного доменного объекта
     *
     * @implNote            Возвращает все permissions, включая permissions роли
     * @param entity      бизнес сущность
     */
    fun getPermissionsNameListForCurrentUser(entity: BusinessEntity) : MutableSet<String>

}

data class UserPermissions(
    val userName: String,
    val permissions: List<BusinessEntityPermissions>
) {
    fun findByObjectIdentity(entity: ObjectIdentity): BusinessEntityPermissions? {
        return permissions.firstOrNull{ it.entity.isSame(entity) }
    }
}

data class BusinessEntityPermissions(
    val entity: BusinessEntity,
    val permissions: List<Permission>
)