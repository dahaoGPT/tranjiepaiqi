package com.jiepaiqi.config;

import com.jiepaiqi.audio.AudioStorageService;
import com.jiepaiqi.audio.LocalAudioStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 音频存储配置。
 */
@Configuration
public class AudioStorageConfig {

    /**
     * 创建音频存储服务 Bean。
     * 使用本地文件系统存储音频文件。
     * 
     * @param storageRoot 音频文件存储根目录路径，默认为 backend/storage/audio
     * @return 音频存储服务实例
     */
    @Bean
    public AudioStorageService audioStorageService(
            @Value("${jiepaiqi.audio.storage-root:backend/storage/audio}") String storageRoot) {
        return new LocalAudioStorageService(Paths.get(storageRoot));
    }
}