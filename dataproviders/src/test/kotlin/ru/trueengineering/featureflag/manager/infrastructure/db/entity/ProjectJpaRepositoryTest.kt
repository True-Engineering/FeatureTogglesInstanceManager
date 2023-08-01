package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProjectJpaRepositoryTest(
    @Autowired override var uut: ProjectJpaRepository
) : JpaRepositoryBaseTest<ProjectJpaRepository>() {

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldUpdateProject() {
        val projectEntityOptional = uut.findById(1)
        assertThat(projectEntityOptional).isPresent

        val projectEntity = projectEntityOptional.get()
        projectEntity.name = "Cool Project"

        val actual = uut.save(projectEntity)

        assertEquals("Cool Project", actual.name)
        assertThat(actual.id).isEqualTo(1)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldUpdateName() {
        uut.updateName("New name", 1)
        val actualEntity = uut.findById(1).get()
        assertEquals("New name", actualEntity.name)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldDeleteProject() {
        val project = uut.findById(1)
        assertTrue(project.isPresent)

        uut.delete(project.get())

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldDeleteById() {
        val project = uut.findById(1)
        assertTrue(project.isPresent)

        uut.deleteById(1)

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldFetchAll() {
        val projects = uut.findAll()
        assertEquals(2, projects.count())
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldGetById() {
        val entity = uut.getById(1)
        assertNotNull(entity)
        assertEquals("Super test project 1", entity.name)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldNotGetByIdRemoved() {
        val entity = uut.getById(2)
        assertNull(entity)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldSetRemoved() {
        uut.setRemoved(1)
        val project = uut.findById(1)
        assertEquals(true, project.get().removed)
    }

}