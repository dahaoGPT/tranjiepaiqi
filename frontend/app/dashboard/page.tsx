'use client';

import { useState, useEffect } from 'react';
import { getDashboard, getAlerts } from '../../lib/api';
import StatusCard from '../../components/StatusCard';

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      const data = await getDashboard('550e8400-e29b-41d4-a716-446655440001');
      setDashboard(data);
      setError(null);
    } catch (err) {
      console.error('获取看板数据失败:', err);
      setError('获取看板数据失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return <div className="card"><p>加载中...</p></div>;
  }

  if (error || !dashboard) {
    return <div className="card"><p className="text-red-600">{error || '无法加载看板数据'}</p></div>;
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">老人看板</h1>
        <button 
          onClick={() => { setLoading(true); fetchData(); }}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
        >
          刷新
        </button>
      </div>
      <div className="grid">
        <StatusCard
          title={dashboard.elderName}
          status={dashboard.todayStatus}
          statusLabel="今日状态"
          statusColor={dashboard.todayStatus === 'NORMAL' ? 'green' : 'red'}
        />
        <StatusCard
          title="设备状态"
          status={dashboard.deviceStatus}
          statusLabel={dashboard.deviceStatus === 'ONLINE' ? '在线' : '离线'}
          statusColor={dashboard.deviceStatus === 'ONLINE' ? 'green' : 'red'}
        />
        <StatusCard
          title="未处理异常"
          status={dashboard.openAlertCount.toString()}
          statusLabel="数量"
          statusColor={dashboard.openAlertCount > 0 ? 'red' : 'green'}
        />
      </div>

      <div className="card">
        <h2 className="text-lg font-bold mb-4">今日用水节奏</h2>
        {dashboard.rhythmTimeline && dashboard.rhythmTimeline.length > 0 ? (
          <div>
            {dashboard.rhythmTimeline.map((item: any, index: number) => (
              <div key={index} className="timeline-item">
                <div className="timeline-dot"></div>
                <span className="timeline-time">{new Date(item.time).toLocaleTimeString()}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500">暂无数据</p>
        )}
      </div>

      <div className="card">
        <h2 className="text-lg font-bold mb-4">当前异常提醒</h2>
        {dashboard.openAlerts && dashboard.openAlerts.length > 0 ? (
          <ul>
            {dashboard.openAlerts.map((alert: any) => (
              <li key={alert.id} className="mb-3 p-3 bg-red-50 rounded-lg">
                <div className="flex justify-between items-start">
                  <span className="font-bold text-red-600">{alert.type}</span>
                  <span className="text-sm text-gray-500">{new Date(alert.occurredAt).toLocaleString()}</span>
                </div>
                <p className="text-sm mt-1">{alert.reason}</p>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-green-600">暂无异常提醒</p>
        )}
      </div>
    </div>
  );
}