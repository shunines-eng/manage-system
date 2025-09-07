package com.example.pim.controller;

import com.example.pim.entity.User;
import com.example.pim.repository.UserRepository;
import com.example.pim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // 检查用户名是否可用
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    // 检查邮箱是否已被注册
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = !userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    // 获取当前登录用户的信息
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 返回用户信息，不包含密码等敏感信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("age", user.getAge());
        userInfo.put("gender", user.getGender());
        userInfo.put("role", user.getRole());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        
        return ResponseEntity.ok(userInfo);
    }

    // 更新用户信息
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> userData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 更新用户信息
        if (userData.containsKey("fullName")) {
            user.setFullName((String) userData.get("fullName"));
        }
        if (userData.containsKey("phone")) {
            user.setPhone((String) userData.get("phone"));
        }
        // 注意：更新邮箱需要额外的验证逻辑
        if (userData.containsKey("email")) {
            String newEmail = (String) userData.get("email");
            if (!user.getEmail().equals(newEmail) && userService.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body("该邮箱已被注册");
            }
            user.setEmail(newEmail);
        }
        
        userRepository.save(user);
        
        return ResponseEntity.ok("用户信息更新成功");
    }

    // 更改密码
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        String confirmPassword = passwordData.get("confirmPassword");
        
        // 验证参数
        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body("请提供完整的密码信息");
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("两次输入的新密码不一致");
        }
        
        // 验证当前密码
        boolean authenticated = userService.authenticate(username, currentPassword);
        if (!authenticated) {
            return ResponseEntity.badRequest().body("当前密码不正确");
        }
        
        // 更新密码
        User user = userService.findByUsername(username);
        userService.updatePassword(user.getId(), newPassword);
        
        return ResponseEntity.ok("密码更新成功");
    }
}