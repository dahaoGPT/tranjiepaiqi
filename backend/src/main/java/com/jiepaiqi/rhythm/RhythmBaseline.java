package com.jiepaiqi.rhythm;

import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 个人节奏基线。
 * 用于判断今天是否偏离平常节奏。
 */
@Data
public class RhythmBaseline {
    private UUID id;
    private UUID elderId;
    private Instant calculatedAt;
    private LocalTime morningWindowStart;
    private LocalTime morningWindowEnd;
    private Double averageDailyEventCount;
    private Double averageDailyDurationSeconds;

    public static RhythmBaseline of(LocalTime morningWindowStart, LocalTime morningWindowEnd, 
                                    double averageDailyEventCount, double averageDailyDurationSeconds) {
        RhythmBaseline baseline = new RhythmBaseline();
        baseline.setMorningWindowStart(morningWindowStart);
        baseline.setMorningWindowEnd(morningWindowEnd);
        baseline.setAverageDailyEventCount(averageDailyEventCount);
        baseline.setAverageDailyDurationSeconds(averageDailyDurationSeconds);
        return baseline;
    }
}