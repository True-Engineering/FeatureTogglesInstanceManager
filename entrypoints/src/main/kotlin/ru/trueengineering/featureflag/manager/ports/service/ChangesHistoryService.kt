package ru.trueengineering.featureflag.manager.ports.service

import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryCommand
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryUseCase
import ru.trueengineering.featureflag.manager.ports.rest.controller.ChangesHistoryResponseDto
import ru.trueengineering.featureflag.manager.ports.service.mapper.ChangesHistoryMapper


@Service
class ChangesHistoryService(
    private val getProjectChangesHistoryUseCase: GetProjectChangesHistoryUseCase,
    private val mapper: ChangesHistoryMapper
) {
    fun getProjectChangesHistory(command: GetProjectChangesHistoryCommand): ChangesHistoryResponseDto {
        val page = getProjectChangesHistoryUseCase.execute(command).apply {
            map { it.featureFlag.environments = mutableListOf() }
        }

        return ChangesHistoryResponseDto(
            changesHistory = page.map(mapper::convertToDto).toList(),
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            pageSize = page.size
        )
    }
}
