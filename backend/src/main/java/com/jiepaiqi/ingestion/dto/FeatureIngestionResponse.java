package com.jiepaiqi.ingestion.dto;

import lombok.Data;
import lombok.Builder;

/**
 * 声学特征上报响应。
 */
@Data
@Builder
public class FeatureIngestionResponse {
    private int acceptedCount;
    private int rejectedCount;
}