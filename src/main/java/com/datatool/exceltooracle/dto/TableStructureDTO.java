package com.datatool.exceltooracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表结构DTO
 * 用于传输表的整体结构信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStructureDTO {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表类型（TABLE, VIEW等）
     */
    private String tableType;
    
    /**
     * 列信息
     */
    @Builder.Default
    private List<TableColumnDTO> columns = new ArrayList<>();
    
    /**
     * 是否存在
     */
    private boolean exists;
    
    /**
     * 附加信息（备注、错误信息等）
     */
    private String message;
} 