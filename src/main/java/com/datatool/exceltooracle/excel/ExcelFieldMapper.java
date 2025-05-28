package com.datatool.exceltooracle.excel;

import com.datatool.exceltooracle.core.DataConverter;
import com.datatool.exceltooracle.core.ValueGenerator;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Excel字段映射类
 * 用于配置Excel列和数据库字段的映射关系
 */
public class ExcelFieldMapper {
    
    private final Map<String, FieldMapping> mappings = new HashMap<>();
    private final boolean caseInsensitive;
    
    /**
     * 构造Excel字段映射器
     * 
     * @param caseInsensitive 列名是否大小写不敏感
     */
    public ExcelFieldMapper(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }
    
    /**
     * 添加字段映射（基本方法）
     * 
     * @param columnName Excel列名
     * @param fieldName 数据库字段名
     * @param required 是否必填
     * @return 当前映射器实例 (链式调用)
     */
    public ExcelFieldMapper addMapping(String columnName, String fieldName, boolean required) {
        String key = caseInsensitive ? columnName.toLowerCase() : columnName;
        mappings.put(key, FieldMapping.builder()
                .columnName(columnName)
                .fieldName(fieldName)
                .required(required)
                .build());
        return this;
    }
    
    /**
     * 添加字段映射（简化版本）
     * 
     * @param columnName Excel列名
     * @param fieldName 数据库字段名
     * @return 当前映射器实例 (链式调用)
     */
    public ExcelFieldMapper addMapping(String columnName, String fieldName) {
        return addMapping(columnName, fieldName, false);
    }
    
    /**
     * 添加字段映射（带转换器）
     * 
     * @param columnName Excel列名
     * @param fieldName 数据库字段名
     * @param required 是否必填
     * @param converter 数据转换器
     * @return 当前映射器实例 (链式调用)
     */
    public ExcelFieldMapper addMappingWithConverter(String columnName, String fieldName, boolean required, DataConverter<?, ?> converter) {
        String key = caseInsensitive ? columnName.toLowerCase() : columnName;
        mappings.put(key, FieldMapping.builder()
                .columnName(columnName)
                .fieldName(fieldName)
                .required(required)
                .converter(converter)
                .build());
        return this;
    }
    
    /**
     * 添加字段映射（带默认值）
     * 
     * @param columnName Excel列名
     * @param fieldName 数据库字段名
     * @param required 是否必填
     * @param defaultValue 默认值
     * @return 当前映射器实例 (链式调用)
     */
    public ExcelFieldMapper addMappingWithDefault(String columnName, String fieldName, boolean required, String defaultValue) {
        String key = caseInsensitive ? columnName.toLowerCase() : columnName;
        mappings.put(key, FieldMapping.builder()
                .columnName(columnName)
                .fieldName(fieldName)
                .required(required)
                .defaultValue(defaultValue)
                .build());
        return this;
    }
    
    /**
     * 添加字段映射（带值生成器）
     * 
     * @param columnName Excel列名
     * @param fieldName 数据库字段名
     * @param required 是否必填
     * @param valueGenerator 值生成器
     * @return 当前映射器实例 (链式调用)
     */
    public ExcelFieldMapper addMappingWithGenerator(String columnName, String fieldName, boolean required, ValueGenerator valueGenerator) {
        String key = caseInsensitive ? columnName.toLowerCase() : columnName;
        mappings.put(key, FieldMapping.builder()
                .columnName(columnName)
                .fieldName(fieldName)
                .required(required)
                .valueGenerator(valueGenerator)
                .build());
        return this;
    }
    
    /**
     * 从表头行构建列索引映射
     * 
     * @param headerRow 表头行
     * @return 列索引映射
     */
    public Map<String, Integer> buildColumnIndexMap(Row headerRow) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        if (headerRow == null) {
            return columnIndexMap;
        }
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String columnName = cell.getStringCellValue();
                if (StringUtils.isNotBlank(columnName)) {
                    String key = caseInsensitive ? columnName.toLowerCase() : columnName;
                    columnIndexMap.put(key, i);
                }
            }
        }
        
        return columnIndexMap;
    }
    
    /**
     * 获取单元格值
     * 
     * @param row 行
     * @param columnIndex 列索引
     * @return 单元格值（可能为null）
     */
    public Object getCellValue(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        return ExcelUtils.getCellValue(cell);
    }
    
    /**
     * 根据映射关系从Excel行提取数据
     * 
     * @param row Excel行
     * @param columnIndexMap 列索引映射
     * @return 字段名-值映射
     */
    public Map<String, Object> extractData(Row row, Map<String, Integer> columnIndexMap) {
        Map<String, Object> data = new HashMap<>();
        
        for (FieldMapping mapping : mappings.values()) {
            String columnKey = caseInsensitive ? mapping.getColumnName().toLowerCase() : mapping.getColumnName();
            Integer columnIndex = columnIndexMap.get(columnKey);
            
            Object value = null;
            if (columnIndex != null) {
                value = getCellValue(row, columnIndex);
            }
            
            // 如果值为空且有值生成器，使用生成器
            if (value == null && mapping.getValueGenerator() != null) {
                value = mapping.getValueGenerator().generate();
            }
            
            // 如果值为空且有默认值，使用默认值
            if (value == null && mapping.getDefaultValue() != null) {
                value = mapping.getDefaultValue();
            }
            
            // 如果还是空值，但字段必填，则抛出异常
            if (value == null && mapping.isRequired()) {
                throw new IllegalStateException("缺少必填列: " + mapping.getColumnName());
            }
            
            // 只有在值不为空的情况下才添加到结果中
            if (value != null) {
                data.put(mapping.getFieldName(), value);
            }
        }
        
        return data;
    }
    
    /**
     * 获取所有映射关系
     * 
     * @return 映射关系集合
     */
    public Map<String, FieldMapping> getMappings() {
        return new HashMap<>(mappings);
    }
    
    /**
     * 获取指定列的映射关系
     * 
     * @param columnName 列名
     * @return 映射关系（可能为空）
     */
    public Optional<FieldMapping> getMapping(String columnName) {
        String key = caseInsensitive ? columnName.toLowerCase() : columnName;
        return Optional.ofNullable(mappings.get(key));
    }
    
    /**
     * 字段映射信息
     */
    @Data
    @Builder
    public static class FieldMapping {
        private final String columnName;      // Excel列名
        private final String fieldName;       // 数据库字段名
        private final boolean required;       // 是否必填
        private final DataConverter<?, ?> converter;  // 数据转换器
        private final String defaultValue;    // 默认值（字符串形式）
        private final ValueGenerator valueGenerator; // 值生成器
    }
} 