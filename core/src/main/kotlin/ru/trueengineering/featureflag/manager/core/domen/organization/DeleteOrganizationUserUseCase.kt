package ru.trueengineering.featureflag.manager.core.domen.organization

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteOrganizationUserUseCase {

    @PreAuthorize("hasPermission(#command.organizationId, " +
            "'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'EDIT_MEMBERS')")
    fun execute(command: DeleteOrganizationUserCommand)

}

data class DeleteOrganizationUserCommand(val userId: Long, val organizationId: Long)