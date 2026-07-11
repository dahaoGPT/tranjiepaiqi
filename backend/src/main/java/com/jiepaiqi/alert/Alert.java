package com.jiepaiqi.alert;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 异常提醒。
 * 保存需要家属或志愿者确认的事件。
 */
@Data
public class Alert {
    private UUID id;
    private UUID elderId;
    private UUID deviceId;
    private String type;
    private String status;
    private String reason;
    private String suggestedAction;
    private Instant occurredAt;
    private Instant acknowledgedAt;
    private Instant resolvedAt;
    private Instant createdAt;
}