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

    /**
     * 创建用水事件对象。
     * 自动计算持续时长（秒）。
     * 
     * @param startedAt  事件开始时间
     * @param endedAt    事件结束时间
     * @param confidence 平均水流置信度（0-1）
     * @return 创建的用水事件对象
     */
    public static WaterEvent of(Instant startedAt, Instant endedAt, double confidence) {
        WaterEvent event = new WaterEvent();
        event.setStartedAt(startedAt);
        event.setEndedAt(endedAt);
        event.setDurationSeconds((int) (endedAt.toEpochMilli() - startedAt.toEpochMilli()) / 1000);
        event.setAverageConfidence(confidence);
        return event;
    }
}