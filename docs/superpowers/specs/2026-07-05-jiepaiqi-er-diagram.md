# jiepaiqi 表结构 E-R 图

日期：2026-07-05

## 1. 总体 E-R 图

```mermaid
erDiagram
    USERS ||--o{ USER_ELDER_BINDINGS : "绑定"
    ELDERS ||--o{ USER_ELDER_BINDINGS : "被绑定"
    ELDERS ||--o{ DEVICES : "安装设备"
    DEVICES ||--o{ AUDIO_CLIPS : "采集音频"
    DEVICES ||--o{ ACOUSTIC_FEATURES : "上报特征"
    AUDIO_CLIPS ||--o{ ACOUSTIC_FEATURES : "对应特征"
    ELDERS ||--o{ WATER_EVENTS : "产生用水事件"
    DEVICES ||--o{ WATER_EVENTS : "参与事件识别"
    ELDERS ||--o{ RHYTHM_BASELINES : "计算节奏基线"
    ELDERS ||--o{ ALERTS : "触发异常提醒"
    DEVICES ||--o{ ALERTS : "关联设备异常"
    ALERTS ||--o{ ALERT_NOTES : "记录处理备注"
    USERS ||--o{ ALERT_NOTES : "填写备注"

    USERS {
        uuid id PK "用户ID"
        varchar username UK "登录用户名"
        varchar display_name "显示名称"
        varchar role "用户角色"
        timestamp created_at "创建时间"
    }

    ELDERS {
        uuid id PK "老人ID"
        varchar name "老人姓名"
        varchar notes "照护备注"
        timestamp created_at "创建时间"
    }

    DEVICES {
        uuid id PK "设备ID"
        uuid elder_id FK "老人ID"
        varchar serial_number UK "设备序列号"
        varchar status "设备状态"
        timestamp last_seen_at "最近上报时间"
        timestamp created_at "创建时间"
    }

    USER_ELDER_BINDINGS {
        uuid user_id PK,FK "用户ID"
        uuid elder_id PK,FK "老人ID"
    }

    AUDIO_CLIPS {
        uuid id PK "音频片段ID"
        uuid device_id FK "设备ID"
        timestamp window_started_at "采样窗口开始时间"
        timestamp window_ended_at "采样窗口结束时间"
        varchar storage_path "音频存储路径"
        varchar content_type "音频MIME类型"
        integer duration_seconds "音频时长秒数"
        bigint size_bytes "音频文件大小"
        timestamp created_at "创建时间"
    }

    ACOUSTIC_FEATURES {
        uuid id PK "声学特征ID"
        uuid device_id FK "设备ID"
        uuid audio_clip_id FK "音频片段ID，可为空"
        timestamp window_started_at "特征窗口开始时间"
        timestamp window_ended_at "特征窗口结束时间"
        numeric average_decibels "平均分贝"
        numeric peak_decibels "峰值分贝"
        numeric low_band_energy "低频能量"
        numeric mid_band_energy "中频能量"
        numeric high_band_energy "高频能量"
        numeric flow_confidence "水流置信度"
        timestamp created_at "创建时间"
    }

    WATER_EVENTS {
        uuid id PK "用水事件ID"
        uuid elder_id FK "老人ID"
        uuid device_id FK "设备ID"
        timestamp started_at "事件开始时间"
        timestamp ended_at "事件结束时间"
        integer duration_seconds "持续秒数"
        numeric average_confidence "平均水流置信度"
        timestamp created_at "创建时间"
    }

    RHYTHM_BASELINES {
        uuid id PK "节奏基线ID"
        uuid elder_id FK "老人ID"
        timestamp calculated_at "计算时间"
        time morning_window_start "晨间窗口开始"
        time morning_window_end "晨间窗口结束"
        numeric average_daily_event_count "日均用水次数"
        numeric average_daily_duration_seconds "日均用水时长秒数"
    }

    ALERTS {
        uuid id PK "异常提醒ID"
        uuid elder_id FK "老人ID"
        uuid device_id FK "设备ID，可为空"
        varchar type "提醒类型"
        varchar status "处理状态"
        varchar reason "异常原因"
        varchar suggested_action "建议动作"
        timestamp occurred_at "发生时间"
        timestamp acknowledged_at "确认时间"
        timestamp resolved_at "解决时间"
        timestamp created_at "创建时间"
    }

    ALERT_NOTES {
        uuid id PK "备注ID"
        uuid alert_id FK "异常提醒ID"
        uuid author_user_id FK "填写用户ID"
        varchar body "备注正文"
        timestamp created_at "创建时间"
    }
```

## 2. 关系说明

- `users` 与 `elders` 是多对多关系，通过 `user_elder_bindings` 限定家属或志愿者能查看哪些老人。
- `elders` 与 `devices` 是一对多关系，一个老人可以绑定多个声感设备。
- `devices` 与 `audio_clips` 是一对多关系，原始音频只保存元数据和存储路径。
- `devices` 与 `acoustic_features` 是一对多关系，声学特征是后端识别用水事件的结构化输入。
- `audio_clips` 与 `acoustic_features` 是一对多关系，但 `acoustic_features.audio_clip_id` 允许为空，用于兼容只上传特征、不上传音频的设备。
- `elders`、`devices` 与 `water_events` 共同描述一次用水事件，事件由连续高置信度声学特征聚合而来。
- `elders` 与 `rhythm_baselines` 是一对多关系，用于保留不同计算时间点的个人节奏基线。
- `elders` 与 `alerts` 是一对多关系，所有异常都必须归属到老人。
- `devices` 与 `alerts` 是可选一对多关系，生活节奏异常可以没有明确设备归因，设备离线异常通常会关联设备。
- `alerts` 与 `alert_notes` 是一对多关系，用于记录确认、联系、上门等人工处理过程。
- `users` 与 `alert_notes` 是一对多关系，用于追踪备注是谁填写的。

## 3. 完整性检查

- 已覆盖身份授权、老人档案、设备、原始音频、声学特征、用水事件、节奏基线、异常提醒和处理备注这 9 类核心业务对象。
- 已满足“保留原始音频用于复盘”的要求：`audio_clips` 保存音频元数据和路径，`acoustic_features.audio_clip_id` 可关联复盘音频。
- 已满足“非侵入式守护”的要求：表结构不包含图像、语音转文字、说话人识别或对话内容字段。
- 建议实现时为所有外键列增加查询索引，尤其是 `elder_id`、`device_id`、`audio_clip_id`、`alert_id` 和 `author_user_id`。
- 建议实现时为时间线查询增加组合索引，例如 `(device_id, window_started_at)`、`(elder_id, started_at)`、`(elder_id, occurred_at)`。
- 建议实现时用枚举或检查约束收敛 `role`、`status`、`type` 等状态字段，避免脏数据进入业务流程。
