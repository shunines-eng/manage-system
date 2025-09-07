package com.example.pim.repository;

import com.example.pim.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
    
    // 分页查询操作日志
    Page<AdminOperationLog> findByAdminUsernameContainingAndOperationTypeContainingAndObjectTypeContainingAndOperationTimeBetweenAndSuccess(
            String adminUsername, String operationType, String objectType,
            LocalDateTime startTime, LocalDateTime endTime, Boolean success,
            Pageable pageable);
    
    // 获取统计信息
    @Query(value = "SELECT operation_type as operationType, COUNT(*) as count FROM admin_operation_log " +
            "WHERE (?1 IS NULL OR admin_username LIKE %?1%) " +
            "AND (?2 IS NULL OR operation_time >= ?2) " +
            "AND (?3 IS NULL OR operation_time <= ?3) " +
            "GROUP BY operation_type", nativeQuery = true)
    List<Map<String, Object>> getLogStatisticsByOperationType(String adminUsername, LocalDateTime startTime, LocalDateTime endTime);
    
    // 获取所有不同的操作类型
    @Query("SELECT DISTINCT o.operationType FROM AdminOperationLog o")
    List<String> findDistinctOperationTypes();
    
    // 获取所有不同的对象类型
    @Query("SELECT DISTINCT o.objectType FROM AdminOperationLog o")
    List<String> findDistinctObjectTypes();
}