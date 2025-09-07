package com.example.pim.service.impl;

import com.example.pim.entity.AdminOperationLog;
import com.example.pim.entity.User;
import com.example.pim.repository.AdminOperationLogRepository;
import com.example.pim.service.AdminOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    private final AdminOperationLogRepository adminOperationLogRepository;

    @Autowired
    public AdminOperationLogServiceImpl(AdminOperationLogRepository adminOperationLogRepository) {
        this.adminOperationLogRepository = adminOperationLogRepository;
    }

    @Override
    public void logOperation(Authentication authentication, String operationType, String objectType,
                             Long objectId, String objectName, boolean isSuccess, HttpServletRequest request) {
        // 如果authentication为null，尝试从SecurityContext获取
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        if (authentication != null && authentication.isAuthenticated()) {
            AdminOperationLog log = new AdminOperationLog();
            
            // 设置管理员信息
            try {
                // 假设Principal是User对象或包含用户名的对象
                Object principal = authentication.getPrincipal();
                if (principal instanceof User) {
                    User adminUser = (User) principal;
                    log.setAdminId(adminUser.getId());
                    log.setAdminUsername(adminUser.getUsername());
                } else {
                    // 如果不是User对象，使用用户名
                    log.setAdminId(null);
                    log.setAdminUsername(authentication.getName());
                }
            } catch (Exception e) {
                // 如果获取管理员信息失败，至少记录用户名
                log.setAdminId(null);
                log.setAdminUsername(authentication.getName());
            }

            // 设置操作信息
            log.setOperationType(operationType);
            log.setObjectType(objectType);
            log.setObjectId(objectId);
            log.setObjectName(objectName);
            log.setResult(isSuccess ? "SUCCESS" : "FAILURE");
            log.setOperationTime(LocalDateTime.now());

            // 获取IP地址
            if (request != null) {
                String ip = request.getRemoteAddr();
                if (ip != null && !ip.isEmpty()) {
                    log.setIpAddress(ip);
                }
            }

            // 保存日志
            adminOperationLogRepository.save(log);
        }
    }

    @Override
    public void logCreate(Authentication authentication, String objectType, Long objectId, String objectName,
                         boolean isSuccess, HttpServletRequest request) {
        logOperation(authentication, "CREATE", objectType, objectId, objectName, isSuccess, request);
    }

    @Override
    public void logUpdate(Authentication authentication, String objectType, Long objectId, String objectName,
                         boolean isSuccess, HttpServletRequest request) {
        logOperation(authentication, "UPDATE", objectType, objectId, objectName, isSuccess, request);
    }

    @Override
    public void logDelete(Authentication authentication, String objectType, Long objectId, String objectName,
                         boolean isSuccess, HttpServletRequest request) {
        logOperation(authentication, "DELETE", objectType, objectId, objectName, isSuccess, request);
    }

    @Override
    public void logQuery(Authentication authentication, String objectType, Long objectId, String objectName,
                        boolean isSuccess, HttpServletRequest request) {
        logOperation(authentication, "QUERY", objectType, objectId, objectName, isSuccess, request);
    }

    @Override
    public Page<AdminOperationLog> findLogs(String adminUsername, String operationType, String objectType,
                                          LocalDateTime startTime, LocalDateTime endTime, Boolean success,
                                          Pageable pageable) {
        // 处理null参数，确保查询条件正确传递
        String usernameParam = adminUsername != null && !adminUsername.trim().isEmpty() ? adminUsername : "";
        String typeParam = operationType != null && !operationType.trim().isEmpty() ? operationType : "";
        String objTypeParam = objectType != null && !objectType.trim().isEmpty() ? objectType : "";
        
        // 如果开始时间为null，设置为很久以前的时间
        LocalDateTime startParam = startTime != null ? startTime : LocalDateTime.now().minusYears(100);
        // 如果结束时间为null，设置为现在时间
        LocalDateTime endParam = endTime != null ? endTime : LocalDateTime.now().plusDays(1);
        
        return adminOperationLogRepository.findByAdminUsernameContainingAndOperationTypeContainingAndObjectTypeContainingAndOperationTimeBetweenAndSuccess(
                usernameParam, typeParam, objTypeParam, startParam, endParam, success, pageable);
    }

    @Override
    public Map<String, Long> getLogStatistics(String adminUsername, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> statistics = new HashMap<>();
        
        // 初始化常见操作类型的计数为0
        statistics.put("CREATE", 0L);
        statistics.put("UPDATE", 0L);
        statistics.put("DELETE", 0L);
        statistics.put("QUERY", 0L);
        statistics.put("OTHER", 0L);
        
        // 获取数据库统计结果
        List<Map<String, Object>> dbStats = adminOperationLogRepository.getLogStatisticsByOperationType(
                adminUsername, startTime, endTime);
        
        // 将数据库结果转换为Map
        for (Map<String, Object> stat : dbStats) {
            String operationType = (String) stat.get("operationType");
            Long count = ((Number) stat.get("count")).longValue();
            
            // 如果是已知的操作类型，更新计数；否则计入OTHER
            if (statistics.containsKey(operationType)) {
                statistics.put(operationType, count);
            } else {
                statistics.put("OTHER", statistics.get("OTHER") + count);
            }
        }
        
        return statistics;
    }

    @Override
    public List<String> getDistinctOperationTypes() {
        List<String> types = adminOperationLogRepository.findDistinctOperationTypes();
        return types != null ? types : Collections.emptyList();
    }

    @Override
    public List<String> getDistinctObjectTypes() {
        List<String> types = adminOperationLogRepository.findDistinctObjectTypes();
        return types != null ? types : Collections.emptyList();
    }
}