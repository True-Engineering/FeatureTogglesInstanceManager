package ru.trueengineering.featureflag.manager.core.domen.organization

import org.springframework.security.access.prepost.PreAuthorize

interface CreateOrganizationUseCase {

    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun execute(command: CreateOrganizationCommand) : Organization

}

data class CreateOrganizationCommand(val name: String)