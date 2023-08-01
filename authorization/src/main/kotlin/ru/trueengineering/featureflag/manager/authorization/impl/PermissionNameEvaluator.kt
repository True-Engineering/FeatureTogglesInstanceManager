package ru.trueengineering.featureflag.manager.authorization.impl

import org.slf4j.LoggerFactory
import org.springframework.security.acls.model.Permission
import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import java.lang.reflect.Field

@Component
class PermissionNameEvaluator {

    private val log = LoggerFactory.getLogger(javaClass)

    private val registeredPermissionsName: MutableMap<CustomPermission, String> = HashMap()

    init {
        val clazz = CustomPermission::class.java
        val fields: Array<Field> = clazz.fields
        for (field in fields) {
            try {
                val fieldValue = field[null]
                if (Permission::class.java.isAssignableFrom(fieldValue.javaClass)) {
                    val perm = fieldValue as CustomPermission
                    val permissionName = field.name
                    registeredPermissionsName[perm] = permissionName
                }
            } catch (e: Exception) {
                log.warn("Unable to load permissions!", e)
            }
        }
    }

    fun getName(permission: Permission): String? {
        return this.registeredPermissionsName[permission]
    }

    fun getNames(permissions: Iterable<Permission>): MutableSet<String> {
        return permissions.mapNotNull(this::getName).toMutableSet()
    }
}