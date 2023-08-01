package ru.trueengineering.featureflag.manager.core.impl.environment.scheduler

import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.Date

@Component
class DelayUnfreezeService(private val scheduler: Scheduler) {
    fun delayUnfreeze(projectId: Long, environmentId: Long, endTime: OffsetDateTime) {
        val jobKey = JobKey("unfreezeEnvironmentJob-$projectId-$environmentId", "environmentGroup")
        val jobDetail = JobBuilder.newJob(UnfreezeEnvironmentJob::class.java)
            .withIdentity(jobKey)
            .usingJobData("projectId", projectId)
            .usingJobData("environmentId", environmentId)
            .storeDurably()
            .build()

        val triggerKey = TriggerKey("unfreezeEnvironmentTrigger-$projectId-$environmentId", "environmentGroup")
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(Date.from(endTime.toInstant()))
            .forJob(jobDetail)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build()

        scheduler.scheduleJob(jobDetail, trigger)

        if (!scheduler.isStarted) {
            scheduler.start()
        }
    }

    fun deleteUnfreezingJob(projectId: Long, environmentId: Long) {
        val jobKey = JobKey("unfreezeEnvironmentJob-$projectId-$environmentId", "environmentGroup")

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
    }
}