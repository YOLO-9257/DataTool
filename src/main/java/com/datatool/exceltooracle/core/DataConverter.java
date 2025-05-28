package com.datatool.exceltooracle.core;

/**
 * 数据转换器接口
 * 用于将源数据类型转换为目标数据类型
 * 
 * @param <S> 源数据类型
 * @param <T> 目标数据类型
 */
public interface DataConverter<S, T> {
    
    /**
     * 转换数据
     * 
     * @param source 源数据
     * @param context 处理上下文
     * @return 转换后的数据
     * @throws Exception 如果转换过程中出现错误
     */
    T convert(S source, ProcessContext context) throws Exception;
    
    /**
     * 判断是否可以转换
     * 
     * @param sourceClass 源数据类
     * @param targetClass 目标数据类
     * @return 是否可以转换
     */
    boolean canConvert(Class<?> sourceClass, Class<?> targetClass);
    
    /**
     * 获取转换器名称
     * 
     * @return 转换器名称
     */
    String getName();
    
    /**
     * 获取转换器描述
     * 
     * @return 转换器描述
     */
    String getDescription();
} 