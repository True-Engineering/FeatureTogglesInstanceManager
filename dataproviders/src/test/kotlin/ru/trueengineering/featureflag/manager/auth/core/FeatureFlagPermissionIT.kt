package ru.trueengineering.featureflag.manager.auth.core

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import ru.trueengineering.featureflag.manager.auth.TestPermissionFilter
import ru.trueengineering.featureflag.manager.authorization.config.AuthorizationConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
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
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectUseCase
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaBaseTest

@ContextConfiguration(classes = [AuthorizationConfiguration::class, FeatureFlagUseCaseTestConfiguration::class, TestRoleDefiner::class])
@Sql(
    "/organization_project_dataset.sql", "/permissions/environment_authorization.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class FeatureFlagPermissionTest :
    JpaBaseTest() {

    @Autowired lateinit var createFeatureFlagUseCase: CreateFeatureFlagUseCase
    @Autowired lateinit var deleteFeatureFlagUseCase: DeleteFeatureFlagUseCase
    @Autowired lateinit var disableFeatureFlagUseCase: DisableFeatureFlagUseCase
    @Autowired lateinit var editFeatureFlagStrategyUseCase: EditFeatureFlagStrategyUseCase
    @Autowired lateinit var editFeatureFlagUseCase: EditFeatureFlagUseCase
    @Autowired lateinit var enableFeatureFlagUseCase: EnableFeatureFlagUseCase
    @Autowired lateinit var fetchAllFeatureFlagsForAgentUseCase: FetchAllFeatureFlagsForAgentUseCase
    @Autowired lateinit var fetchAllFeatureFlagsForProjectUseCase: FetchAllFeatureFlagsForProjectUseCase


    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can create flag`() {
        val actual = createFeatureFlagUseCase.execute(CreateFeatureFlagCommand("",1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not create flag`() {
        assertThrows<AccessDeniedException> { createFeatureFlagUseCase.execute(CreateFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not create flag`() {
        assertThrows<AccessDeniedException> { createFeatureFlagUseCase.execute(CreateFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete flag`() {
        val actual = deleteFeatureFlagUseCase.execute(DeleteFeatureFlagCommand("",1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete flag`() {
        assertThrows<AccessDeniedException> { deleteFeatureFlagUseCase.execute(DeleteFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete flag`() {
        assertThrows<AccessDeniedException> { deleteFeatureFlagUseCase.execute(DeleteFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can disable flag`() {
        val actual = disableFeatureFlagUseCase.execute(DisableFeatureFlagCommand("",1, 1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not disable flag`() {
        assertThrows<AccessDeniedException> { disableFeatureFlagUseCase.execute(DisableFeatureFlagCommand("",1, 1)) }
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not disable flag`() {
        assertThrows<AccessDeniedException> { disableFeatureFlagUseCase.execute(DisableFeatureFlagCommand("",1, 1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can edit flag strategy`() {
        val actual = editFeatureFlagStrategyUseCase.execute(EditFeatureFlagStrategyCommand("",1, 1, null, null))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not edit flag strategy`() {
        assertThrows<AccessDeniedException> { editFeatureFlagStrategyUseCase.execute(EditFeatureFlagStrategyCommand("",1, 1, null, null)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not edit flag strategy`() {
        assertThrows<AccessDeniedException> { editFeatureFlagStrategyUseCase.execute(EditFeatureFlagStrategyCommand("",1, 1, null, null)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can edit flag`() {
        val actual = editFeatureFlagUseCase.execute(EditFeatureFlagCommand("",1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not edit flag`() {
        assertThrows<AccessDeniedException> { editFeatureFlagUseCase.execute(EditFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not edit flag`() {
        assertThrows<AccessDeniedException> { editFeatureFlagUseCase.execute(EditFeatureFlagCommand("",1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can enable flag`() {
        val actual = enableFeatureFlagUseCase.execute(EnableFeatureFlagCommand("",1, 1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not enable flag`() {
        assertThrows<AccessDeniedException> { enableFeatureFlagUseCase.execute(EnableFeatureFlagCommand("",1, 1)) }
    }

    @Test
    @WithMockUser(username = "agent", authorities = ["AGENT"])
    internal fun `agent can fetch agent flags`() {
        val actual = fetchAllFeatureFlagsForAgentUseCase.search(FetchAllFeatureFlagsForAgentQuery("", "", ""))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can not fetch agent flags`() {
        assertThrows<AccessDeniedException> { fetchAllFeatureFlagsForAgentUseCase.search(FetchAllFeatureFlagsForAgentQuery("", "", "")) }
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not fetch agent flags`() {
        assertThrows<AccessDeniedException> { fetchAllFeatureFlagsForAgentUseCase.search(FetchAllFeatureFlagsForAgentQuery("", "", "")) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not fetch agent flags`() {
        assertThrows<AccessDeniedException> { fetchAllFeatureFlagsForAgentUseCase.search(FetchAllFeatureFlagsForAgentQuery("", "", "")) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can fetch all feature flags`() {
        val actual = fetchAllFeatureFlagsForProjectUseCase.search(FetchAllFeatureFlagsForProjectQuery(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can fetch all feature flags`() {
        val actual = fetchAllFeatureFlagsForProjectUseCase.search(FetchAllFeatureFlagsForProjectQuery(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not fetch all feature flags`() {
        assertThrows<AccessDeniedException> { fetchAllFeatureFlagsForProjectUseCase.search(FetchAllFeatureFlagsForProjectQuery(1)) }
    }

}

@TestConfiguration
class FeatureFlagUseCaseTestConfiguration {

    @Bean fun createFeatureFlagUseCase(): CreateFeatureFlagUseCase =
    mockk<CreateFeatureFlagUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun deleteFeatureFlagUseCase(): DeleteFeatureFlagUseCase =
    mockk<DeleteFeatureFlagUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun disableFeatureFlagUseCase(): DisableFeatureFlagUseCase =
    mockk<DisableFeatureFlagUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun editFeatureFlagStrategyUseCase(): EditFeatureFlagStrategyUseCase =
    mockk<EditFeatureFlagStrategyUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun editFeatureFlagUseCase(): EditFeatureFlagUseCase =
    mockk<EditFeatureFlagUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun enableFeatureFlagUseCase(): EnableFeatureFlagUseCase =
    mockk<EnableFeatureFlagUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun fetchAllFeatureFlagsForAgentUseCase(): FetchAllFeatureFlagsForAgentUseCase =
    mockk<FetchAllFeatureFlagsForAgentUseCase>().also { every { it.search(any()) } returns mockk() }
    @Bean fun fetchAllFeatureFlagsForProjectUseCase(): FetchAllFeatureFlagsForProjectUseCase =
    mockk<FetchAllFeatureFlagsForProjectUseCase>().also { every { it.search(any()) } returns mockk() }

}

