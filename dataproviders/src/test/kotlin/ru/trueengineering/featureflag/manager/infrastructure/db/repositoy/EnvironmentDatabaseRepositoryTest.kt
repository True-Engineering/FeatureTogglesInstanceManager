package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EmailEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EmailJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.EnvironmentEntityMapper
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.ProjectEntityMapper
import java.util.Optional
import kotlin.test.assertTrue

internal class EnvironmentDatabaseRepositoryTest {

    private val environmentJpaRepository: EnvironmentJpaRepository = mockk()
    private val emailJpaRepository: EmailJpaRepository = mockk()
    private val instanceJpaRepository: InstanceJpaRepository = mockk()
    private val environmentEntityMapper: EnvironmentEntityMapper = mockk()
    private val projectEntityMapper: ProjectEntityMapper = mockk()

    private val uut: EnvironmentDatabaseRepository =
        EnvironmentDatabaseRepository(
            environmentJpaRepository,
            emailJpaRepository,
            instanceJpaRepository,
            environmentEntityMapper,
            projectEntityMapper
        )

    @Test
    fun deleteEnvironmentIfExists() {
        every { environmentJpaRepository.findById(any()) } returns Optional.of(EnvironmentEntity("removed"))
        every { environmentJpaRepository.setRemoved(any()) } just Runs

        uut.remove(123)
        verify { environmentJpaRepository.setRemoved(eq(123)) }
    }

    @Test
    fun deleteEnvironmentIfDoesNotExist() {
        every { environmentJpaRepository.findById(any()) } returns Optional.empty()
        uut.remove(123)
    }

    @Test
    fun checkAndUpdateInstanceStatus() {
        every { environmentJpaRepository.updateInstanceStatus(100) } just Runs
        uut.checkAndUpdateInstanceStatus(100)
        verify { environmentJpaRepository.updateInstanceStatus(100) }
    }

    @Test
    fun createEmails() {
        val environment = EnvironmentEntity("env")

        val slot = slot<EnvironmentEntity>()

        every { environmentJpaRepository.getById(1) } returns environment
        every { environmentJpaRepository.save(capture(slot)) } answers { firstArg() }

        uut.createEmails(1, listOf("test1@test.ru", "test2@test.ru"))

        val captured = slot.captured
        assertTrue(captured.emails.size == 2)
        assertTrue { captured.emails.find { it.email == "test1@test.ru" } != null }
        assertTrue { captured.emails.find { it.email == "test2@test.ru" } != null }
    }

    @Test
    fun removeEmails() {
        val environment = EnvironmentEntity("env")
        val testEmail = EmailEntity("test1@test.ru")
        val anotherTestEmail = EmailEntity("test2@test.ru")
        val emails = listOf(testEmail, anotherTestEmail)
        environment.addEmail(testEmail)
        environment.addEmail(anotherTestEmail)
        val slot = slot<EnvironmentEntity>()

        every { environmentJpaRepository.getById(1) } returns environment
        every { emailJpaRepository.findByEnvironmentIdAndEmailIn(1, listOf("test1@test.ru", "test2@test.ru")) } returns emails
        every { environmentJpaRepository.save(capture(slot)) } answers { firstArg() }

        uut.removeEmails(1, listOf("test1@test.ru", "test2@test.ru"))

        val captured = slot.captured
        assertTrue(captured.emails.isEmpty())
    }

    @Test
    fun addProperties() {
        val environmentEntity = EnvironmentEntity("env").apply {
            properties[EnvironmentPropertiesClass.FREEZING_USER] = "user"
        }
        val newProperties = mutableMapOf<EnvironmentPropertiesClass, String?>(
            EnvironmentPropertiesClass.FREEZING_ENABLE to true.toString()
        )
        val slot = slot<EnvironmentEntity>()

        every { environmentJpaRepository.getById(1) } returns environmentEntity
        every { environmentJpaRepository.save(capture(slot)) } answers { firstArg() }
        every { environmentEntityMapper.convertToDomain(capture(slot)) } returns mockk()

        uut.addProperties(1, newProperties)

        assertThat(slot.captured.properties.keys).hasSize(2)
        assertThat(slot.captured.properties.containsKey(EnvironmentPropertiesClass.FREEZING_ENABLE))
        assertThat(slot.captured.properties[EnvironmentPropertiesClass.FREEZING_ENABLE].toBoolean()).isTrue()
        assertThat(slot.captured.properties.containsKey(EnvironmentPropertiesClass.FREEZING_USER))
        assertThat(slot.captured.properties[EnvironmentPropertiesClass.FREEZING_USER]).isEqualTo("user")
    }
}