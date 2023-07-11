package run.wyatt.oneplatform.system.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.common.exception.DatabaseException;

/**
 * @author Wyatt
 * @date 2023/7/11 17:48
 */
@Slf4j
@Aspect
@Component
public class DaoAspect {
    /**
     * DAO切面
     */
    @Pointcut("bean(*Dao)")
    public void daoPointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * Advice: Dao抛出异常后打印的日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "daoPointcut()", throwing = "e")
    public void daoAfterThrowingLogAdvice(JoinPoint joinPoint, Exception e) {
        log.info("----------");
        log.info("数据库抛出异常详细信息: {}", e.getMessage());
        log.info("----------");
        throw new DatabaseException();
    }
}
