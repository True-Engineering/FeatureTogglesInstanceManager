package ru.trueengineering.featureflag.manager.core.impl.environment

import org.springframework.scheduling.annotation.Scheduled

open class InstanceService(
    private val environmentRepository: EnvironmentRepository,
    private val instanceOutOfSyncPeriodSec: Int
) {

    @Scheduled(fixedDelayString = "\${featureFlag.instance.checkStatusPeriod:10000}")
    open fun updateInstanceStatus() {
        environmentRepository.checkAndUpdateInstanceStatus(instanceOutOfSyncPeriodSec)
    }
}