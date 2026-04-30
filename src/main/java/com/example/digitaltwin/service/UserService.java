package com.example.digitaltwin.service;

import com.example.digitaltwin.entity.User;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User findById(Long id);
    boolean register(String username, String password, String nickname);
    boolean register(String username, String password, String nickname, String role);
    boolean existsByUsername(String username);
    List<User> listUsers(String username, String role, String status);
    boolean updateRole(Long userId, String role);
    boolean updateStatus(Long userId, String status);
    long countUsers();
}
