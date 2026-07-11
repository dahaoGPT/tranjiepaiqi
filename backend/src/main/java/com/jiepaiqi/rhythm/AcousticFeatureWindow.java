package com.jiepaiqi.rhythm;

import lombok.Data;

import java.time.Instant;

/**
 * 声学特征窗口。
 * 表示设备在一个时间窗口内采集的声学特征。
 */
@Data
public class AcousticFeatureWindow {
    private Instant windowStartedAt;
    private Instant windowEndedAt;
    private double flowConfidence;

    /**
     * 创建示例声学特征窗口。
     * 窗口持续时间固定为10秒。
     * 
     * @param startedAt  窗口开始时间
     * @param confidence 水流置信度（0-1）
     * @return 创建的示例窗口
     */
    public static AcousticFeatureWindow sample(Instant startedAt, double confidence) {
        AcousticFeatureWindow window = new AcousticFeatureWindow();
        window.setWindowStartedAt(startedAt);
        window.setWindowEndedAt(startedAt.plusSeconds(10));
        window.setFlowConfidence(confidence);
        return window;
    }
}