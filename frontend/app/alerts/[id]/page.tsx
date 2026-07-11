'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { getAlert, acknowledgeAlert, resolveAlert } from '../../../lib/api';
import AudioReviewPlayer from '../../../components/AudioReviewPlayer';

export default function AlertDetailPage() {
  const params = useParams();
  const alertId = params.id as string;
  const [alert, setAlert] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getAlert(alertId);
        setAlert(data);
      } catch (error) {
        console.error('获取异常详情失败:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [alertId]);

  async function handleAcknowledge() {
    await acknowledgeAlert(alertId);
    setAlert({ ...alert, status: 'ACKNOWLEDGED' });
  }

  async function handleResolve() {
    await resolveAlert(alertId);
    setAlert({ ...alert, status: 'RESOLVED' });
  }

  if (loading) {
    return <div className="card"><p>加载中...</p></div>;
  }

  return (
    <div>
      <div className="card">
        <div className="flex justify-between items-start mb-4">
          <h2 className="text-xl font-bold">{alert.type}</h2>
          <span className={`inline-block px-3 py-1 rounded-full text-sm font-bold ${
            alert.status === 'OPEN' ? 'bg-red-100 text-red-600' :
            alert.status === 'ACKNOWLEDGED' ? 'bg-yellow-100 text-yellow-600' :
            'bg-green-100 text-green-600'
          }`}>
            {alert.status}
          </span>
        </div>

        <div className="mb-4">
          <h3 className="font-bold text-gray-700">发生了什么</h3>
          <p className="text-gray-600 mt-1">{alert.reason}</p>
        </div>

        <div className="mb-4">
          <h3 className="font-bold text-gray-700">建议动作</h3>
          <p className="text-gray-600 mt-1">{alert.suggestedAction}</p>
        </div>

        <div className="mb-4 text-sm text-gray-500">
          <p>发生时间: {new Date(alert.occurredAt).toLocaleString()}</p>
          {alert.acknowledgedAt && (
            <p>确认时间: {new Date(alert.acknowledgedAt).toLocaleString()}</p>
          )}
          {alert.resolvedAt && (
            <p>解决时间: {new Date(alert.resolvedAt).toLocaleString()}</p>
          )}
        </div>

        <div className="flex gap-3">
          {alert.status !== 'RESOLVED' && (
            <button 
              className={`btn ${alert.status === 'OPEN' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={handleAcknowledge}
              disabled={alert.status === 'ACKNOWLEDGED'}
            >
              {alert.status === 'OPEN' ? '确认' : '已确认'}
            </button>
          )}
          {alert.status !== 'RESOLVED' && (
            <button className="btn btn-success" onClick={handleResolve}>
              解决
            </button>
          )}
        </div>
      </div>

      <div className="card">
        <h3 className="font-bold mb-3">关联音频复盘</h3>
        <AudioReviewPlayer clips={[]} />
        <p className="text-sm text-gray-500 mt-2">Audio is for authorized review only.</p>
      </div>
    </div>
  );
}