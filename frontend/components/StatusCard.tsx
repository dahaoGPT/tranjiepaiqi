interface StatusCardProps {
  title: string;
  status: string;
  statusLabel: string;
  statusColor: 'green' | 'red' | 'yellow';
}

export default function StatusCard({ title, status, statusLabel, statusColor }: StatusCardProps) {
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