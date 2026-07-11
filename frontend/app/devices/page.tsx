'use client';

export default function DevicesPage() {
  const devices = [
    { id: 'device-001', name: '厨房水龙头传感器', status: 'ONLINE', lastSeen: '5分钟前' },
    { id: 'device-002', name: '浴室水龙头传感器', status: 'ONLINE', lastSeen: '10分钟前' }
  ];

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">设备列表</h2>
      <div className="grid">
        {devices.map((device) => (
          <div key={device.id} className="card">
            <h3 className="font-bold">{device.name}</h3>
            <p className="text-sm text-gray-600 mt-1">设备编号: {device.id}</p>
            <span className={`inline-block mt-2 px-3 py-1 rounded-full text-sm font-bold ${
              device.status === 'ONLINE' ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
            }`}>
              {device.status === 'ONLINE' ? '在线' : '离线'}
            </span>
            <p className="text-xs text-gray-400 mt-2">最近上报: {device.lastSeen}</p>
          </div>
        ))}
      </div>
    </div>
  );
}