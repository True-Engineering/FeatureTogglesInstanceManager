package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import java.time.Instant
import java.util.Collections
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass
import kotlin.collections.ArrayList

@EntityListeners(value = arrayOf(AuditingEntityListener::class))
@MappedSuperclass
open class BaseEntity {

    @CreatedDate
    @Column(name = "CREATED")
    open var created: Instant? = null

    @LastModifiedDate
    @Column(name = "UPDATED")
    open var updated: Instant? = null;

    /**
     * todo возможно не всем entity нужна такая функциональность -
     */
    @Transient
    protected val domainEvents: ArrayList<DomainEvent> = ArrayList()

    @DomainEvents
    fun domainEvents() : Collection<DomainEvent> = Collections.unmodifiableList(domainEvents)

    @AfterDomainEventPublication
    fun clearDomainEvents() = domainEvents.clear()

    //https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing.auditor-aware Нужен SecurityCOntext
//    @LastModifiedBy
//    @Column(name = "updated_by")
//    open var updatedBy: Instant? = null
//
//    @CreatedBy
//    @Column(name = "created_by")
//    open var createdBy: Instant? = null

}