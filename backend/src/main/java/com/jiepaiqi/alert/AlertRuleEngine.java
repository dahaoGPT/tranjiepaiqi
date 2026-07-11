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
    /**
     * 评估晨间用水异常。
     * 判断当前时间是否已过晨间用水窗口结束时间，且今日尚未发生晨间用水事件。
     * @param now 当前时间
     * @param baseline 个人用水节律基线
     * @param todayEvents 今日所有用水事件列表
     * @return 异常候选列表，无异常时返回空列表
     */
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

    /**
     * 评估长流水异常。
     * 检查每个用水事件的持续时长是否超过阈值，可能表示水龙头忘记关闭。
     * @param events 用水事件列表
     * @param maxDurationSeconds 最大允许持续时长（秒）
     * @return 异常候选列表，无异常时返回空列表
     */
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

    /**
     * 评估每日活动量低异常。
     * 比较今日用水次数和总时长与个人基线的30%阈值，显著偏低时触发异常。
     * @param todayEvents 今日所有用水事件列表
     * @param baseline 个人用水节律基线
     * @return 异常候选列表，无异常时返回空列表
     */
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

    /**
     * 判断异常类型是否为生活节律类异常。
     * 设备离线类异常不属于生活节律异常。
     * @param type 异常类型
     * @return true表示是生活节律异常，false表示是设备异常
     */
    public boolean isLifeRhythmAlert(AlertType type) {
        return type != AlertType.DEVICE_OFFLINE;
    }
}