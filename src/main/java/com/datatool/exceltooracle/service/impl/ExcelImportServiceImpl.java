package com.datatool.exceltooracle.service.impl;import com.datatool.exceltooracle.core.DataValidator;import com.datatool.exceltooracle.core.ValueGenerator;import com.datatool.exceltooracle.core.ValueGeneratorRegistry;import com.datatool.exceltooracle.db.OracleDataTarget;
import com.datatool.exceltooracle.dto.ImportRequest;
import com.datatool.exceltooracle.dto.ImportResponse;
import com.datatool.exceltooracle.dto.TableColumnDTO;
import com.datatool.exceltooracle.dto.TableStructureDTO;
import com.datatool.exceltooracle.dto.ImportResponse.ErrorRecord;
import com.datatool.exceltooracle.excel.ExcelDataSource;
import com.datatool.exceltooracle.excel.ExcelFieldMapper;
import com.datatool.exceltooracle.pipeline.DataPipeline;
import com.datatool.exceltooracle.pipeline.DataPipeline.PipelineResult;
import com.datatool.exceltooracle.pipeline.PipelineListener;
import com.datatool.exceltooracle.processor.ExcelRowToMapProcessor;
import com.datatool.exceltooracle.service.ExcelImportService;
import com.datatool.exceltooracle.service.TableMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Excel导入服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {
    
    private final JdbcTemplate jdbcTemplate;
    private final TableMetadataService tableMetadataService;
    
    // 存储任务结果的缓存（实际应用中可以使用Redis等缓存）
    private final Map<String, ImportResponse> taskResults = new ConcurrentHashMap<>();
    
    @Override
    public ImportResponse importExcel(MultipartFile file, ImportRequest request) throws IOException {
        log.info("开始导入Excel文件: {}, 目标表: {}", file.getOriginalFilename(), request.getTableName());
        
        // 验证表是否存在
        if (!tableMetadataService.tableExists(request.getTableName())) {
            log.error("目标表不存在: {}", request.getTableName());
            return ImportResponse.failure("目标表不存在: " + request.getTableName());
        }
        
        // 获取表结构信息
        TableStructureDTO tableStructure = tableMetadataService.getTableStructure(request.getTableName());
        if (!tableStructure.isExists()) {
            log.error("获取表结构失败: {}", tableStructure.getMessage());
            return ImportResponse.failure("获取表结构失败: " + tableStructure.getMessage());
        }
        
        // 创建表结构映射 (列名 -> 列信息)
        Map<String, TableColumnDTO> tableColumns = tableStructure.getColumns().stream()
                .collect(Collectors.toMap(
                        col -> col.getColumnName().toUpperCase(),
                        col -> col,
                        (c1, c2) -> c1)); // 如果有重复键，保留第一个
        
        // 检查必填列
        List<String> requiredDbColumns = tableStructure.getColumns().stream()
                .filter(col -> !col.isNullable() && col.getDefaultValue() == null)
                .map(TableColumnDTO::getColumnName)
                .collect(Collectors.toList());
        
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 创建导入响应对象
        ImportResponse response = new ImportResponse();
        response.setTaskId(taskId);
        response.setStartTime(new Date());
        
        // 创建临时文件
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "excel-import");
        Files.createDirectories(tempDir);
        
        File tempFile = File.createTempFile("import-", "-" + file.getOriginalFilename(), tempDir.toFile());
        file.transferTo(tempFile);
        
        try {
            // 创建Excel数据源
            ExcelDataSource dataSource = new ExcelDataSource(
                    tempFile,
                    request.getSheetName(),
                    request.getHeaderRowNum(),
                    true
            );
            
            // 打开数据源，读取表头信息
            dataSource.open();
            
            // 创建字段映射器
            ExcelFieldMapper fieldMapper = createFieldMapper(request, tableColumns, requiredDbColumns);
            
            // 如果是自动映射模式，根据Excel表头自动创建映射关系
            if (request.isAutoMapping()) {
                Row headerRow = dataSource.getHeaderRow();
                if (headerRow != null) {
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
                        if (cell != null && cell.getCellType() != CellType.BLANK) {
                            String columnName = cell.getStringCellValue().trim();
                            if (!columnName.isEmpty()) {
                                // 检查Excel列名是否存在于数据库表中
                                String dbColumnName = columnName.toUpperCase();
                                if (tableColumns.containsKey(dbColumnName)) {
                                    // 列存在于数据库表中，创建映射
                                    TableColumnDTO columnInfo = tableColumns.get(dbColumnName);
                                    boolean required = !columnInfo.isNullable() && columnInfo.getDefaultValue() == null;
                                    
                                    fieldMapper.addMapping(columnName, dbColumnName, required);
                                    log.debug("自动映射成功: Excel列 '{}' -> 数据库字段 '{}'", columnName, dbColumnName);
                                } else {
                                    // 列不存在于数据库表中，记录日志
                                    log.warn("Excel列 '{}' 不存在于表 {} 中，将被忽略", columnName, request.getTableName());
                                }
                            }
                        }
                    }
                }
            }
            
            // 创建Oracle数据目标
            OracleDataTarget dataTarget = new OracleDataTarget(
                    jdbcTemplate,
                    request.getTableName(),
                    request.isInsert(),
                    request.getKeyColumns()
            );
            
            // 创建管道
            DataPipeline<Row, Map<String, Object>> pipeline = new DataPipeline<>(dataSource, dataTarget);
            
            // 添加处理器 - Excel行转Map
            Map<String, Integer> columnIndexMap = fieldMapper.buildColumnIndexMap(dataSource.getHeaderRow());
            ExcelRowToMapProcessor processor = new ExcelRowToMapProcessor(
                    fieldMapper,
                    columnIndexMap,
                    request.getDataStartRowNum()
            );
            pipeline.addProcessor(processor);
            
            // 设置批处理大小
            pipeline.setBatchSize(request.getBatchSize());
            
            // 设置错误处理方式
            pipeline.setStopOnError(!request.isIgnoreErrors());
            
            // 设置监听器
            pipeline.setListener(new ImportPipelineListener());
            
            // 执行导入
            PipelineResult result = pipeline.execute();
            
            // 设置响应信息
            response.setSuccess(true);
            response.setMessage("导入完成");
            response.setTotalRecords(result.getTotalRecords());
            response.setSuccessRecords(result.getSuccessRecords());
            response.setFailureRecords(result.getFailureRecords());
            
            // 处理错误记录
            for (DataPipeline.ErrorRecord<?> error : result.getErrorRecords()) {
                if (error.getData() instanceof Row) {
                    Row row = (Row) error.getData();
                    ErrorRecord errorRecord = new ErrorRecord();
                    errorRecord.setRowNum(row.getRowNum() + 1); // 行号从1开始
                    errorRecord.setErrorMessage(error.getErrorMessage());
                    errorRecord.setRowData("Row " + row.getRowNum()); // 实际应用中可以获取行数据内容
                    response.getErrorRecords().add(errorRecord);
                }
            }
        } catch (Exception e) {
            log.error("导入Excel出错: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("导入失败: " + e.getMessage());
        } finally {
            // 删除临时文件
            tempFile.delete();
            
            // 设置结束时间和处理耗时
            response.setEndTime(new Date());
            response.setProcessingTime(response.getEndTime().getTime() - response.getStartTime().getTime());
            
            // 保存任务结果
            taskResults.put(taskId, response);
        }
        
        return response;
    }
    
    @Override
    public ImportResponse getImportResult(String taskId) {
        return taskResults.getOrDefault(taskId, 
                ImportResponse.failure("找不到任务ID: " + taskId));
    }
    
    /**
     * 创建字段映射器
     * 
     * @param request 导入请求参数
     * @param tableColumns 表列信息
     * @param requiredDbColumns 必填列
     * @return 字段映射器
     */
    private ExcelFieldMapper createFieldMapper(ImportRequest request, 
                                              Map<String, TableColumnDTO> tableColumns,
                                              List<String> requiredDbColumns) {
        ExcelFieldMapper fieldMapper = new ExcelFieldMapper(true);
        ValueGeneratorRegistry generatorRegistry = ValueGeneratorRegistry.getInstance();
        
        // 如果手动提供了列映射关系，则使用提供的映射
        if (!request.isAutoMapping() && !request.getColumnMapping().isEmpty()) {
            // 添加手动配置的列映射
            for (Map.Entry<String, String> entry : request.getColumnMapping().entrySet()) {
                String excelColumn = entry.getKey();
                String dbColumn = entry.getValue();
                
                // 检查数据库字段是否存在
                if (tableColumns.containsKey(dbColumn.toUpperCase())) {
                    TableColumnDTO columnInfo = tableColumns.get(dbColumn.toUpperCase());
                    boolean required = !columnInfo.isNullable() && columnInfo.getDefaultValue() == null;
                    
                    // 检查是否有默认值配置
                    String defaultValue = request.getColumnDefaultValue().get(excelColumn);
                    if (defaultValue != null) {
                        // 使用默认值
                        log.debug("列 {} 使用默认值: {}", excelColumn, defaultValue);
                        fieldMapper.addMappingWithDefault(excelColumn, dbColumn, required, defaultValue);
                    } 
                    // 检查是否有生成器配置
                    else if (request.getColumnGenerator().containsKey(excelColumn)) {
                        String generatorId = request.getColumnGenerator().get(excelColumn);
                        if (generatorRegistry.hasGenerator(generatorId)) {
                            ValueGenerator generator = generatorRegistry.getGenerator(generatorId);
                            log.debug("列 {} 使用生成器: {}", excelColumn, generator.getName());
                            fieldMapper.addMappingWithGenerator(excelColumn, dbColumn, required, generator);
                        } else {
                            log.warn("指定的生成器 {} 不存在，将使用普通映射", generatorId);
                            fieldMapper.addMapping(excelColumn, dbColumn, required);
                        }
                    } 
                    // 没有特殊配置，使用基本映射
                    else {
                        fieldMapper.addMapping(excelColumn, dbColumn, required);
                    }
                    
                    log.debug("手动映射: Excel列 '{}' -> 数据库字段 '{}'", excelColumn, dbColumn);
                } else {
                    log.warn("手动映射的数据库字段 '{}' 不存在于表中，将被忽略", dbColumn);
                }
            }
        }
        // 自动映射模式下，将在打开数据源、读取表头后根据表结构进行自动映射
        
        // 处理额外字段（Excel中不存在但需要添加到目标表的字段）
        if (!request.getExtraFields().isEmpty() || !request.getExtraFieldGenerators().isEmpty()) {
            log.info("处理额外字段映射，共 {} 个默认值字段，{} 个生成器字段", 
                     request.getExtraFields().size(), request.getExtraFieldGenerators().size());
            
            // 处理带默认值的额外字段
            for (Map.Entry<String, String> entry : request.getExtraFields().entrySet()) {
                String dbColumn = entry.getKey();
                String defaultValue = entry.getValue();
                
                // 检查数据库字段是否存在
                if (tableColumns.containsKey(dbColumn.toUpperCase())) {
                    TableColumnDTO columnInfo = tableColumns.get(dbColumn.toUpperCase());
                    boolean required = !columnInfo.isNullable() && columnInfo.getDefaultValue() == null;
                    
                    // 使用虚拟列名，确保不与Excel实际列名冲突
                    String virtualColumnName = "__VIRTUAL_" + dbColumn;
                    fieldMapper.addMappingWithDefault(virtualColumnName, dbColumn, required, defaultValue);
                    log.debug("额外字段映射: 虚拟列 '{}' -> 数据库字段 '{}' 使用默认值: {}", 
                              virtualColumnName, dbColumn, defaultValue);
                } else {
                    log.warn("额外字段 '{}' 不存在于表中，将被忽略", dbColumn);
                }
            }
            
            // 处理带生成器的额外字段
            for (Map.Entry<String, String> entry : request.getExtraFieldGenerators().entrySet()) {
                String dbColumn = entry.getKey();
                String generatorId = entry.getValue();
                
                // 检查数据库字段是否存在
                if (tableColumns.containsKey(dbColumn.toUpperCase())) {
                    TableColumnDTO columnInfo = tableColumns.get(dbColumn.toUpperCase());
                    boolean required = !columnInfo.isNullable() && columnInfo.getDefaultValue() == null;
                    
                    // 检查生成器是否存在
                    if (generatorRegistry.hasGenerator(generatorId)) {
                        ValueGenerator generator = generatorRegistry.getGenerator(generatorId);
                        
                        // 使用虚拟列名，确保不与Excel实际列名冲突
                        String virtualColumnName = "__VIRTUAL_" + dbColumn;
                        fieldMapper.addMappingWithGenerator(virtualColumnName, dbColumn, required, generator);
                        log.debug("额外字段映射: 虚拟列 '{}' -> 数据库字段 '{}' 使用生成器: {}", 
                                  virtualColumnName, dbColumn, generator.getName());
                    } else {
                        log.warn("指定的生成器 '{}' 不存在，额外字段 '{}' 将被忽略", generatorId, dbColumn);
                    }
                } else {
                    log.warn("额外字段 '{}' 不存在于表中，将被忽略", dbColumn);
                }
            }
        }
        
        return fieldMapper;
    }
    
    /**
     * 导入管道监听器
     */
    private class ImportPipelineListener implements PipelineListener<Row, Map<String, Object>> {
        @Override
        public void onPipelineStart(com.datatool.exceltooracle.core.DataSource<Row> source, 
                                   com.datatool.exceltooracle.core.DataTarget<Map<String, Object>> target) {
            log.info("开始数据导入: 从 {} 到 {}", source.getName(), target.getName());
        }
        
        @Override
        public void onPipelineComplete(int totalRecords, int successRecords, int failureRecords) {
            log.info("导入完成: 总记录数 {}, 成功 {}, 失败 {}", 
                    totalRecords, successRecords, failureRecords);
        }
        
        @Override
        public void onBatchComplete(int batchSize) {
            log.debug("批处理完成: {} 条记录", batchSize);
        }
        
        @Override
        public void onRecordError(Row sourceData, String errorMessage) {
            log.warn("记录处理错误: 行 {}, 错误: {}", 
                    sourceData.getRowNum() + 1, errorMessage);
        }
    }
} 