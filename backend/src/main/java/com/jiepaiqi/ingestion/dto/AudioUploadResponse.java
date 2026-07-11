package com.jiepaiqi.ingestion.dto;

import lombok.Data;
import lombok.Builder;

import java.util.UUID;

/**
 * 音频上传响应。
 */
@Data
@Builder
public class AudioUploadResponse {
    private UUID audioClipId;
    private String contentType;
    private long sizeBytes;
}