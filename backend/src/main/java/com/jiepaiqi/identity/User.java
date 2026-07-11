package com.jiepaiqi.identity;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 系统登录用户。
 * 保存家属、社区志愿者等使用者账号信息。
 */
@Data
public class User {
    private UUID id;
    private String username;
    private String displayName;
    private String role;
    private Instant createdAt;
}