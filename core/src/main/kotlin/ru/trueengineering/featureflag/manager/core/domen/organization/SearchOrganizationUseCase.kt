package ru.trueengineering.featureflag.manager.core.domen.organization

import org.springframework.security.access.prepost.PreAuthorize

interface SearchOrganizationUseCase {

    @PreAuthorize("hasPermission(#query?.organizationId, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'READ_ORGANIZATION')" +
            "|| hasPermission(#query?.organizationId, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'EDIT')")
    fun search(query: SearchOrganizationByIdQuery) : Organization

}

data class SearchOrganizationByIdQuery(val organizationId: Long)