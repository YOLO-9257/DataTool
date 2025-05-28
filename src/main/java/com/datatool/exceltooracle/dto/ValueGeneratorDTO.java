package com.datatool.exceltooracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 值生成器信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueGeneratorDTO {
    
    /**
     * 生成器ID
     */
    private String id;
    
    /**
     * 生成器名称
     */
    private String name;
    
    /**
     * 生成器描述
     */
    private String description;
    
    /**
     * 生成值的数据类型
     */
    private String valueType;
} 