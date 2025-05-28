package com.datatool.exceltooracle.util;

/**
 * 雪花算法ID生成器
 * <p>
 * 雪花算法是一种分布式ID生成算法，能够生成全局唯一的ID。
 * 默认结构：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位符号位 - 41位时间戳 - 5位数据中心ID - 5位工作机器ID - 12位序列号
 * </p>
 */
public class SnowflakeIdGenerator {

    // 开始时间戳 (2024-01-01)，可通过构造函数自定义
    private final long startEpoch;
    // 默认起始时间戳：2024-01-01
    private static final long DEFAULT_START_EPOCH = 1704067200000L;

    // 每部分占用的位数
    private static final long SEQUENCE_BITS = 12L;      // 序列号占用位数
    private static final long WORKER_ID_BITS = 5L;      // 机器ID占用位数
    private static final long DATACENTER_ID_BITS = 5L;  // 数据中心ID占用位数

    // 各部分最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);         // 最大机器ID: 31
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 最大数据中心ID: 31
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);           // 最大序列号: 4095

    // 各部分向左的位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;                             // 机器ID左移位数: 12
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;        // 数据中心ID左移位数: 17
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS; // 时间戳左移位数: 22

    // 时钟回拨最大容忍毫秒数
    private static final long MAX_BACKWARD_MS = 5L;

    private final long workerId;        // 工作机器ID
    private final long datacenterId;    // 数据中心ID
    private long sequence = 0L;         // 序列号
    private long lastTimestamp = -1L;   // 上次生成ID的时间戳

    private static volatile SnowflakeIdGenerator instance;

    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        this(workerId, datacenterId, DEFAULT_START_EPOCH);
    }

    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @param startEpoch 起始时间戳，用于计算相对时间
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId, long startEpoch) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_ID + " or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.startEpoch = startEpoch;
    }

    /**
     * 获取单例实例，默认使用0作为工作机器ID和数据中心ID
     */
    public static SnowflakeIdGenerator getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    // 默认使用0作为工作机器ID和数据中心ID
                    instance = new SnowflakeIdGenerator(0, 0);
                }
            }
        }
        return instance;
    }

    /**
     * 获取自定义参数的单例实例
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public static SnowflakeIdGenerator getInstance(long workerId, long datacenterId) {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(workerId, datacenterId);
                }
            }
        }
        return instance;
    }

    /**
     * 获取自定义参数的单例实例
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @param startEpoch 起始时间戳
     */
    public static SnowflakeIdGenerator getInstance(long workerId, long datacenterId, long startEpoch) {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(workerId, datacenterId, startEpoch);
                }
            }
        }
        return instance;
    }

    /**
     * 获取下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= MAX_BACKWARD_MS) {
                // 如果时钟回拨在容忍范围内，等待时钟追上来
                try {
                    Thread.sleep(offset);
                    timestamp = System.currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        // 还是小于，使用上一次的时间戳
                        timestamp = lastTimestamp;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Clock moved backwards. Interrupted while waiting.", e);
                }
            } else {
                // 超出容忍范围，抛出异常
                throw new RuntimeException("Clock moved backwards. Refusing to generate id for " +
                        offset + " milliseconds");
            }
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = getNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组合成64位的ID
        return ((timestamp - startEpoch) << TIMESTAMP_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long getNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 从ID中解析出时间戳
     * @param id 雪花算法生成的ID
     * @return 时间戳
     */
    public long getTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + startEpoch;
    }

    /**
     * 从ID中解析出数据中心ID
     * @param id 雪花算法生成的ID
     * @return 数据中心ID
     */
    public long getDatacenterId(long id) {
        return (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 从ID中解析出工作机器ID
     * @param id 雪花算法生成的ID
     * @return 工作机器ID
     */
    public long getWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 从ID中解析出序列号
     * @param id 雪花算法生成的ID
     * @return 序列号
     */
    public long getSequence(long id) {
        return id & MAX_SEQUENCE;
    }

    /**
     * 获取当前工作机器ID
     * @return 工作机器ID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 获取当前数据中心ID
     * @return 数据中心ID
     */
    public long getDatacenterId() {
        return datacenterId;
    }

    /**
     * 获取起始时间戳
     * @return 起始时间戳
     */
    public long getStartEpoch() {
        return startEpoch;
    }
}
