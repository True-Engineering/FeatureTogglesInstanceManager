package ru.trueengineering.featureflag.manager.auth.core

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.config.AuthorizationConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
import ru.trueengineering.featureflag.manager.core.domen.environment.AddNewEnvironmentToProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaBaseTest

@ContextConfiguration(classes = [AuthorizationConfiguration::class, ProjectUseCaseTestConfiguration::class, TestRoleDefiner::class])
@Sql(
    "/organization_project_dataset.sql", "/permissions/project_authorization.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class ProjectPermissionTest :
    JpaBaseTest() {

    @Autowired lateinit var activateUserUseCase: ActivateUserUseCase
    @Autowired lateinit var addNewEnvironmentToProjectUseCase: AddNewEnvironmentToProjectUseCase
    @Autowired lateinit var deleteProjectUseCase: DeleteProjectUseCase
    @Autowired lateinit var deleteUserFromProjectUseCase: DeleteUserFromProjectUseCase
    @Autowired lateinit var fetchMembersForProjectUseCase: FetchMembersForProjectUseCase
    @Autowired lateinit var searchProjectUseCase: SearchProjectUseCase
    @Autowired lateinit var updateProjectUseCase: UpdateProjectUseCase


    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can activate users`() {
        activateUserUseCase.execute(ActivateUserCommand(1L, 1))
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not activate users`() {
        assertThrows<AccessDeniedException> {activateUserUseCase.execute(ActivateUserCommand(1L, 1))}
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not activate users`() {
        assertThrows<AccessDeniedException> {activateUserUseCase.execute(ActivateUserCommand(1L, 1))}
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can add env`() {
        addNewEnvironmentToProjectUseCase.execute(CreateEnvironmentCommand("dev", 1))
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER user can not add env`() {
        assertThrows<AccessDeniedException> { addNewEnvironmentToProjectUseCase.execute(CreateEnvironmentCommand("dev", 1)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not add env`() {
        assertThrows<AccessDeniedException> { addNewEnvironmentToProjectUseCase.execute(CreateEnvironmentCommand("dev", 1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete project`() {
        deleteProjectUseCase.execute(DeleteProjectCommand(1L))
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete project`() {
        assertThrows<AccessDeniedException> { deleteProjectUseCase.execute(DeleteProjectCommand(1L)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete project`() {
        assertThrows<AccessDeniedException> { deleteProjectUseCase.execute(DeleteProjectCommand(1L)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can delete users`() {
        deleteUserFromProjectUseCase.execute(DeleteUserFromProjectCommand(1L, 1L))
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can not delete users`() {
        assertThrows<AccessDeniedException> { deleteUserFromProjectUseCase.execute(DeleteUserFromProjectCommand(1L, 1L)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not delete users`() {
        assertThrows<AccessDeniedException> { deleteUserFromProjectUseCase.execute(DeleteUserFromProjectCommand(1L, 1L)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can read projects users`() {
        val actual = fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can read projects users`() {
        val actual = fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L))
        assertThat(actual).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not read projects users`() {
        assertThrows<AccessDeniedException> {  fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(1L)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can read project`() {
        val project = searchProjectUseCase.search(SearchProjectByIdQuery(1))
        assertThat(project).isNotNull
    }

    @Test
    @WithMockUser(username = "user_4")
    internal fun `READER can read project`() {
        val project = searchProjectUseCase.search(SearchProjectByIdQuery(1))
        assertThat(project).isNotNull
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not read project`() {
        assertThrows<AccessDeniedException> {  searchProjectUseCase.search(SearchProjectByIdQuery(1)) }
    }

    @Test
    @WithMockUser(username = "user_5")
    internal fun `EDITOR can update project`() {
        updateProjectUseCase.execute(UpdateProjectCommand("project",1L, 1L))
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `READER can not update project`() {
        assertThrows<AccessDeniedException> { updateProjectUseCase.execute(UpdateProjectCommand("project",1L, 1L)) }
    }

    @Test
    @WithMockUser(username = "user_6")
    internal fun `NO_ACCESS user can not update project`() {
        assertThrows<AccessDeniedException> { updateProjectUseCase.execute(UpdateProjectCommand("project",1L, 1L)) }
    }
}

@TestConfiguration
class ProjectUseCaseTestConfiguration {

    private val project = Project(1L, "project")

    private val user = User("name", "email", id = 1L)

    @Bean
    fun activateUserUseCase(): ActivateUserUseCase {
        val useCase: ActivateUserUseCase = mockk()
        every { useCase.execute(ActivateUserCommand(1L, 1)) } just Runs
        return useCase
    }

    @Bean
    fun addNewEnvironmentToProjectUseCase(): AddNewEnvironmentToProjectUseCase {
        val useCase: AddNewEnvironmentToProjectUseCase = mockk()
        every { useCase.execute(CreateEnvironmentCommand("dev", 1)) } returns Environment(1, "dev")
        return useCase
    }

    @Bean
    fun deleteProjectUseCase(): DeleteProjectUseCase {
        val useCase: DeleteProjectUseCase = mockk()
        every { useCase.execute(DeleteProjectCommand(1L)) } returns Unit
        return useCase
    }

    @Bean
    fun deleteUserFromProjectUseCase(): DeleteUserFromProjectUseCase {
        val useCase: DeleteUserFromProjectUseCase = mockk()
        every { useCase.execute(DeleteUserFromProjectCommand(1L, 1L)) } returns Unit
        return useCase
    }

    @Bean
    fun fetchMembersForProjectUseCase() : FetchMembersForProjectUseCase {
        val useCase : FetchMembersForProjectUseCase = mockk()
        every { useCase.search(FetchMembersForProjectQuery(1L)) } returns listOf(ProjectUser(1L, "name", user, CustomRole.MEMBER, emptyList()))
        return useCase
    }

    @Bean
    fun searchProjectUseCase() : SearchProjectUseCase {
        val useCase : SearchProjectUseCase = mockk()
        every { useCase.search(SearchProjectByIdQuery(1)) } returns project
        return useCase
    }

    @Bean
    fun updateProjectUseCase(): UpdateProjectUseCase {
        val useCase: UpdateProjectUseCase = mockk()
        every { useCase.execute(UpdateProjectCommand("project",1L, 1L)) } returns project
        return useCase
    }

}

