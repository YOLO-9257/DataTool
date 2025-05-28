package com.datatool.exceltooracle.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel导入请求DTO
 */
@Data
public class ImportRequest {
    
    /**
     * 目标表名
     */
    private String tableName;
    
    /**
     * 工作表名（可选，默认使用第一个工作表）
     */
    private String sheetName;
    
    /**
     * 列映射关系 (Excel列名 -> 数据库字段名)
     */
    private Map<String, String> columnMapping = new HashMap<>();
    
    /**
     * 列默认值设置 (Excel列名 -> 默认值)
     */
    private Map<String, String> columnDefaultValue = new HashMap<>();
    
    /**
     * 列生成器设置 (Excel列名 -> 生成器ID)
     */
    private Map<String, String> columnGenerator = new HashMap<>();
    
    /**
     * 额外字段映射 (数据库字段名 -> 默认值)
     * 用于添加Excel中不存在的字段
     */
    private Map<String, String> extraFields = new HashMap<>();
    
    /**
     * 额外字段生成器 (数据库字段名 -> 生成器ID)
     * 用于添加Excel中不存在且需要使用生成器的字段
     */
    private Map<String, String> extraFieldGenerators = new HashMap<>();
    
    /**
     * 必填列名列表
     */
    private String[] requiredColumns;
    
    /**
     * 表头行号（默认为0）
     */
    private int headerRowNum = 0;
    
    /**
     * 数据起始行号（默认为1，即表头的下一行）
     */
    private int dataStartRowNum = 1;
    
    /**
     * 是否忽略错误继续处理（默认为true）
     */
    private boolean ignoreErrors = true;
    
    /**
     * 批处理大小（默认为1000）
     */
    private int batchSize = 1000;
    
    /**
     * 是否执行插入操作（默认为true，否则为更新操作）
     */
    private boolean isInsert = true;
    
    /**
     * 唯一键列名（用于更新操作）
     */
    private String[] keyColumns;
    
    /**
     * 是否自动映射列（默认为true，Excel列名直接映射到数据库字段名）
     */
    private boolean autoMapping = true;
} 