'use client';

import { useState, useEffect } from 'react';
import { getDashboard, getAlerts } from '../../lib/api';
import StatusCard from '../../components/StatusCard';

/**
 * 看板页面组件。
 * 展示老人今日状态、设备状态、用水节奏时间线和异常提醒。
 * 支持自动刷新（30秒间隔）和手动刷新。
 */
export default function DashboardPage() {
  // 看板数据状态
  const [dashboard, setDashboard] = useState<any>(null);
  // 加载状态
  const [loading, setLoading] = useState(true);
  // 错误信息状态
  const [error, setError] = useState<string | null>(null);

  /**
   * 获取看板数据。
   * 从后端 API 获取老人的聚合数据。
   */
  const fetchData = async () => {
    try {
      // 固定老人ID，实际应用中应从用户绑定关系获取
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

  /**
   * 组件挂载时获取数据，并设置定时刷新（30秒间隔）。
   */
  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  // 加载中状态
  if (loading) {
    return <div className="card"><p>加载中...</p></div>;
  }

  // 错误状态
  if (error || !dashboard) {
    return <div className="card"><p className="text-red-600">{error || '无法加载看板数据'}</p></div>;
  }

  // 正常渲染
  return (
    <div>
      {/* 页面标题和刷新按钮 */}
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">老人看板</h1>
        <button 
          onClick={() => { setLoading(true); fetchData(); }}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
        >
          刷新
        </button>
      </div>
      {/* 状态卡片网格 */}
      <div className="grid">
        {/* 老人状态卡片 */}
        <StatusCard
          title={dashboard.elderName}
          status={dashboard.todayStatus}
          statusLabel="今日状态"
          statusColor={dashboard.todayStatus === 'NORMAL' ? 'green' : 'red'}
        />
        {/* 设备状态卡片 */}
        <StatusCard
          title="设备状态"
          status={dashboard.deviceStatus}
          statusLabel={dashboard.deviceStatus === 'ONLINE' ? '在线' : '离线'}
          statusColor={dashboard.deviceStatus === 'ONLINE' ? 'green' : 'red'}
        />
        {/* 未处理异常数量卡片 */}
        <StatusCard
          title="未处理异常"
          status={dashboard.openAlertCount.toString()}
          statusLabel="数量"
          statusColor={dashboard.openAlertCount > 0 ? 'red' : 'green'}
        />
      </div>

      {/* 今日用水节奏 */}
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

      {/* 当前异常提醒 */}
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