/**
 * 老人看板数据接口。
 * 包含老人姓名、今日状态、设备状态、异常数量、用水时间线等聚合信息。
 */
export interface ElderDashboard {
  /** 老人姓名 */
  elderName: string;
  /** 今日状态，NORMAL 或 ATTENTION */
  todayStatus: string;
  /** 最近一次用水事件时间 */
  lastWaterEventAt: string | null;
  /** 设备在线状态 */
  deviceStatus: string;
  /** 当前未处理异常数量 */
  openAlertCount: number;
  /** 今日用水节奏时间线 */
  rhythmTimeline: Array<{ time: string; type: string }>;
  /** 当前未处理异常摘要列表 */
  openAlerts: Array<{ id: string; type: string; reason: string; occurredAt: string }>;
}

/**
 * 异常摘要接口。
 * 用于异常列表页展示。
 */
export interface AlertSummary {
  /** 异常ID */
  id: string;
  /** 异常类型 */
  type: string;
  /** 异常原因 */
  reason: string;
  /** 发生时间 */
  occurredAt: string;
  /** 状态：OPEN、ACKNOWLEDGED、RESOLVED */
  status: string;
}

/**
 * 异常详情接口。
 * 用于异常详情页展示完整信息。
 */
export interface AlertDetail {
  /** 异常ID */
  id: string;
  /** 异常类型 */
  type: string;
  /** 状态 */
  status: string;
  /** 异常原因 */
  reason: string;
  /** 建议处理动作 */
  suggestedAction: string;
  /** 发生时间 */
  occurredAt: string;
  /** 确认时间 */
  acknowledgedAt: string | null;
  /** 解决时间 */
  resolvedAt: string | null;
}

/**
 * 获取老人看板数据。
 * @param elderId 老人ID
 * @returns 看板数据
 */
export async function getDashboard(elderId: string): Promise<ElderDashboard> {
  const response = await fetch(`/api/elders/${elderId}/dashboard`);
  return response.json();
}

/**
 * 获取老人的异常列表。
 * @param elderId 老人ID
 * @returns 异常摘要列表
 */
export async function getAlerts(elderId: string): Promise<AlertSummary[]> {
  const response = await fetch(`/api/alerts/elder/${elderId}`);
  return response.json();
}

/**
 * 获取异常详情。
 * @param alertId 异常ID
 * @returns 异常详情
 */
export async function getAlert(alertId: string): Promise<AlertDetail> {
  const response = await fetch(`/api/alerts/${alertId}`);
  return response.json();
}

/**
 * 确认异常。
 * 将异常状态从 OPEN 改为 ACKNOWLEDGED。
 * @param alertId 异常ID
 */
export async function acknowledgeAlert(alertId: string): Promise<void> {
  await fetch(`/api/alerts/${alertId}/acknowledge`, { method: 'POST' });
}

/**
 * 解决异常。
 * 将异常状态改为 RESOLVED。
 * @param alertId 异常ID
 */
export async function resolveAlert(alertId: string): Promise<void> {
  await fetch(`/api/alerts/${alertId}/resolve`, { method: 'POST' });
}