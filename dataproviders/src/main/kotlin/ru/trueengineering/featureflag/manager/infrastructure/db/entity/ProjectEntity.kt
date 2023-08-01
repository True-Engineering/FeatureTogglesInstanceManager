package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.Where
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.event.EnvironmentCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectProperties
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectPropertiesClass
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import java.io.Serializable
import java.util.EnumMap
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapKeyColumn
import javax.persistence.MapKeyEnumerated
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "PROJECT")
@Entity
open class ProjectEntity(
    @Column(name = "PROJECT_NAME", nullable = false)
    open var name: String
) : BaseEntity(), BusinessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    /**
     * Use lateinit on fields that are guaranteed to be non-null in the DB.
     * This allows the use of Kotlin nullability mechanism without removing the no-arg constructor.
     * NOTE: lateinit does not work with primitive types such as Long
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "ORGANIZATION_ID", nullable = false)
    open lateinit var organization: OrganizationEntity

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL])
    @Where(clause = "removed <> true")
    open var environments: MutableSet<EnvironmentEntity> = mutableSetOf()

    @Column(name = "REMOVED", nullable = false)
    open var removed: Boolean = false

    @ElementCollection
    @CollectionTable(name = "PROJECT_CUSTOM_PROPERTIES", joinColumns = [JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID")])
    @MapKeyColumn(name = "NAME")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "VALUE")
    open var properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)

    fun addNewEnvironment(env: EnvironmentEntity) : ProjectEntity {
        if (environments.any { it.name == env.name }) {
            throw ServiceException(ErrorCode.ENVIRONMENT_ALREADY_EXIST)
        }
        environments.add(env)
        env.project = this
        domainEvents.add(EnvironmentCreatedEvent(env.name, id!!, this))
        return this
    }

    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = Project::class.java.name

}