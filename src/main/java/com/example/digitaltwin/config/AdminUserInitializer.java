package com.example.digitaltwin.config;

import com.example.digitaltwin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 首次启动时若不存在 admin，则写入默认管理员（admin / admin123），密码为 BCrypt。
 */
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        if (!userService.existsByUsername("admin")) {
            userService.register("admin", "admin123", "管理员");
        }
    }
}
