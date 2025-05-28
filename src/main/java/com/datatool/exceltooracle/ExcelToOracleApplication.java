package com.datatool.exceltooracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

/**
 * Excel导入Oracle应用程序主类
 */
@SpringBootApplication
@EnableBatchProcessing
@Slf4j
public class ExcelToOracleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelToOracleApplication.class, args);
        log.info("ExcelToOracleApplication started:" + "http://localhost:8080");
    }
} 