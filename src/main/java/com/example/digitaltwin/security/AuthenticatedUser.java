package com.example.digitaltwin.security;

public class AuthenticatedUser {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String status;

    public AuthenticatedUser() {
    }

    public AuthenticatedUser(Long id, String username, String nickname, String role, String status) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
