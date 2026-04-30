package com.example.digitaltwin.security;

import com.example.digitaltwin.common.UserStatuses;
import com.example.digitaltwin.entity.User;
import com.example.digitaltwin.service.UserService;
import com.example.digitaltwin.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.extractUserId(token);
        User user = userId != null ? userService.findById(userId) : null;
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!UserStatuses.ACTIVE.equalsIgnoreCase(user.getStatus())) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus()
        );
        SecurityContextHolder.getContext().setAuthentication(SecurityUtils.buildAuthentication(authenticatedUser));
        filterChain.doFilter(request, response);
    }
}
