package com.datatool.exceltooracle.processor;

import com.datatool.exceltooracle.core.DataProcessor;
import com.datatool.exceltooracle.core.ProcessContext;
import com.datatool.exceltooracle.excel.ExcelFieldMapper;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

/**
 * Excel行转Map处理器
 */
public class ExcelRowToMapProcessor implements DataProcessor<Row, Map<String, Object>> {
    
    private final ExcelFieldMapper fieldMapper;
    private final Map<String, Integer> columnIndexMap;
    private final int dataStartRowNum;
    
    /**
     * 构造Excel行转Map处理器
     * 
     * @param fieldMapper 字段映射器
     * @param columnIndexMap 列索引映射
     * @param dataStartRowNum 数据起始行号
     */
    public ExcelRowToMapProcessor(ExcelFieldMapper fieldMapper, 
                                  Map<String, Integer> columnIndexMap,
                                  int dataStartRowNum) {
        this.fieldMapper = fieldMapper;
        this.columnIndexMap = columnIndexMap;
        this.dataStartRowNum = dataStartRowNum;
    }
    
    @Override
    public Map<String, Object> process(Row row, ProcessContext context) throws Exception {
        // 只处理表头行后面的数据行
        if (row.getRowNum() < dataStartRowNum) {
            return null;
        }
        
        return fieldMapper.extractData(row, columnIndexMap);
    }
    
    @Override
    public String getName() {
        return "ExcelRowToMapProcessor";
    }
    
    @Override
    public String getDescription() {
        return "将Excel行数据转换为字段-值映射";
    }
} 