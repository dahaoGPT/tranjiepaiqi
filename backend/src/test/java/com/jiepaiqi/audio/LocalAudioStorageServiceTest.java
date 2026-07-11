package com.jiepaiqi.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class LocalAudioStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesAudioClipUnderDeviceFolder() throws Exception {
        LocalAudioStorageService service = new LocalAudioStorageService(tempDir);

        AudioClipMetadata metadata = service.store(
            "device-001",
            "2026-07-05T08-00-00Z.wav",
            "audio/wav",
            new byte[] {1, 2, 3, 4}
        );

        assertThat(metadata.getStoragePath()).contains("device-001");
        assertThat(metadata.getContentType()).isEqualTo("audio/wav");
        assertThat(metadata.getSizeBytes()).isEqualTo(4);
        assertThat(tempDir.resolve(metadata.getStoragePath())).exists();
    }

    @Test
    void loadsStoredAudioClip() throws Exception {
        LocalAudioStorageService service = new LocalAudioStorageService(tempDir);
        byte[] originalData = new byte[] {1, 2, 3, 4, 5, 6};

        AudioClipMetadata metadata = service.store(
            "device-001",
            "test.wav",
            "audio/wav",
            originalData
        );

        byte[] loadedData = service.load(metadata.getStoragePath());
        assertThat(loadedData).isEqualTo(originalData);
    }
}