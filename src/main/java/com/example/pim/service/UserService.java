package com.example.pim.service;

import com.example.pim.entity.User;

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
    User getUserById(Long userId);
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
}