package com.datatool.exceltooracle.controller;

import com.datatool.exceltooracle.dto.ImportRequest;
import com.datatool.exceltooracle.dto.ImportResponse;
import com.datatool.exceltooracle.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Excel导入控制器
 * 提供HTTP接口进行Excel导入操作
 */
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin // 添加跨域支持
public class ExcelImportController {
    
    private final ExcelImportService excelImportService;
    
    /**
     * 导入Excel文件
     * 
     * @param file Excel文件
     * @param request 导入请求参数
     * @return 导入结果
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importExcel(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute ImportRequest request) {
        
        log.info("接收到Excel导入请求，文件名: {}, 大小: {}, 目标表: {}", 
                file.getOriginalFilename(), file.getSize(), request.getTableName());
        log.info("导入参数: headerRowNum={}, dataStartRowNum={}, autoMapping={}, batchSize={}, isInsert={}",
                request.getHeaderRowNum(), request.getDataStartRowNum(), request.isAutoMapping(),
                request.getBatchSize(), request.isInsert());
        
        if (file.isEmpty()) {
            log.error("上传的文件为空");
            ImportResponse errorResponse = ImportResponse.failure("上传的文件为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            ImportResponse response = excelImportService.importExcel(file, request);
            log.info("导入处理完成，任务ID: {}, 总记录数: {}, 成功: {}, 失败: {}", 
                    response.getTaskId(), response.getTotalRecords(), 
                    response.getSuccessRecords(), response.getFailureRecords());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("处理Excel文件时出错: {}", e.getMessage(), e);
            ImportResponse errorResponse = ImportResponse.failure("处理Excel文件时出错: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("导入过程中发生未预期的错误: {}", e.getMessage(), e);
            ImportResponse errorResponse = ImportResponse.failure("导入过程中发生未预期的错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 查询导入结果
     * 
     * @param taskId 任务ID
     * @return 导入结果
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<ImportResponse> getImportStatus(@PathVariable String taskId) {
        log.info("查询导入任务状态，任务ID: {}", taskId);
        
        ImportResponse response = excelImportService.getImportResult(taskId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 健康检查接口
     * 用于测试API是否正常工作
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("收到健康检查请求");
        return ResponseEntity.ok("API正常工作中");
    }
} 