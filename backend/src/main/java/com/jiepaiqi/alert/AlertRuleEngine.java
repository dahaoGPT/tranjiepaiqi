package com.jiepaiqi.alert;

import com.jiepaiqi.rhythm.RhythmBaseline;
import com.jiepaiqi.rhythm.WaterEvent;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 异常规则引擎。
 * 根据固定规则和个人基线判断异常。
 */
public class AlertRuleEngine {
    public List<AlertCandidate> evaluateMorning(Instant now, RhythmBaseline baseline, List<WaterEvent> todayEvents) {
        List<AlertCandidate> candidates = new ArrayList<>();

        LocalTime currentLocalTime = now.atZone(ZoneOffset.UTC).toLocalTime();
        if (currentLocalTime.isBefore(baseline.getMorningWindowEnd())) {
            return candidates;
        }

        boolean hasMorningWater = todayEvents.stream()
            .anyMatch(e -> {
                LocalTime eventTime = e.getStartedAt().atZone(ZoneOffset.UTC).toLocalTime();
                return !eventTime.isBefore(baseline.getMorningWindowStart()) 
                    && !eventTime.isAfter(baseline.getMorningWindowEnd());
            });

        if (!hasMorningWater) {
            candidates.add(AlertCandidate.builder()
                .type(AlertType.NO_MORNING_WATER)
                .reason(String.format("晨间用水窗口(%s-%s)结束后仍无用水事件", 
                    baseline.getMorningWindowStart(), baseline.getMorningWindowEnd()))
                .suggestedAction("建议联系老人确认情况")
                .occurredAt(now)
                .build());
        }

        return candidates;
    }

    public List<AlertCandidate> evaluateLongFlow(List<WaterEvent> events, int maxDurationSeconds) {
        List<AlertCandidate> candidates = new ArrayList<>();

        events.forEach(event -> {
            if (event.getDurationSeconds() > maxDurationSeconds) {
                candidates.add(AlertCandidate.builder()
                    .type(AlertType.LONG_CONTINUOUS_FLOW)
                    .reason(String.format("连续用水时长(%d秒)超过阈值(%d秒)", 
                        event.getDurationSeconds(), maxDurationSeconds))
                    .suggestedAction("建议确认水龙头是否已关闭")
                    .occurredAt(event.getEndedAt())
                    .build());
            }
        });

        return candidates;
    }

    public List<AlertCandidate> evaluateLowDailyActivity(List<WaterEvent> todayEvents, RhythmBaseline baseline) {
        List<AlertCandidate> candidates = new ArrayList<>();

        int eventCount = todayEvents.size();
        int totalDuration = todayEvents.stream()
            .mapToInt(WaterEvent::getDurationSeconds)
            .sum();

        double eventCountThreshold = baseline.getAverageDailyEventCount() * 0.3;
        double durationThreshold = baseline.getAverageDailyDurationSeconds() * 0.3;

        if (eventCount < eventCountThreshold || totalDuration < durationThreshold) {
            candidates.add(AlertCandidate.builder()
                .type(AlertType.LOW_DAILY_ACTIVITY)
                .reason(String.format("今日用水次数(%d)和时长(%d秒)显著低于个人基线", 
                    eventCount, totalDuration))
                .suggestedAction("建议联系老人确认安全")
                .occurredAt(Instant.now())
                .build());
        }

        return candidates;
    }

    public boolean isLifeRhythmAlert(AlertType type) {
        return type != AlertType.DEVICE_OFFLINE;
    }
}