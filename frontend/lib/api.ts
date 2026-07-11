export interface ElderDashboard {
  elderName: string;
  todayStatus: string;
  lastWaterEventAt: string | null;
  deviceStatus: string;
  openAlertCount: number;
  rhythmTimeline: Array<{ time: string; type: string }>;
  openAlerts: Array<{ id: string; type: string; reason: string; occurredAt: string }>;
}

export interface AlertSummary {
  id: string;
  type: string;
  reason: string;
  occurredAt: string;
  status: string;
}

export interface AlertDetail {
  id: string;
  type: string;
  status: string;
  reason: string;
  suggestedAction: string;
  occurredAt: string;
  acknowledgedAt: string | null;
  resolvedAt: string | null;
}

export async function getDashboard(elderId: string): Promise<ElderDashboard> {
  const response = await fetch(`/api/elders/${elderId}/dashboard`);
  return response.json();
}

export async function getAlerts(elderId: string): Promise<AlertSummary[]> {
  const response = await fetch(`/api/alerts/elder/${elderId}`);
  return response.json();
}

export async function getAlert(alertId: string): Promise<AlertDetail> {
  const response = await fetch(`/api/alerts/${alertId}`);
  return response.json();
}

export async function acknowledgeAlert(alertId: string): Promise<void> {
  await fetch(`/api/alerts/${alertId}/acknowledge`, { method: 'POST' });
}

export async function resolveAlert(alertId: string): Promise<void> {
  await fetch(`/api/alerts/${alertId}/resolve`, { method: 'POST' });
}