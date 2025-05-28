package com.datatool.exceltooracle.core;

/**
 * 数据处理器接口
 * 
 * @param <I> 输入数据类型
 * @param <O> 输出数据类型
 */
public interface DataProcessor<I, O> {
    
    /**
     * 处理数据
     * 
     * @param input 输入数据
     * @param context 处理上下文
     * @return 处理后的数据
     * @throws Exception 如果处理过程中出现错误
     */
    O process(I input, ProcessContext context) throws Exception;
    
    /**
     * 获取处理器名称
     * 
     * @return 处理器名称
     */
    String getName();
    
    /**
     * 获取处理器描述
     * 
     * @return 处理器描述
     */
    String getDescription();
    
    /**
     * 获取处理器优先级
     * 数字越小优先级越高
     * 
     * @return 处理器优先级
     */
    default int getOrder() {
        return 0;
    }
} 