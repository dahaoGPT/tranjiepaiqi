'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { getAlerts } from '../../lib/api';

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getAlerts('550e8400-e29b-41d4-a716-446655440001');
        setAlerts(data);
      } catch (error) {
        console.error('获取异常列表失败:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  if (loading) {
    return <div className="card"><p>加载中...</p></div>;
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">异常提醒列表</h2>
      {alerts.length > 0 ? (
        <div className="space-y-4">
          {alerts.map((alert) => (
            <div key={alert.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <span className={`inline-block px-2 py-1 rounded text-xs font-bold ${
                    alert.status === 'OPEN' ? 'bg-red-100 text-red-600' :
                    alert.status === 'ACKNOWLEDGED' ? 'bg-yellow-100 text-yellow-600' :
                    'bg-green-100 text-green-600'
                  }`}>
                    {alert.status}
                  </span>
                  <h3 className="font-bold mt-2">{alert.type}</h3>
                  <p className="text-sm text-gray-600 mt-1">{alert.reason}</p>
                </div>
                <span className="text-sm text-gray-400">
                  {new Date(alert.occurredAt).toLocaleString()}
                </span>
              </div>
              <Link href={`/alerts/${alert.id}`} className="btn btn-secondary mt-4">
                查看详情
              </Link>
            </div>
          ))}
        </div>
      ) : (
        <div className="card text-center py-8">
          <p className="text-green-600">暂无异常提醒</p>
        </div>
      )}
    </div>
  );
}