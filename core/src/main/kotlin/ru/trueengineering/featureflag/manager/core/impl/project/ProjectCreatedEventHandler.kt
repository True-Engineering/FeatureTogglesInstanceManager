package ru.trueengineering.featureflag.manager.core.impl.project

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.BusinessEntityRepository
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEventHandler
import ru.trueengineering.featureflag.manager.core.domen.event.ProjectCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

@Component
class ProjectCreatedEventHandler(
        @Autowired private val businessEntityRepository: BusinessEntityRepository,
        @Autowired private val projectRepository: ProjectRepository,
        @Autowired private val permissionService: IPermissionService,
        @Autowired private val fetchCurrentUserUseCase: FetchCurrentUserUseCase
) : DomainEventHandler<ProjectCreatedEvent> {

    private final val log = LoggerFactory.getLogger(javaClass)

    @WithAdminRole
    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    override fun handle(event: ProjectCreatedEvent) {
        log.debug(
                "Handle new event - has created project with name ${event.projectName} " +
                        "in organization with id - ${event.organizationId}"
        )
        val project = projectRepository.findByNameOrNull(event.projectName)
                ?: throw ServiceException(ErrorCode.PROJECT_NOT_FOUND, "Project not found by name - '${event.projectName}'")

        businessEntityRepository.createBusinessEntity(project, event.organizationBusinessEntity)

        val permissions = CustomRole.ADMIN.projectPermissions
            .plus(CustomRole.ADMIN.environmentPermissions)
            .toList()
        if (fetchCurrentUserUseCase.search().authorities?.contains("FEATURE_FLAGS_ADMIN") == true) {
            permissionService.grantPermissionsForCurrentUser(project, permissions)
        }
        permissionService.grantPermissionsForOwner(project, permissions)
    }

}