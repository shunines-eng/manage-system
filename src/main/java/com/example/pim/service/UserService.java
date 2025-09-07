package com.example.pim.service;

import com.example.pim.entity.User;

public interface UserService {
    User registerUser(User user);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean authenticate(String username, String password);
    void updateLastLoginTime(String username);
    void updatePassword(Long userId, String newPassword);
}