package com.example.pim.service;

import com.example.pim.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User authenticate(String username, String password);
    User updateLastLoginTime(Long userId);
    void updatePassword(Long userId, String newPassword);
    List<User> getAllUsers();
    Page<User> getUsersPage(Pageable pageable);
    Page<User> searchUsers(String keyword, Pageable pageable);
    User getUserById(Long userId);
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
    
    // 登录失败处理相关方法
    void incrementLoginAttempts(String username);
    void resetLoginAttempts(Long userId);
    void lockAccount(Long userId);
    boolean isAccountLocked(String username);
    void unlockAccountIfExpired(Long userId);
}