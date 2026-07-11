package com.jiepaiqi.audio;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 原始音频片段元数据。
 * 用于异常复盘和人工核实。
 */
@Data
public class AudioClip {
    private UUID id;
    private UUID deviceId;
    private Instant windowStartedAt;
    private Instant windowEndedAt;
    private String storagePath;
    private String contentType;
    private Integer durationSeconds;
    private Long sizeBytes;
    private Instant createdAt;
}