package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "INVITATION")
@Entity
open class InvitationEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open var id: UUID
): BaseEntity() {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, unique = true)
    open lateinit var project: ProjectEntity

}