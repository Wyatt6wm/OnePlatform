package run.wyatt.oneplatform.system.aop;

import com.alibaba.fastjson2.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.common.http.R;

/**
 * @author Wyatt
 * @date 2023/7/5 15:56
 */
@Aspect
@Order(1)   // 多个Aspect的执行顺序，越小越先执行
@Component
public class LogAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());    // Slf4j

    /**
     * API切面
     */
    @Pointcut("within(run.wyatt.oneplatform.*.controller.*Controller)")
    public void apiPointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * 进入和退出API时打印日志（环绕通知）
     *
     * @param joinPoint 切点
     * @return 方法return的值
     */
    @Around("apiPointcut()")
    public Object apiAroundLogAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("=== RUN CONTROLLER {}() IN {} ===", joinPoint.getSignature().getName(), joinPoint.getSignature().getDeclaringTypeName());
        try {
            Object apiResult = joinPoint.proceed();
            log.info("响应: {}", JSONObject.toJSONString(apiResult)); // 注意响应数据很庞大的情况
            String status = ((R) apiResult).getSucc() ? "SUCCESS" : "FAIL";
            log.info("=== EXIT CONTROLLER {}() WITH {} ===", joinPoint.getSignature().getName(), status);
            return apiResult;
        } catch (Throwable e) {
            log.info("=== EXIT CONTROLLER {}() WITH ERROR ===", joinPoint.getSignature().getName());
            throw e;
        }
    }

    /**
     * API抛出异常后打印的日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "apiPointcut()", throwing = "e")
    public void apiAfterThrowingLogAdvice(JoinPoint joinPoint, Exception e) {
        log.info("错误: {} {}", e.getMessage(), e.getClass().getName());
    }

    /**
     * Service切面
     */
    @Pointcut("within(run.wyatt.oneplatform.*.service.impl.*ServiceImpl)")
    public void servicePointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * 进入和退出Service时打印日志（环绕通知）
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
     * Service抛出异常后打印的日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "servicePointcut()", throwing = "e")
    public void serviceAfterThrowingLogAdvice(JoinPoint joinPoint, Exception e) {
        log.info("抛出异常: {} {}", e.getMessage(), e.getClass().getName());
    }
}
