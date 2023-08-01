package ru.trueengineering.featureflag.manager.core.impl.environment

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEventHandler
import ru.trueengineering.featureflag.manager.core.domen.event.EnvironmentCreatedEvent
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

@Service
class EnvironmentEventHandler(
    private val repository: BusinessEntityRepository,
    private val environmentRepository: EnvironmentRepository,
    private val permissionService: IPermissionService
) : DomainEventHandler<EnvironmentCreatedEvent> {

    private final val log = LoggerFactory.getLogger(javaClass)

    @WithAdminRole
    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    override fun handle(event: EnvironmentCreatedEvent) {
        log.debug(
            "Handle new event - has created environment with name ${event.environmentName} " +
                    "in project with id - ${event.projectBusinessEntity.getBusinessId()}"
        )
        val environment = environmentRepository.getByProjectIdAndName(event.projectId, event.environmentName)
            ?: throw ServiceException(
                ErrorCode.ENVIRONMENT_NOT_FOUND, "Unable to create acl, " +
                        "environment with name ${event.environmentName} is not found in project with id ${event.projectId}!"
            )
        repository.createBusinessEntity(environment, event.projectBusinessEntity)
        permissionService.grantPermissionsForCurrentUser(environment, CustomRole.ADMIN.environmentPermissions.toList())
        permissionService.grantPermissionsForOwner(environment, CustomRole.ADMIN.environmentPermissions.toList())
    }
}
