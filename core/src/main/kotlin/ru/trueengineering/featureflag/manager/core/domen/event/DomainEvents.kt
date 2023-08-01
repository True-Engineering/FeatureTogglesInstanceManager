package ru.trueengineering.featureflag.manager.core.domen.event

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag

sealed interface DomainEvent

data class OrganizationCreatedEvent(val organizationName: String) : DomainEvent

data class ProjectCreatedEvent(val projectName: String,
                               val organizationId: Long,
                               val organizationBusinessEntity: BusinessEntity) : DomainEvent

data class EnvironmentCreatedEvent(val environmentName: String,
                                   val projectId: Long,
                                   val projectBusinessEntity: BusinessEntity) : DomainEvent

data class UserCreatedEvent(val userEmail: String) : DomainEvent

data class FeatureFlagNewActionEvent(val action: ChangeAction,
                                     val projectId: Long,
                                     val featureUid: String,
                                     val environmentId: Long? = null,
                                     val changes: FeatureChanges? = null,
                                     val creationInfo: FeatureFlag? = null): DomainEvent