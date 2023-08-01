package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table


@Table(name = "FEATURE_ENVIRONMENT_STATE")
@Entity
open class FeatureFlagEnvironmentStateEntity(
    @Column(name = "ENABLE", nullable = false)
    open val enable: Boolean
) : BusinessEntity, BaseEntity() {
    @EmbeddedId
    open var primaryKey: FeatureFlagEnvironmentStatePK? = null

    @Column(name = "STRATEGY")
    open var strategy: String? = null

    @Column(name = "STRATEGY_PARAMS")
    open var strategyParams: String? = null
    override fun getBusinessId() = primaryKey?.environment?.id

    override fun getType(): String = Environment::class.java.name

}

@Embeddable
open class FeatureFlagEnvironmentStatePK(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    open var environment: EnvironmentEntity
) : java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FEATURE_ID", nullable = false)
    open lateinit var featureFlag: FeatureFlagEntity

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeatureFlagEnvironmentStatePK) return false

        if (environment != other.environment) return false
        if (featureFlag != other.featureFlag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = environment.hashCode()
        result = 31 * result + featureFlag.hashCode()
        return result
    }
}