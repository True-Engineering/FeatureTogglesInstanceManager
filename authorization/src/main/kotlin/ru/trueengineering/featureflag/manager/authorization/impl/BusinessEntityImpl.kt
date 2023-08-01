package ru.trueengineering.featureflag.manager.authorization.impl

import org.springframework.security.acls.model.ObjectIdentity
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import java.io.Serializable

data class BusinessEntityImpl(private val id: Serializable?, private val type: String) : BusinessEntity {

    constructor(objectIdentity: ObjectIdentity) : this(objectIdentity.identifier, objectIdentity.type)

    constructor(businessEntity: BusinessEntity) : this(businessEntity.getBusinessId(), businessEntity.getType())

    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = type
}