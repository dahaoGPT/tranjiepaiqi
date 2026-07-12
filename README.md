# jiepaiqi - 独居老人生活节拍器

通过声感传感器采集水龙头流水声音，分析独居老人的日常用水节奏，识别异常行为并及时提醒家属或社区志愿者。

## 系统架构

```
┌─────────────────┐    HTTP    ┌─────────────────┐    SQL    ┌─────────────────┐
│                 │ ──────────>│                 │ ─────────>│                 │
│   声感传感器     │            │   后端服务       │            │   PostgreSQL    │
│  (模拟器)        │<────────── │   Spring Boot   │<────────── │   15            │
│                 │    HTTP    │    8080         │    SQL    │    5432         │
└─────────────────┘            └─────────────────┘            └─────────────────┘
                                     │
                                     │ API
                                     ▼
                              ┌─────────────────┐
                              │   前端应用       │
                              │   Next.js       │
                              │    3000         │
                              └─────────────────┘
```

### 模块说明

| 模块   | 目录                   | 技术栈                                   | 默认端口   |
| ---- | -------------------- | ------------------------------------- | ------ |
| 后端服务 | `backend/`           | Spring Boot 2.7.18 + MyBatis + Flyway | `8080` |
| 前端应用 | `frontend/`          | Next.js 14 + React 18 + TypeScript    | `3000` |
| 模拟器  | `simulator/`         | TypeScript + tsx                      | 无常驻端口  |
| 数据库  | `docker-compose.yml` | PostgreSQL 15                         | `5432` |

## 核心数据流

以 `no-morning-water` 场景为例，完整数据流转过程：

1. **传感器上报**：模拟器生成低置信度（0.05-0.1）的声学特征，调用 `POST /api/devices/{deviceId}/features`
2. **特征存储**：后端验证设备后，将特征写入 `acoustic_features` 表
3. **事件聚合**：`WaterEventAggregator` 聚合今日所有特征窗口（置信度阈值 0.80），因置信度过低不生成用水事件
4. **规则检测**：`AlertRuleEngine.evaluateMorning()` 检查晨间窗口（6:30-9:00）内无用水事件，生成 `NO_MORNING_WATER` 告警候选
5. **告警去重**：检查是否已存在同类型 `OPEN` 状态告警，避免重复
6. **告警存储**：将告警写入 `alerts` 表，状态为 `OPEN`
7. **前端展示**：前端调用 `GET /api/elders/{elderId}/dashboard` 获取看板数据，展示未处理告警（注：当前 MVP 版本前端使用本地样例数据，后端 API 已就绪）

### 数据流节点

| 阶段 | 数据类型   | 存储位置                  | 关键处理         |
| -- | ------ | --------------------- | ------------ |
| 采集 | 声学特征窗口 | `acoustic_features` 表 | 置信度验证、时间窗口校验 |
| 聚合 | 用水事件   | `water_events` 表      | 连续高置信度窗口合并   |
| 检测 | 异常候选   | 内存计算                  | 规则引擎评估       |
| 存储 | 异常提醒   | `alerts` 表            | 去重后持久化       |
| 展示 | 看板聚合数据 | API 响应                | 多表联查聚合       |

## 技术栈

### 后端

- **框架**: Spring Boot 2.7.18 (Java 8)
- **数据库访问**: MyBatis 2.3.2
- **数据库迁移**: Flyway
- **数据库**: PostgreSQL 15 (生产), H2 (测试)
- **工具**: Lombok

### 前端

- **框架**: Next.js 14.2
- **UI**: React 18.3
- **语言**: TypeScript 5.5
- **测试**: Vitest 2.0

### 模拟器

- **语言**: TypeScript 5.5
- **运行时**: tsx 4.20

## 快速启动

### 前置条件

- Java 8
- Node.js 18+
- Docker Desktop

### 启动命令（Windows PowerShell）

**1. 启动 PostgreSQL**

```powershell
cd .
docker compose up -d postgres
```

等待约 30 秒，验证数据库连接：

```powershell
docker exec jiepaiqi-postgres pg_isready -U jiepaiqi -d jiepaiqi
```

期望输出：`accepting connections`

**2. 启动后端服务**

```powershell
cd .\backend
.\mvnw.cmd spring-boot:run
```

首次启动会自动执行 Flyway 数据库迁移。后端地址：`http://localhost:8080`

**3. 启动前端应用**

```powershell
cd .\frontend
npm install
npm run dev
```

前端地址：`http://localhost:3000`

**4. 运行模拟器**

```powershell
cd .\simulator
npm install
npm run scenario:normal
```

### 完整启动脚本

```powershell
# 启动数据库
cd .
docker compose up -d postgres
Start-Sleep -Seconds 30

# 启动后端（后台运行）
cd .\backend
Start-Process -NoNewWindow -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run"
Start-Sleep -Seconds 15

# 启动前端（后台运行）
cd .\frontend
Start-Process -NoNewWindow -FilePath "npm" -ArgumentList "run","dev"
Start-Sleep -Seconds 5

# 运行模拟器
cd .\simulator
npm run scenario:normal
```

## 数据库表结构

### 核心表

| 表名                  | 用途           | 关键字段                                                                                  |
| ------------------- | ------------ | ------------------------------------------------------------------------------------- |
| `users`             | 系统用户（家属、志愿者） | `id`, `username`, `role`, `display_name`                                              |
| `elders`            | 老人档案         | `id`, `name`, `notes`                                                                 |
| `devices`           | 声感传感器        | `id`, `elder_id`, `serial_number`, `status`, `last_seen_at`                           |
| `acoustic_features` | 声学特征窗口       | `device_id`, `window_started_at`, `window_ended_at`, `flow_confidence`                |
| `water_events`      | 用水事件         | `elder_id`, `started_at`, `ended_at`, `duration_seconds`                              |
| `rhythm_baselines`  | 个人节奏基线       | `elder_id`, `morning_window_start`, `morning_window_end`, `average_daily_event_count` |
| `alerts`            | 异常提醒         | `elder_id`, `type`, `status`, `reason`, `suggested_action`                            |
| `alert_notes`       | 处理备注         | `alert_id`, `author_user_id`, `body`                                                  |
| `audio_clips`       | 原始音频元数据      | `device_id`, `storage_path`, `duration_seconds`                                       |

### 默认测试数据

| 类型 | ID                                     | 说明                    |
| -- | -------------------------------------- | --------------------- |
| 用户 | `550e8400-e29b-41d4-a716-446655440000` | 默认用户（FAMILY 角色）       |
| 老人 | `550e8400-e29b-41d4-a716-446655440001` | 张奶奶（独居老人）             |
| 设备 | `550e8400-e29b-41d4-a716-446655440002` | sim-device-001（模拟器设备） |

## API 端点

### 特征接收

| 方法     | 路径                                       | 说明                   |
| ------ | ---------------------------------------- | -------------------- |
| POST   | `/api/devices/{deviceId}/features`       | 接收声学特征数据，触发事件聚合和告警检测 |
| DELETE | `/api/devices/{deviceId}/features/reset` | 清除今日测试数据（测试用）        |

### 音频管理

| 方法   | 路径                                                           | 说明           |
| ---- | ------------------------------------------------------------ | ------------ |
| POST | `/api/devices/{deviceId}/audio-clips`                        | 上传原始音频片段     |
| GET  | `/api/devices/{deviceId}/audio-clips/{audioClipId}/playback` | 获取音频播放数据     |
| GET  | `/api/alerts/audio-clips/{audioClipId}/playback`             | 通过告警获取音频播放数据 |

### 看板查询

| 方法  | 路径                                | 说明         |
| --- | --------------------------------- | ---------- |
| GET | `/api/elders/{elderId}/dashboard` | 获取老人看板聚合数据 |

### 异常管理

| 方法   | 路径                                  | 说明                      |
| ---- | ----------------------------------- | ----------------------- |
| GET  | `/api/alerts/elder/{elderId}`       | 查询老人的异常列表               |
| GET  | `/api/alerts/{alertId}`             | 查询异常详情                  |
| POST | `/api/alerts/{alertId}/acknowledge` | 确认异常（状态改为 ACKNOWLEDGED） |
| POST | `/api/alerts/{alertId}/resolve`     | 解决异常（状态改为 RESOLVED）     |
| POST | `/api/alerts/{alertId}/notes`       | 添加处理备注                  |
| GET  | `/api/alerts/{alertId}/audio-clips` | 查询异常关联的音频片段             |

### 认证

| 方法   | 路径                | 说明          |
| ---- | ----------------- | ----------- |
| POST | `/api/auth/login` | 用户登录        |
| GET  | `/api/me`         | 获取当前用户信息    |
| GET  | `/api/me/elders`  | 获取用户绑定的老人列表 |

## 异常类型

| 类型                     | 说明     | 触发条件                  |
| ---------------------- | ------ | --------------------- |
| `NO_MORNING_WATER`     | 晨间未用水  | 晨间窗口（6:30-9:00）内无用水事件 |
| `LONG_CONTINUOUS_FLOW` | 长时间流水  | 单次用水时长超过 20 分钟        |
| `LOW_DAILY_ACTIVITY`   | 每日活动量低 | 用水次数和时长显著低于个人基线       |
| `DEVICE_OFFLINE`       | 设备离线   | 设备长时间未上报数据            |

## 模拟器场景

模拟器提供四种预设场景，用于测试不同异常检测逻辑：

| 场景    | 命令                                  | 预期结果                               |
| ----- | ----------------------------------- | ---------------------------------- |
| 正常用水  | `npm run scenario:normal`           | 晨间、午餐、晚餐三次用水，无异常                   |
| 晨间未用水 | `npm run scenario:no-morning-water` | 触发 `NO_MORNING_WATER` 告警           |
| 长时间流水 | `npm run scenario:long-flow`        | 触发 `LONG_CONTINUOUS_FLOW` 告警（35分钟） |
| 低活动量  | `npm run scenario:low-activity`     | 触发 `LOW_DAILY_ACTIVITY` 告警         |

### 模拟器默认配置

模拟器使用硬编码默认值，无需环境变量：

| 配置项   | 默认值                     | 说明             |
| ----- | ----------------------- | -------------- |
| 后端地址  | `http://localhost:8080` | 后端 API 基础地址    |
| 设备序列号 | `sim-device-001`        | 与测试数据中的设备序列号一致 |

如需修改，可直接编辑 `simulator/src/index.ts` 文件中的 `BASE_URL` 和 `DEVICE_ID` 常量。

## 配置说明

### 后端配置 (`application.yml`)

| 配置项                             | 默认值                                         | 影响范围           |
| ------------------------------- | ------------------------------------------- | -------------- |
| `spring.datasource.url`         | `jdbc:postgresql://localhost:5432/jiepaiqi` | 数据库连接          |
| `spring.datasource.username`    | `jiepaiqi`                                  | 数据库用户名         |
| `spring.datasource.password`    | `jiepaiqi`                                  | 数据库密码          |
| `mybatis.mapper-locations`      | `classpath:mapper/**/*.xml`                 | MyBatis 映射文件位置 |
| `jiepaiqi.audio.storage-root`   | `backend/storage/audio`                     | 音频文件存储目录       |
| `jiepaiqi.audio.retention-days` | `30`                                        | 音频保留天数         |

### 配置依赖关系

```
配置项 → 使用位置 → 影响功能
─────────────────────────────────────────────────────────────
storage-root → LocalAudioStorageService → 音频文件保存路径
retention-days → LocalAudioStorageService → 音频文件清理策略
flow-confidence-threshold (代码内) → WaterEventAggregator → 用水事件聚合阈值
long-flow-threshold-seconds (代码内) → AlertRuleEngine → 长流水告警阈值
```

## 测试命令

### 后端测试

```powershell
cd .\backend

# 运行全部测试
.\mvnw.cmd test

# 运行指定测试类
.\mvnw.cmd test "-Dtest=AlertRuleEngineTest"

# 使用真实 PostgreSQL 测试
.\mvnw.cmd test "-Dtest=SchemaSmokeTest" "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/jiepaiqi" "-Dspring.datasource.username=jiepaiqi" "-Dspring.datasource.password=jiepaiqi"
```

### 前端测试

```powershell
cd .\frontend

# 构建验证
npm run build

# 单元测试
npm run test

# 代码检查
npm run lint
```

### 模拟器测试

```powershell
cd .\simulator

# 运行单元测试
npm run test
```

## 目录结构

```
jiepaiqi/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/jiepaiqi/
│   │   ├── alert/              # 异常检测与管理
│   │   ├── audio/              # 音频存储与管理
│   │   ├── common/             # 通用工具
│   │   ├── config/             # 配置类
│   │   ├── dashboard/          # 看板服务
│   │   ├── device/             # 设备管理
│   │   ├── elder/              # 老人管理
│   │   ├── identity/           # 用户认证
│   │   ├── ingestion/          # 特征接收
│   │   └── rhythm/             # 用水事件聚合与节奏基线
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway 迁移脚本
│   │   └── mapper/             # MyBatis XML 映射
│   └── src/test/               # 单元测试
├── frontend/                   # Next.js 前端
│   ├── app/                    # 页面路由
│   │   ├── alerts/             # 异常列表与详情页
│   │   ├── dashboard/          # 看板首页
│   │   ├── devices/            # 设备管理页
│   │   └── login/              # 登录页
│   ├── components/             # 公共组件
│   └── lib/                    # API 封装
├── simulator/                  # TypeScript 模拟器
│   └── src/
│       ├── index.ts            # 主入口
│       ├── scenarios.ts        # 场景定义
│       └── scenario-runner.ts  # 场景执行器
├── docs/                       # 文档
│   ├── operation-manual.md     # 操作手册
│   ├── runbook.md              # 运行手册
│   └── jiepaiqi.sql            # 数据库脚本
└── docker-compose.yml          # Docker 配置
```

## 关键设计约束

1. **告警去重**：同类型 `OPEN` 状态告警不重复生成
2. **每日聚合**：特征接收时聚合今日所有窗口数据，而非仅当前批次
3. **MyBatis XML 映射**：所有 Mapper 接口必须有对应的 XML 文件
4. **前端自动刷新**：异常列表页每 30 秒自动刷新，支持手动刷新

## 文档参考

- [操作手册](docs/operation-manual.md) - 详细启动步骤、测试命令、常见问题
- [运行手册](docs/runbook.md) - 快速启动指南、API 端点速查

