package com.example.pim.controller;

import com.example.pim.entity.User;
import com.example.pim.service.UserService;
import com.example.pim.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            // 验证验证码
            if (loginRequest.getCaptcha() == null || loginRequest.getCaptcha().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请输入验证码");
            }

            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证码已过期，请刷新");
            }

            if (!sessionCaptcha.equalsIgnoreCase(loginRequest.getCaptcha().trim())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证码错误");
            }

            // 验证成功后，移除session中的验证码
            session.removeAttribute("captcha");

            // 验证用户名和密码
            // 注意：authenticate方法内部已经处理了登录失败次数统计和账户锁定逻辑
            User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            // 创建Spring Security认证对象
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌
            String jwt = tokenProvider.generateToken(authentication);

            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 直接返回异常消息，包括账户锁定的具体提示
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(registrationRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (userService.existsByEmail(registrationRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱已存在");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(registrationRequest.getUsername());
            user.setPassword(registrationRequest.getPassword());
            user.setEmail(registrationRequest.getEmail());
            user.setPhone(registrationRequest.getPhone());
            user.setFullName(registrationRequest.getFullName());

            // 注册用户
            User registeredUser = userService.registerUser(user);

            // 生成验证令牌并发送验证邮件
            String verificationToken = java.util.UUID.randomUUID().toString();
            userService.createVerificationToken(registeredUser, verificationToken);
            userService.sendVerificationEmail(registeredUser, verificationToken);

            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            response.put("email", registeredUser.getEmail());
            response.put("message", "注册成功，请检查您的邮箱完成验证");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            boolean isValid = userService.validateVerificationToken(token);
            if (isValid) {
                return ResponseEntity.ok("邮箱验证成功");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证链接无效或已过期");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证过程中出现错误");
        }
    }

    // 登录请求类
    public static class LoginRequest {
        private String username;
        private String password;
        private String captcha;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
        }
    }

    // 用户注册请求类
    public static class UserRegistrationRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String fullName;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
