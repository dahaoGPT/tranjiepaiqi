# jiepaiqi 操作手册

日期：2026-07-05

本文档用于说明 `jiepaiqi` 独居老人生活节拍器 MVP 的本地启动、模块测试、联调验证和常见问题处理。

## 1. 模块说明

项目由 4 个主要模块组成：

| 模块 | 目录 | 作用 | 默认端口 |
| --- | --- | --- | --- |
| PostgreSQL | `docker-compose.yml` | 保存老人、设备、声学特征、用水事件、异常提醒、音频片段元数据 | `5432` |
| 后端服务 | `backend` | Spring Boot 2.7.18 Web API，使用 MyBatis 和 Flyway | `8080` |
| 前端应用 | `frontend` | Next.js 移动端优先界面，用于看板、提醒、设备和音频复盘页面 | `3000` |
| 模拟器 | `simulator` | TypeScript 脚本，模拟声感传感器上传原始音频片段和声学特征 | 无常驻端口 |

当前 MVP 中，后端 API 和模拟器可以真实联调；前端页面目前使用本地样例数据展示移动端交互效果，后续可继续改造为实时调用后端 API。

## 2. 前置条件

本地需要安装：

- Java 8
- Node.js 18 或更高版本
- npm
- Docker Desktop 或兼容 Docker daemon

在 Windows PowerShell 中进入项目根目录：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi
```

## 3. 启动 PostgreSQL

项目使用 PostgreSQL 14。该版本与 Spring Boot 2.7.18 默认集成的 Flyway 8.5.x 更匹配。

启动数据库：

```powershell
docker compose up -d postgres
```

查看容器状态：

```powershell
docker compose ps
```

验证 PostgreSQL 是否可以接收连接：

```powershell
docker exec jiepaiqi-postgres pg_isready -U jiepaiqi -d jiepaiqi
```

期望输出包含：

```text
accepting connections
```

数据库连接参数：

| 参数 | 值 |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| Database | `jiepaiqi` |
| Username | `jiepaiqi` |
| Password | `jiepaiqi` |

## 4. 启动后端服务

进入后端目录：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\backend
```

启动 Spring Boot：

```powershell
.\mvnw.cmd spring-boot:run
```

启动成功后，后端地址为：

```text
http://localhost:8080
```

后端默认读取：

```text
backend/src/main/resources/application.yml
```

核心配置包括：

- PostgreSQL：`jdbc:postgresql://localhost:5432/jiepaiqi`
- MyBatis Mapper：`classpath:mapper/**/*.xml`
- 音频保存目录：`backend/storage/audio`
- 音频保留天数：`30`

首次启动后，Flyway 会自动执行：

```text
backend/src/main/resources/db/migration/V1__initial_schema.sql
```

## 5. 测试后端模块

进入后端目录：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\backend
```

运行全部后端测试：

```powershell
.\mvnw.cmd test
```

只验证数据库 schema 和 Flyway：

```powershell
.\mvnw.cmd test "-Dtest=SchemaSmokeTest"
```

使用真实 PostgreSQL 14 验证 schema：

```powershell
.\mvnw.cmd test "-Dtest=SchemaSmokeTest" "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/jiepaiqi" "-Dspring.datasource.username=jiepaiqi" "-Dspring.datasource.password=jiepaiqi"
```

期望结果：

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

常用测试类：

| 测试类 | 覆盖内容 |
| --- | --- |
| `SchemaSmokeTest` | Flyway 建表和应用上下文启动 |
| `WaterEventAggregatorTest` | 声学特征聚合为用水事件 |
| `AlertRuleEngineTest` | 晨间未用水、长时间用水、低活动、设备离线等规则 |
| `FeatureIngestionControllerTest` | 声学特征上传 API |
| `AudioClipControllerTest` | 原始音频片段上传 API |
| `LocalAudioStorageServiceTest` | 本地音频文件保存 |
| `DashboardControllerTest` | 老人看板 API |
| `AlertControllerTest` | 异常提醒 API |

## 6. 启动前端应用

进入前端目录：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\frontend
```

安装依赖：

```powershell
npm install
```

启动开发服务器：

```powershell
npm run dev
```

浏览器访问：

```text
http://localhost:3000
```

常用页面：

| 页面 | 地址 | 说明 |
| --- | --- | --- |
| 首页 | `http://localhost:3000/` | 入口页 |
| 登录页 | `http://localhost:3000/login` | 登录入口 |
| 移动端看板 | `http://localhost:3000/dashboard` | 老人状态、用水节奏、提醒摘要 |
| 提醒列表 | `http://localhost:3000/alerts` | 异常提醒列表 |
| 提醒详情 | `http://localhost:3000/alerts/alert-001` | 异常详情和音频复盘入口 |
| 设备页 | `http://localhost:3000/devices` | 设备状态展示 |
| 老人详情 | `http://localhost:3000/elders/elder-001` | 老人维度详情 |

前端构建测试：

```powershell
npm run build
```

前端单元测试：

```powershell
npm run test
```

如果 `next lint` 提示 Next.js 14 的 lint 命令变更，可先以 `npm run build` 和 `npm run test` 作为当前 MVP 的主要验证方式。

## 7. 启动模拟器

模拟器用于模拟声感传感器向后端上传：

- 原始音频片段
- 声学特征窗口

进入模拟器目录：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\simulator
```

安装依赖：

```powershell
npm install
```

运行正常用水节奏场景：

```powershell
npm run scenario:normal
```

运行晨间未用水场景：

```powershell
npm run scenario:no-morning-water
```

运行长时间连续用水场景：

```powershell
npm run scenario:long-flow
```

运行低活动场景：

```powershell
npm run scenario:low-activity
```

模拟器默认调用：

```text
http://localhost:8080
```

默认设备 ID：

```text
device-001
```

可以通过环境变量覆盖：

```powershell
$env:JIEPAIQI_API_BASE_URL = "http://localhost:8080"
$env:JIEPAIQI_DEVICE_ID = "device-001"
npm run scenario:normal
```

模拟器成功时会输出类似：

```text
scenario=normal
audioClipId=...
audioSizeBytes=...
acceptedFeatures=...
rejectedFeatures=0
```

## 8. 端到端联调流程

建议按以下顺序联调：

1. 启动 PostgreSQL。

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi
docker compose up -d postgres
```

2. 启动后端。

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\backend
.\mvnw.cmd spring-boot:run
```

3. 运行模拟器。

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\simulator
npm run scenario:normal
```

4. 启动前端查看移动端页面。

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\frontend
npm run dev
```

5. 手机或浏览器访问：

```text
http://localhost:3000/dashboard
```

如果要用手机访问电脑上的前端页面，需要确保手机和电脑在同一局域网，并使用电脑局域网 IP，例如：

```text
http://192.168.1.10:3000/dashboard
```

## 9. 手动测试后端 API

以下命令适合在后端已启动后执行。

### 9.1 查询老人看板

```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/elders/elder-001/dashboard"
```

### 9.2 查询提醒列表

```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/alerts" -Headers @{ "X-User-Id" = "user-001" }
```

### 9.3 查询提醒详情

```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/alerts/alert-001" -Headers @{ "X-User-Id" = "user-001" }
```

### 9.4 确认提醒

```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/alerts/alert-001/acknowledge" -Headers @{ "X-User-Id" = "user-001" }
```

### 9.5 解决提醒

```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/alerts/alert-001/resolve" -Headers @{ "X-User-Id" = "user-001" }
```

### 9.6 上传声学特征

```powershell
$body = @{
  features = @(
    @{
      windowStartedAt = "2026-07-05T08:00:00Z"
      windowEndedAt = "2026-07-05T08:00:10Z"
      rms = 0.42
      peak = 0.75
      spectralCentroid = 1200
      zeroCrossingRate = 0.08
      flowConfidence = 0.92
      audioClipId = $null
    }
  )
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/devices/device-001/features" `
  -ContentType "application/json" `
  -Body $body
```

期望返回：

```text
acceptedCount: 1
rejectedCount: 0
```

### 9.7 上传原始音频片段

PowerShell 原生命令上传 multipart 文件较繁琐，推荐优先使用模拟器：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\simulator
npm run scenario:normal
```

后端会把音频文件保存到：

```text
backend/storage/audio
```

## 10. 停止服务

停止前端和后端：

- 在对应终端按 `Ctrl+C`

停止 PostgreSQL 容器：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi
docker compose stop postgres
```

停止并移除容器，但保留数据卷：

```powershell
docker compose down
```

如果需要重新初始化 PostgreSQL 14 数据卷，先确认不再需要旧数据，再执行：

```powershell
docker compose down -v
docker compose up -d postgres
```

## 11. 常见问题

### 11.1 Docker 无法连接

现象：

```text
permission denied while trying to connect to the docker API
```

处理：

- 确认 Docker Desktop 已启动。
- 确认当前终端有权限访问 Docker daemon。
- 重新执行 `docker compose up -d postgres`。

### 11.2 端口被占用

PostgreSQL 默认占用 `5432`，后端默认占用 `8080`，前端默认占用 `3000`。

检查端口：

```powershell
netstat -ano | findstr :5432
netstat -ano | findstr :8080
netstat -ano | findstr :3000
```

### 11.3 后端启动时报数据库连接失败

先确认数据库可用：

```powershell
docker exec jiepaiqi-postgres pg_isready -U jiepaiqi -d jiepaiqi
```

再确认 `backend/src/main/resources/application.yml` 中的连接信息没有被改错。

### 11.4 模拟器上传失败

检查顺序：

1. 后端是否正在运行：`http://localhost:8080`
2. 模拟器环境变量是否指向正确地址：`JIEPAIQI_API_BASE_URL`
3. 上传音频文件类型是否为后端允许类型：`audio/wav`、`audio/mpeg`、`audio/mp4`

### 11.5 前端页面显示的是样例数据

这是当前 MVP 的已知状态。前端页面已经完成移动端优先的界面和交互骨架，但 `frontend/lib/api.ts` 仍返回本地样例数据。后续可以把该文件替换为真实 `fetch` 调用，让页面读取后端 API。

## 12. 推荐日常验证命令

每次改动后建议至少运行：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\backend
.\mvnw.cmd test
```

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\frontend
npm run build
```

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\simulator
npm run scenario:normal
```

如果本机 PostgreSQL 已启动，也建议额外验证真实数据库：

```powershell
cd E:\3-workspace\2-aiprojects\jiepaiqi\backend
.\mvnw.cmd test "-Dtest=SchemaSmokeTest" "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/jiepaiqi" "-Dspring.datasource.username=jiepaiqi" "-Dspring.datasource.password=jiepaiqi"
```
