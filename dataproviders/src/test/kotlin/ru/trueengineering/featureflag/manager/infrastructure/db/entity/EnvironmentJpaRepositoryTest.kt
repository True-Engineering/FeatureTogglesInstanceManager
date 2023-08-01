package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EnvironmentJpaRepositoryTest(
    @Autowired override var uut: EnvironmentJpaRepository,
    @Autowired var instanceJpaRepository: InstanceJpaRepository,
    @Autowired var emailJpaRepository: EmailJpaRepository
) : JpaRepositoryBaseTest<EnvironmentJpaRepository>() {

    @Test
    @Sql("/environment_dataset.sql")
    internal fun setAuthKeyHash() {
        val authKeyHash = "HASH"
        uut.setAuthKeyHash(1, authKeyHash)
        assertNotNull(uut.findByAuthKeyHash(authKeyHash))
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun findByAuthKeyHash() {
        val environmentEntity = uut.findByAuthKeyHash("AUTH_KEY_HASH")
        assertNotNull(environmentEntity)
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun setRemoved() {
        uut.setRemoved(1)
        val environmentEntity = uut.findById(1).get()
        assertNotNull(environmentEntity)
        assertEquals(true, environmentEntity.removed)
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun getAllByProjectId() {
        val entities = uut.getAllByProjectId(1)
        assertEquals(1, entities.size)
        assertEquals(1, entities.get(0).project.id)

    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun updateEnvironment() {

        val envName = "test new name"

        var environmentEntityOptional = uut.findById(1)
        assertTrue(environmentEntityOptional.isPresent)
        val environmentEntity = environmentEntityOptional.get()
        environmentEntity.name = envName

        uut.save(environmentEntity)
        environmentEntityOptional = uut.findById(1)
        assertTrue(environmentEntityOptional.isPresent)
        assertEquals(envName, environmentEntityOptional.get().name)
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun dateInstance() {

        val environmentEntityOptional = uut.findById(1)
        assertTrue(environmentEntityOptional.isPresent)

        val environmentEntity = environmentEntityOptional.get()
        assertTrue(environmentEntity.instances.isNotEmpty())

        var instanceId = 0L
        environmentEntity.instances.last().id?.let {
            instanceId = it
            instanceJpaRepository.deleteById(it)
        }

        val instanceEntityOptional = instanceJpaRepository.findById(instanceId)
        assertFalse(instanceEntityOptional.isPresent)
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun updateInstanceStatus() {

        uut.updateInstanceStatus(1800)

        val instance = uut.getById(1)!!.instances.first()
        assertEquals(InstanceConnectionStatus.OUT_OF_SYNC, instance.status)
    }

    @Test
    @Sql("/environment_dataset.sql")
    internal fun findEmails() {
        val environment = uut.getById(1)
        val emails = emailJpaRepository.findByEnvironmentIdAndEmailIn(1, listOf("test@test.ru", "test2@test.ru"))

        assertTrue { environment?.emails?.size == 2}
        assertTrue(emails != null)
        assertTrue { emails.size == 2 }
        assertTrue { emails.find { it.email == "test@test.ru" } != null }
        assertTrue { emails.find { it.email == "test2@test.ru" } != null }
    }
}