package com.datatool.exceltooracle.core;

import com.datatool.exceltooracle.core.generator.SnowflakeValueGenerator;
import com.datatool.exceltooracle.core.generator.UuidValueGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 值生成器注册表
 * 管理所有可用的值生成器
 */
public class ValueGeneratorRegistry {
    
    private static final ValueGeneratorRegistry INSTANCE = new ValueGeneratorRegistry();
    
    private final Map<String, ValueGenerator> generators = new HashMap<>();
    
    /**
     * 私有构造函数
     * 注册内置的值生成器
     */
    private ValueGeneratorRegistry() {
        // 注册默认的值生成器
        register(new SnowflakeValueGenerator());
        register(new UuidValueGenerator());
        register(new UuidValueGenerator(true));
        
        // 尝试注册时间戳生成器
        try {
            Class<?> clazz = Class.forName("com.datatool.exceltooracle.core.generator.TimestampValueGenerator");
            ValueGenerator generator = (ValueGenerator) clazz.getDeclaredConstructor().newInstance();
            register(generator);
        } catch (Exception e) {
            // 忽略异常，可能时间戳生成器尚未实现
        }
    }
    
    /**
     * 获取单例实例
     * 
     * @return 值生成器注册表实例
     */
    public static ValueGeneratorRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册值生成器
     * 
     * @param generator 值生成器
     */
    public void register(ValueGenerator generator) {
        generators.put(generator.getId(), generator);
    }
    
    /**
     * 注销值生成器
     * 
     * @param generatorId 生成器ID
     */
    public void unregister(String generatorId) {
        generators.remove(generatorId);
    }
    
    /**
     * 获取值生成器
     * 
     * @param generatorId 生成器ID
     * @return 值生成器
     */
    public ValueGenerator getGenerator(String generatorId) {
        return generators.get(generatorId);
    }
    
    /**
     * 获取所有值生成器
     * 
     * @return 所有值生成器
     */
    public Collection<ValueGenerator> getAllGenerators() {
        return generators.values();
    }
    
    /**
     * 检查生成器是否存在
     * 
     * @param generatorId 生成器ID
     * @return 是否存在
     */
    public boolean hasGenerator(String generatorId) {
        return generators.containsKey(generatorId);
    }
} 