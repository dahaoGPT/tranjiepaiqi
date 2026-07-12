'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { getAlerts } from '../../lib/api';

/**
 * 异常提醒列表页面组件。
 * 展示老人的所有异常提醒，支持按状态筛选显示。
 */
export default function AlertsPage() {
  // 异常列表状态
  const [alerts, setAlerts] = useState<any[]>([]);
  // 加载状态
  const [loading, setLoading] = useState(true);

  /**
   * 获取异常列表数据。
   */
  const fetchData = async () => {
    try {
      // 固定老人ID，实际应用中应从用户绑定关系获取
      const data = await getAlerts('550e8400-e29b-41d4-a716-446655440001');
      setAlerts(data);
    } catch (error) {
      console.error('获取异常列表失败:', error);
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

  // 正常渲染
  return (
    <div>
      {/* 页面标题和刷新按钮 */}
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">异常提醒列表</h2>
        <button 
          onClick={() => { setLoading(true); fetchData(); }}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
        >
          刷新
        </button>
      </div>
      {alerts.length > 0 ? (
        <div className="space-y-4">
          {alerts.map((alert) => (
            <div key={alert.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  {/* 状态标签 */}
                  <span className={`inline-block px-2 py-1 rounded text-xs font-bold ${
                    alert.status === 'OPEN' ? 'bg-red-100 text-red-600' :
                    alert.status === 'ACKNOWLEDGED' ? 'bg-yellow-100 text-yellow-600' :
                    'bg-green-100 text-green-600'
                  }`}>
                    {alert.status}
                  </span>
                  {/* 异常类型 */}
                  <h3 className="font-bold mt-2">{alert.type}</h3>
                  {/* 异常原因 */}
                  <p className="text-sm text-gray-600 mt-1">{alert.reason}</p>
                </div>
                {/* 发生时间 */}
                <span className="text-sm text-gray-400">
                  {new Date(alert.occurredAt).toLocaleString()}
                </span>
              </div>
              {/* 查看详情链接 */}
              <Link href={`/alerts/${alert.id}`} className="btn btn-secondary mt-4">
                查看详情
              </Link>
            </div>
          ))}
        </div>
      ) : (
        // 无异常提醒时的提示
        <div className="card text-center py-8">
          <p className="text-green-600">暂无异常提醒</p>
        </div>
      )}
    </div>
  );
}