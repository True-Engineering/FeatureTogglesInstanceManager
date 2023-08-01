package ru.trueengineering.featureflag.manager.core.domen.organization

import org.springframework.security.access.prepost.PreAuthorize

interface FetchMembersForOrganizationUseCase {

    @PreAuthorize("hasPermission(#query.organizationId, " +
            "'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'READ_MEMBERS')")
    fun search(query: FetchMembersForOrganizationQuery): List<OrganizationUser>

    @PreAuthorize("hasPermission(#query.organizationId, " +
            "'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'READ_MEMBERS')")
    fun searchMembersCount(query: FetchMembersForOrganizationQuery): Int?

}

data class FetchMembersForOrganizationQuery(val organizationId: Long)