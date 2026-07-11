package com.jiepaiqi.alert;

import com.jiepaiqi.rhythm.RhythmBaseline;
import com.jiepaiqi.rhythm.WaterEvent;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class AlertRuleEngineTest {
    @Test
    void createsMorningAlertWhenNoWaterAfterMorningWindow() {
        RhythmBaseline baseline = RhythmBaseline.of(LocalTime.of(6, 30), LocalTime.of(9, 0), 5.0, 900.0);
        AlertRuleEngine engine = new AlertRuleEngine();

        assertThat(engine.evaluateMorning(
            Instant.parse("2026-07-05T09:30:00Z"), baseline, Collections.emptyList()
        )).extracting(AlertCandidate::getType).contains(AlertType.NO_MORNING_WATER);
    }

    @Test
    void createsLongFlowAlertForLongContinuousEvent() {
        WaterEvent event = WaterEvent.of(
            Instant.parse("2026-07-05T08:00:00Z"),
            Instant.parse("2026-07-05T08:25:00Z"),
            0.91
        );

        assertThat(new AlertRuleEngine().evaluateLongFlow(Arrays.asList(event), 20 * 60))
            .extracting(AlertCandidate::getType)
            .contains(AlertType.LONG_CONTINUOUS_FLOW);
    }

    @Test
    void deviceOfflineIsNotLifeRhythmAlert() {
        assertThat(new AlertRuleEngine().isLifeRhythmAlert(AlertType.DEVICE_OFFLINE)).isFalse();
    }

    @Test
    void noMorningAlertWhenWaterOccurred() {
        RhythmBaseline baseline = RhythmBaseline.of(LocalTime.of(6, 30), LocalTime.of(9, 0), 5.0, 900.0);
        WaterEvent event = WaterEvent.of(
            Instant.parse("2026-07-05T07:30:00Z"),
            Instant.parse("2026-07-05T07:35:00Z"),
            0.90
        );
        AlertRuleEngine engine = new AlertRuleEngine();

        assertThat(engine.evaluateMorning(
            Instant.parse("2026-07-05T09:30:00Z"), baseline, Arrays.asList(event)
        )).isEmpty();
    }

    @Test
    void createsLowActivityAlertWhenBelowBaseline() {
        RhythmBaseline baseline = RhythmBaseline.of(LocalTime.of(6, 30), LocalTime.of(9, 0), 5.0, 900.0);
        WaterEvent event = WaterEvent.of(
            Instant.parse("2026-07-05T08:00:00Z"),
            Instant.parse("2026-07-05T08:05:00Z"),
            0.90
        );
        AlertRuleEngine engine = new AlertRuleEngine();

        assertThat(engine.evaluateLowDailyActivity(Arrays.asList(event), baseline))
            .extracting(AlertCandidate::getType)
            .contains(AlertType.LOW_DAILY_ACTIVITY);
    }
}