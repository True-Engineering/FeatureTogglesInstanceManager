package ru.trueengineering.featureflag.manager.ports.service

import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.environment.AddNewEnvironmentToProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsForProject
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsOfProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FindByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.SearchEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentUseCase
import ru.trueengineering.featureflag.manager.ports.rest.controller.GetCompareEnvironmentsStateResponse
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateUseCase
import ru.trueengineering.featureflag.manager.ports.service.mapper.EnvironmentMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.FeatureFlagMapper

@Service
class EnvironmentService(
    private val environmentMapper: EnvironmentMapper,
    private val featureMapper: FeatureFlagMapper,
    private val deleteUseCase: DeleteEnvironmentUseCase,
    private val deleteInstanceUseCase: DeleteInstanceUseCase,
    private val createUseCase: AddNewEnvironmentToProjectUseCase,
    private val updateUseCase: UpdateEnvironmentUseCase,
    private val fetchAllUseCase: FetchAllEnvironmentsOfProjectUseCase,
    private val createTokenUseCase: CreateEnvironmentTokenUseCase,
    private val searchEnvironmentUseCase: SearchEnvironmentUseCase,
    private val getCompareEnvironmentsStateUseCase: GetCompareEnvironmentsStateUseCase,
    private val updateFlagsStateUseCase: UpdateFlagsStateUseCase,
    private val freezeEnvironmentUseCase: FreezeEnvironmentUseCase,
    private val unfreezeEnvironmentUseCase: UnfreezeEnvironmentUseCase
) {

    fun create(command: CreateEnvironmentCommand) = environmentMapper.convertToDto(createUseCase.execute(command))

    fun createToken(command: CreateEnvironmentTokenCommand) = createTokenUseCase.execute(command)

    fun update(command: UpdateEnvironmentCommand) = environmentMapper.convertToDto(updateUseCase.execute(command))

    fun delete(command: DeleteEnvironmentCommand) = deleteUseCase.execute(command)

    fun deleteInstance(command: DeleteInstanceCommand) = deleteInstanceUseCase.execute(command)

    fun searchById(environmentId: Long) =
            environmentMapper.convertToDto(searchEnvironmentUseCase.search(FindByIdQuery(environmentId)))

    fun findAllForProject(query: FetchAllEnvironmentsForProject) =
        environmentMapper.convertToDtoList(fetchAllUseCase.search(query))

    fun getCompareEnvironmentsState(command: GetCompareEnvironmentsStateCommand): GetCompareEnvironmentsStateResponse {
        val compareLists = getCompareEnvironmentsStateUseCase.execute(command)
        return GetCompareEnvironmentsStateResponse(
            featureMapper.convertToDtoList(compareLists.enable),
            featureMapper.convertToDtoList(compareLists.disable))
    }

    fun updateFlagsState(command: UpdateFlagsStateCommand) = updateFlagsStateUseCase.execute(command)

    fun freezeEnvironment(command: FreezeEnvironmentCommand) =
        environmentMapper.convertToDto(freezeEnvironmentUseCase.execute(command))

    fun unfreezeEnvironment(command: UnfreezeEnvironmentCommand) =
        environmentMapper.convertToDto(unfreezeEnvironmentUseCase.execute(command))
}