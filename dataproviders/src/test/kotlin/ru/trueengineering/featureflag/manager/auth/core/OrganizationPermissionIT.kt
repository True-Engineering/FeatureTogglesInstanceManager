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
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchAllOrganizationsUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.AddNewProjectToOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.CreateProjectCommand
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaBaseTest

@ContextConfiguration(classes = [AuthorizationConfiguration::class, OrganizationUseCaseTestConfiguration::class, TestRoleDefiner::class])
@Sql(
    "/organization_project_dataset.sql", "/permissions/organization_authorization.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class OrganizationPermissionTest :
    JpaBaseTest() {

    @Autowired lateinit var addNewProjectToOrganizationUseCase: AddNewProjectToOrganizationUseCase
    @Autowired lateinit var createOrganizationUseCase: CreateOrganizationUseCase
    @Autowired lateinit var deleteOrganizationUseCase: DeleteOrganizationUseCase
    @Autowired lateinit var fetchAllOrganizationsUseCase: FetchAllOrganizationsUseCase
    @Autowired lateinit var searchOrganizationUseCase: SearchOrganizationUseCase



    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can create project`() {
        val actual = addNewProjectToOrganizationUseCase.execute(CreateProjectCommand("project", 1L))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not create project`() {
        assertThrows<AccessDeniedException> { addNewProjectToOrganizationUseCase.execute(CreateProjectCommand("project", 1L))}
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not create project`() {
        assertThrows<AccessDeniedException> { addNewProjectToOrganizationUseCase.execute(CreateProjectCommand("project", 1L))}
    }

    @Test
    @WithMockUser(username = "admin", roles = ["FEATURE_FLAGS_ADMIN"])
    internal fun `admin can create organization`() {
        val actual = createOrganizationUseCase.execute(CreateOrganizationCommand("org"))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can not create organization`() {
        assertThrows<AccessDeniedException> { createOrganizationUseCase.execute(CreateOrganizationCommand("org")) }
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not create organization`() {
        assertThrows<AccessDeniedException> { createOrganizationUseCase.execute(CreateOrganizationCommand("org")) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not create organization`() {
        assertThrows<AccessDeniedException> { createOrganizationUseCase.execute(CreateOrganizationCommand("org")) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete organization`() {
        deleteOrganizationUseCase.execute(DeleteOrganizationCommand(1L))

    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete organization`() {
        assertThrows<AccessDeniedException> { deleteOrganizationUseCase.execute(DeleteOrganizationCommand(1L)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete organization`() {
        assertThrows<AccessDeniedException> { deleteOrganizationUseCase.execute(DeleteOrganizationCommand(1L)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can fetch all organizations`() {
        val actual = fetchAllOrganizationsUseCase.search()
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can fetch all organizations`() {
        val actual = fetchAllOrganizationsUseCase.search()
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can fetch all organizations`() {
        fetchAllOrganizationsUseCase.search()
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can fetch organization`() {
        val actual = searchOrganizationUseCase.search(SearchOrganizationByIdQuery(1L))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can fetch organization`() {
        val actual = searchOrganizationUseCase.search(SearchOrganizationByIdQuery(1L))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not fetch organization`() {
        assertThrows<AccessDeniedException> { searchOrganizationUseCase.search(SearchOrganizationByIdQuery(1L)) }
    }
}

@TestConfiguration
class OrganizationUseCaseTestConfiguration {


    @Bean fun addNewProjectToOrganizationUseCase(): AddNewProjectToOrganizationUseCase =
        mockk<AddNewProjectToOrganizationUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun createOrganizationUseCase(): CreateOrganizationUseCase =
        mockk<CreateOrganizationUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun deleteOrganizationUseCase(): DeleteOrganizationUseCase =
        mockk<DeleteOrganizationUseCase>().also { every { it.execute(any()) } returns mockk() }
    @Bean fun fetchAllOrganizationsUseCase(): FetchAllOrganizationsUseCase =
        mockk<FetchAllOrganizationsUseCase>().also { every { it.search() } returns mockk() }
    @Bean fun searchOrganizationUseCase(): SearchOrganizationUseCase =
        mockk<SearchOrganizationUseCase>().also { every { it.search(any()) } returns mockk() }

}

