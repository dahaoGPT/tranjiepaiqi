package com.jiepaiqi.audio;

/**
 * 音频片段存储服务。
 * 负责把设备上传的原始音频保存到本地存储，并提供复盘读取能力。
 */
public interface AudioStorageService {
    /** 保存单个设备采样窗口的原始音频片段。 */
    AudioClipMetadata store(String deviceSerial, String originalFileName, String contentType, byte[] bytes);

    /** 按存储路径读取音频内容，用于授权复盘播放。 */
    byte[] load(String storagePath);
}