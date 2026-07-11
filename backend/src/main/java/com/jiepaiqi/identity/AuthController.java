package com.jiepaiqi.identity;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 认证控制器。
 * 提供轻量登录和用户信息查询。
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/auth/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            user = createDefaultUser(request.getUsername());
        }
        return UserResponse.fromUser(user);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        User user = userMapper.findByUsername("default");
        if (user == null) {
            user = createDefaultUser("default");
        }
        return UserResponse.fromUser(user);
    }

    @GetMapping("/me/elders")
    public List<UUID> getBoundElders() {
        User user = userMapper.findByUsername("default");
        if (user == null) {
            return Collections.emptyList();
        }
        return userMapper.findBoundElderIds(user.getId());
    }

    private User createDefaultUser(String username) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setDisplayName(username);
        user.setRole("FAMILY");
        user.setCreatedAt(Instant.now());
        userMapper.insert(user);
        return user;
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserResponse {
        private UUID id;
        private String username;
        private String displayName;
        private String role;

        public static UserResponse fromUser(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .role(user.getRole())
                    .build();
        }
    }
}