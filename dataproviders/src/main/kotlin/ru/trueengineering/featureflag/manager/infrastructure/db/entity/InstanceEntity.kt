package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
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

@Table(name = "ENV_INSTANCE")
@Entity
open class InstanceEntity(
    @Column(name = "INSTANCE_NAME", nullable = false)
    open val name: String,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    open lateinit var environment: EnvironmentEntity

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    open var status: InstanceConnectionStatus? = InstanceConnectionStatus.ACTIVE
}