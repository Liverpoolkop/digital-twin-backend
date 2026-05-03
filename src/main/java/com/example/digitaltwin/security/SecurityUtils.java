package com.example.digitaltwin.security;

import com.example.digitaltwin.common.UserRoles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {
        AuthenticatedUser user = getCurrentUserOrNull();
        if (user == null) {
            throw new IllegalStateException("当前未登录");
        }
        return user;
    }

    public static AuthenticatedUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    public static boolean isAdmin() {
        return UserRoles.ADMIN.equalsIgnoreCase(getCurrentUser().getRole());
    }

    public static UsernamePasswordAuthenticationToken buildAuthentication(AuthenticatedUser user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(() -> "ROLE_" + user.getRole())
        );
    }
}
