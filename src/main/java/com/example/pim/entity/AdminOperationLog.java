package com.example.pim.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admin_operation_logs")
public class AdminOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 操作的管理员ID
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    // 操作的管理员用户名
    @Column(name = "admin_username", nullable = false, length = 50)
    private String adminUsername;

    // 操作类型（CREATE, UPDATE, DELETE, QUERY）
    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType;

    // 操作对象类型（USER, ROLE等）
    @Column(name = "object_type", nullable = false, length = 50)
    private String objectType;

    // 操作对象ID
    @Column(name = "object_id")
    private Long objectId;

    // 操作对象名称
    @Column(name = "object_name", length = 100)
    private String objectName;

    // 操作结果（SUCCESS, FAILURE）
    @Column(name = "result", nullable = false, length = 20)
    private String result;

    // 操作时间
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime = LocalDateTime.now();

    // 操作IP地址
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}