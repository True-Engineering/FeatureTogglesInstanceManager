package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrganizationJpaRepositoryTest(
    @Autowired override var uut: OrganizationJpaRepository
) : JpaRepositoryBaseTest<OrganizationJpaRepository>() {

    @Test
    internal fun shouldCreateNewOrganization() {
        val actualEntity = uut.save(OrganizationEntity("Org"))
        assertEquals("Org", actualEntity.name)
        assertNotNull(actualEntity.id)
        assertNotNull(actualEntity.created)
        assertNotNull(actualEntity.updated)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldDeleteOrganization() {
        val organization = uut.findById(1)
        assertTrue(organization.isPresent)

        uut.delete(organization.get())

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldDeleteOrganizationById() {
        val organization = uut.findById(1)
        assertTrue(organization.isPresent)

        uut.deleteById(1)

        assertFalse(uut.findById(1).isPresent)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldFetchAll() {
        val organizations = uut.findAll()
        assertEquals(2, organizations.count())
        organizations.forEach { it.projects.forEach{ prj -> prj.environments.map { env -> env.name }.forEach{println()} } }
        assertEquals(2, organizations.count())
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldFetchById() {
        val organizationOpt = uut.findById(1)
        assertTrue(organizationOpt.isPresent)
        val entity = organizationOpt.get()
        assertEquals("organization_1", entity.name)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldntFetchRemovedProjects() {
        val organizationOpt = uut.findById(1)
        assertTrue(organizationOpt.isPresent)
        val entity = organizationOpt.get()
        assertEquals(1, entity.projects.size)
        assertEquals("Super test project 1", entity.projects.first().name)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldRemove() {
        uut.setRemoved(1)
        val organizationEntity = uut.findById(1).get()
        assertTrue(organizationEntity.removed)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldGetNonRemoved() {
        uut.setRemoved(1)
        val organizationEntity = uut.getNonRemovedById(1)
        assertNull(organizationEntity)
    }

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun shouldGetAllNonRemoved() {
        uut.setRemoved(1)
        val organizationEntities = uut.getAllNonRemoved()
        assertEquals(1, organizationEntities.size)
        assertEquals(20, organizationEntities[0].id)
    }

}