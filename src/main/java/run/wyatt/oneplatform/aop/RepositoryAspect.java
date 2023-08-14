package run.wyatt.oneplatform.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.model.exception.DatabaseException;

/**
 * @author Wyatt
 * @date 2023/7/11 17:48
 */
@Slf4j
@Aspect
@Component
public class RepositoryAspect {
    /**
     * DAO切面
     */
    @Pointcut("within(run.wyatt.oneplatform.repository.*)")
    public void repositoryPointcut() {
        // 方法为空，仅用来定义切入点，逻辑另外实现。
    }

    /**
     * Advice: Repository抛出异常后打印的日志
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "repositoryPointcut()", throwing = "e")
    public void repositoryAfterThrowingLogAdvice(JoinPoint joinPoint, Exception e) {
        // 根据数据库异常类型推断业务异常信息的异常，拦截抛出
        if (e instanceof DataIntegrityViolationException) {
            return;
        }

        // 其他数据库异常类型打印日志，抛出数据库异常
        log.info("----------");
        log.info("数据库抛出异常详细信息: {}", e.getMessage());
        log.info("----------");
        throw new DatabaseException();
    }
}
