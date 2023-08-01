package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize

interface SearchEnvironmentUseCase {

    @PreAuthorize("hasPermission(#findByIdQuery.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'READ_ENVIRONMENT')" +
            "|| hasPermission(#findByIdQuery.environmentId, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT')")
    fun search(findByIdQuery: FindByIdQuery): Environment

}

data class FindByIdQuery(val environmentId: Long)