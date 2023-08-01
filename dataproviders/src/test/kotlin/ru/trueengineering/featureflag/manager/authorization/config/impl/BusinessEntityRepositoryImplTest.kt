package ru.trueengineering.featureflag.manager.authorization.config.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.acls.domain.AclAuthorizationStrategy
import org.springframework.security.acls.domain.AclImpl
import org.springframework.security.acls.domain.AuditLogger
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.MutableAclService
import org.springframework.security.acls.model.ObjectIdentity
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.authorization.impl.BusinessEntityRepositoryImpl
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project

internal class BusinessEntityRepositoryImplTest {

    private val aclService: MutableAclService = mockk()

    private val uut = BusinessEntityRepositoryImpl(aclService)

    @Test
    fun createBusinessEntity() {

        val entity = Organization(1, "org")
        val objectIdentityCapture = slot<ObjectIdentity>()
        val authorizationStrategy = mockkClass(AclAuthorizationStrategy::class)
        every { authorizationStrategy.securityCheck(any(), any()) } just Runs
        val aclImpl = AclImpl(mockkClass(ObjectIdentity::class), 1, authorizationStrategy, mockkClass(AuditLogger::class))
        every { aclService.createAcl(capture(objectIdentityCapture)) } returns aclImpl
        every { aclService.updateAcl(aclImpl) } returns aclImpl

        uut.createBusinessEntity(entity)

        val capture = objectIdentityCapture.captured
        assertEquals(1L, capture.identifier)
        assertEquals(Organization::class.java.name, capture.type)
        assertEquals(GrantedAuthoritySid(ADMIN_ROLE), aclImpl.owner)
    }

    @Test
    fun createBusinessEntityWithParents() {

        val parentEntity = Organization(1, "org")
        val entity = Project(1, "org")
        val parentObjectIdentity = ObjectIdentityImpl(Organization::class.java.name, 1L)
        val objectIdentityCapture = slot<ObjectIdentity>()
        val authorizationStrategy = mockkClass(AclAuthorizationStrategy::class)
        every { authorizationStrategy.securityCheck(any(), any()) } just Runs
        val aclImpl = AclImpl(mockkClass(ObjectIdentity::class), 1, authorizationStrategy, mockkClass(AuditLogger::class))
        val parentAclImpl = AclImpl(mockkClass(ObjectIdentity::class), 1, authorizationStrategy, mockkClass(AuditLogger::class))
        every { aclService.createAcl(capture(objectIdentityCapture)) } returns aclImpl
        every { aclService.updateAcl(aclImpl) } returns aclImpl
        every { aclService.readAclById(eq(parentObjectIdentity)) } returns parentAclImpl

        uut.createBusinessEntity(entity, parentEntity)

        val capture = objectIdentityCapture.captured
        assertEquals(1L, capture.identifier)
        assertEquals(Project::class.java.name, capture.type)
        assertEquals(GrantedAuthoritySid(ADMIN_ROLE), aclImpl.owner)
        assertEquals(aclImpl.parentAcl, parentAclImpl)
    }
}