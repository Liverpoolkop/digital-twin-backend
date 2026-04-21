package com.example.digitaltwin.service;

import com.example.digitaltwin.entity.User;

public interface UserService {
    User findByUsername(String username);
    boolean register(String username, String password, String nickname);
    boolean existsByUsername(String username);
}
