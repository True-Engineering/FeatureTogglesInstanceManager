package ru.trueengineering.featureflag.manager.core.impl.organization

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.event.OrganizationCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

internal class OrganizationEventHandlerTest {

    private val repository: BusinessEntityRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val permissionService: IPermissionService = mockk()

    private val uut = OrganizationEventHandler(repository, organizationRepository, permissionService)

    private val allPermission = listOf(
        CustomPermission.READ_ORGANIZATION,
        CustomPermission.READ_PROJECT,
        CustomPermission.READ_ENVIRONMENT,
        CustomPermission.EDIT,
        CustomPermission.DELETE,
        CustomPermission.CREATE_PROJECT,
        CustomPermission.CREATE_FLAG,
        CustomPermission.CREATE_ENV,
        CustomPermission.READ_MEMBERS,
        CustomPermission.EDIT_MEMBERS,
        CustomPermission.DELETE_FLAG,
        CustomPermission.UPLOAD_ENVIRONMENT
    )

    @Test
    fun handle() {
        val organizationCreatedEvent = OrganizationCreatedEvent("org")
        val organization = Organization(1, "org")
        val slotFirst = slot<List<CustomPermission>>()
        val slotSecond = slot<List<CustomPermission>>()
        every { organizationRepository.findByName("org") } returns organization
        every { repository.createBusinessEntity(organization) } just Runs
        every { permissionService.grantPermissionsForCurrentUser(organization, capture(slotFirst)) } just Runs
        every { permissionService.grantPermissionsForOwner(organization, capture(slotSecond)) } just Runs
        uut.handle(organizationCreatedEvent)

        assertThat(slotFirst.captured).containsExactlyInAnyOrderElementsOf(allPermission)
        assertThat(slotSecond.captured).containsExactlyInAnyOrderElementsOf(allPermission)
    }

    @Test
    fun handleThrow() {
        val organizationCreatedEvent = OrganizationCreatedEvent("org")
        every { organizationRepository.findByName("org") } returns null

        val actualException =
            org.junit.jupiter.api.assertThrows<ServiceException> { uut.handle(organizationCreatedEvent) }
        kotlin.test.assertEquals(actualException.errorCode, ErrorCode.ORGANIZATION_NOT_FOUND)
        kotlin.test.assertEquals(
            actualException.errorMessage, "Unable to create acl, " +
                    "organization with name org is not found!"
        )
    }
}