package com.datatool.exceltooracle.service.impl;

import com.datatool.exceltooracle.dto.TableColumnDTO;
import com.datatool.exceltooracle.dto.TableStructureDTO;
import com.datatool.exceltooracle.service.TableMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表元数据服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TableMetadataServiceImpl implements TableMetadataService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public TableStructureDTO getTableStructure(String tableName) {
        log.info("获取表结构信息: {}", tableName);
        
        if (!tableExists(tableName)) {
            log.warn("表不存在: {}", tableName);
            return TableStructureDTO.builder()
                    .tableName(tableName)
                    .exists(false)
                    .message("表不存在")
                    .build();
        }
        
        Connection connection = null;
        try {
            List<TableColumnDTO> columns = new ArrayList<>();
            Map<String, TableColumnDTO> columnMap = new HashMap<>();
            
            // 获取表的基本信息 - 明确获取和关闭连接
            connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取主键信息
            try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName.toUpperCase())) {
                while (primaryKeys.next()) {
                    String columnName = primaryKeys.getString("COLUMN_NAME");
                    columnMap.put(columnName, TableColumnDTO.builder()
                            .columnName(columnName)
                            .primaryKey(true)
                            .build());
                }
            }
            
            // 获取列信息
            try (ResultSet columnsRS = metaData.getColumns(null, null, tableName.toUpperCase(), null)) {
                while (columnsRS.next()) {
                    String columnName = columnsRS.getString("COLUMN_NAME");
                    int dataType = columnsRS.getInt("DATA_TYPE");
                    String typeName = columnsRS.getString("TYPE_NAME");
                    int columnSize = columnsRS.getInt("COLUMN_SIZE");
                    int decimalDigits = columnsRS.getInt("DECIMAL_DIGITS");
                    int nullable = columnsRS.getInt("NULLABLE");
                    String remarks = columnsRS.getString("REMARKS");
                    String defaultValue = columnsRS.getString("COLUMN_DEF");
                    int ordinalPosition = columnsRS.getInt("ORDINAL_POSITION");
                    
                    TableColumnDTO columnDTO = columnMap.getOrDefault(columnName, 
                            TableColumnDTO.builder().columnName(columnName).build());
                    
                    columnDTO.setDataType(getDataTypeName(dataType));
                    columnDTO.setTypeName(typeName);
                    columnDTO.setColumnSize(columnSize);
                    columnDTO.setDecimalDigits(decimalDigits);
                    columnDTO.setNullable(nullable == DatabaseMetaData.columnNullable);
                    columnDTO.setRemarks(remarks);
                    columnDTO.setDefaultValue(defaultValue);
                    columnDTO.setOrdinalPosition(ordinalPosition);
                    
                    columnMap.put(columnName, columnDTO);
                }
            }
            
            // 转换为列表并按序号排序
            columns.addAll(columnMap.values());
            columns.sort((c1, c2) -> Integer.compare(c1.getOrdinalPosition(), c2.getOrdinalPosition()));
            
            // 创建表结构DTO
            return TableStructureDTO.builder()
                    .tableName(tableName)
                    .tableType("TABLE")
                    .columns(columns)
                    .exists(true)
                    .build();
            
        } catch (SQLException e) {
            log.error("获取表结构时出错: {}", e.getMessage(), e);
            return TableStructureDTO.builder()
                    .tableName(tableName)
                    .exists(false)
                    .message("获取表结构失败: " + e.getMessage())
                    .build();
        } finally {
            // 确保连接关闭
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("关闭数据库连接时出错", e);
                }
            }
        }
    }
    
    @Override
    public boolean tableExists(String tableName) {
        Connection connection = null;
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            // 在Oracle中表名通常为大写
            try (ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
                return tables.next();
            }
        } catch (SQLException e) {
            log.error("检查表是否存在时出错: {}", e.getMessage(), e);
            return false;
        } finally {
            // 确保连接关闭
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("关闭数据库连接时出错", e);
                }
            }
        }
    }
    
    /**
     * 获取数据类型的名称
     * 
     * @param sqlType SQL类型代码
     * @return 数据类型名称
     */
    private String getDataTypeName(int sqlType) {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                return "文本";
            case Types.NUMERIC:
            case Types.DECIMAL:
                return "数值";
            case Types.BIT:
            case Types.BOOLEAN:
                return "布尔";
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return "整数";
            case Types.BIGINT:
                return "长整数";
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return "浮点数";
            case Types.DATE:
                return "日期";
            case Types.TIME:
                return "时间";
            case Types.TIMESTAMP:
                return "日期时间";
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "二进制";
            case Types.CLOB:
                return "大文本";
            default:
                return "其他";
        }
    }
} 