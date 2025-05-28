package com.datatool.exceltooracle.controller;

import com.datatool.exceltooracle.core.ValueGenerator;
import com.datatool.exceltooracle.core.ValueGeneratorRegistry;
import com.datatool.exceltooracle.dto.TableStructureDTO;
import com.datatool.exceltooracle.dto.ValueGeneratorDTO;
import com.datatool.exceltooracle.service.TableMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表结构控制器
 * 提供表结构查询API
 */
@RestController
@RequestMapping("/api/table")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin // 添加跨域支持
public class TableStructureController {
    
    private final TableMetadataService tableMetadataService;
    
    /**
     * 获取表结构
     * 
     * @param tableName 表名
     * @return 表结构信息
     */
    @GetMapping("/structure/{tableName}")
    public ResponseEntity<TableStructureDTO> getTableStructure(@PathVariable String tableName) {
        log.info("收到获取表结构请求: {}", tableName);
        
        TableStructureDTO structure = tableMetadataService.getTableStructure(tableName);
        
        if (structure.isExists()) {
            log.info("表 {} 结构获取成功，共 {} 列", tableName, structure.getColumns().size());
            return ResponseEntity.ok(structure);
        } else {
            log.warn("表 {} 不存在或获取结构失败: {}", tableName, structure.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 检查表是否存在
     * 
     * @param tableName 表名
     * @return 检查结果
     */
    @GetMapping("/exists/{tableName}")
    public ResponseEntity<Boolean> tableExists(@PathVariable String tableName) {
        log.info("检查表是否存在: {}", tableName);
        boolean exists = tableMetadataService.tableExists(tableName);
        log.info("表 {} {}", tableName, exists ? "存在" : "不存在");
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 获取所有可用的值生成器
     * 
     * @return 值生成器列表
     */
    @GetMapping("/generators")
    public ResponseEntity<List<ValueGeneratorDTO>> getValueGenerators() {
        log.info("获取所有值生成器");
        
        List<ValueGeneratorDTO> generators = ValueGeneratorRegistry.getInstance()
                .getAllGenerators()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("找到 {} 个值生成器", generators.size());
        return ResponseEntity.ok(generators);
    }
    
    /**
     * 将值生成器转换为DTO
     * 
     * @param generator 值生成器
     * @return 值生成器DTO
     */
    private ValueGeneratorDTO convertToDTO(ValueGenerator generator) {
        return ValueGeneratorDTO.builder()
                .id(generator.getId())
                .name(generator.getName())
                .description(generator.getDescription())
                .valueType(generator.getValueType().getSimpleName())
                .build();
    }
} 