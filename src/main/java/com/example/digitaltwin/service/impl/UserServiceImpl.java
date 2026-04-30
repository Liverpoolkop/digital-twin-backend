package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.common.UserRoles;
import com.example.digitaltwin.common.UserStatuses;
import com.example.digitaltwin.entity.User;
import com.example.digitaltwin.mapper.UserMapper;
import com.example.digitaltwin.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean register(String username, String password, String nickname) {
        return register(username, password, nickname, UserRoles.USER);
    }

    @Override
    public boolean register(String username, String password, String nickname, String role) {
        if (existsByUsername(username)) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : username);
        user.setRole(normalizeRole(role));
        user.setStatus(UserStatuses.ACTIVE);
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public List<User> listUsers(String username, String role, String status) {
        return userMapper.selectUsers(blankToNull(username), normalizeNullableRole(role), normalizeNullableStatus(status));
    }

    @Override
    public boolean updateRole(Long userId, String role) {
        return userMapper.updateRole(userId, normalizeRole(role)) > 0;
    }

    @Override
    public boolean updateStatus(Long userId, String status) {
        return userMapper.updateStatus(userId, normalizeStatus(status)) > 0;
    }

    @Override
    public long countUsers() {
        return userMapper.countAll();
    }

    private String normalizeRole(String role) {
        if (UserRoles.ADMIN.equalsIgnoreCase(role)) {
            return UserRoles.ADMIN;
        }
        return UserRoles.USER;
    }

    private String normalizeStatus(String status) {
        if (UserStatuses.DISABLED.equalsIgnoreCase(status)) {
            return UserStatuses.DISABLED;
        }
        return UserStatuses.ACTIVE;
    }

    private String normalizeNullableRole(String role) {
        return StringUtils.hasText(role) ? normalizeRole(role) : null;
    }

    private String normalizeNullableStatus(String status) {
        return StringUtils.hasText(status) ? normalizeStatus(status) : null;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
