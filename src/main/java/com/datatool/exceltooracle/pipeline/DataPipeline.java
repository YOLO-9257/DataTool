package com.datatool.exceltooracle.pipeline;

import com.datatool.exceltooracle.core.DataProcessor;
import com.datatool.exceltooracle.core.DataSource;
import com.datatool.exceltooracle.core.DataTarget;
import com.datatool.exceltooracle.core.ProcessContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据处理管道
 * 用于构建和执行数据处理流程
 * 
 * @param <S> 源数据类型
 * @param <T> 目标数据类型
 */
@Slf4j
public class DataPipeline<S, T> {
    
    private final DataSource<S> source;
    private final DataTarget<T> target;
    private final List<DataProcessor<?, ?>> processors = new ArrayList<>();
    private final ProcessContext context = new ProcessContext();
    
    private int batchSize = 1000;
    private boolean stopOnError = false;
    private PipelineListener<S, T> listener;
    
    /**
     * 构造数据处理管道
     * 
     * @param source 数据源
     * @param target 数据目标
     */
    public DataPipeline(DataSource<S> source, DataTarget<T> target) {
        this.source = source;
        this.target = target;
    }
    
    /**
     * 添加数据处理器
     * 
     * @param processor 数据处理器
     * @return 当前管道实例 (链式调用)
     */
    public DataPipeline<S, T> addProcessor(DataProcessor<?, ?> processor) {
        processors.add(processor);
        return this;
    }
    
    /**
     * 设置批处理大小
     * 
     * @param batchSize 批处理大小
     * @return 当前管道实例 (链式调用)
     */
    public DataPipeline<S, T> setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    /**
     * 设置遇到错误是否停止处理
     * 
     * @param stopOnError 遇到错误是否停止
     * @return 当前管道实例 (链式调用)
     */
    public DataPipeline<S, T> setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
        return this;
    }
    
    /**
     * 设置管道处理监听器
     * 
     * @param listener 监听器
     * @return 当前管道实例 (链式调用)
     */
    public DataPipeline<S, T> setListener(PipelineListener<S, T> listener) {
        this.listener = listener;
        return this;
    }
    
    /**
     * 执行数据处理
     * 
     * @return 处理结果
     * @throws IOException 如果处理过程中出现IO错误
     */
    @SuppressWarnings("unchecked")
    public PipelineResult execute() throws IOException {
        if (processors.isEmpty()) {
            throw new IllegalStateException("没有配置数据处理器");
        }
        
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        List<ErrorRecord<S>> errorRecords = new ArrayList<>();
        
        try {
            // 打开数据源和目标
            source.open();
            target.open();
            
            if (listener != null) {
                listener.onPipelineStart(source, target);
            }
            
            List<T> batch = new ArrayList<>(batchSize);
            
            // 处理数据
            Iterator<S> iterator = source.iterator();
            while (iterator.hasNext()) {
                S sourceData = iterator.next();
                total.incrementAndGet();
                
                try {
                    if (listener != null) {
                        listener.onRecordStart(sourceData);
                    }
                    
                    // 执行处理链
                    Object data = sourceData;
                    for (DataProcessor<?, ?> processor : processors) {
                        if (context.isAborted()) {
                            break;
                        }
                        
                        data = ((DataProcessor<Object, Object>) processor).process(data, context);
                        
                        if (data == null) {
                            break;
                        }
                    }
                    
                    // 如果处理链执行完毕且未中止，将数据添加到批处理中
                    if (data != null && !context.isAborted()) {
                        batch.add((T) data);
                        success.incrementAndGet();
                        
                        if (listener != null) {
                            listener.onRecordSuccess(sourceData, (T) data);
                        }
                        
                        // 达到批处理大小，执行写入
                        if (batch.size() >= batchSize) {
                            target.write(batch);
                            batch.clear();
                            
                            if (listener != null) {
                                listener.onBatchComplete(batch.size());
                            }
                        }
                    } else if (context.isAborted()) {
                        // 处理被中止
                        failure.incrementAndGet();
                        errorRecords.add(new ErrorRecord<>(sourceData, context.getAbortReason()));
                        
                        if (listener != null) {
                            listener.onRecordError(sourceData, context.getAbortReason());
                        }
                        
                        context.clear();
                        
                        if (stopOnError) {
                            log.error("数据处理被中止: {}", context.getAbortReason());
                            break;
                        }
                    }
                } catch (Exception e) {
                    failure.incrementAndGet();
                    errorRecords.add(new ErrorRecord<>(sourceData, e.getMessage()));
                    
                    if (listener != null) {
                        listener.onRecordError(sourceData, e.getMessage());
                    }
                    
                    log.error("处理数据时出错: {}", e.getMessage(), e);
                    
                    if (stopOnError) {
                        break;
                    }
                }
            }
            
            // 处理剩余的批数据
            if (!batch.isEmpty()) {
                target.write(batch);
                
                if (listener != null) {
                    listener.onBatchComplete(batch.size());
                }
            }
            
            // 类型转换以配构造函数参数
            List<ErrorRecord<?>> typedErrorRecords = new ArrayList<>(errorRecords);
            return new PipelineResult(total.get(), success.get(), failure.get(), typedErrorRecords);
        } finally {
            try {
                source.close();
            } catch (Exception e) {
                log.error("关闭数据源时出错: {}", e.getMessage(), e);
            }
            
            try {
                target.close();
            } catch (Exception e) {
                log.error("关闭数据目标时出错: {}", e.getMessage(), e);
            }
            
            if (listener != null) {
                listener.onPipelineComplete(total.get(), success.get(), failure.get());
            }
        }
    }
    
    /**
     * 管道处理结果
     */
    public static class PipelineResult {
        private final int totalRecords;
        private final int successRecords;
        private final int failureRecords;
        private final List<ErrorRecord<?>> errorRecords;
        
        public PipelineResult(int totalRecords, int successRecords, int failureRecords, List<ErrorRecord<?>> errorRecords) {
            this.totalRecords = totalRecords;
            this.successRecords = successRecords;
            this.failureRecords = failureRecords;
            this.errorRecords = errorRecords;
        }
        
        public int getTotalRecords() {
            return totalRecords;
        }
        
        public int getSuccessRecords() {
            return successRecords;
        }
        
        public int getFailureRecords() {
            return failureRecords;
        }
        
        public List<ErrorRecord<?>> getErrorRecords() {
            return errorRecords;
        }
    }
    
    /**
     * 错误记录
     * 
     * @param <D> 数据类型
     */
    public static class ErrorRecord<D> {
        private final D data;
        private final String errorMessage;
        
        public ErrorRecord(D data, String errorMessage) {
            this.data = data;
            this.errorMessage = errorMessage;
        }
        
        public D getData() {
            return data;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
} 