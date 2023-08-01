package ru.trueengineering.featureflag.manager.core.impl.user

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.annotation.AspectUtils
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationQuery
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationUserCase
import ru.trueengineering.featureflag.manager.core.domen.user.Invitation
import ru.trueengineering.featureflag.manager.core.domen.user.InviteUserToProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.InviteUserToProjectUseCase
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import java.util.Optional
import java.util.UUID

open class InvitationFacade(
    private val invitationRepository: InvitationRepository,
    private val permissionService: IPermissionService,
    private val fetchCurrentUserUseCase: FetchCurrentUserUseCase,
    private val organizationRepository: OrganizationRepository
) :
    FetchInvitationUserCase,
    InviteUserToProjectUseCase {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Value("\${featureFlag.portal.default.organization.name}")
    private lateinit var defaultOrganizationName: Optional<String>

    @Transactional
    override fun search(query: FetchInvitationQuery): Invitation {
        return invitationRepository.findByProject(query.projectId) ?: createInvitation(query)
    }

    @Transactional
    override fun execute(command: InviteUserToProjectCommand) {
        val invitation = invitationRepository.findById(command.invitationUid)
            ?: throw ServiceException(
                ErrorCode.INVITATION_NOT_FOUND,
                "Invitation with id \"${command.invitationUid}\" not found or expired"
            )

        val organization = organizationRepository.findByName(defaultOrganizationName.get())
        val project = invitation.project
        val user = fetchCurrentUserUseCase.search()
        return if (permissionService.isGrantedPermissionForCurrentUser(project, CustomPermission.READ_PROJECT)
            || permissionService.isGrantedPermissionForCurrentUser(project, CustomPermission.EDIT)
        ) {
            log.debug(
                "User ${user.userName} already has permission for reading project ${project.name}. " +
                        "No invitation needed"
            )
        } else if (permissionService.isGrantedPermissionForCurrentUser(project, CustomPermission.PENDING_APPROVE)) {
            log.debug(
                "User ${user.userName} already is pending approve to project ${project.name}. " +
                        "No invitation needed"
            )
        } else {
            log.debug("Grant to user ${user.userName} permission PENDING_APPROVE")
            AspectUtils.executeWithAdminRole {
                permissionService.grantPermissionForUser(project, CustomPermission.PENDING_APPROVE, user)

                if (organization != null) {
                    permissionService.grantPermissionForUser(organization, CustomPermission.READ_ORGANIZATION, user)
                }
            }
            return
        }
    }

    private fun createInvitation(query: FetchInvitationQuery): Invitation {
        return try {
            invitationRepository.create(UUID.randomUUID(), query.projectId)
        } catch (e: Exception) {
            invitationRepository.findByProject(query.projectId)!!
        }
    }
}