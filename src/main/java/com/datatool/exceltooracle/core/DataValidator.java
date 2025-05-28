package com.datatool.exceltooracle.core;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 数据验证器接口
 * 
 * @param <T> 需要验证的数据类型
 */
public interface DataValidator<T> {
    
    /**
     * 验证数据
     * 
     * @param data 需要验证的数据
     * @param context 处理上下文
     * @return 验证结果
     */
    ValidationResult validate(T data, ProcessContext context);
    
    /**
     * 获取验证器名称
     * 
     * @return 验证器名称
     */
    String getName();
    
    /**
     * 获取验证器描述
     * 
     * @return 验证器描述
     */
    String getDescription();
    
    /**
     * 获取验证器优先级
     * 数字越小优先级越高
     * 
     * @return 验证器优先级
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 验证结果类
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        /**
         * 构造有效的验证结果
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, Collections.emptyList());
        }
        
        /**
         * 构造无效的验证结果
         * 
         * @param errors 错误信息列表
         */
        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }
        
        /**
         * 构造无效的验证结果
         * 
         * @param error 错误信息
         */
        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, Collections.singletonList(error));
        }
        
        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        /**
         * 判断验证结果是否有效
         * 
         * @return 是否有效
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * 获取错误信息列表
         * 
         * @return 错误信息列表
         */
        public List<String> getErrors() {
            return errors;
        }
    }
} 