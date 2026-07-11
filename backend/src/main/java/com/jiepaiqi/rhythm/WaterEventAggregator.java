package com.jiepaiqi.rhythm;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用水事件聚合器。
 * 将连续高置信度声学特征窗口聚合为用水事件。
 */
public class WaterEventAggregator {
    private final double confidenceThreshold;
    private final int maxGapSeconds;

    /**
     * 构造函数。
     * @param confidenceThreshold 置信度阈值，低于此值的窗口将被过滤
     * @param maxGapSeconds 窗口间最大间隔秒数，超过此间隔则分割为不同事件
     */
    public WaterEventAggregator(double confidenceThreshold, int maxGapSeconds) {
        this.confidenceThreshold = confidenceThreshold;
        this.maxGapSeconds = maxGapSeconds;
    }

    /**
     * 聚合声学特征窗口为用水事件。
     * 按置信度过滤窗口，按时间排序后，将间隔小于阈值的连续窗口聚合成单个用水事件。
     * @param windows 声学特征窗口列表
     * @return 聚合后的用水事件列表
     */
    public List<WaterEvent> aggregate(List<AcousticFeatureWindow> windows) {
        List<WaterEvent> events = new ArrayList<>();
        if (windows == null || windows.isEmpty()) {
            return events;
        }

        List<AcousticFeatureWindow> filtered = windows.stream()
            .filter(w -> w.getFlowConfidence() >= confidenceThreshold)
            .sorted(Comparator.comparing(AcousticFeatureWindow::getWindowStartedAt))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return events;
        }

        List<AcousticFeatureWindow> currentGroup = new ArrayList<>();
        currentGroup.add(filtered.get(0));

        for (int i = 1; i < filtered.size(); i++) {
            AcousticFeatureWindow prev = filtered.get(i - 1);
            AcousticFeatureWindow curr = filtered.get(i);

            long gapSeconds = Duration.between(prev.getWindowEndedAt(), curr.getWindowStartedAt()).getSeconds();
            if (gapSeconds <= maxGapSeconds) {
                currentGroup.add(curr);
            } else {
                events.add(createEvent(currentGroup));
                currentGroup = new ArrayList<>();
                currentGroup.add(curr);
            }
        }

        if (!currentGroup.isEmpty()) {
            events.add(createEvent(currentGroup));
        }

        return events;
    }

    /**
     * 从窗口组创建用水事件。
     * 使用第一个窗口的开始时间和最后一个窗口的结束时间作为事件的时间范围。
     * @param windows 连续的声学特征窗口组
     * @return 创建的用水事件
     */
    private WaterEvent createEvent(List<AcousticFeatureWindow> windows) {
        Instant startedAt = windows.get(0).getWindowStartedAt();
        Instant endedAt = windows.get(windows.size() - 1).getWindowEndedAt();

        double totalConfidence = windows.stream()
                .mapToDouble(AcousticFeatureWindow::getFlowConfidence)
                .sum();
        double averageConfidence = totalConfidence / windows.size();

        return WaterEvent.of(startedAt, endedAt, averageConfidence);
    }
}