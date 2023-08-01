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
import ru.trueengineering.featureflag.manager.authorization.config.AuthorizationConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsForProject
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsOfProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FindByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.SearchEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentUseCase
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaBaseTest

@ContextConfiguration(classes = [AuthorizationConfiguration::class, EnvironmentUseCaseTestConfiguration::class, TestRoleDefiner::class])
@Sql(
    "/organization_project_dataset.sql", "/permissions/environment_authorization.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class EnvironmentPermissionTest :
    JpaBaseTest() {

    @Autowired lateinit var createEnvironmentTokenUseCase : CreateEnvironmentTokenUseCase
    @Autowired lateinit var deleteEnvironmentUseCase: DeleteEnvironmentUseCase
    @Autowired lateinit var deleteInstanceUseCase: DeleteInstanceUseCase
    @Autowired lateinit var fetchAllEnvironmentsOfProjectUseCase: FetchAllEnvironmentsOfProjectUseCase
    @Autowired lateinit var searchEnvironmentUseCase: SearchEnvironmentUseCase
    @Autowired lateinit var updateEnvironmentUseCase: UpdateEnvironmentUseCase
    @Autowired lateinit var getCompareEnvironmentsStateUseCase: GetCompareEnvironmentsStateUseCase


    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can create token`() {
        val actual = createEnvironmentTokenUseCase.execute(CreateEnvironmentTokenCommand(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not create token`() {
        assertThrows<AccessDeniedException> { createEnvironmentTokenUseCase.execute(CreateEnvironmentTokenCommand(1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not create token`() {
        assertThrows<AccessDeniedException> { createEnvironmentTokenUseCase.execute(CreateEnvironmentTokenCommand(1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete env`() {
        val actual = deleteEnvironmentUseCase.execute(DeleteEnvironmentCommand(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete env`() {
        assertThrows<AccessDeniedException> { deleteEnvironmentUseCase.execute(DeleteEnvironmentCommand(1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete env`() {
        assertThrows<AccessDeniedException> { deleteEnvironmentUseCase.execute(DeleteEnvironmentCommand(1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete env instance`() {
        val actual = deleteInstanceUseCase.execute(DeleteInstanceCommand(1,1,1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete env instance`() {
        assertThrows<AccessDeniedException> { deleteInstanceUseCase.execute(DeleteInstanceCommand(1,1,1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete env instance`() {
        assertThrows<AccessDeniedException> { deleteInstanceUseCase.execute(DeleteInstanceCommand(1,1,1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can fetch all env`() {
        val actual = fetchAllEnvironmentsOfProjectUseCase.search(FetchAllEnvironmentsForProject(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can fetch all env`() {
        val actual = fetchAllEnvironmentsOfProjectUseCase.search(FetchAllEnvironmentsForProject(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not fetch all env`() {
        assertThrows<AccessDeniedException> { fetchAllEnvironmentsOfProjectUseCase.search(FetchAllEnvironmentsForProject(1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can fetch env`() {
        val actual = searchEnvironmentUseCase.search(FindByIdQuery(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can fetch env`() {
        val actual = searchEnvironmentUseCase.search(FindByIdQuery(1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not fetch env`() {
        assertThrows<AccessDeniedException> { searchEnvironmentUseCase.search(FindByIdQuery(1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can update env`() {
        val actual = updateEnvironmentUseCase.execute(UpdateEnvironmentCommand(1, "name", 1))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not update env`() {
        assertThrows<AccessDeniedException> { updateEnvironmentUseCase.execute(UpdateEnvironmentCommand(1, "name", 1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not update env`() {
        assertThrows<AccessDeniedException> { updateEnvironmentUseCase.execute(UpdateEnvironmentCommand(1, "name", 1)) }
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER user can compare envs`() {
        val actual = getCompareEnvironmentsStateUseCase.execute(GetCompareEnvironmentsStateCommand(1, 1, 2))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR user can compare envs`() {
        val actual = getCompareEnvironmentsStateUseCase.execute(GetCompareEnvironmentsStateCommand(1, 1, 2))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not compare envs`() {
        assertThrows<AccessDeniedException> { getCompareEnvironmentsStateUseCase.execute(
            GetCompareEnvironmentsStateCommand(1, 1, 2)
        ) }
    }
}

@TestConfiguration
class EnvironmentUseCaseTestConfiguration {

    @Bean fun createEnvironmentTokenUseCase(): CreateEnvironmentTokenUseCase =
        mockk<CreateEnvironmentTokenUseCase>().also { every { it.execute(any()) } returns "" }
    @Bean fun deleteEnvironmentUseCase(): DeleteEnvironmentUseCase =
        mockk<DeleteEnvironmentUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun deleteInstanceUseCase(): DeleteInstanceUseCase =
        mockk<DeleteInstanceUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun fetchAllEnvironmentsOfProjectUseCase(): FetchAllEnvironmentsOfProjectUseCase =
        mockk<FetchAllEnvironmentsOfProjectUseCase>().also { every { it.search(any()) } returns mockk() }
    @Bean fun searchEnvironmentUseCase(): SearchEnvironmentUseCase =
        mockk<SearchEnvironmentUseCase>().also { every { it.search(any()) } returns mockk() }
    @Bean fun updateEnvironmentUseCase(): UpdateEnvironmentUseCase =
        mockk<UpdateEnvironmentUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun getCompareEnvironmentsStateUseCase(): GetCompareEnvironmentsStateUseCase =
        mockk<GetCompareEnvironmentsStateUseCase>().also { every { it.execute(any()) } returns mockk() }
}

