package com.example.pim.service;

import com.example.pim.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminOperationLogService {

    // 记录操作日志
    void logOperation(Authentication authentication, String operationType, String objectType,
                      Long objectId, String objectName, boolean isSuccess, HttpServletRequest request);
    
    // 记录创建操作
    void logCreate(Authentication authentication, String objectType, Long objectId, String objectName,
                  boolean isSuccess, HttpServletRequest request);
    
    // 记录更新操作
    void logUpdate(Authentication authentication, String objectType, Long objectId, String objectName,
                  boolean isSuccess, HttpServletRequest request);
    
    // 记录删除操作
    void logDelete(Authentication authentication, String objectType, Long objectId, String objectName,
                  boolean isSuccess, HttpServletRequest request);
    
    // 记录查询操作
    void logQuery(Authentication authentication, String objectType, Long objectId, String objectName,
                 boolean isSuccess, HttpServletRequest request);
    
    // 分页查询操作日志
    Page<AdminOperationLog> findLogs(String adminUsername, String operationType, String objectType,
                                     LocalDateTime startTime, LocalDateTime endTime, Boolean success,
                                     Pageable pageable);
    
    // 获取日志统计信息
    Map<String, Long> getLogStatistics(String adminUsername, LocalDateTime startTime, LocalDateTime endTime);
    
    // 获取不同的操作类型
    List<String> getDistinctOperationTypes();
    
    // 获取不同的对象类型
    List<String> getDistinctObjectTypes();
}