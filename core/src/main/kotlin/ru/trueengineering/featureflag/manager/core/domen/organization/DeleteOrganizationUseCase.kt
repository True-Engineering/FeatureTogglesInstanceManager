package ru.trueengineering.featureflag.manager.core.domen.organization

import org.springframework.security.access.prepost.PreAuthorize

interface DeleteOrganizationUseCase {

    @PreAuthorize("hasPermission(#command?.organizationId, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'DELETE')")
    fun execute(command: DeleteOrganizationCommand)

}

data class DeleteOrganizationCommand(val organizationId: Long)