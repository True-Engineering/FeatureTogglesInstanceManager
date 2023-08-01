package ru.trueengineering.featureflag.manager.authorization.impl

import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.MutableAclService
import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository

@Service
class BusinessEntityRepositoryImpl(
    private val aclService: MutableAclService
): BusinessEntityRepository {

    override fun createBusinessEntity(entity: BusinessEntity) {
        val baseAcl = createBaseAcl(entity)
        aclService.updateAcl(baseAcl)

    }

    override fun createBusinessEntity(entity: BusinessEntity, parent: BusinessEntity) {
        val baseAcl = createBaseAcl(entity)
        val parentAcl = aclService.readAclById(ObjectIdentityImpl(parent.getType(), parent.getBusinessId()))
        baseAcl.setParent(parentAcl)
        aclService.updateAcl(baseAcl)

    }

    private fun createBaseAcl(entity: BusinessEntity): MutableAcl {
        val acl = aclService.createAcl(ObjectIdentityImpl(entity.getType(), entity.getBusinessId()))
        acl.owner = GrantedAuthoritySid(ADMIN_ROLE)
        acl.isEntriesInheriting = true
        return acl
    }
}