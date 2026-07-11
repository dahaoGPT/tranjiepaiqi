package com.jiepaiqi.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureIngestionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void acceptsValidFeatures() throws Exception {
        mockMvc.perform(post("/api/devices/test-device/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"features\":[{\"windowStartedAt\":\"2026-07-05T08:00:00Z\",\"windowEndedAt\":\"2026-07-05T08:00:10Z\",\"averageDecibels\":42.5,\"peakDecibels\":61.2,\"lowBandEnergy\":0.12,\"midBandEnergy\":0.45,\"highBandEnergy\":0.20,\"flowConfidence\":0.93}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acceptedCount").value(1))
            .andExpect(jsonPath("$.rejectedCount").value(0));
    }
}