package run.wyatt.oneplatform.service;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
public interface IdService {
    Long generateSnowflakeId();

    String generateSimpleUuid();
}
