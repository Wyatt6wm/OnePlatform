package run.wyatt.oneplatform.service.impl;

import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.model.entity.support.SnowflakeIdGenerator;
import run.wyatt.oneplatform.service.IdService;

import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
@Service
public class IdServiceImpl implements IdService {

    @Override
    public Long generateSnowflakeId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator();
        return generator.snowflakeId();
    }

    @Override
    public String generateSimpleUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
