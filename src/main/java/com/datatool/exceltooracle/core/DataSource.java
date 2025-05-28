package com.datatool.exceltooracle.core;

import java.io.IOException;
import java.util.Iterator;

/**
 * 数据源接口
 * 
 * @param <T> 数据源中的数据类型
 */
public interface DataSource<T> {
    
    /**
     * 打开数据源
     * 
     * @throws IOException 如果打开数据源失败
     */
    void open() throws IOException;
    
    /**
     * 关闭数据源
     * 
     * @throws IOException 如果关闭数据源失败
     */
    void close() throws IOException;
    
    /**
     * 获取数据迭代器
     * 
     * @return 数据迭代器
     */
    Iterator<T> iterator();
    
    /**
     * 获取数据源名称
     * 
     * @return 数据源名称
     */
    String getName();
    
    /**
     * 获取数据源类型
     * 
     * @return 数据源类型
     */
    SourceType getType();
    
    /**
     * 数据源类型枚举
     */
    enum SourceType {
        EXCEL_XLS,
        EXCEL_XLSX,
        CSV,
        JSON,
        XML,
        DATABASE
    }
} 