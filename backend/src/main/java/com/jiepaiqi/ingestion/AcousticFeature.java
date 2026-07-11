package com.jiepaiqi.ingestion;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 声学特征记录。
 * 后端用它判断是否存在用水事件。
 */
@Data
public class AcousticFeature {
    private UUID id;
    private UUID deviceId;
    private UUID audioClipId;
    private Instant windowStartedAt;
    private Instant windowEndedAt;
    private Double averageDecibels;
    private Double peakDecibels;
    private Double lowBandEnergy;
    private Double midBandEnergy;
    private Double highBandEnergy;
    private Double flowConfidence;
    private Instant createdAt;
}