package ru.trueengineering.featureflag.manager.core.impl.organization

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEventHandler
import ru.trueengineering.featureflag.manager.core.domen.event.OrganizationCreatedEvent
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

@Service
class OrganizationEventHandler(
    private val repository: BusinessEntityRepository,
    private val organizationRepository: OrganizationRepository,
    private val permissionService: IPermissionService
) : DomainEventHandler<OrganizationCreatedEvent> {

    private final val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRED)
    override fun handle(event: OrganizationCreatedEvent) {
        log.debug("Handle new event - has created organization with name ${event.organizationName} ")
        val organization = organizationRepository.findByName(event.organizationName) ?: throw ServiceException(
            ErrorCode.ORGANIZATION_NOT_FOUND, "Unable to create acl, " +
                    "organization with name ${event.organizationName} is not found!"
        )
        repository.createBusinessEntity(organization)
        val permissions = CustomRole.ADMIN.organizationPermissions
            .plus(CustomRole.ADMIN.projectPermissions)
            .plus(CustomRole.ADMIN.environmentPermissions)
            .toList()
        permissionService.grantPermissionsForCurrentUser(organization, permissions)
        permissionService.grantPermissionsForOwner(organization, permissions)
    }
}
