package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import ru.trueengineering.featureflag.manager.core.domen.event.UserCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "USERS")
@Entity
open class UserEntity(
    @Column(name = "EMAIL", nullable = false, unique = true)
    open var email: String
) : BaseEntity() {

    init {
        domainEvents.add(UserCreatedEvent(email))
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    open var id: Long? = null

    @Column(name = "USER_NAME")
    open var name: String? = null

    @Column(name = "USER_STATUS")
    @Enumerated(EnumType.STRING)
    open var status: UserStatus? = null

    @Column(name = "DEFAULT_PROJECT_ID")
    open var defaultProjectId: Long? = null

    @Column(name = "LAST_LOGIN")
    open var lastLogin: Instant? = null

    @Column(name = "REMOVED", nullable = false)
    open var removed: Boolean = false
}