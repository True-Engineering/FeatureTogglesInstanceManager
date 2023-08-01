package ru.trueengineering.featureflag.manager.auth

import org.springframework.security.acls.model.ObjectIdentity
import java.io.Serializable

interface BusinessEntity : ObjectIdentity {

    /**
     * Primary ID of entity
     */
    fun getBusinessId(): Serializable?;

    /**
     * Имя класса
     */
    override fun getType(): String;

    override fun getIdentifier(): Serializable = getBusinessId()!!

    fun isSame(objectIdentity: ObjectIdentity): Boolean {
        return identifier == objectIdentity.identifier && type == objectIdentity.type
    }

    fun isSame(businessEntity: BusinessEntity): Boolean {
        return isSame(businessEntity as ObjectIdentity)
    }

}

