package com.jiepaiqi.rhythm;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 用水事件。
 * 由连续高置信度声学特征聚合而来，表示一次业务可读的用水行为。
 */
@Data
public class WaterEvent {
    private UUID id;
    private UUID elderId;
    private UUID deviceId;
    private Instant startedAt;
    private Instant endedAt;
    private Integer durationSeconds;
    private Double averageConfidence;
    private Instant createdAt;

    public static WaterEvent of(Instant startedAt, Instant endedAt, double confidence) {
        WaterEvent event = new WaterEvent();
        event.setStartedAt(startedAt);
        event.setEndedAt(endedAt);
        event.setDurationSeconds((int) (endedAt.toEpochMilli() - startedAt.toEpochMilli()) / 1000);
        event.setAverageConfidence(confidence);
        return event;
    }
}