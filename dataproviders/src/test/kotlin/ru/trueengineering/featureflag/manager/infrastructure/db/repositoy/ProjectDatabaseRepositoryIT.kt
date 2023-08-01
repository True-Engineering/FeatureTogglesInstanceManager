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
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import ru.trueengineering.featureflag.manager.core.domen.event.EnvironmentCreatedEvent
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.capture

@Sql("/organization_project_dataset.sql", config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
internal class ProjectDatabaseRepositoryIT(@Autowired override var uut: ProjectDatabaseRepository) :
    JpaRepositoryBaseTest<ProjectDatabaseRepository>() {

    @Autowired
    private lateinit var environmentDatabaseRepository: EnvironmentDatabaseRepository
    @Autowired
    private lateinit var featureFlagRepository: FeatureFlagRepository

    @MockBean
    private lateinit var eventHandler: TestEventHandler
    private val captor: ArgumentCaptor<DomainEvent> = ArgumentCaptor.forClass(DomainEvent::class.java)

    @Test
    internal fun `should add new env to project`() {
        uut.addNewEnvironment(1, Environment(name = "dev"))
        val actualProject = uut.getById(1)
        assertThat(actualProject.id).isNotNull
        assertThat(actualProject.environments).hasSize(1)
        assertThat(actualProject.environments[0].id).isNotNull
        assertThat(actualProject.environments[0].name).isEqualTo("dev")

        val environmentById = environmentDatabaseRepository.getById(actualProject.environments[0].id!!)
        assertThat(environmentById).isNotNull
        assertThat(environmentById.name).isNotNull

        // закрываем транзакцию, это нужно для генерации событий
        TestTransaction.flagForCommit()
        TestTransaction.end()

        Mockito.verify(eventHandler).handle(capture(captor))
        val domainEvent = captor.value
        assertThat(domainEvent).isNotNull
        assertThat(domainEvent).isInstanceOf(EnvironmentCreatedEvent::class.java)
        domainEvent as EnvironmentCreatedEvent
        assertThat(domainEvent.environmentName).isEqualTo("dev")
        assertThat(domainEvent.projectId).isEqualTo(1)
    }

    @Test
    internal fun `should not add new env with duplicated name to project`() {
        // первое окружение добавляется без проблем
        uut.addNewEnvironment(1, Environment(name = "dev"))

        val environmentAlreadyExistException = assertThrows<ServiceException> {
            uut.addNewEnvironment(1, Environment(name = "dev"))
        }
        assertThat(environmentAlreadyExistException.errorCode).isEqualTo(ErrorCode.ENVIRONMENT_ALREADY_EXIST)
    }

    @Test
    internal fun `should delete project`() {
        // первое окружение добавляется без проблем
        assertThat(uut.getById(1)).isNotNull

        uut.deleteProject(1)

        val projectNotFoundException = assertThrows<ServiceException> {
            uut.getById(1)
        }
        assertThat(projectNotFoundException.errorCode).isEqualTo(ErrorCode.PROJECT_NOT_FOUND)
    }

    @Test
    internal fun `should update project`() {
        uut.updateName(1, "new name")
        val project = uut.getById(1)
        assertThat(project.name).isEqualTo("new name")
    }

    @Test
    @Sql("/feature_flag_dataset.sql", "/update_id_sequences.sql")
    internal fun `should create new environment`() {
        uut.addNewEnvironment(1, Environment(name = "prod-msk"))
        val newEnvironment = environmentDatabaseRepository.getByProjectIdAndName(1, "prod-msk")
        assertThat(newEnvironment).isNotNull
        assertThat(featureFlagRepository.getFeatureFlagsForEnvironment(newEnvironment!!.id!!)).hasSize(2)
    }
}