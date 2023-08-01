package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserJpaRepositoryTest(
    @Autowired override var uut: UserJpaRepository
) : JpaRepositoryBaseTest<UserJpaRepository>() {

    @Test
    internal fun shouldCreate() {
        val entity = UserEntity("email").apply { name = "name" }
        val actualEntity = uut.save(entity)
        assertEquals("name", actualEntity.name)
        assertEquals("email", actualEntity.email)
        assertNotNull(actualEntity.id)
        assertNotNull(actualEntity.created)
        assertNotNull(actualEntity.updated)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldDelete() {
        val entity = uut.findById(1)
        assertTrue(entity.isPresent)

        uut.delete(entity.get())

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldDeleteById() {
        val project = uut.findById(1)
        assertTrue(project.isPresent)

        uut.deleteById(1)

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldFetchAll() {
        val entities = uut.findAll()
        assertEquals(2, entities.count())
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldGetNonRemovedById() {
        val entity = uut.getNonRemovedById(1)
        assertNotNull(entity)
        assertEquals("name", entity.name)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldGetNonRemovedByEmailList() {
        val actual = uut.getAllNonRemovedByEmailList(listOf("email", "email2"))
        assertNotNull(actual)
        assertEquals(1, actual.size)
        assertEquals("email", actual[0].email)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldGetNonRemovedCountByEmailList() {
        val actual = uut.getNonRemovedCountByEmailList(listOf("email", "email2"))
        assertNotNull(actual)
        assertEquals(1, actual)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldntGetByIdRemoved() {
        val entity = uut.getNonRemovedById(2)
        assertNull(entity)
    }

    @Test
    @Sql("/user_dataset.sql")
    internal fun shouldSetRemoved() {
        uut.setRemoved(1)
        val entity = uut.findById(1)
        assertEquals(true, entity.get().removed)
    }

}