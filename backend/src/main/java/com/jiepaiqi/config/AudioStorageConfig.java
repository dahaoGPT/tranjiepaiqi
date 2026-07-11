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

    @Bean
    public AudioStorageService audioStorageService(@Value("${jiepaiqi.audio.storage-root:backend/storage/audio}") String storageRoot) {
        return new LocalAudioStorageService(Paths.get(storageRoot));
    }
}