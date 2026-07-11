package com.jiepaiqi.rhythm;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class WaterEventAggregatorTest {
    @Test
    void groupsAdjacentHighConfidenceWindowsIntoOneWaterEvent() {
        Instant t = Instant.parse("2026-07-05T08:00:00Z");
        List<AcousticFeatureWindow> windows = Arrays.asList(
            AcousticFeatureWindow.sample(t, 0.92),
            AcousticFeatureWindow.sample(t.plusSeconds(10), 0.88),
            AcousticFeatureWindow.sample(t.plusSeconds(20), 0.91)
        );

        List<WaterEvent> events = new WaterEventAggregator(0.80, 15).aggregate(windows);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getDurationSeconds()).isEqualTo(30);
        assertThat(events.get(0).getAverageConfidence()).isBetween(0.89, 0.91);
    }

    @Test
    void ignoresLowConfidenceNoise() {
        Instant t = Instant.parse("2026-07-05T08:00:00Z");
        List<AcousticFeatureWindow> windows = Arrays.asList(
            AcousticFeatureWindow.sample(t, 0.20),
            AcousticFeatureWindow.sample(t.plusSeconds(10), 0.30)
        );

        assertThat(new WaterEventAggregator(0.80, 15).aggregate(windows)).isEmpty();
    }

    @Test
    void splitsEventsWhenGapExceedsThreshold() {
        Instant t = Instant.parse("2026-07-05T08:00:00Z");
        List<AcousticFeatureWindow> windows = Arrays.asList(
            AcousticFeatureWindow.sample(t, 0.90),
            AcousticFeatureWindow.sample(t.plusSeconds(10), 0.90),
            AcousticFeatureWindow.sample(t.plusSeconds(30), 0.90),
            AcousticFeatureWindow.sample(t.plusSeconds(40), 0.90)
        );

        List<WaterEvent> events = new WaterEventAggregator(0.80, 15).aggregate(windows);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getDurationSeconds()).isEqualTo(20);
        assertThat(events.get(1).getDurationSeconds()).isEqualTo(20);
    }
}