package ru.trueengineering.featureflag.manager.core.impl.changes

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordCommand
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordUseCase
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEventHandler
import ru.trueengineering.featureflag.manager.core.domen.event.FeatureFlagNewActionEvent

@Component
class FeatureFlagNewActionEventHandler(
    private val createChangesHistoryRecordUseCase: CreateChangesHistoryRecordUseCase
): DomainEventHandler<FeatureFlagNewActionEvent> {

    private val log = LoggerFactory.getLogger(javaClass)

    @WithAdminRole
    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    override fun handle(event: FeatureFlagNewActionEvent) {
        createChangesHistoryRecordUseCase.execute(
            CreateChangesHistoryRecordCommand(
                event.action,
                event.projectId,
                event.featureUid,
                event.environmentId,
                event.changes,
                event.creationInfo
            )
        )

        log.debug("Handle new event - has created changesHistoryRecord for " +
                "${event.featureUid} feature flag with ${event.action} action")
    }
}