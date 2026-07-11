# jiepaiqi MVP 运行手册

## 启动步骤

### 1. 启动 PostgreSQL

```bash
docker compose up -d postgres
```

等待约 30 秒，数据库启动完成。

### 2. 启动后端

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动。

### 4. 运行模拟器

```bash
cd simulator
npm install
npm run scenario:normal
```

可用场景：
- `npm run scenario:normal` - 正常日
- `npm run scenario:no-morning-water` - 晨间无用水
- `npm run scenario:long-flow` - 长流水
- `npm run scenario:low-activity` - 低活动

## API 端点

### 设备数据上报

```
POST /api/devices/{deviceId}/features
POST /api/devices/{deviceId}/audio-clips
```

### 看板查询

```
GET /api/elders/{elderId}/dashboard
```

### 异常管理

```
GET /api/alerts/elder/{elderId}
GET /api/alerts/{alertId}
POST /api/alerts/{alertId}/acknowledge
POST /api/alerts/{alertId}/resolve
GET /api/alerts/{alertId}/audio-clips
GET /api/audio-clips/{audioClipId}/playback
```

### 认证

```
POST /api/auth/login
GET /api/me
GET /api/me/elders
```

## 测试验证

### 运行后端测试

```bash
cd backend
.\mvnw.cmd test
```

### 运行前端构建

```bash
cd frontend
npm run build
```

## 默认数据

- 数据库: jiepaiqi (用户: jiepaiqi, 密码: jiepaiqi)
- 默认用户: 任意用户名密码均可登录 (MVP 版本)
- 默认老人 ID: 00000000-0000-0000-0000-000000000001

## 注意事项

1. 确保 Node.js >= 16.0 和 Java 8 已安装
2. PostgreSQL 默认端口 5432，确保未被占用
3. 前端开发模式会将 API 请求代理到后端
4. 音频文件默认保存在 `backend/storage/audio` 目录