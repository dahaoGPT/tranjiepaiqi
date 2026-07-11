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

    public LocalAudioStorageService(Path storageRoot) {
        this.storageRoot = storageRoot;
    }

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