package run.wyatt.oneplatform.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author Wyatt
 * @date 2023/7/8 1:06
 */
@Slf4j
@Aspect
@Component
public class ServiceAspect {
    /**
     * Service切面
     */
    @Pointcut("within(run.wyatt.oneplatform.*.service.impl.*ServiceImpl)")
    public void servicePointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * Advice: 进入和退出Service时打印日志（环绕通知）
     *
     * @param joinPoint 切点
     * @return 方法return的值
     */
    @Around("servicePointcut()")
    public Object serviceAroundLogAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("--- CALL SERVICE {}() IN {} ---", joinPoint.getSignature().getName(), joinPoint.getSignature().getDeclaringTypeName());
        try {
            Object serviceReturn = joinPoint.proceed();
            log.info("--- EXIT SERVICE {}() ---", joinPoint.getSignature().getName());
            return serviceReturn;
        } catch (Throwable e) {
            log.info("--- EXIT SERVICE {}() WITH EXCEPTION ---", joinPoint.getSignature().getName());
            throw e;
        }
    }

    /**
     * Advice: Service抛出异常后打印的日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "servicePointcut()", throwing = "e")
    public void serviceAfterThrowingLogAdvice(JoinPoint joinPoint, Exception e) {
        log.info("抛出异常: {} {}", e.getMessage(), e.getClass().getName());
    }
}
