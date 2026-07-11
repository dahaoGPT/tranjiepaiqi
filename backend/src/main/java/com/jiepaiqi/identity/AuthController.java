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

    /**
     * 构造函数。
     * 注入用户数据访问接口依赖。
     * 
     * @param userMapper 用户数据访问接口
     */
    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录。
     * 根据用户名查找用户，不存在时自动创建默认用户。
     * 
     * @param request 登录请求，包含用户名和密码
     * @return 用户信息响应
     */
    @PostMapping("/auth/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            user = createDefaultUser(request.getUsername());
        }
        return UserResponse.fromUser(user);
    }

    /**
     * 获取当前登录用户信息。
     * 
     * @return 当前用户信息响应
     */
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        User user = userMapper.findByUsername("default");
        if (user == null) {
            user = createDefaultUser("default");
        }
        return UserResponse.fromUser(user);
    }

    /**
     * 获取用户绑定的老人ID列表。
     * 
     * @return 绑定的老人ID列表
     */
    @GetMapping("/me/elders")
    public List<UUID> getBoundElders() {
        User user = userMapper.findByUsername("default");
        if (user == null) {
            return Collections.emptyList();
        }
        return userMapper.findBoundElderIds(user.getId());
    }

    /**
     * 创建默认用户。
     * 当用户不存在时自动创建，角色默认为 FAMILY。
     * 
     * @param username 用户名
     * @return 创建的用户对象
     */
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

        /**
         * 从用户实体创建响应对象。
         * 
         * @param user 用户实体对象
         * @return 用户响应对象
         */
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