package com.jiepaiqi.dashboard.dto;

import lombok.Data;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * 老人移动端看板聚合响应。
 * 面向手机端首屏，避免前端拼接多个底层接口。
 */
@Data
@Builder
public class ElderDashboardResponse {
    /** 老人姓名。 */
    private String elderName;
    /** 今日节奏状态，例如 NORMAL 或 ATTENTION。 */
    private String todayStatus;
    /** 最近一次用水事件时间。 */
    private Instant lastWaterEventAt;
    /** 设备在线状态。 */
    private String deviceStatus;
    /** 当前未处理异常数量。 */
    private int openAlertCount;
    /** 今日用水节奏时间线。 */
    private List<TimelineItem> rhythmTimeline;
    /** 当前未处理异常摘要。 */
    private List<AlertSummary> openAlerts;

    @Data
    @Builder
    public static class TimelineItem {
        private Instant time;
        private String type;
    }

    @Data
    @Builder
    public static class AlertSummary {
        private String id;
        private String type;
        private String reason;
        private Instant occurredAt;
    }
}