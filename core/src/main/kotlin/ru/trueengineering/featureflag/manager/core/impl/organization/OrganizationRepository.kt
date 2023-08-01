package ru.trueengineering.featureflag.manager.core.impl.organization

import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project

interface OrganizationRepository {

    fun findAll(): List<Organization>

    fun findById(organizationId: Long): Organization

    fun findByName(name: String): Organization?

    fun removeById(organizationId: Long)

    fun create(name: String): Organization

    fun addNewProject(organizationId: Long, project: Project) : Project;
}