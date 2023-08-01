package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagProperties
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagPropertiesClass
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import java.util.EnumMap
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
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

@Table(name = "FEATURES")
@Entity
open class FeatureFlagEntity(
    @Column(name = "FEAT_UID", nullable = false)
    open val uid: String
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @Column(name = "DESCRIPTION")
    open var description: String? = null

    @Column(name = "FEATURE_GROUP")
    open var group: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "FEATURE_TYPE")
    open var type: FeatureFlagType? = null

    @Column(name = "SPRINT")
    open var sprint: String? = null

    @Column(name = "FEATURE_TAG")
    open var tag: String? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    open lateinit var project: ProjectEntity

    @OneToMany(mappedBy = "primaryKey.featureFlag", cascade = [CascadeType.ALL])
    open var environments: MutableSet<FeatureFlagEnvironmentStateEntity> = mutableSetOf()

    @Column(name = "REMOVED", nullable = false)
    open var removed: Boolean = false

    @ElementCollection
    @CollectionTable(name = "FF_CUSTOM_PROPERTIES", joinColumns = [JoinColumn(name = "FEAT_ID", referencedColumnName = "ID")])
    @MapKeyColumn(name = "NAME")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "VALUE")
    open var properties: FeatureFlagProperties = EnumMap(FeatureFlagPropertiesClass::class.java)

    fun addNewFeatureFlagEnvironment(newEnv: FeatureFlagEnvironmentStateEntity) {
        environments.add(newEnv)
    }

    fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}