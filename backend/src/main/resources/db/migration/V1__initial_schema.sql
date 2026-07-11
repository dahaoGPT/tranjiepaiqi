-- 系统登录用户表：保存家属、社区志愿者等使用者账号。
create table users (
  id uuid primary key,
  username varchar(80) not null unique,
  display_name varchar(120) not null,
  role varchar(30) not null,
  created_at timestamp not null
);

comment on table users is '系统登录用户表，保存家属和社区志愿者账号';
comment on column users.username is '登录用户名，MVP 阶段保持唯一';
comment on column users.role is '用户角色，例如 FAMILY 或 VOLUNTEER';

-- 老人档案表：保存被守护老人的基础信息。
create table elders (
  id uuid primary key,
  name varchar(120) not null,
  notes varchar(500),
  created_at timestamp not null
);

comment on table elders is '老人档案表，作为设备、节奏和异常记录的核心归属对象';
comment on column elders.notes is '老人备注信息，例如居住情况或照护说明';

-- 设备表：保存安装在水龙头附近的声感传感器。
create table devices (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  serial_number varchar(120) not null unique,
  status varchar(30) not null,
  last_seen_at timestamp,
  created_at timestamp not null
);

comment on table devices is '声感传感器设备表，记录设备绑定老人和最近在线状态';
comment on column devices.serial_number is '设备唯一序列号，用于设备上报';
comment on column devices.status is '设备状态，例如 ONLINE 或 OFFLINE';
comment on column devices.last_seen_at is '设备最近一次成功上报时间';

-- 用户与老人绑定表：限制用户只能查看绑定老人数据。
create table user_elder_bindings (
  user_id uuid not null references users(id),
  elder_id uuid not null references elders(id),
  primary key (user_id, elder_id)
);

comment on table user_elder_bindings is '用户与老人绑定关系表，用于看板和音频复盘授权';

-- 原始音频片段元数据表：音频文件存文件系统或对象存储，表中只保存元数据和路径。
create table audio_clips (
  id uuid primary key,
  device_id uuid not null references devices(id),
  window_started_at timestamp not null,
  window_ended_at timestamp not null,
  storage_path varchar(500) not null,
  content_type varchar(80) not null,
  duration_seconds integer not null,
  size_bytes bigint not null,
  created_at timestamp not null
);

comment on table audio_clips is '原始音频片段元数据表，用于异常复盘和人工核实';
comment on column audio_clips.storage_path is '音频文件存储路径或对象存储键';
comment on column audio_clips.content_type is '音频 MIME 类型，例如 audio/wav';
comment on column audio_clips.duration_seconds is '音频片段时长，单位秒';
comment on column audio_clips.size_bytes is '音频文件大小，单位字节';

-- 声学特征表：保存设备从音频中提取出的结构化水流特征。
create table acoustic_features (
  id uuid primary key,
  device_id uuid not null references devices(id),
  audio_clip_id uuid references audio_clips(id),
  window_started_at timestamp not null,
  window_ended_at timestamp not null,
  average_decibels numeric(8,2) not null,
  peak_decibels numeric(8,2) not null,
  low_band_energy numeric(10,4) not null,
  mid_band_energy numeric(10,4) not null,
  high_band_energy numeric(10,4) not null,
  flow_confidence numeric(5,4) not null,
  created_at timestamp not null
);

comment on table acoustic_features is '声学特征表，后端用它判断是否存在用水事件';
comment on column acoustic_features.audio_clip_id is '关联的原始音频片段，用于异常复盘';
comment on column acoustic_features.flow_confidence is '水流置信度，范围 0 到 1';

-- 用水事件表：由连续高置信度声学特征聚合而来。
create table water_events (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  device_id uuid not null references devices(id),
  started_at timestamp not null,
  ended_at timestamp not null,
  duration_seconds integer not null,
  average_confidence numeric(5,4) not null,
  created_at timestamp not null
);

comment on table water_events is '用水事件表，表示一次业务可读的用水行为';
comment on column water_events.duration_seconds is '用水事件持续时长，单位秒';
comment on column water_events.average_confidence is '该事件内声学特征的平均水流置信度';

-- 个人节奏基线表：保存最近历史计算出的老人常见生活节奏。
create table rhythm_baselines (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  calculated_at timestamp not null,
  morning_window_start time not null,
  morning_window_end time not null,
  average_daily_event_count numeric(8,2) not null,
  average_daily_duration_seconds numeric(10,2) not null
);

comment on table rhythm_baselines is '个人节奏基线表，用于判断今天是否偏离平常节奏';
comment on column rhythm_baselines.morning_window_start is '常见晨间首次用水窗口开始时间';
comment on column rhythm_baselines.morning_window_end is '常见晨间首次用水窗口结束时间';

-- 异常提醒表：保存节奏异常和设备异常。
create table alerts (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  device_id uuid references devices(id),
  type varchar(60) not null,
  status varchar(30) not null,
  reason varchar(1000) not null,
  suggested_action varchar(1000) not null,
  occurred_at timestamp not null,
  acknowledged_at timestamp,
  resolved_at timestamp,
  created_at timestamp not null
);

comment on table alerts is '异常提醒表，保存需要家属或志愿者确认的事件';
comment on column alerts.type is '提醒类型，例如 NO_MORNING_WATER 或 LONG_CONTINUOUS_FLOW';
comment on column alerts.status is '处理状态，例如 OPEN、ACKNOWLEDGED、RESOLVED';
comment on column alerts.reason is '系统生成的可解释异常原因';
comment on column alerts.suggested_action is '建议家属或志愿者采取的动作';

-- 异常处理备注表：保存家属或志愿者对提醒的处理记录。
create table alert_notes (
  id uuid primary key,
  alert_id uuid not null references alerts(id),
  author_user_id uuid not null references users(id),
  body varchar(1000) not null,
  created_at timestamp not null
);

comment on table alert_notes is '异常处理备注表，记录确认、联系、上门等人工处理信息';
comment on column alert_notes.body is '处理备注正文';

-- 创建索引以优化常见查询
create index idx_devices_elder_id on devices(elder_id);
create index idx_acoustic_features_device_time on acoustic_features(device_id, window_started_at);
create index idx_water_events_elder_time on water_events(elder_id, started_at);
create index idx_alerts_elder_status on alerts(elder_id, status);
create index idx_alerts_occurred_at on alerts(occurred_at);
create index idx_audio_clips_device_time on audio_clips(device_id, window_started_at);