package com.jiepaiqi.ingestion.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 声学特征上报请求。
 */
@Data
public class FeatureIngestionRequest {
    private List<FeatureDto> features;

    /**
     * 声学特征数据传输对象。
     */
    @Data
    public static class FeatureDto {
        /** 特征窗口开始时间。 */
        private Instant windowStartedAt;
        /** 特征窗口结束时间。 */
        private Instant windowEndedAt;
        /** 平均分贝值。 */
        private Double averageDecibels;
        /** 峰值分贝值。 */
        private Double peakDecibels;
        /** 低频能量。 */
        private Double lowBandEnergy;
        /** 中频能量。 */
        private Double midBandEnergy;
        /** 高频能量。 */
        private Double highBandEnergy;
        /** 水流置信度（0-1）。 */
        private Double flowConfidence;
        /** 关联的音频片段ID。 */
        private UUID audioClipId;
    }
}