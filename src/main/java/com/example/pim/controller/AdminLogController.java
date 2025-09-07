package com.example.pim.controller;

import com.example.pim.entity.AdminOperationLog;
import com.example.pim.service.AdminOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    @Autowired
    private AdminOperationLogService adminOperationLogService;

    /**
     * 获取管理员操作日志列表（支持分页、搜索和筛选）
     */
    @GetMapping
    public ResponseEntity<?> getOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String adminUsername,  // 管理员用户名筛选
            @RequestParam(required = false) String operationType,  // 操作类型筛选
            @RequestParam(required = false) String objectType,     // 操作对象类型筛选
            @RequestParam(required = false) String startTime,      // 开始时间筛选
            @RequestParam(required = false) String endTime,        // 结束时间筛选
            @RequestParam(required = false) Boolean success        // 操作结果筛选
    ) {
        try {
            // 创建分页请求，默认按操作时间降序排序
            Pageable pageable = PageRequest.of(
                    page - 1, // PageRequest是从0开始的，而前端通常从1开始
                    size,
                    Sort.by(Sort.Direction.DESC, "operationTime")
            );

            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime, formatter);
            }
            
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime, formatter);
            }

            // 获取日志列表
            Page<AdminOperationLog> logPage = adminOperationLogService.findLogs(
                    adminUsername, operationType, objectType, startDateTime, endDateTime, success, pageable);

            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logPage.getContent());
            response.put("total", logPage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", logPage.getTotalPages());
            response.put("pageSize", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getLogStatistics(
            @RequestParam(required = false) String adminUsername,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        try {
            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime, formatter);
            }
            
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime, formatter);
            }

            // 获取统计信息
            Map<String, Long> statistics = adminOperationLogService.getLogStatistics(
                    adminUsername, startDateTime, endDateTime);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取日志统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作类型列表
     */
    @GetMapping("/operation-types")
    public ResponseEntity<?> getOperationTypes() {
        try {
            return ResponseEntity.ok(adminOperationLogService.getDistinctOperationTypes());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取操作类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取对象类型列表
     */
    @GetMapping("/object-types")
    public ResponseEntity<?> getObjectTypes() {
        try {
            return ResponseEntity.ok(adminOperationLogService.getDistinctObjectTypes());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取对象类型列表失败: " + e.getMessage());
        }
    }
}