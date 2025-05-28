package com.datatool.exceltooracle.pipeline;

import com.datatool.exceltooracle.core.DataSource;
import com.datatool.exceltooracle.core.DataTarget;

/**
 * 管道处理监听器接口
 * 用于监听数据处理管道的各个事件
 * 
 * @param <S> 源数据类型
 * @param <T> 目标数据类型
 */
public interface PipelineListener<S, T> {
    
    /**
     * 管道处理开始事件
     * 
     * @param source 数据源
     * @param target 数据目标
     */
    default void onPipelineStart(DataSource<S> source, DataTarget<T> target) {}
    
    /**
     * 管道处理完成事件
     * 
     * @param totalRecords 总记录数
     * @param successRecords 成功记录数
     * @param failureRecords 失败记录数
     */
    default void onPipelineComplete(int totalRecords, int successRecords, int failureRecords) {}
    
    /**
     * 批处理完成事件
     * 
     * @param batchSize 批次大小
     */
    default void onBatchComplete(int batchSize) {}
    
    /**
     * 记录处理开始事件
     * 
     * @param sourceData 源数据
     */
    default void onRecordStart(S sourceData) {}
    
    /**
     * 记录处理成功事件
     * 
     * @param sourceData 源数据
     * @param targetData 目标数据
     */
    default void onRecordSuccess(S sourceData, T targetData) {}
    
    /**
     * 记录处理失败事件
     * 
     * @param sourceData 源数据
     * @param errorMessage 错误信息
     */
    default void onRecordError(S sourceData, String errorMessage) {}
} 