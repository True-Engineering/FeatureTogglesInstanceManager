package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import javax.persistence.Table
import javax.persistence.Entity
import javax.persistence.Column
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.ManyToOne
import javax.persistence.FetchType
import javax.persistence.JoinColumn

@Table(name = "ENV_EMAIL")
@Entity
open class EmailEntity(
    @Column(name = "EMAIL", nullable = false)
    open var email: String
) :BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    open lateinit var environment: EnvironmentEntity
}