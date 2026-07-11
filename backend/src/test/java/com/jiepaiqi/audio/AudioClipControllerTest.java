package com.jiepaiqi.audio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AudioClipControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadsAudioClip() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "sample.wav", "audio/wav", new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/api/devices/test-device/audio-clips")
                .file(file)
                .param("windowStartedAt", "2026-07-05T08:00:00Z")
                .param("windowEndedAt", "2026-07-05T08:00:10Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.audioClipId").exists())
            .andExpect(jsonPath("$.contentType").value("audio/wav"))
            .andExpect(jsonPath("$.sizeBytes").value(4));
    }
}