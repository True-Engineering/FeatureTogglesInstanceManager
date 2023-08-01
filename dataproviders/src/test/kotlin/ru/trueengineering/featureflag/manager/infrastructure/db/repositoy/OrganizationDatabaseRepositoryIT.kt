package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.transaction.TestTransaction
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import ru.trueengineering.featureflag.manager.core.domen.event.OrganizationCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.event.ProjectCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.capture


@Sql(
    "/organization_project_dataset.sql", "/update_id_sequences.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class OrganizationDatabaseRepositoryIT(
    @Autowired override var uut: OrganizationDatabaseRepository

) : JpaRepositoryBaseTest<OrganizationDatabaseRepository>() {

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @MockBean
    private lateinit var eventHandler: TestEventHandler
    private val captor: ArgumentCaptor<DomainEvent> = ArgumentCaptor.forClass(DomainEvent::class.java)

    @Test
    internal fun `should add new project`() {
        val actualProject = uut.addNewProject(1, Project(name = "SmartTicketing"))
        // закрываем транзакцию, это нужно для генерации событий
        TestTransaction.flagForCommit()
        TestTransaction.end()

        assertThat(actualProject.id).isNotNull
        assertThat(actualProject.name).isEqualTo("SmartTicketing")

        val projectById = projectRepository.getById(actualProject.id!!.toLong())
        assertThat(projectById.name).isEqualTo("SmartTicketing")
        assertThat(projectById).isNotNull

        Mockito.verify(eventHandler).handle(capture(captor))
        val domainEvent = captor.value
        assertThat(domainEvent).isNotNull
        assertThat(domainEvent).isInstanceOf(ProjectCreatedEvent::class.java)
        domainEvent as ProjectCreatedEvent
        assertThat(domainEvent.organizationId).isEqualTo(1)
        assertThat(domainEvent.projectName).isEqualTo("SmartTicketing")
    }

    @Test
    internal fun `should add duplicated project`() {
        // проект с таким именем уже существует
        val projectAlreadyExistException = assertThrows<ServiceException> {
            uut.addNewProject(1, Project(name = "Super test project 1"))
        }
        assertThat(projectAlreadyExistException.errorCode).isEqualTo(ErrorCode.PROJECT_ALREADY_EXIST)
    }

    @Test
    fun `should find organization with projects by name`() {
        val actual = uut.findByName("organization_1")
        assertThat(actual).isNotNull
        assertThat(actual!!.name).isEqualTo("organization_1")
        assertThat(actual.id).isEqualTo(1)
        assertThat(actual.projects).isNotEmpty
        // только неудалённые
        assertThat(actual.projects).hasSize(1)
        assertThat(actual.projects[0].id).isEqualTo(1)
    }

    @Test
    fun `should find organization without projects by name`() {
        val actual = uut.findByName("organization_2")
        assertThat(actual).isNotNull
        assertThat(actual!!.name).isEqualTo("organization_2")
        assertThat(actual.id).isEqualTo(20)
        assertThat(actual.projects).isEmpty()
    }

    @Test
    fun `should not find organization by name`() {
        val actual = uut.findByName("unknown organization")
        assertThat(actual).isNull()
    }

    @Test
    fun `should delete organization by Id`() {
        assertThat(uut.findById(1)).isNotNull

        uut.removeById(1)

        val organizationNotFoundException = assertThrows<ServiceException> {
            uut.findById(1)
        }
        assertThat(organizationNotFoundException.errorCode).isEqualTo(ErrorCode.ORGANIZATION_NOT_FOUND)
    }

    @Test
    fun `should create new organization`() {
        val actual = uut.create("new organization")

        assertThat(actual.name).isEqualTo("new organization")
        assertThat(actual.id).isNotNull

        val organizationById = uut.findById(actual.id)
        assertThat(organizationById.name).isEqualTo("new organization")

        // закрываем транзакцию, это нужно для генерации событий
        TestTransaction.flagForCommit()
        TestTransaction.end()

        Mockito.verify(eventHandler).handle(capture(captor))
        val domainEvent = captor.value
        assertThat(domainEvent).isNotNull
        assertThat(domainEvent).isInstanceOf(OrganizationCreatedEvent::class.java)
        domainEvent as OrganizationCreatedEvent
        assertThat(domainEvent.organizationName).isEqualTo("new organization")
    }

    @Test
    fun `should not create organization with duplicated name`() {
        val organizationAlreadyExistException = assertThrows<ServiceException> {
            uut.create("organization_1")
        }
        assertThat(organizationAlreadyExistException.errorCode).isEqualTo(ErrorCode.ORGANIZATION_ALREADY_EXIST)
    }
}