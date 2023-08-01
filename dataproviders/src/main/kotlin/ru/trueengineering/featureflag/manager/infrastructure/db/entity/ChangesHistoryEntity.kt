package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import kotlinx.serialization.Serializable
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "FF_CHANGES_HISTORY")
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
open class ChangesHistoryEntity(
    @Column(name = "ACTION")
    @Enumerated(EnumType.STRING)
    open var action: ChangeAction
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    open lateinit var project: ProjectEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    open lateinit var user: UserEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ENVIRONMENT_ID")
    open var environment: EnvironmentEntity? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FEATURE_ID", nullable = false)
    open lateinit var featureFlag: FeatureFlagEntity

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "CHANGES")
    @Basic(fetch = FetchType.LAZY)
    open var featureChanges: FeatureChangesEntity? = null

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "CREATION_INFO")
    @Basic(fetch = FetchType.LAZY)
    open var creationInfo: FeatureFlag? = null

    fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}

@Serializable
data class FeatureChangesEntity(
    val changes: MutableMap<String, DifferenceEntity>? = mutableMapOf()
)

data class DifferenceEntity(
    var old: Any?,
    var new: Any?
)