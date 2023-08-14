package run.wyatt.oneplatform.model.entity.support;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import run.wyatt.oneplatform.util.SnowflakeUtil;

import java.io.Serializable;

/**
 * @author Wyatt
 * @date 2023/8/11
 */
public class SnowflakeIdGenerator extends IdentityGenerator {
    private static final long WORKER_ID = 1L;
    private static final long DATACENTER_ID = 1L;
    private final SnowflakeUtil snowflakeUtil = new SnowflakeUtil(WORKER_ID, DATACENTER_ID);

    synchronized public long snowflakeId() {
        return snowflakeUtil.nextId();
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return snowflakeId();
    }
}
