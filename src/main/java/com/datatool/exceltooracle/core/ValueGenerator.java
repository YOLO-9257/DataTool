package com.datatool.exceltooracle.core;

/**
 * 值生成器接口
 * 用于生成字段的值，主要用于主键或特殊字段的自动填充
 */
public interface ValueGenerator {
    
    /**
     * 生成值
     * 
     * @param context 处理上下文
     * @return 生成的值
     */
    Object generate(ProcessContext context);
    
    /**
     * 生成值
     * 
     * @return 生成的值
     */
    default Object generate() {
        return generate(null);
    }
    
    /**
     * 获取生成器名称
     * 
     * @return 生成器名称
     */
    String getName();
    
    /**
     * 获取生成器描述
     * 
     * @return 生成器描述
     */
    String getDescription();
    
    /**
     * 获取生成器ID
     * 
     * @return 生成器ID
     */
    String getId();
    
    /**
     * 获取生成值的类型
     * 
     * @return 生成值的类型
     */
    Class<?> getValueType();
} 