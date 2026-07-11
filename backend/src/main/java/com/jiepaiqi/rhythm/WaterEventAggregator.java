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

    public WaterEventAggregator(double confidenceThreshold, int maxGapSeconds) {
        this.confidenceThreshold = confidenceThreshold;
        this.maxGapSeconds = maxGapSeconds;
    }

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