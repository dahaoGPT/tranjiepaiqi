package com.jiepaiqi.device;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 声感传感器设备。
 * 记录设备绑定老人和最近在线状态。
 */
@Data
public class Device {
    private UUID id;
    private UUID elderId;
    private String serialNumber;
    private String status;
    private Instant lastSeenAt;
    private Instant createdAt;
}