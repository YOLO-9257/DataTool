package com.datatool.exceltooracle.excel;

import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Excel工具类
 * 提供Excel相关的工具方法
 */
public class ExcelUtils {
    
    /**
     * 获取单元格值
     * 
     * @param cell 单元格
     * @return 单元格的值（转换为对应的Java类型）
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
                
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    if (date != null) {
                        // 判断是否包含时间部分
                        LocalDateTime dateTime = date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        
                        if (dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0) {
                            return dateTime.toLocalDate();
                        } else {
                            return dateTime;
                        }
                    }
                    return null;
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 判断是否为整数
                    if (Math.floor(numericValue) == numericValue) {
                        if (numericValue <= Integer.MAX_VALUE && numericValue >= Integer.MIN_VALUE) {
                            return (int) numericValue;
                        } else {
                            return (long) numericValue;
                        }
                    } else {
                        return BigDecimal.valueOf(numericValue);
                    }
                }
                
            case BOOLEAN:
                return cell.getBooleanCellValue();
                
            case FORMULA:
                try {
                    return getCellValueByFormulaType(cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
                
            case BLANK:
                return null;
                
            case ERROR:
                return null;
                
            default:
                return null;
        }
    }
    
    /**
     * 根据公式类型获取单元格值
     * 
     * @param cell 包含公式的单元格
     * @return 公式计算结果
     */
    private static Object getCellValueByFormulaType(Cell cell) {
        CellType formulaType = cell.getCachedFormulaResultType();
        
        switch (formulaType) {
            case STRING:
                return cell.getStringCellValue();
                
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    if (date != null) {
                        return date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                    }
                    return null;
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (Math.floor(numericValue) == numericValue) {
                        if (numericValue <= Integer.MAX_VALUE && numericValue >= Integer.MIN_VALUE) {
                            return (int) numericValue;
                        } else {
                            return (long) numericValue;
                        }
                    } else {
                        return BigDecimal.valueOf(numericValue);
                    }
                }
                
            case BOOLEAN:
                return cell.getBooleanCellValue();
                
            case ERROR:
                return null;
                
            default:
                return cell.getCellFormula();
        }
    }
    
    /**
     * 获取单元格的字符串值
     * 
     * @param cell 单元格
     * @return 字符串值，如果单元格为null则返回空字符串
     */
    public static String getStringCellValue(Cell cell) {
        Object value = getCellValue(cell);
        return value == null ? "" : value.toString();
    }
    
    /**
     * 获取单元格的整数值
     * 
     * @param cell 单元格
     * @param defaultValue 默认值
     * @return 整数值，如果无法转换则返回默认值
     */
    public static int getIntCellValue(Cell cell, int defaultValue) {
        Object value = getCellValue(cell);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取单元格的日期值
     * 
     * @param cell 单元格
     * @return 日期值，如果无法转换则返回null
     */
    public static LocalDate getDateCellValue(Cell cell) {
        Object value = getCellValue(cell);
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        return null;
    }
} 