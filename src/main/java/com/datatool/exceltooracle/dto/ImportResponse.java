package com.datatool.exceltooracle.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel导入响应DTO
 */
@Data
public class ImportResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 总记录数
     */
    private int totalRecords;
    
    /**
     * 成功记录数
     */
    private int successRecords;
    
    /**
     * 失败记录数
     */
    private int failureRecords;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 处理耗时（毫秒）
     */
    private long processingTime;
    
    /**
     * 错误记录列表
     */
    private List<ErrorRecord> errorRecords = new ArrayList<>();
    
    /**
     * 构造成功响应
     * 
     * @param message 消息
     * @return 响应对象
     */
    public static ImportResponse success(String message) {
        ImportResponse response = new ImportResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 构造失败响应
     * 
     * @param message 消息
     * @return 响应对象
     */
    public static ImportResponse failure(String message) {
        ImportResponse response = new ImportResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 错误记录
     */
    @Data
    public static class ErrorRecord {
        /**
         * 行号
         */
        private int rowNum;
        
        /**
         * 错误消息
         */
        private String errorMessage;
        
        /**
         * 列数据
         */
        private String rowData;
    }
} 