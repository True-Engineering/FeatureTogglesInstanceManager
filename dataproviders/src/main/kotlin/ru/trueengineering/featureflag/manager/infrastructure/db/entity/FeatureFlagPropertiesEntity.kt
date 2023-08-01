package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagPropertiesClass
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

@Table(name = "FF_CUSTOM_PROPERTIES")
@Entity
open class FeatureFlagPropertiesEntity(
    @Column(name = "NAME", nullable = false)
    @Enumerated(EnumType.STRING)
    open var name: FeatureFlagPropertiesClass,

    @Column(name = "VALUE")
    open var value: String? = null
): BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROPERTY_ID", nullable = false)
    open var id: Long? = null

    @Column(name = "DESCRIPTION")
    open var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FEAT_ID", nullable = false)
    open lateinit var featureFlag: FeatureFlagEntity
}