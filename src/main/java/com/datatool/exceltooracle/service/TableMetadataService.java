package com.datatool.exceltooracle.service;

import com.datatool.exceltooracle.dto.TableStructureDTO;

/**
 * 表元数据服务接口
 * 提供表结构查询等功能
 */
public interface TableMetadataService {
    
    /**
     * 获取表结构信息
     * 
     * @param tableName 表名（区分大小写）
     * @return 表结构信息
     */
    TableStructureDTO getTableStructure(String tableName);
    
    /**
     * 表是否存在
     * 
     * @param tableName 表名
     * @return 是否存在
     */
    boolean tableExists(String tableName);
} 