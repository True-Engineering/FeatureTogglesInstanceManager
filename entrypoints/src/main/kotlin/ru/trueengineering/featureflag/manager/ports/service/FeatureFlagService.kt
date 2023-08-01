package ru.trueengineering.featureflag.manager.ports.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.trueengineering.featureflag.manager.core.domen.toggle.CopyDirection
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagProperties
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsUseCase
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.ports.rest.controller.AgentFeatureFlagResponseDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagFilterResponseDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportChangesDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportEnvironmentsDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.TagFilterResponseDto
import ru.trueengineering.featureflag.manager.ports.service.casheService.CacheService
import ru.trueengineering.featureflag.manager.ports.service.casheService.FeatureFlagList
import ru.trueengineering.featureflag.manager.ports.service.mapper.AgentFeatureFlagMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.FeatureFlagMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ImportChangesMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ImportEnvironmentsMapper

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
class FeatureFlagService(
    private val objectMapper: ObjectMapper,
    private val featureFlagMapper: FeatureFlagMapper,
    private val importChangesMapper: ImportChangesMapper,
    private val importEnvironmentsMapper: ImportEnvironmentsMapper,
    private val agentFeatureFlagMapper: AgentFeatureFlagMapper,
    private val createFeatureFlagUseCase: CreateFeatureFlagUseCase,
    private val editFeatureFlagUseCase: EditFeatureFlagUseCase,
    private val fetchAllFeatureFlagsForProjectUseCase: FetchAllFeatureFlagsForProjectUseCase,
    private val fetchAllFeatureFlagsForAgentUseCase: FetchAllFeatureFlagsForAgentUseCase,
    private val enableFeatureFlagUseCase: EnableFeatureFlagUseCase,
    private val disableFeatureFlagUseCase: DisableFeatureFlagUseCase,
    private val deleteFeatureFlagUseCase: DeleteFeatureFlagUseCase,
    private val editFeatureFlagStrategyUseCase: EditFeatureFlagStrategyUseCase,
    private val synchronizePortalsUseCase: SynchronizePortalsUseCase,
    private val synchronizeEnvironmentsUseCase: SynchronizeEnvironmentsUseCase,
    private val getImportChangesUseCase: GetImportChangesUseCase,
    private val getImportEnvironmentsUseCase: GetImportEnvironmentsUseCase,
    private val fetchAllEnabledFeatureFlagsForEnvironmentUseCase: FetchAllEnabledFeatureFlagsForEnvironmentUseCase,
    private val findFeatureFlagByPatternUseCase: FindFeatureFlagByPatternUseCase,
    private val fetchAllFeatureFlagsTagsUseCase: FetchAllFeatureFlagsTagsUseCase,
    private val cacheService: CacheService
) {

    fun fetchAll(projectId: Long) =
        featureFlagMapper.convertToDtoList(
            fetchAllFeatureFlagsForProjectUseCase.search(FetchAllFeatureFlagsForProjectQuery(projectId))
        )

    fun createFlag(
        uuid: String,
        projectId: Long,
        description: String?,
        group: String?,
        type: FeatureFlagType?,
        sprint: String?,
        tags: Set<String>,
        properties: FeatureFlagProperties
    ): FeatureFlagDto {
        return featureFlagMapper.convertToDto(
            createFeatureFlagUseCase.execute(
                CreateFeatureFlagCommand(
                    uuid, projectId, description, group, type, sprint, tags, properties
                )
            )
        )
    }

    fun editFlag(
        uuid: String,
        projectId: Long,
        description: String?,
        group: String?,
        type: FeatureFlagType?,
        sprint: String?,
        tags: Set<String>,
        properties: FeatureFlagProperties
    ): FeatureFlagDto {
        return featureFlagMapper.convertToDto(
            editFeatureFlagUseCase.execute(
                EditFeatureFlagCommand(
                    uuid, projectId, description, group, type, sprint, tags, properties
                )
            )
        )
    }

    fun enableFlag(uuid: String, projectId: Long, environmentId: Long) =
        enableFeatureFlagUseCase.execute(EnableFeatureFlagCommand(uuid, projectId, environmentId))

    fun disableFlag(uuid: String, projectId: Long, environmentId: Long) =
        disableFeatureFlagUseCase.execute(DisableFeatureFlagCommand(uuid, projectId, environmentId))

    fun deleteFeatureFlag(uuid: String, projectId: Long) =
        deleteFeatureFlagUseCase.execute(DeleteFeatureFlagCommand(uuid, projectId))

    fun getAgentFeatureFlags(
        token: String,
        agentName: String,
        featureFlagHash: String?
    ): AgentFeatureFlagResponseDto {

        val featureFlagsWithUpdateStatus = fetchAllFeatureFlagsForAgentUseCase.search(
            FetchAllFeatureFlagsForAgentQuery(token, agentName, featureFlagHash)
        )

        val featureFlagDtos = agentFeatureFlagMapper.convertToDtoList(featureFlagsWithUpdateStatus.featureFlags)

        return AgentFeatureFlagResponseDto(featureFlagDtos, featureFlagsWithUpdateStatus.update)

    }

    fun updateStrategy(
        uuid: String,
        projectId: Long,
        environmentId: Long,
        type: String?,
        initParams: MutableMap<String, String>?
    ): FeatureFlagDto {
        return featureFlagMapper.convertToDto(
            editFeatureFlagStrategyUseCase.execute(
                EditFeatureFlagStrategyCommand(
                    uuid,
                    projectId,
                    environmentId,
                    type,
                    initParams
                )
            )
        )
    }

    fun getFeatureFlagResourceForProject(projectId: Long): Resource {
        val featureFlags = fetchAllFeatureFlagsForProjectUseCase.search(
            FetchAllFeatureFlagsForProjectQuery(projectId)
        )
        val responseDto = featureFlagMapper.convertToDtoList(featureFlags)
        return ByteArrayResource(objectMapper.writeValueAsBytes(responseDto))
    }

    fun getImportEnvironments(projectId: Long, file: MultipartFile): ImportEnvironmentsDto {
        val featureFlags = getFeatureFlagsFromFile(file)
        return importEnvironmentsMapper.convertToDto(
            getImportEnvironmentsUseCase.execute(
                GetImportEnvironmentsCommand(
                    cacheService.getKey(file),
                    projectId,
                    featureFlags
                )
            )
        )
    }

    fun getImportChanges(projectId: Long, file: MultipartFile): ImportChangesDto {
        val featureFlags = getFeatureFlagsFromFile(file)
        return importChangesMapper.convertToDto(
            getImportChangesUseCase.execute(GetImportChangesCommand(cacheService.getKey(file), projectId, featureFlags))
        )
    }

    fun synchronizeEnvironments(projectId: Long, key: String, copyDirection: CopyDirection): List<FeatureFlagDto> {
        if (!cacheService.contains(key)) {
            return ArrayList()
        }

        return featureFlagMapper.convertToDtoList(
            synchronizeEnvironmentsUseCase.execute(
                SynchronizeEnvironmentsCommand(
                    projectId,
                    cacheService.getFromCache(key).featureFlags,
                    copyDirection
                )
            )
        )
    }

    fun synchronizePortals(projectId: Long, key: String, needToDelete: Boolean): List<FeatureFlagDto> {
        if (!cacheService.contains(key)) {
            return ArrayList()
        }

        return featureFlagMapper.convertToDtoList(
            synchronizePortalsUseCase.execute(
                SynchronizePortalsCommand(projectId, cacheService.getFromCache(key).featureFlags, needToDelete)
            )
        )
    }

    fun getEnabledFeatureFlagsResource(projectId: Long, environmentId: Long, days: Int): Resource {
        val featureFlags = fetchAllEnabledFeatureFlagsForEnvironmentUseCase.execute(
            FetchAllEnabledFeatureFlagsForEnvironmentCommand(projectId, environmentId, days)
        )
        val responseDto = featureFlagMapper.convertToDtoList(featureFlags)
        return ByteArrayResource(objectMapper.writeValueAsBytes(responseDto))
    }

    fun findFeatureFlagsByPattern(command: FindFeatureFlagByPatternCommand): FeatureFlagFilterResponseDto {
        val page = findFeatureFlagByPatternUseCase.execute(command).apply {
            map { it.environments = mutableListOf() }
        }

        return FeatureFlagFilterResponseDto(
            resultList = page.map(featureFlagMapper::convertToDto).toList(),
            page = page.number,
            pageSize = page.size,
            totalPages = page.totalPages,
            totalElements = page.totalElements
        )
    }

    fun getFeatureFlagsTags(projectId: Long, pageable: Pageable): TagFilterResponseDto {
        val page = fetchAllFeatureFlagsTagsUseCase.execute(FetchAllFeatureFlagsTagsCommand(projectId, pageable))

        return TagFilterResponseDto(
            resultList = page.content,
            page = page.number,
            pageSize = page.size,
            totalPages = page.totalPages,
            totalElements = page.totalElements
        )
    }

    private fun getFeatureFlagsFromFile(file: MultipartFile): List<FeatureFlag> {
        val key = cacheService.getKey(file)
        val featureFlags: List<FeatureFlag>

        if (cacheService.contains(key)) {
            featureFlags = cacheService.getFromCache(key).featureFlags
        } else {
            val requestDto = try {
                objectMapper.readValue<List<FeatureFlagDto>>(file.inputStream)
            } catch (e: Exception) {
                throw ServiceException(ErrorCode.UNABLE_TO_READ_FILE, e)
            }
            featureFlags = featureFlagMapper.convertToDomainList(requestDto)
            cacheService.addToCache(key, FeatureFlagList(featureFlags))
        }
        return featureFlags
    }
}