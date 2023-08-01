package ru.trueengineering.featureflag.manager.core.domen.organization

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.user.User

data class Organization(val id: Long, val name: String,
                        val projects: List<Project> = listOf()) : BusinessEntity {
    override fun getBusinessId() = id

    override fun getType(): String = this.javaClass.name
}

data class OrganizationUser(
        val user: User,
        val projects: List<ProjectUser>
)