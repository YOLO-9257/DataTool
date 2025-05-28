package com.datatool.exceltooracle.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据处理上下文
 * 用于在不同处理器之间传递数据和状态
 */
public class ProcessContext {
    
    private final Map<String, Object> attributes = new HashMap<>();
    private boolean aborted = false;
    private String abortReason;
    
    /**
     * 设置属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 获取属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * 移除属性
     * 
     * @param key 属性键
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }
    
    /**
     * 判断是否包含某个属性
     * 
     * @param key 属性键
     * @return 是否包含
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * 中止处理
     * 
     * @param reason 中止原因
     */
    public void abort(String reason) {
        this.aborted = true;
        this.abortReason = reason;
    }
    
    /**
     * 判断是否已中止处理
     * 
     * @return 是否已中止
     */
    public boolean isAborted() {
        return aborted;
    }
    
    /**
     * 获取中止原因
     * 
     * @return 中止原因
     */
    public String getAbortReason() {
        return abortReason;
    }
    
    /**
     * 清空上下文
     */
    public void clear() {
        attributes.clear();
        aborted = false;
        abortReason = null;
    }
} 