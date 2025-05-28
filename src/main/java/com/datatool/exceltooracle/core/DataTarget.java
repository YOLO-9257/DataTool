package com.datatool.exceltooracle.core;

import java.io.IOException;
import java.util.List;

/**
 * 数据目标接口
 * 
 * @param <T> 数据目标接受的数据类型
 */
public interface DataTarget<T> {
    
    /**
     * 打开数据目标
     * 
     * @throws IOException 如果打开数据目标失败
     */
    void open() throws IOException;
    
    /**
     * 关闭数据目标
     * 
     * @throws IOException 如果关闭数据目标失败
     */
    void close() throws IOException;
    
    /**
     * 写入单条数据
     * 
     * @param data 要写入的数据
     * @throws IOException 如果写入失败
     */
    void write(T data) throws IOException;
    
    /**
     * 批量写入数据
     * 
     * @param data 要写入的数据列表
     * @throws IOException 如果写入失败
     */
    void write(List<T> data) throws IOException;
    
    /**
     * 获取数据目标名称
     * 
     * @return 数据目标名称
     */
    String getName();
    
    /**
     * 获取数据目标类型
     * 
     * @return 数据目标类型
     */
    TargetType getType();
    
    /**
     * 数据目标类型枚举
     */
    enum TargetType {
        ORACLE_TABLE,
        ORACLE_PROCEDURE,
        CSV,
        JSON,
        XML,
        OTHER_DATABASE
    }
} 