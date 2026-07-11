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

    @Data
    public static class FeatureDto {
        private Instant windowStartedAt;
        private Instant windowEndedAt;
        private Double averageDecibels;
        private Double peakDecibels;
        private Double lowBandEnergy;
        private Double midBandEnergy;
        private Double highBandEnergy;
        private Double flowConfidence;
        private UUID audioClipId;
    }
}