package com.example.pim.service.impl;

import com.example.pim.entity.User;
import com.example.pim.repository.UserRepository;
import com.example.pim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 最大登录失败次数
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    // 账户锁定时间（分钟）
    private static final int ACCOUNT_LOCK_MINUTES = 10;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 检查账户是否已锁定
            if (isAccountLocked(username)) {
                // 检查锁定时间是否已过
                unlockAccountIfExpired(user.getId());
                // 如果仍然锁定，抛出异常
                if (user.getAccountLocked()) {
                    long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), 
                            user.getLockTime().plusMinutes(ACCOUNT_LOCK_MINUTES));
                    throw new RuntimeException("账户已被锁定，请" + minutesLeft + "分钟后再试");
                }
            }
            
            // 验证密码
            if (passwordEncoder.matches(password, user.getPassword()) && user.getEnabled()) {
                // 登录成功，重置失败次数
                resetLoginAttempts(user.getId());
                return user;
            } else {
                // 登录失败，增加失败次数
                incrementLoginAttempts(username);
                throw new RuntimeException("用户名或密码错误");
            }
        }

        throw new RuntimeException("用户名或密码错误");
    }
    
    @Override
    public void incrementLoginAttempts(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            int attempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(attempts);
            
            // 如果失败次数达到阈值，锁定账户
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                lockAccount(user.getId());
            }
            
            userRepository.save(user);
        }
    }
    
    @Override
    public void resetLoginAttempts(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }
    
    @Override
    public void lockAccount(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    @Override
    public boolean isAccountLocked(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 如果账户被锁定，检查锁定时间是否已过
            if (user.getAccountLocked() && user.getLockTime() != null) {
                LocalDateTime lockExpiryTime = user.getLockTime().plusMinutes(ACCOUNT_LOCK_MINUTES);
                if (LocalDateTime.now().isAfter(lockExpiryTime)) {
                    // 锁定时间已过，自动解锁
                    unlockAccountIfExpired(user.getId());
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void unlockAccountIfExpired(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getAccountLocked() && user.getLockTime() != null) {
                LocalDateTime lockExpiryTime = user.getLockTime().plusMinutes(ACCOUNT_LOCK_MINUTES);
                if (LocalDateTime.now().isAfter(lockExpiryTime)) {
                    // 锁定时间已过，解锁账户并重置失败次数
                    user.setAccountLocked(false);
                    user.setLoginAttempts(0);
                    user.setLockTime(null);
                    userRepository.save(user);
                }
            }
        }
    }

    @Override
    public void createVerificationToken(User user, String token) {
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
    }

    @Override
    public boolean validateVerificationToken(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getVerificationTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendVerificationEmail(User user, String token) {
        // 在实际项目中，这里应该发送验证邮件
        // 由于邮件配置较复杂，这里仅打印日志
        System.out.println("Sending verification email to: " + user.getEmail());
        System.out.println("Verification link: http://localhost:8080/api/auth/verify-email?token=" + token);
    }

    @Override
    public User updateLastLoginTime(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }
        throw new RuntimeException("用户不存在");
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getUsersPage(pageable);
        }
        // 实际项目中应该在UserRepository中添加search方法，这里为了演示简单处理
        // 注意：这不是一个高效的实现，只是为了演示功能
        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        
        // 手动实现分页（实际项目中应该使用数据库查询）
        int start = Math.min((int) pageable.getOffset(), filteredUsers.size());
        int end = Math.min(start + pageable.getPageSize(), filteredUsers.size());
        
        return new org.springframework.data.domain.PageImpl<>(
                filteredUsers.subList(start, end),
                pageable,
                filteredUsers.size()
        );
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户名（如果提供了新用户名）
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
            // 检查新用户名是否与当前用户名不同
            if (!user.getUsername().equals(userDetails.getUsername())) {
                // 检查新用户名是否已被其他用户使用
                if (existsByUsername(userDetails.getUsername())) {
                    throw new RuntimeException("用户名已被使用");
                }
                user.setUsername(userDetails.getUsername());
            }
        }

        // 更新用户信息
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        if (userDetails.getEmail() != null) {
            // 检查新邮箱是否已被其他用户使用
            if (!user.getEmail().equals(userDetails.getEmail()) && existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getAge() != null) {
            user.setAge(userDetails.getAge());
        }
        if (userDetails.getGender() != null) {
            user.setGender(userDetails.getGender());
        }
        if (userDetails.getEnabled() != null) {
            user.setEnabled(userDetails.getEnabled());
        }
        
        // 更新时间戳
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }
}
