package ru.trueengineering.featureflag.manager.authorization.annotation

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.Ordered
import org.springframework.stereotype.Component


@Aspect
@Component
class WithAdminRoleAspect : Ordered {

    @Around("@annotation(WithAdminRole)")
    @Throws(Throwable::class)
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        return AspectUtils.executeWithAdminRole { joinPoint.proceed() }
    }

    /**
     * Нужно, чтобы соблюсти порядок нашей аннотации и аннотации @Transactional
     * У аннотации @Transactional порядок [Integer.MAX_VALUE], что значит наименьший ордер
     */
    override fun getOrder(): Int = 0
}