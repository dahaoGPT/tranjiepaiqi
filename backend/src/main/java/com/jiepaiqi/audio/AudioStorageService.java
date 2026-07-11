package com.jiepaiqi.audio;

/**
 * 音频片段存储服务。
 * 负责把设备上传的原始音频保存到本地存储，并提供复盘读取能力。
 */
public interface AudioStorageService {
    /**
     * 保存单个设备采样窗口的原始音频片段。
     * @param deviceSerial 设备序列号
     * @param originalFileName 原始文件名
     * @param contentType 内容类型（MIME类型）
     * @param bytes 音频文件字节数组
     * @return 音频片段元数据
     */
    AudioClipMetadata store(String deviceSerial, String originalFileName, String contentType, byte[] bytes);

    /**
     * 按存储路径读取音频内容，用于授权复盘播放。
     * @param storagePath 存储路径
     * @return 音频文件字节数组
     */
    byte[] load(String storagePath);
}