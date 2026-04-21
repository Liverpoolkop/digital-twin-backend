package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.entity.User;
import com.example.digitaltwin.service.UserService;
import com.example.digitaltwin.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null
                || req.getUsername().isBlank() || req.getPassword().isBlank()) {
            return Result.error("账号和密码不能为空");
        }
        if (req.getPassword().length() < 6) {
            return Result.error("密码至少 6 位");
        }
        if (userService.existsByUsername(req.getUsername().trim())) {
            return Result.error("该账号已被注册");
        }
        String nickname = req.getNickname() != null && !req.getNickname().isBlank()
                ? req.getNickname().trim()
                : req.getUsername().trim();
        boolean ok = userService.register(req.getUsername().trim(), req.getPassword(), nickname);
        return ok ? Result.success() : Result.error("注册失败");
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest req) {
        if (req.getUsername() == null || req.getPassword() == null
                || req.getUsername().isBlank() || req.getPassword().isBlank()) {
            return Result.error("账号或密码不能为空");
        }
        User user = userService.findByUsername(req.getUsername().trim());
        if (user == null) {
            return Result.error("账号或密码错误");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error("账号或密码错误");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        return Result.success(Map.of(
                "token", token,
                "username", user.getUsername()
        ));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;
    }
}
