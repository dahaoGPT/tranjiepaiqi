package com.jiepaiqi.audio;

import com.jiepaiqi.ingestion.dto.AudioUploadResponse;
import com.jiepaiqi.device.DeviceMapper;
import com.jiepaiqi.device.Device;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 音频片段控制器。
 * 处理音频文件上传和授权播放。
 */
@RestController
@RequestMapping("/api/devices/{deviceId}/audio-clips")
public class AudioClipController {
    private final AudioClipMapper audioClipMapper;
    private final DeviceMapper deviceMapper;
    private final AudioStorageService audioStorageService;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "audio/wav", "audio/mpeg", "audio/mp4", "audio/x-wav"
    );

    public AudioClipController(AudioClipMapper audioClipMapper, 
                               DeviceMapper deviceMapper,
                               @Value("${jiepaiqi.audio.storage-root:backend/storage/audio}") String storageRoot) {
        this.audioClipMapper = audioClipMapper;
        this.deviceMapper = deviceMapper;
        this.audioStorageService = new LocalAudioStorageService(Paths.get(storageRoot));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AudioUploadResponse uploadAudio(@PathVariable String deviceId,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam("windowStartedAt") String windowStartedAt,
                                           @RequestParam("windowEndedAt") String windowEndedAt) throws IOException {
        Device device = deviceMapper.findBySerialNumber(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceId);
        }

        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的音频格式: " + contentType);
        }

        byte[] bytes = file.getBytes();
        if (bytes.length == 0) {
            throw new IllegalArgumentException("音频内容不能为空");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        AudioClipMetadata metadata = audioStorageService.store(deviceId, fileName, contentType, bytes);

        AudioClip audioClip = new AudioClip();
        audioClip.setId(metadata.getId());
        audioClip.setDeviceId(device.getId());
        audioClip.setWindowStartedAt(Instant.parse(windowStartedAt));
        audioClip.setWindowEndedAt(Instant.parse(windowEndedAt));
        audioClip.setStoragePath(metadata.getStoragePath());
        audioClip.setContentType(contentType);
        audioClip.setDurationSeconds(0);
        audioClip.setSizeBytes((long) bytes.length);
        audioClip.setCreatedAt(Instant.now());

        audioClipMapper.insert(audioClip);

        device.setStatus("ONLINE");
        device.setLastSeenAt(Instant.now());
        deviceMapper.updateStatus(device);

        return AudioUploadResponse.builder()
            .audioClipId(metadata.getId())
            .contentType(contentType)
            .sizeBytes(bytes.length)
            .build();
    }

    @GetMapping("/{audioClipId}/playback")
    public byte[] getAudioPlayback(@PathVariable UUID audioClipId) {
        AudioClip audioClip = audioClipMapper.findById(audioClipId);
        if (audioClip == null) {
            throw new IllegalArgumentException("音频片段不存在");
        }
        return audioStorageService.load(audioClip.getStoragePath());
    }
}