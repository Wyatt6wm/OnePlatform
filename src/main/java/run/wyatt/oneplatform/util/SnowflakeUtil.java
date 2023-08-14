package run.wyatt.oneplatform.util;

/**
 * 雪花算法
 * 算法原理解析：https://juejin.cn/post/7082669476658806792
 *
 * @author Wyatt
 * @date 2023/8/11
 */
public class SnowflakeUtil {
    private static final long DATACENTER_ID_BITS = 5L;  // 机房ID位数
    private static final long WORKER_ID_BITS = 5L;      // 机器ID位数
    private static final long SEQUENCE_BITS = 12L;      // 序列号位数
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 机房ID最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);         // 机器ID最大值
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);          // 序列号掩码
    private static final long TIMESTAMP_LSHIFT = DATACENTER_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS;   // 时间戳左移位数
    private static final long DATACENTER_ID_LSHIFT = WORKER_ID_BITS + SEQUENCE_BITS;                    // 机房ID左移位数
    private static final long WORKER_ID_LSHIFT = SEQUENCE_BITS;                                         // 机器ID左移位数
    private static final long INIT_TIMESTAMP = 1691719990757L;  // 初始基准时间戳
    private long datacenterId = 1L;  // 机房ID
    private long workerId = 1L;      // 机器ID
    private long sequence;      // 序列号
    private long lastTimeMillis = -1L;

    public SnowflakeUtil() {}

    public SnowflakeUtil(long workerId, long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(String.format(" 0 <= workId <= %d", MAX_WORKER_ID));
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(String.format("0 <= datacenterId <= %d", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获取下一个ID
     *
     * @return ID
     */
    synchronized public long nextId() {
        long currentTimeMillis = System.currentTimeMillis();
        // 当前时间小于上次时间，可能出现服务器时钟回拨
        if (currentTimeMillis < lastTimeMillis) {
            throw new RuntimeException(String.format("服务器时钟回拨，%dms内服务不可用", lastTimeMillis - currentTimeMillis));
        }
        // 在同一毫秒内则序列号递增，否则重置序列号
        if (currentTimeMillis == lastTimeMillis) {
            // 根据掩码与运算判断序列号是否超出最大值
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimeMillis = nextMillis(lastTimeMillis);
            }
        } else {
            sequence = 0;
        }
        // 更新时间
        lastTimeMillis = currentTimeMillis;

        // 根据雪花算法规则拼装ID
        // 0 11111111111111111111111111111111111111111 11111 11111 111111111111
        //                   毫秒时间戳                机房ID 机器ID    序列号
        return ((currentTimeMillis - INIT_TIMESTAMP) << TIMESTAMP_LSHIFT)
                | (datacenterId << DATACENTER_ID_LSHIFT)
                | (workerId << WORKER_ID_LSHIFT)
                | sequence;
    }

    /**
     * 获取下一个毫秒值
     *
     * @param lastTimeMillis 上次时间
     * @return 下一个时间
     */
    private long nextMillis(long lastTimeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        while (currentTimeMillis <= lastTimeMillis) {
            currentTimeMillis = System.currentTimeMillis();
        }
        return currentTimeMillis;
    }
}
