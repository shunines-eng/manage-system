package com.example.pim.controller;

import com.example.pim.entity.User;
import com.example.pim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserService userService;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取用户列表失败: " + e.getMessage());
        }
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取用户信息失败: " + e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            // 防止更新敏感信息
            userDetails.setId(id); // 确保ID一致
            userDetails.setPassword(null); // 不允许直接修改密码
            userDetails.setUsername(null); // 不允许修改用户名
            
            User updatedUser = userService.updateUser(id, userDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("user", updatedUser);
            response.put("message", "用户信息更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新用户信息失败: " + e.getMessage());
        }
    }

    // 创建新用户
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", createdUser);
            response.put("message", "用户创建成功");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("创建用户失败: " + e.getMessage());
        }
    }
    
    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除用户失败: " + e.getMessage());
        }
    }

    // 更新用户密码的专用接口
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request) {
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
            
            return ResponseEntity.ok("密码更新成功");
        } catch (Exception e) {
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