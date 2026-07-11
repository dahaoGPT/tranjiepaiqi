package com.jiepaiqi.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地音频存储服务实现。
 * 将音频文件保存到本地文件系统。
 */
public class LocalAudioStorageService implements AudioStorageService {
    private final Path storageRoot;

    /**
     * 构造函数。
     * 
     * @param storageRoot 存储根目录路径
     */
    public LocalAudioStorageService(Path storageRoot) {
        this.storageRoot = storageRoot;
    }

    /**
     * 存储音频文件到本地文件系统。
     * 在存储根目录下按设备序列号创建子目录，文件名进行安全处理后保存。
     * 
     * @param deviceSerial     设备序列号
     * @param originalFileName 原始文件名
     * @param contentType      内容类型（MIME类型）
     * @param bytes            音频文件字节数组
     * @return 音频片段元数据，包含ID、存储路径、内容类型和大小
     * @throws IllegalArgumentException 音频内容为空时抛出
     * @throws RuntimeException         文件写入失败时抛出
     */
    @Override
    public AudioClipMetadata store(String deviceSerial, String originalFileName, String contentType, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("音频内容不能为空");
        }

        try {
            Path deviceDir = storageRoot.resolve(deviceSerial);
            if (!Files.exists(deviceDir)) {
                Files.createDirectories(deviceDir);
            }

            String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path filePath = deviceDir.resolve(safeFileName);

            Files.write(filePath, bytes);

            return AudioClipMetadata.builder()
                    .id(UUID.randomUUID())
                    .storagePath(deviceDir.relativize(filePath).toString())
                    .contentType(contentType)
                    .sizeBytes(bytes.length)
                    .durationSeconds(0)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("存储音频失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从本地文件系统加载音频文件。
     * 
     * @param storagePath 相对存储路径
     * @return 音频文件字节数组
     * @throws IllegalArgumentException 文件不存在时抛出
     * @throws RuntimeException         文件读取失败时抛出
     */
    @Override
    public byte[] load(String storagePath) {
        try {
            Path filePath = storageRoot.resolve(storagePath);
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("音频文件不存在: " + storagePath);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("读取音频失败: " + e.getMessage(), e);
        }
    }
}