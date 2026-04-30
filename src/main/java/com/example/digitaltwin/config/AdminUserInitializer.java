package com.example.digitaltwin.config;

import com.example.digitaltwin.common.UserRoles;
import com.example.digitaltwin.entity.User;
import com.example.digitaltwin.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 首次启动时若不存在 admin，则写入默认管理员（admin / admin123），密码为 BCrypt。
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserService userService;

    public AdminUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        User admin = userService.findByUsername("admin");
        if (admin == null) {
            userService.register("admin", "admin123", "管理员", UserRoles.ADMIN);
            return;
        }
        if (!UserRoles.ADMIN.equalsIgnoreCase(admin.getRole())) {
            userService.updateRole(admin.getId(), UserRoles.ADMIN);
        }
    }
}
