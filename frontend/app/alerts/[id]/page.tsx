'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { getAlert, acknowledgeAlert, resolveAlert } from '../../../lib/api';
import AudioReviewPlayer from '../../../components/AudioReviewPlayer';

/**
 * 异常详情页面组件。
 * 展示单个异常的详细信息，支持确认和解决操作。
 */
export default function AlertDetailPage() {
  // 获取路由参数
  const params = useParams();
  const alertId = params.id as string;
  // 异常详情状态
  const [alert, setAlert] = useState<any>(null);
  // 加载状态
  const [loading, setLoading] = useState(true);

  /**
   * 获取异常详情数据。
   */
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

  /**
   * 处理确认操作。
   * 将异常状态从 OPEN 改为 ACKNOWLEDGED。
   */
  async function handleAcknowledge() {
    await acknowledgeAlert(alertId);
    setAlert({ ...alert, status: 'ACKNOWLEDGED' });
  }

  /**
   * 处理解决操作。
   * 将异常状态改为 RESOLVED。
   */
  async function handleResolve() {
    await resolveAlert(alertId);
    setAlert({ ...alert, status: 'RESOLVED' });
  }

  // 加载中状态
  if (loading) {
    return <div className="card"><p>加载中...</p></div>;
  }

  // 正常渲染
  return (
    <div>
      {/* 异常详情卡片 */}
      <div className="card">
        <div className="flex justify-between items-start mb-4">
          {/* 异常类型 */}
          <h2 className="text-xl font-bold">{alert.type}</h2>
          {/* 状态标签 */}
          <span className={`inline-block px-3 py-1 rounded-full text-sm font-bold ${
            alert.status === 'OPEN' ? 'bg-red-100 text-red-600' :
            alert.status === 'ACKNOWLEDGED' ? 'bg-yellow-100 text-yellow-600' :
            'bg-green-100 text-green-600'
          }`}>
            {alert.status}
          </span>
        </div>

        {/* 发生了什么 */}
        <div className="mb-4">
          <h3 className="font-bold text-gray-700">发生了什么</h3>
          <p className="text-gray-600 mt-1">{alert.reason}</p>
        </div>

        {/* 建议动作 */}
        <div className="mb-4">
          <h3 className="font-bold text-gray-700">建议动作</h3>
          <p className="text-gray-600 mt-1">{alert.suggestedAction}</p>
        </div>

        {/* 时间信息 */}
        <div className="mb-4 text-sm text-gray-500">
          <p>发生时间: {new Date(alert.occurredAt).toLocaleString()}</p>
          {alert.acknowledgedAt && (
            <p>确认时间: {new Date(alert.acknowledgedAt).toLocaleString()}</p>
          )}
          {alert.resolvedAt && (
            <p>解决时间: {new Date(alert.resolvedAt).toLocaleString()}</p>
          )}
        </div>

        {/* 操作按钮 */}
        <div className="flex gap-3">
          {/* 确认按钮 */}
          {alert.status !== 'RESOLVED' && (
            <button 
              className={`btn ${alert.status === 'OPEN' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={handleAcknowledge}
              disabled={alert.status === 'ACKNOWLEDGED'}
            >
              {alert.status === 'OPEN' ? '确认' : '已确认'}
            </button>
          )}
          {/* 解决按钮 */}
          {alert.status !== 'RESOLVED' && (
            <button className="btn btn-success" onClick={handleResolve}>
              解决
            </button>
          )}
        </div>
      </div>

      {/* 关联音频复盘 */}
      <div className="card">
        <h3 className="font-bold mb-3">关联音频复盘</h3>
        <AudioReviewPlayer clips={[]} />
        <p className="text-sm text-gray-500 mt-2">音频仅供授权人员复盘使用</p>
      </div>
    </div>
  );
}