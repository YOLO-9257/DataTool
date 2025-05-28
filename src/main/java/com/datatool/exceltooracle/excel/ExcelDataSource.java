package com.datatool.exceltooracle.excel;

import com.datatool.exceltooracle.core.DataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Excel数据源实现类
 */
public class ExcelDataSource implements DataSource<Row> {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataSource.class);
    
    private final File excelFile;
    private final String sheetName;
    private final int headerRowNum;
    private final boolean ignoreHeader;
    private final SourceType sourceType;
    
    private Workbook workbook;
    private Sheet sheet;
    private int currentRowIndex;
    
    /**
     * 构造Excel数据源
     * 
     * @param excelFile Excel文件
     * @param sheetName 工作表名称，如果为null则使用第一个工作表
     * @param headerRowNum 表头行号，默认为0
     * @param ignoreHeader 是否忽略表头行
     */
    public ExcelDataSource(File excelFile, String sheetName, int headerRowNum, boolean ignoreHeader) {
        this.excelFile = excelFile;
        this.sheetName = sheetName;
        this.headerRowNum = headerRowNum;
        this.ignoreHeader = ignoreHeader;
        
        String fileName = excelFile.getName().toLowerCase();
        if (fileName.endsWith(".xlsx")) {
            this.sourceType = SourceType.EXCEL_XLSX;
        } else if (fileName.endsWith(".xls")) {
            this.sourceType = SourceType.EXCEL_XLS;
        } else {
            throw new IllegalArgumentException("不支持的Excel文件格式: " + fileName);
        }
    }
    
    /**
     * 构造Excel数据源（使用第一个工作表，表头行号为0，忽略表头）
     * 
     * @param excelFile Excel文件
     */
    public ExcelDataSource(File excelFile) {
        this(excelFile, null, 0, true);
    }
    
    @Override
    public void open() throws IOException {
        logger.debug("打开Excel数据源: {}", excelFile.getAbsolutePath());
        
        try (InputStream is = new FileInputStream(excelFile)) {
            if (sourceType == SourceType.EXCEL_XLSX) {
                workbook = new XSSFWorkbook(is);
            } else {
                workbook = new HSSFWorkbook(is);
            }
            
            if (sheetName != null && !sheetName.isEmpty()) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IOException("工作表不存在: " + sheetName);
                }
            } else {
                sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    throw new IOException("Excel文件中没有工作表");
                }
            }
            
            currentRowIndex = ignoreHeader ? headerRowNum + 1 : headerRowNum;
            logger.debug("Excel数据源已打开，工作表: {}, 起始行: {}", sheet.getSheetName(), currentRowIndex);
        }
    }
    
    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
            workbook = null;
            sheet = null;
            logger.debug("Excel数据源已关闭: {}", excelFile.getAbsolutePath());
        }
    }
    
    @Override
    public Iterator<Row> iterator() {
        if (sheet == null) {
            throw new IllegalStateException("数据源未打开");
        }
        
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return currentRowIndex <= sheet.getLastRowNum();
            }
            
            @Override
            public Row next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("没有更多的行数据");
                }
                
                Row row = sheet.getRow(currentRowIndex);
                currentRowIndex++;
                return row;
            }
        };
    }
    
    @Override
    public String getName() {
        return excelFile.getName();
    }
    
    @Override
    public SourceType getType() {
        return sourceType;
    }
    
    /**
     * 获取表头行
     * 
     * @return 表头行
     */
    public Row getHeaderRow() {
        if (sheet == null) {
            throw new IllegalStateException("数据源未打开");
        }
        return sheet.getRow(headerRowNum);
    }
    
    /**
     * 获取工作表对象
     * 
     * @return 工作表对象
     */
    public Sheet getSheet() {
        return sheet;
    }
    
    /**
     * 获取工作簿对象
     * 
     * @return 工作簿对象
     */
    public Workbook getWorkbook() {
        return workbook;
    }
} 