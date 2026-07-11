package com.jiepaiqi.audio;

import lombok.Data;
import lombok.Builder;

import java.util.UUID;

/**
 * 音频片段元数据。
 * 记录音频文件的存储信息。
 */
@Data
@Builder
public class AudioClipMetadata {
    private UUID id;
    private String storagePath;
    private String contentType;
    private int durationSeconds;
    private long sizeBytes;
}