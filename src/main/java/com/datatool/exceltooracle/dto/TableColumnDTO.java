package com.datatool.exceltooracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表列结构DTO
 * 用于传输表的列结构信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnDTO {
    
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 类型名称
     */
    private String typeName;
    
    /**
     * 列大小
     */
    private int columnSize;
    
    /**
     * 小数位数
     */
    private int decimalDigits;
    
    /**
     * 是否允许为空
     */
    private boolean nullable;
    
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 注释描述
     */
    private String remarks;
    
    /**
     * 序号位置
     */
    private int ordinalPosition;
} 