package com.datatool.exceltooracle.core.generator;

import com.datatool.exceltooracle.core.ProcessContext;
import com.datatool.exceltooracle.core.ValueGenerator;
import com.datatool.exceltooracle.util.SnowflakeIdGenerator;

/**
 * 雪花算法值生成器
 * 使用SnowflakeIdGenerator生成全局唯一ID
 */
public class SnowflakeValueGenerator implements ValueGenerator {
    
    private final SnowflakeIdGenerator idGenerator;
    
    /**
     * 构造函数，使用默认的SnowflakeIdGenerator实例
     */
    public SnowflakeValueGenerator() {
        this.idGenerator = SnowflakeIdGenerator.getInstance();
    }
    
    /**
     * 构造函数，指定工作机器ID和数据中心ID
     * 
     * @param workerId 工作机器ID
     * @param datacenterId 数据中心ID
     */
    public SnowflakeValueGenerator(long workerId, long datacenterId) {
        this.idGenerator = SnowflakeIdGenerator.getInstance(workerId, datacenterId);
    }
    
    @Override
    public Object generate(ProcessContext context) {
        return idGenerator.nextId();
    }
    
    @Override
    public String getName() {
        return "雪花算法ID生成器";
    }
    
    @Override
    public String getDescription() {
        return "使用雪花算法生成分布式全局唯一ID";
    }
    
    @Override
    public String getId() {
        return "snowflake";
    }
    
    @Override
    public Class<?> getValueType() {
        return Long.class;
    }
} 