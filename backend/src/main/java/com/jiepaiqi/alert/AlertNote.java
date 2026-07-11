package com.jiepaiqi.alert;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 异常处理备注。
 * 记录确认、联系、上门等人工处理信息。
 */
@Data
public class AlertNote {
    private UUID id;
    private UUID alertId;
    private UUID authorUserId;
    private String body;
    private Instant createdAt;
}