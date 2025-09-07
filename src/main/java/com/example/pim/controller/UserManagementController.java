package com.example.pim.controller;

import com.example.pim.entity.User;
import com.example.pim.service.UserService;
import com.example.pim.service.AdminOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminOperationLogService adminOperationLogService;

    // 获取所有用户（支持分页和搜索）
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            // 创建分页请求，默认按ID升序排序
            Pageable pageable = PageRequest.of(
                    page - 1, // PageRequest是从0开始的，而前端通常从1开始
                    size,
                    Sort.by(Sort.Direction.ASC, "id")
            );
            
            Page<User> userPage;
            if (keyword != null && !keyword.trim().isEmpty()) {
                userPage = userService.searchUsers(keyword, pageable);
            } else {
                userPage = userService.getUsersPage(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", userPage.getContent());
            response.put("total", userPage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", userPage.getTotalPages());
            response.put("pageSize", size);
            
            // 记录查询日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", null, 
                    keyword != null ? "搜索: " + keyword : "所有用户", true, request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", null, 
                    keyword != null ? "搜索: " + keyword : "所有用户", false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取用户列表失败: " + e.getMessage());
        }
    }
    
    // 保持原有接口兼容性的方法
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("total", users.size());
            // 记录查询日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", null, "所有用户(all接口)", true, request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", null, "所有用户(all接口)", false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取用户列表失败: " + e.getMessage());
        }
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpServletRequest request) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                // 记录失败日志
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                adminOperationLogService.logQuery(authentication, "USER", id, "未知用户", false, request);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
            }
            
            // 记录查询日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", id, user.getUsername(), true, request);
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logQuery(authentication, "USER", id, "未知用户", false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取用户信息失败: " + e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, HttpServletRequest request) {
        try {
            // 防止更新敏感信息
            userDetails.setId(id); // 确保ID一致
            userDetails.setPassword(null); // 不允许直接修改密码
            userDetails.setUsername(null); // 不允许修改用户名
            
            User updatedUser = userService.updateUser(id, userDetails);
            
            // 记录更新日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logUpdate(authentication, "USER", id, updatedUser.getUsername(), true, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", updatedUser);
            response.put("message", "用户信息更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = userDetails != null && userDetails.getUsername() != null ? 
                    userDetails.getUsername() : "未知用户";
            adminOperationLogService.logUpdate(authentication, "USER", id, username, false, request);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = userDetails != null && userDetails.getUsername() != null ? 
                    userDetails.getUsername() : "未知用户";
            adminOperationLogService.logUpdate(authentication, "USER", id, username, false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新用户信息失败: " + e.getMessage());
        }
    }

    // 创建新用户
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱已存在");
            }
            
            // 注册新用户
            User createdUser = userService.registerUser(user);
            
            // 记录创建日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logCreate(authentication, "USER", createdUser.getId(), createdUser.getUsername(), true, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", createdUser);
            response.put("message", "用户创建成功");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = user != null && user.getUsername() != null ? user.getUsername() : "未知用户";
            adminOperationLogService.logCreate(authentication, "USER", null, username, false, request);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = user != null && user.getUsername() != null ? user.getUsername() : "未知用户";
            adminOperationLogService.logCreate(authentication, "USER", null, username, false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("创建用户失败: " + e.getMessage());
        }
    }
    
    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 先获取用户信息用于日志记录
            User user = userService.getUserById(id);
            String username = user != null ? user.getUsername() : "未知用户";
            
            userService.deleteUser(id);
            
            // 记录删除日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logDelete(authentication, "USER", id, username, true, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logDelete(authentication, "USER", id, "未知用户", false, request);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logDelete(authentication, "USER", id, "未知用户", false, request);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除用户失败: " + e.getMessage());
        }
    }

    // 更新用户密码的专用接口
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request, 
                                               HttpServletRequest servletRequest) {
        try {
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新密码不能为空");
            }
            
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
            }
            
            // 更新密码
            userService.updatePassword(user.getId(), request.getNewPassword());
            
            // 记录更新密码日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logUpdate(authentication, "USER_PASSWORD", id, user.getUsername(), true, servletRequest);
            
            return ResponseEntity.ok("密码更新成功");
        } catch (Exception e) {
            // 记录失败日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            adminOperationLogService.logUpdate(authentication, "USER_PASSWORD", id, "未知用户", false, servletRequest);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新密码失败: " + e.getMessage());
        }
    }

    // 密码更新请求类
    public static class PasswordUpdateRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}