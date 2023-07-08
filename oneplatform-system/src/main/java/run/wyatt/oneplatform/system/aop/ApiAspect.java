package run.wyatt.oneplatform.system.aop;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.alibaba.fastjson2.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.common.http.Data;
import run.wyatt.oneplatform.common.http.R;

/**
 * Around --> Before --> method --> After --> AfterReturning --> AfterThrowing --> Around
 *
 * @author Wyatt
 * @date 2023/7/5 15:56
 */
@Aspect
@Component
public class ApiAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());    // Slf4j

    /**
     * API切面
     */
    @Pointcut("within(run.wyatt.oneplatform.*.controller.*Controller)")
    public void apiPointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * Advice: 进入和退出API时打印日志，封装返回结果
     *
     * @param joinPoint 切点
     * @return 方法return的值
     */
    @Around("apiPointcut()")
    public Object apiAroundAdvice(ProceedingJoinPoint joinPoint) {
        log.info("=== RUN CONTROLLER {}() IN {} ===", joinPoint.getSignature().getName(), joinPoint.getSignature().getDeclaringTypeName());
        Object apiResult = null;
        try {
            apiResult = joinPoint.proceed();
            log.info("=== EXIT CONTROLLER {}() WITH {} ===", joinPoint.getSignature().getName(), ((R) apiResult).getSucc() ? "SUCCESS" : "FAIL");
            return apiResult;
        } catch (Throwable e) { // 当API抛出异常时，封装成失败的响应信息
            int code = 500;
            String mesg = "服务器内部错误";

            // 未登录
            if (e instanceof NotLoginException) {
                code = 401;
                mesg = "未登录";
            }
            // 鉴权失败
            if (e instanceof NotRoleException || e instanceof NotPermissionException) {
                code = 503;
                mesg = "服务不可用";
            }

            Data data = new Data();
            data.put("code", code);
            R r = R.fail(mesg, data);
            r.setTraceId(MDC.get("RID"));
            log.info("响应: {}", JSONObject.toJSONString(r)); // 注意响应数据很庞大的情况
            log.info("=== EXIT CONTROLLER {}() WITH FAIL ===", joinPoint.getSignature().getName());
            return r;
        }
    }

    /**
     * Advice: 为API的响应数据装配上traceId
     *
     * @param joinPoint 切点
     * @param r         主方法的返回
     */
    @AfterReturning(pointcut = "apiPointcut()", returning = "r")
    public void apiAfterReturningAdvice(JoinPoint joinPoint, R r) {
        r.setTraceId(MDC.get("RID"));
        log.info("响应: {}", JSONObject.toJSONString(r)); // 注意响应数据很庞大的情况
    }

    /**
     * Advice: 当API抛出异常时，打印日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "apiPointcut()", throwing = "e")
    public void apiAfterThrowingAdvice(JoinPoint joinPoint, Exception e) {
        log.info("错误: {} {}", e.getMessage(), e.getClass().getName());
    }
}
