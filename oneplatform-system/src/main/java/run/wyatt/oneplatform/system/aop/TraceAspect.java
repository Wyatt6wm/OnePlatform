package run.wyatt.oneplatform.system.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.common.http.R;

/**
 * @author Wyatt
 * @date 2023/7/6 9:53
 */
@Aspect
@Order(2)   // 多个Aspect的执行顺序，越小越先执行
@Component
public class TraceAspect {
    /**
     * API切面
     */
    @Pointcut("within(run.wyatt.oneplatform.*.controller.*Controller)")
    public void apiPointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * 为API的响应数据装配上traceId的通知
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "apiPointcut()", returning = "response")
    public void apiTraceIdAdvice(JoinPoint joinPoint, R response) {
        response.setTraceId(MDC.get("RID"));
    }
}
