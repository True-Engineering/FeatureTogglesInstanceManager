package ru.trueengineering.featureflag.manager.core.impl.project

import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectProperties

interface ProjectRepository {

    fun deleteProject(projectId: Long)

    fun getById(id: Long): Project

    fun findByNameOrNull(name: String): Project?

    fun addNewEnvironment(projectId: Long, environment: Environment)

    fun updateName(id: Long, projectName: String, )

    fun setProperties(projectId: Long, properties: ProjectProperties)

}