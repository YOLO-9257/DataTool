package com.datatool.exceltooracle.core.generator;

import com.datatool.exceltooracle.core.ProcessContext;
import com.datatool.exceltooracle.core.ValueGenerator;

import java.util.UUID;

/**
 * UUID值生成器
 * 生成全局唯一的UUID字符串
 */
public class UuidValueGenerator implements ValueGenerator {
    
    private final boolean removeDashes;
    
    /**
     * 构造函数，默认保留连字符
     */
    public UuidValueGenerator() {
        this(false);
    }
    
    /**
     * 构造函数
     * 
     * @param removeDashes 是否移除连字符
     */
    public UuidValueGenerator(boolean removeDashes) {
        this.removeDashes = removeDashes;
    }
    
    @Override
    public Object generate(ProcessContext context) {
        String uuid = UUID.randomUUID().toString();
        return removeDashes ? uuid.replace("-", "") : uuid;
    }
    
    @Override
    public String getName() {
        return "UUID生成器";
    }
    
    @Override
    public String getDescription() {
        return "生成全局唯一的UUID字符串" + (removeDashes ? "（不含连字符）" : "");
    }
    
    @Override
    public String getId() {
        return removeDashes ? "uuid-no-dashes" : "uuid";
    }
    
    @Override
    public Class<?> getValueType() {
        return String.class;
    }
} 