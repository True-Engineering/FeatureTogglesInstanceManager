package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.hibernate.annotations.Where
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.event.OrganizationCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.event.ProjectCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import java.io.Serializable
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "ORGANIZATION")
@Entity
open class OrganizationEntity(
    @Column(name = "ORGANIZATION_NAME", nullable = false, unique = true)
    open val name: String
) : BaseEntity(), BusinessEntity {

    init {
        domainEvents.add(OrganizationCreatedEvent(name))
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL])
    @Where(clause = "removed <> true")
    open var projects: MutableSet<ProjectEntity> = mutableSetOf()

    @Column(name = "REMOVED", nullable = false)
    open var removed: Boolean = false

    fun addNewProject(project: ProjectEntity) : OrganizationEntity {
        if (projects.any { it.name == project.name }) {
            throw ServiceException(ErrorCode.PROJECT_ALREADY_EXIST)
        }
        projects.add(project)
        project.organization = this
        domainEvents.add(ProjectCreatedEvent(project.name, id!!, this))
        return this
    }

    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = Organization::class.java.name
}