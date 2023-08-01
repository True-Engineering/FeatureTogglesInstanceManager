package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent

interface TestEventHandler  {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handle(event: DomainEvent)
}
