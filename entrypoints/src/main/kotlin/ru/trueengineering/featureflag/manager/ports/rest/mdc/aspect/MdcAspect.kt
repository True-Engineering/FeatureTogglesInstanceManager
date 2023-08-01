package ru.trueengineering.featureflag.manager.ports.rest

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.ports.rest.mdc.MDCProperties
import ru.trueengineering.featureflag.manager.ports.rest.mdc.MDCProperty
import ru.trueengineering.featureflag.manager.ports.service.EnvironmentService
import ru.trueengineering.featureflag.manager.ports.service.OrganizationService
import ru.trueengineering.featureflag.manager.ports.service.ProjectService

@Aspect
@Component
class MdcAspect(
        val organizationService: OrganizationService,
        val projectService: ProjectService,
        val environmentService: EnvironmentService
) : Ordered {

    @Around("@within(org.springframework.web.bind.annotation.RestController) && execution(public * *(..))")
    @Throws(Throwable::class)
    fun execute(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val parameters = signature.method.parameters
        val mdcProperties = ArrayList<MDCProperty>()
        for ((index, parameter) in parameters.withIndex()) {
            if (parameter.isAnnotationPresent(OrganizationId::class.java)) {
                val organizationId = joinPoint.args[index] as Long
                mdcProperties.add(MDCProperty("organizationName", organizationService.searchById(organizationId).name))
            } else if (parameter.isAnnotationPresent(ProjectId::class.java)) {
                val projectId = joinPoint.args[index] as Long
                mdcProperties.add(MDCProperty("projectName", projectService.searchById(projectId).name))
            } else if (parameter.isAnnotationPresent(EnvironmentId::class.java)) {
                val environmentId = joinPoint.args[index] as Long
                mdcProperties.add(MDCProperty("environmentName", environmentService.searchById(environmentId).name))
            } else if (parameter.isAnnotationPresent(FeatureFlagId::class.java)) {
                mdcProperties.add(MDCProperty("featureFLagUUID", joinPoint.args[index]))
            }
        }
        return MDCProperties(mdcProperties).use { joinPoint.proceed() }
    }

    /**
     * Нужно, чтобы соблюсти порядок нашей аннотации и аннотации @Transactional
     * У аннотации @Transactional порядок [Integer.MAX_VALUE], что значит наименьший ордер
     */
    override fun getOrder(): Int = 0

}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class OrganizationId()

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProjectId()

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvironmentId()

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureFlagId()
