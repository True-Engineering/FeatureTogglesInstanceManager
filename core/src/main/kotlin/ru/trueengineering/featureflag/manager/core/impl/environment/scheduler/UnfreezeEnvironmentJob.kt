package ru.trueengineering.featureflag.manager.core.impl.environment.scheduler

import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentFacade

@Component
class UnfreezeEnvironmentJob(private val environmentFacade: EnvironmentFacade): QuartzJobBean() {

    @WithAdminRole
    override fun executeInternal(context: JobExecutionContext) {
        val dataMap = context.jobDetail.jobDataMap
        val environmentId = dataMap.getLong("environmentId")
        val projectId = dataMap.getLong("projectId")

        val command = UnfreezeEnvironmentCommand(projectId, environmentId)

        environmentFacade.execute(command)
    }
}