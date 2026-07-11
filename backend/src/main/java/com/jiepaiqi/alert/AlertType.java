package com.jiepaiqi.alert;

/**
 * 异常提醒类型枚举。
 */
public enum AlertType {
    /** 晨间未用水。晨间用水窗口结束后仍无用水事件。 */
    NO_MORNING_WATER,
    /** 长流水。连续用水时长超过阈值，可能表示水龙头忘记关闭。 */
    LONG_CONTINUOUS_FLOW,
    /** 每日活动量低。今日用水次数和时长显著低于个人基线。 */
    LOW_DAILY_ACTIVITY,
    /** 设备离线。设备长时间未上报数据。 */
    DEVICE_OFFLINE
}