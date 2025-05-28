package com.datatool.exceltooracle.db;

import com.datatool.exceltooracle.core.DataTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Oracle数据库目标实现类
 */
@Slf4j
public class OracleDataTarget implements DataTarget<Map<String, Object>> {
    
    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final boolean isInsert;
    private final String[] keyColumns;
    private Connection currentConnection;
    private SingleConnectionDataSource singleConnectionDataSource;
    private NamedParameterJdbcTemplate singleConnectionJdbcTemplate;
    
    /**
     * 构造Oracle数据目标
     * 
     * @param jdbcTemplate JdbcTemplate实例
     * @param tableName 表名
     * @param isInsert 是否为插入操作
     * @param keyColumns 唯一键列名（用于更新操作）
     */
    public OracleDataTarget(JdbcTemplate jdbcTemplate, String tableName, boolean isInsert, String[] keyColumns) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.isInsert = isInsert;
        this.keyColumns = keyColumns;
    }
    
    @Override
    public void open() throws IOException {
        log.info("打开Oracle数据目标: {}", tableName);
        try {
            // 获取一个连接并手动管理事务
            currentConnection = jdbcTemplate.getDataSource().getConnection();
            currentConnection.setAutoCommit(false);
            log.debug("已获取数据库连接，设置为手动提交");
            
            // 创建基于单一连接的数据源和JdbcTemplate，确保事务管理一致
            singleConnectionDataSource = new SingleConnectionDataSource(currentConnection, true);
            singleConnectionJdbcTemplate = new NamedParameterJdbcTemplate(singleConnectionDataSource);
            
            log.debug("创建了基于单一连接的JdbcTemplate实例");
        } catch (Exception e) {
            log.error("获取数据库连接失败: {}", e.getMessage(), e);
            throw new IOException("获取数据库连接失败", e);
        }
    }
    
    @Override
    public void close() throws IOException {
        log.info("关闭Oracle数据目标: {}", tableName);
        if (currentConnection != null) {
            try {
                // 提交事务并关闭连接
                currentConnection.commit();
                log.info("提交事务成功");
                
                // 释放资源
                if (singleConnectionDataSource != null) {
                    singleConnectionDataSource.destroy();
                }
                
                // 不要主动关闭connection，让SingleConnectionDataSource去管理它
                log.debug("数据库连接资源已释放");
            } catch (Exception e) {
                log.error("关闭数据库连接时出错: {}", e.getMessage(), e);
                try {
                    // 尝试回滚
                    currentConnection.rollback();
                    log.warn("事务已回滚");
                } catch (Exception ex) {
                    log.error("回滚事务失败: {}", ex.getMessage(), ex);
                } finally {
                    // 释放资源
                    if (singleConnectionDataSource != null) {
                        singleConnectionDataSource.destroy();
                    }
                }
                throw new IOException("关闭数据库连接失败", e);
            }
        }
    }
    
    @Override
    public void write(Map<String, Object> data) throws IOException {
        List<Map<String, Object>> dataList = Collections.singletonList(data);
        write(dataList);
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws IOException {
        if (data.isEmpty()) {
            return;
        }
        
        try {
            if (isInsert) {
                executeInsert(data);
            } else {
                executeUpdate(data);
            }
        } catch (Exception e) {
            log.error("写入数据到Oracle时出错: {}", e.getMessage(), e);
            try {
                // 出错时回滚事务
                if (currentConnection != null && !currentConnection.isClosed()) {
                    currentConnection.rollback();
                    log.warn("写入失败，事务已回滚");
                }
            } catch (Exception ex) {
                log.error("回滚事务失败: {}", ex.getMessage(), ex);
            }
            throw new IOException("写入数据到Oracle失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行插入操作
     * 
     * @param dataList 数据列表
     */
    private void executeInsert(List<Map<String, Object>> dataList) throws SQLException {
        Map<String, Object> firstRow = dataList.get(0);
        String[] columns = firstRow.keySet().toArray(new String[0]);
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");
        
        StringJoiner columnJoiner = new StringJoiner(", ");
        for (String column : columns) {
            columnJoiner.add(column);
        }
        sqlBuilder.append(columnJoiner.toString()).append(") VALUES (");
        
        StringJoiner valueJoiner = new StringJoiner(", ");
        for (String column : columns) {
            valueJoiner.add(":" + column);
        }
        sqlBuilder.append(valueJoiner.toString()).append(")");
        
        String sql = sqlBuilder.toString();
        log.debug("执行批量插入SQL: {}", sql);
        
        // 使用基于同一连接的JdbcTemplate执行更新
        int[] updateCounts = singleConnectionJdbcTemplate.batchUpdate(sql, dataList.toArray(new Map[0]));
        
        int totalInserted = 0;
        for (int count : updateCounts) {
            totalInserted += count;
        }
        log.info("成功插入 {} 条数据", totalInserted);
    }
    
    /**
     * 执行更新操作
     * 
     * @param dataList 数据列表
     */
    private void executeUpdate(List<Map<String, Object>> dataList) throws SQLException {
        if (keyColumns == null || keyColumns.length == 0) {
            throw new IllegalStateException("更新操作必须指定唯一键列");
        }
        
        Map<String, Object> firstRow = dataList.get(0);
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(tableName).append(" SET ");
        
        StringJoiner setJoiner = new StringJoiner(", ");
        for (String column : firstRow.keySet()) {
            boolean isKeyColumn = false;
            for (String keyColumn : keyColumns) {
                if (column.equalsIgnoreCase(keyColumn)) {
                    isKeyColumn = true;
                    break;
                }
            }
            
            if (!isKeyColumn) {
                setJoiner.add(column + " = :" + column);
            }
        }
        sqlBuilder.append(setJoiner.toString()).append(" WHERE ");
        
        StringJoiner whereJoiner = new StringJoiner(" AND ");
        for (String keyColumn : keyColumns) {
            whereJoiner.add(keyColumn + " = :" + keyColumn);
        }
        sqlBuilder.append(whereJoiner.toString());
        
        String sql = sqlBuilder.toString();
        log.debug("执行批量更新SQL: {}", sql);
        
        // 使用基于同一连接的JdbcTemplate执行更新
        int[] updateCounts = singleConnectionJdbcTemplate.batchUpdate(sql, dataList.toArray(new Map[0]));
        
        int totalUpdated = 0;
        for (int count : updateCounts) {
            totalUpdated += count;
        }
        log.info("成功更新 {} 条数据", totalUpdated);
    }
    
    @Override
    public String getName() {
        return "Oracle_" + tableName;
    }
    
    @Override
    public TargetType getType() {
        return TargetType.ORACLE_TABLE;
    }
} 