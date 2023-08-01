package ru.trueengineering.featureflag.manager.ports.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.mock.web.MockMultipartFile
import ru.trueengineering.featureflag.manager.core.domen.toggle.CopyDirection
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.Environment
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportChanges
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportEnvironments
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsUseCase
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagFilterResponseDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportChangesDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportEnvironmentsDto
import ru.trueengineering.featureflag.manager.ports.service.casheService.CacheService
import ru.trueengineering.featureflag.manager.ports.service.mapper.AgentFeatureFlagMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.FeatureFlagMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ImportChangesMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.ImportEnvironmentsMapper

internal class FeatureFlagServiceTest {
    private val objectMapper: ObjectMapper = mockk()
    private val featureFlagMapper: FeatureFlagMapper = mockk()
    private val importEnvironmentsMapper: ImportEnvironmentsMapper = mockk()
    private val agentFeatureFlagMapper: AgentFeatureFlagMapper = mockk()
    private val createFeatureFlagUseCase: CreateFeatureFlagUseCase = mockk()
    private val editFeatureFlagUseCase: EditFeatureFlagUseCase = mockk()
    private val fetchAllFeatureFlagsForProjectUseCase: FetchAllFeatureFlagsForProjectUseCase = mockk()
    private val fetchAllFeatureFlagsForAgentUseCase: FetchAllFeatureFlagsForAgentUseCase = mockk()
    private val enableFeatureFlagUseCase: EnableFeatureFlagUseCase = mockk()
    private val disableFeatureFlagUseCase: DisableFeatureFlagUseCase = mockk()
    private val deleteFeatureFlagUseCase: DeleteFeatureFlagUseCase = mockk()
    private val editFeatureFlagStrategyUseCase: EditFeatureFlagStrategyUseCase = mockk()
    private val synchronizePortalsUseCase: SynchronizePortalsUseCase = mockk()
    private val synchronizeEnvironmentsUseCase: SynchronizeEnvironmentsUseCase = mockk()
    private val cacheService: CacheService = mockk()
    private val importChangesMapper: ImportChangesMapper = mockk()
    private val getImportChangesUseCase: GetImportChangesUseCase = mockk()
    private val getImportEnvironmentsUseCase: GetImportEnvironmentsUseCase = mockk()
    private val findFeatureFlagByPatternUseCase: FindFeatureFlagByPatternUseCase = mockk()
    private val fetchAllFeatureFlagsTagsUseCase: FetchAllFeatureFlagsTagsUseCase = mockk()
    private val fetchAllEnabledFeatureFlagsForEnvironmentUseCase: FetchAllEnabledFeatureFlagsForEnvironmentUseCase = mockk()

    private val uut: FeatureFlagService = FeatureFlagService(
        objectMapper,
        featureFlagMapper,
        importChangesMapper,
        importEnvironmentsMapper,
        agentFeatureFlagMapper,
        createFeatureFlagUseCase,
        editFeatureFlagUseCase,
        fetchAllFeatureFlagsForProjectUseCase,
        fetchAllFeatureFlagsForAgentUseCase,
        enableFeatureFlagUseCase,
        disableFeatureFlagUseCase,
        deleteFeatureFlagUseCase,
        editFeatureFlagStrategyUseCase,
        synchronizePortalsUseCase,
        synchronizeEnvironmentsUseCase,
        getImportChangesUseCase,
        getImportEnvironmentsUseCase,
        fetchAllEnabledFeatureFlagsForEnvironmentUseCase,
        findFeatureFlagByPatternUseCase,
        fetchAllFeatureFlagsTagsUseCase,
        cacheService
    )

    @Test
    fun getFeatureFlagResourceForEnvironment() {
        val featureFlag = FeatureFlag("uid")
        val featureFlagDto = FeatureFlagDto("uid")
        val featureFlags = listOf(featureFlag)
        val featureFlagDtos = listOf(featureFlagDto)
        every { featureFlagMapper.convertToDtoList(featureFlags) } returns featureFlagDtos
        every {
            fetchAllFeatureFlagsForProjectUseCase.search(
                FetchAllFeatureFlagsForProjectQuery(22L)
            )
        } returns featureFlags
        val byteArray = ByteArray(1)
        every { objectMapper.writeValueAsBytes(featureFlagDtos) } returns byteArray
        val actual = uut.getFeatureFlagResourceForProject(22L)
        assertThat(actual).isEqualTo(ByteArrayResource(byteArray))
    }

    @Test
    fun getImportEnvironments() {
        val file = MockMultipartFile("name", ByteArray(1))
        val key = "key"
        val featureFlags = listOf(FeatureFlag("uid", environments = mutableListOf(Environment(1, "UAT"))))
        val importEnvironments = ImportEnvironments(
            key,
            true,
            listOf(ru.trueengineering.featureflag.manager.core.domen.environment.Environment(1, "UAT")),
            listOf()
        )
        val importEnvironmentsDto =
            ImportEnvironmentsDto(key, true, listOf(EnvironmentInfoDto(1, "UAT", listOf(), false, null, null, listOf())), listOf())

        every { cacheService.getKey(file) } returns key
        every { cacheService.contains(key) } returns true
        every { cacheService.getFromCache(key).featureFlags } returns featureFlags
        every {
            getImportEnvironmentsUseCase.execute(
                GetImportEnvironmentsCommand(
                    key,
                    1L,
                    featureFlags
                )
            )
        } returns importEnvironments
        every { importEnvironmentsMapper.convertToDto(importEnvironments) } returns importEnvironmentsDto

        val actual = uut.getImportEnvironments(1L, file)
        assertThat(actual).isEqualTo(importEnvironmentsDto)
    }

    @Test
    fun getImportChanges() {
        val file = MockMultipartFile("name", ByteArray(1))
        val key = "key"
        val featureFlags = listOf(FeatureFlag("uid"))
        val featureFlagDto = listOf(FeatureFlagDto("uid"))
        val importChanges = ImportChanges(key, listOf(), featureFlags, listOf())
        val importChangesDto = ImportChangesDto(key, listOf(), featureFlagDto, listOf())

        every { cacheService.getKey(file) } returns key
        every { cacheService.contains(key) } returns true
        every { cacheService.getFromCache(key).featureFlags } returns featureFlags
        every { getImportChangesUseCase.execute(GetImportChangesCommand(key, 1L, featureFlags)) } returns importChanges
        every { importChangesMapper.convertToDto(importChanges) } returns importChangesDto

        val actual = uut.getImportChanges(1L, file)
        assertThat(actual).isEqualTo(importChangesDto)
    }

    @Test
    fun synchronizeEnvironments() {
        val featureFlag = FeatureFlag("uid")
        val featureFlagDto = FeatureFlagDto("uid")
        val featureFlags = listOf(featureFlag)
        val featureFlagDtos = listOf(featureFlagDto)
        val copyDirection = CopyDirection("", listOf())
        val key = "key"

        every { cacheService.contains(key) } returns true
        every { cacheService.getFromCache(key).featureFlags } returns featureFlags
        every {
            synchronizeEnvironmentsUseCase.execute(
                SynchronizeEnvironmentsCommand(
                    1L,
                    featureFlags,
                    copyDirection
                )
            )
        } returns featureFlags
        every { featureFlagMapper.convertToDtoList(featureFlags) } returns featureFlagDtos

        val actual = uut.synchronizeEnvironments(1L, key, copyDirection)
        assertThat(actual).isEqualTo(featureFlagDtos)
    }

    @Test
    fun synchronizePortals() {
        val featureFlag = FeatureFlag("uid")
        val featureFlagDto = FeatureFlagDto("uid")
        val featureFlags = listOf(featureFlag)
        val featureFlagDtos = listOf(featureFlagDto)
        val key = "key"

        every { cacheService.contains(key) } returns true
        every { cacheService.getFromCache(key).featureFlags } returns featureFlags
        every {
            synchronizePortalsUseCase.execute(
                SynchronizePortalsCommand(11L, featureFlags, false)
            )
        } returns featureFlags

        every { featureFlagMapper.convertToDtoList(featureFlags) } returns featureFlagDtos

        val actual = uut.synchronizePortals(11L, key, false)
        assertThat(actual).isEqualTo(featureFlagDtos)
    }

    @Test
    fun findFeatureFlagsByPattern() {
        val pageable = PageRequest.of(0, 1)
        val command = FindFeatureFlagByPatternCommand(1, "test", pageable)
        val featureFlag = FeatureFlag("test123")
        val featureFlagDto = FeatureFlagDto("test123")
        val page = PageImpl(listOf(featureFlag))
        val responseDto = FeatureFlagFilterResponseDto(
            listOf(featureFlagDto),
            1,
            0,
            1,
            1
        )

        every { findFeatureFlagByPatternUseCase.execute(command) } returns page
        every { featureFlagMapper.convertToDto(featureFlag) } returns featureFlagDto

        val actual = uut.findFeatureFlagsByPattern(command)

        assertThat(actual).isEqualTo(responseDto)
    }

    fun getFeatureFlagsTags() {
        val pageable = PageRequest.of(0, 5)
        val page = PageImpl(listOf("TEST", "WEB"))

        every { fetchAllFeatureFlagsTagsUseCase.execute(FetchAllFeatureFlagsTagsCommand(1, pageable)) } returns page

        val actual = uut.getFeatureFlagsTags(1, pageable)

        assertThat(actual.totalElements).isEqualTo(2)
    }
}