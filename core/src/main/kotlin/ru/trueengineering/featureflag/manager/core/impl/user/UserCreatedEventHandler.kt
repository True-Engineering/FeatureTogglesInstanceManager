package ru.trueengineering.featureflag.manager.core.impl.user

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEventHandler
import ru.trueengineering.featureflag.manager.core.domen.event.UserCreatedEvent
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import java.util.Optional

@Component
open class UserCreatedEventHandler(
    @Autowired private val permissionService: IPermissionService,
    @Autowired private val organizationRepository: OrganizationRepository
): DomainEventHandler<UserCreatedEvent> {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${featureFlag.portal.default.organization.name}")
    private lateinit var defaultOrganizationName: Optional<String>;

    @WithAdminRole
    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    override fun handle(event: UserCreatedEvent) {
        if (defaultOrganizationName.isEmpty) {
            log.debug(
                "Handle new event - has created user with mail ${event.userEmail}, " +
                        "because of defaultOrganizationName is empty it not will be granted any permissions to user"
            )
            return
        }
        log.debug(
            "Handle new event - has created user with mail ${event.userEmail}, " +
                    "it will be granted READ_ORGANIZATION permission into - ${defaultOrganizationName.get()}"
        )
        organizationRepository.findByName(defaultOrganizationName.get())?.also {
            permissionService.grantPermissionForUser(it, CustomPermission.READ_ORGANIZATION) { event.userEmail }
        }
    }
}