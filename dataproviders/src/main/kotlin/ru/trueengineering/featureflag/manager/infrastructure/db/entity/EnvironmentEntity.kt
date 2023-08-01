package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentProperties
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentPropertiesClass
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

@Table(name = "ENVIRONMENT")
@Entity
open class EnvironmentEntity(
    @Column(name = "ENVIRONMENT_NAME", nullable = false)
    open var name: String
) : BaseEntity(), BusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    open lateinit var project: ProjectEntity

    @Column(name = "AUTH_KEY_HASH")
    open var authKeyHash: String? = null

    @OneToMany(mappedBy = "environment", cascade = [CascadeType.ALL])
    open var instances: MutableSet<InstanceEntity> = mutableSetOf()

    @OneToMany(mappedBy = "environment", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var emails: MutableSet<EmailEntity> = mutableSetOf()

    @Column(name = "REMOVED", nullable = false)
    open var removed: Boolean = false

    @ElementCollection
    @CollectionTable(name = "ENV_CUSTOM_PROPERTIES", joinColumns = [JoinColumn(name = "ENV_ID", referencedColumnName = "ID")])
    @MapKeyColumn(name = "NAME")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "VALUE")
    open var properties: EnvironmentProperties = EnumMap(EnvironmentPropertiesClass::class.java)

    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = Environment::class.java.name

    fun addEmail(email: EmailEntity) {
        emails.add(email)
        email.environment = this
    }

    fun removeEmail(email: EmailEntity) {
        emails.remove(email)
    }
}