package com.datatool.exceltooracle.service;

import com.datatool.exceltooracle.dto.ImportRequest;
import com.datatool.exceltooracle.dto.ImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Excel导入服务接口
 */
public interface ExcelImportService {
    
    /**
     * 导入Excel数据
     * 
     * @param file Excel文件
     * @param request 导入请求参数
     * @return 导入结果
     * @throws IOException 如果处理过程中出现IO错误
     */
    ImportResponse importExcel(MultipartFile file, ImportRequest request) throws IOException;
    
    /**
     * 根据任务ID查询导入结果
     * 
     * @param taskId 任务ID
     * @return 导入结果
     */
    ImportResponse getImportResult(String taskId);
} 