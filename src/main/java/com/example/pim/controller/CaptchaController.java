package com.example.pim.controller;

import com.example.pim.util.CaptchaUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateCaptcha(HttpSession session) {
        try {
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
            
            // 将验证码保存到session中，设置5分钟过期
            session.setAttribute("captcha", captchaResult.getCode());
            session.setMaxInactiveInterval(5 * 60); // 5分钟过期
            
            Map<String, String> response = new HashMap<>();
            response.put("image", captchaResult.getImageBase64());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("生成验证码失败");
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateCaptcha(@RequestParam String code, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("captcha");
        
        if (sessionCaptcha == null) {
            return ResponseEntity.badRequest().body("验证码已过期，请刷新");
        }
        
        if (code == null || !sessionCaptcha.equalsIgnoreCase(code)) {
            return ResponseEntity.badRequest().body("验证码错误");
        }
        
        // 验证成功后，可以移除session中的验证码
        session.removeAttribute("captcha");
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", true);
        
        return ResponseEntity.ok(response);
    }
}