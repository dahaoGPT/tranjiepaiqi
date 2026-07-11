/**
 * 状态卡片组件属性接口。
 */
interface StatusCardProps {
  /** 卡片标题 */
  title: string;
  /** 状态值 */
  status: string;
  /** 状态标签说明 */
  statusLabel: string;
  /** 状态颜色：green(正常)、red(警告)、yellow(注意) */
  statusColor: 'green' | 'red' | 'yellow';
}

/**
 * 状态卡片组件。
 * 用于看板页面展示老人状态、设备状态、异常数量等信息。
 */
export default function StatusCard({ title, status, statusLabel, statusColor }: StatusCardProps) {
  // 颜色样式映射表
  const colorClasses = {
    green: 'text-green-600 bg-green-100',
    red: 'text-red-600 bg-red-100',
    yellow: 'text-yellow-600 bg-yellow-100'
  };

  return (
    <div className="card">
      <h3 className="text-sm text-gray-500 mb-2">{title}</h3>
      <div className={`inline-block px-3 py-1 rounded-full text-sm font-bold ${colorClasses[statusColor]}`}>
        {status}
      </div>
      <p className="text-xs text-gray-400 mt-2">{statusLabel}</p>
    </div>
  );
}