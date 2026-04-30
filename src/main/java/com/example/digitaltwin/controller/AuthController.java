package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.common.UserStatuses;
import com.example.digitaltwin.dto.LoginResponse;
import com.example.digitaltwin.entity.User;
import com.example.digitaltwin.service.UserService;
import com.example.digitaltwin.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

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
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        if (req.getUsername() == null || req.getPassword() == null
                || req.getUsername().isBlank() || req.getPassword().isBlank()) {
            return Result.error("账号或密码不能为空");
        }
        User user = userService.findByUsername(req.getUsername().trim());
        if (user == null) {
            return Result.error("账号或密码错误");
        }
        if (!UserStatuses.ACTIVE.equalsIgnoreCase(user.getStatus())) {
            return Result.error("账号已被禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error("账号或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        ));
    }

    public static class LoginRequest {
        private String username;
        private String password;

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
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;

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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
