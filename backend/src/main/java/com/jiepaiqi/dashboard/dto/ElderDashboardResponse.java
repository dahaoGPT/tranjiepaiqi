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

    /**
     * 时间线项。
     * 表示今日用水时间线上的一个事件点。
     */
    @Data
    @Builder
    public static class TimelineItem {
        /** 事件发生时间。 */
        private Instant time;
        /** 事件类型，如 "water"。 */
        private String type;
    }

    /**
     * 异常摘要。
     * 用于移动端展示异常概览信息。
     */
    @Data
    @Builder
    public static class AlertSummary {
        /** 异常ID（字符串格式）。 */
        private String id;
        /** 异常类型。 */
        private String type;
        /** 异常原因描述。 */
        private String reason;
        /** 异常发生时间。 */
        private Instant occurredAt;
    }
}