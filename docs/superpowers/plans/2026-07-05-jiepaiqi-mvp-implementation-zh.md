# 节拍器 MVP 实施计划

> **对于代理式工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 来逐任务实施此计划。步骤使用复选框（`- [ ]`）语法进行跟踪。

**目标：** 构建 `节拍器` 的第一个端到端 MVP：Spring Boot 2.7.18 后端、PostgreSQL 持久化、原始音频留存用于复盘、模拟器数据注入，以及一个移动端优先的 Next.js 看板。

**架构：** 采用前后端分离架构。后端是一个 Spring Boot 模块化单体应用，包含 identity（身份）、elder（老人）、device（设备）、ingestion（数据注入）、audio（音频）、rhythm（节奏）、alert（告警）和 dashboard（看板）等包。前端是一个 Next.js 应用，消费 REST API，优先考虑手机使用场景。

**技术栈：** Java 8、Spring Boot 2.7.18、Maven Wrapper、PostgreSQL、Flyway、MyBatis、JUnit 5、Next.js、TypeScript、npm、Playwright 或 React Testing Library 用于前端测试。

***

## 参考规范

- `docs/superpowers/specs/2026-07-02-jiepaiqi-requirements.md`
- `docs/superpowers/specs/2026-07-02-jiepaiqi-design.md`
- `docs/superpowers/specs/2026-07-05-jiepaiqi-er-diagram.md`

## 目标文件结构

```text
backend/
  pom.xml
  mvnw
  mvnw.cmd
  .mvn/wrapper/maven-wrapper.properties
  src/main/java/com/jiepaiqi/JiepaiqiApplication.java
  src/main/java/com/jiepaiqi/common/
  src/main/java/com/jiepaiqi/identity/
  src/main/java/com/jiepaiqi/elder/
  src/main/java/com/jiepaiqi/device/
  src/main/java/com/jiepaiqi/ingestion/
  src/main/java/com/jiepaiqi/audio/
  src/main/java/com/jiepaiqi/rhythm/
  src/main/java/com/jiepaiqi/alert/
  src/main/java/com/jiepaiqi/dashboard/
  src/main/resources/application.yml
  src/main/resources/db/migration/
  src/main/resources/mapper/
  src/test/java/com/jiepaiqi/
frontend/
  package.json
  next.config.js
  tsconfig.json
  app/
  components/
  lib/
  tests/
simulator/
  package.json
  src/index.ts
docker-compose.yml
.gitignore
docs/superpowers/plans/2026-07-05-jiepaiqi-mvp-implementation.md
```

## 注释规范

- SQL 迁移文件必须为每个业务表和重要业务列添加中文注释。
- Java 类在表示领域概念、API 边界或服务时，必须包含简短的中文类级注释。
- 复杂的 Java 方法必须包含简短的中文注释来描述业务规则，尤其是节奏聚合、告警生成、音频存储和授权相关方法。
- TypeScript 组件和 API 类型必须为面向用户的业务概念添加简短的中文注释。
- 注释应解释领域意图，而不是重述明显的语法。

## 任务 1：仓库初始化

**文件：**

- 创建：`.gitignore`
- 创建：`docker-compose.yml`
- 创建：`backend/pom.xml`
- 创建：`backend/src/main/java/com/jiepaiqi/JiepaiqiApplication.java`
- 创建：`backend/src/main/resources/application.yml`
- 创建：`frontend/package.json`
- 创建：`simulator/package.json`
- [ ] **步骤 1：如需要，初始化 git**

运行：`git status`
预期：显示现有状态输出或 `fatal: not a git repository`。

如果不是仓库，运行：

```bash
git init
```

预期：仓库已初始化。

- [ ] **步骤 2：创建忽略规则**

创建 `.gitignore`：

```gitignore
.idea/
.vscode/
.superpowers/
backend/target/
backend/storage/
frontend/.next/
frontend/node_modules/
simulator/node_modules/
*.log
.env
.env.local
```

- [ ] **步骤 3：添加本地 PostgreSQL 服务**

创建 `docker-compose.yml`：

```yaml
services:
  postgres:
    image: postgres:14
    container_name: jiepaiqi-postgres
    environment:
      POSTGRES_DB: jiepaiqi
      POSTGRES_USER: jiepaiqi
      POSTGRES_PASSWORD: jiepaiqi
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

- [ ] **步骤 4：创建 Spring Boot Maven 项目**

创建 `backend/pom.xml`：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
  </parent>
  <groupId>com.jiepaiqi</groupId>
  <artifactId>jiepaiqi-backend</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <properties>
    <java.version>1.8</java.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>2.3.2</version>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

- [ ] **步骤 5：添加应用入口和配置**

创建 `backend/src/main/java/com/jiepaiqi/JiepaiqiApplication.java`：

```java
package com.jiepaiqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 节拍器后端应用入口。
 * 负责启动 Spring Boot 模块化单体应用。
 */
@SpringBootApplication
public class JiepaiqiApplication {
    public static void main(String[] args) {
        SpringApplication.run(JiepaiqiApplication.class, args);
    }
}
```

创建 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jiepaiqi
    username: jiepaiqi
    password: jiepaiqi
  flyway:
    enabled: true

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.jiepaiqi
  configuration:
    map-underscore-to-camel-case: true

jiepaiqi:
  audio:
    storage-root: backend/storage/audio
    retention-days: 30
```

- [ ] **步骤 6：验证后端骨架**

运行：`cd backend && .\mvnw.cmd test`

如果 Maven Wrapper 尚不存在，请在执行期间使用已安装的 Maven 生成它，或在此步骤之前创建 wrapper 文件。

预期：构建成功，零个或一个上下文加载测试。

- [ ] **步骤 7：提交**

```bash
git add .gitignore docker-compose.yml backend frontend simulator
git commit -m "chore: bootstrap jiepaiqi workspace"
```

## 任务 2：数据库迁移和 MyBatis Mapper 骨架

**文件：**

- 创建：`backend/src/main/resources/db/migration/V1__initial_schema.sql`
- 创建：`identity`、`elder`、`device`、`ingestion`、`audio`、`rhythm`、`alert` 下的纯 Java 模型文件
- 创建：每个后端模块包下的 MyBatis mapper 接口
- 创建：`backend/src/main/resources/mapper/` 下的 MyBatis XML mapper 文件
- 测试：`backend/src/test/java/com/jiepaiqi/db/SchemaSmokeTest.java`
- [ ] **步骤 1：编写 schema 冒烟测试**

创建 `SchemaSmokeTest.java`：

```java
package com.jiepaiqi.db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchemaSmokeTest {
    @Test
    void contextLoadsWithFlywaySchema() {
    }
}
```

- [ ] **步骤 2：运行测试并观察失败**

运行：`cd backend && .\mvnw.cmd test -Dtest=SchemaSmokeTest`

预期：失败，因为数据库 schema 和/或测试数据源未配置。

- [ ] **步骤 3：添加测试数据源配置**

创建 `backend/src/test/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:jiepaiqi;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
  flyway:
    enabled: true

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.jiepaiqi
  configuration:
    map-underscore-to-camel-case: true
```

- [ ] **步骤 4：创建初始 Flyway schema**

创建 `V1__initial_schema.sql`，包含以下表：

```sql
-- 系统登录用户表：保存家属、社区志愿者等使用者账号。
create table users (
  id uuid primary key,
  username varchar(80) not null unique,
  display_name varchar(120) not null,
  role varchar(30) not null,
  created_at timestamp not null
);

comment on table users is '系统登录用户表，保存家属和社区志愿者账号';
comment on column users.username is '登录用户名，MVP 阶段保持唯一';
comment on column users.role is '用户角色，例如 FAMILY 或 VOLUNTEER';

-- 老人档案表：保存被守护老人的基础信息。
create table elders (
  id uuid primary key,
  name varchar(120) not null,
  notes varchar(500),
  created_at timestamp not null
);

comment on table elders is '老人档案表，作为设备、节奏和异常记录的核心归属对象';
comment on column elders.notes is '老人备注信息，例如居住情况或照护说明';

-- 设备表：保存安装在水龙头附近的声感传感器。
create table devices (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  serial_number varchar(120) not null unique,
  status varchar(30) not null,
  last_seen_at timestamp,
  created_at timestamp not null
);

comment on table devices is '声感传感器设备表，记录设备绑定老人和最近在线状态';
comment on column devices.serial_number is '设备唯一序列号，用于设备上报';
comment on column devices.status is '设备状态，例如 ONLINE 或 OFFLINE';
comment on column devices.last_seen_at is '设备最近一次成功上报时间';

-- 用户与老人绑定表：限制用户只能查看绑定老人数据。
create table user_elder_bindings (
  user_id uuid not null references users(id),
  elder_id uuid not null references elders(id),
  primary key (user_id, elder_id)
);

comment on table user_elder_bindings is '用户与老人绑定关系表，用于看板和音频复盘授权';

-- 原始音频片段元数据表：音频文件存文件系统或对象存储，表中只保存元数据和路径。
create table audio_clips (
  id uuid primary key,
  device_id uuid not null references devices(id),
  window_started_at timestamp not null,
  window_ended_at timestamp not null,
  storage_path varchar(500) not null,
  content_type varchar(80) not null,
  duration_seconds integer not null,
  size_bytes bigint not null,
  created_at timestamp not null
);

comment on table audio_clips is '原始音频片段元数据表，用于异常复盘和人工核实';
comment on column audio_clips.storage_path is '音频文件存储路径或对象存储键';
comment on column audio_clips.content_type is '音频 MIME 类型，例如 audio/wav';
comment on column audio_clips.duration_seconds is '音频片段时长，单位秒';
comment on column audio_clips.size_bytes is '音频文件大小，单位字节';
```

- [ ] **步骤 5：追加特征、事件、基线、告警表**

在迁移文件中追加：

```sql
-- 声学特征表：保存设备从音频中提取出的结构化水流特征。
create table acoustic_features (
  id uuid primary key,
  device_id uuid not null references devices(id),
  audio_clip_id uuid references audio_clips(id),
  window_started_at timestamp not null,
  window_ended_at timestamp not null,
  average_decibels numeric(8,2) not null,
  peak_decibels numeric(8,2) not null,
  low_band_energy numeric(10,4) not null,
  mid_band_energy numeric(10,4) not null,
  high_band_energy numeric(10,4) not null,
  flow_confidence numeric(5,4) not null,
  created_at timestamp not null
);

comment on table acoustic_features is '声学特征表，后端用它判断是否存在用水事件';
comment on column acoustic_features.audio_clip_id is '关联的原始音频片段，用于异常复盘';
comment on column acoustic_features.flow_confidence is '水流置信度，范围 0 到 1';

-- 用水事件表：由连续高置信度声学特征聚合而来。
create table water_events (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  device_id uuid not null references devices(id),
  started_at timestamp not null,
  ended_at timestamp not null,
  duration_seconds integer not null,
  average_confidence numeric(5,4) not null,
  created_at timestamp not null
);

comment on table water_events is '用水事件表，表示一次业务可读的用水行为';
comment on column water_events.duration_seconds is '用水事件持续时长，单位秒';
comment on column water_events.average_confidence is '该事件内声学特征的平均水流置信度';

-- 个人节奏基线表：保存最近历史计算出的老人常见生活节奏。
create table rhythm_baselines (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  calculated_at timestamp not null,
  morning_window_start time not null,
  morning_window_end time not null,
  average_daily_event_count numeric(8,2) not null,
  average_daily_duration_seconds numeric(10,2) not null
);

comment on table rhythm_baselines is '个人节奏基线表，用于判断今天是否偏离平常节奏';
comment on column rhythm_baselines.morning_window_start is '常见晨间首次用水窗口开始时间';
comment on column rhythm_baselines.morning_window_end is '常见晨间首次用水窗口结束时间';

-- 异常提醒表：保存节奏异常和设备异常。
create table alerts (
  id uuid primary key,
  elder_id uuid not null references elders(id),
  device_id uuid references devices(id),
  type varchar(60) not null,
  status varchar(30) not null,
  reason varchar(1000) not null,
  suggested_action varchar(1000) not null,
  occurred_at timestamp not null,
  acknowledged_at timestamp,
  resolved_at timestamp,
  created_at timestamp not null
);

comment on table alerts is '异常提醒表，保存需要家属或志愿者确认的事件';
comment on column alerts.type is '提醒类型，例如 NO_MORNING_WATER 或 LONG_CONTINUOUS_FLOW';
comment on column alerts.status is '处理状态，例如 OPEN、ACKNOWLEDGED、RESOLVED';
comment on column alerts.reason is '系统生成的可解释异常原因';
comment on column alerts.suggested_action is '建议家属或志愿者采取的动作';

-- 异常处理备注表：保存家属或志愿者对提醒的处理记录。
create table alert_notes (
  id uuid primary key,
  alert_id uuid not null references alerts(id),
  author_user_id uuid not null references users(id),
  body varchar(1000) not null,
  created_at timestamp not null
);

comment on table alert_notes is '异常处理备注表，记录确认、联系、上门等人工处理信息';
comment on column alert_notes.body is '处理备注正文';
```

- [ ] **步骤 6：运行 schema 测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=SchemaSmokeTest`

预期：通过。

- [ ] **步骤 7：提交**

```bash
git add backend/src/main/resources/db/migration backend/src/test/resources backend/src/test/java
git commit -m "feat: add initial database schema"
```

## 任务 3：节奏领域逻辑

**文件：**

- 创建：`backend/src/main/java/com/jiepaiqi/rhythm/AcousticFeatureWindow.java`
- 创建：`backend/src/main/java/com/jiepaiqi/rhythm/WaterEvent.java`
- 创建：`backend/src/main/java/com/jiepaiqi/rhythm/WaterEventAggregator.java`
- 测试：`backend/src/test/java/com/jiepaiqi/rhythm/WaterEventAggregatorTest.java`
- [ ] **步骤 1：编写失败的聚合测试**

创建 `WaterEventAggregatorTest.java`：

```java
package com.jiepaiqi.rhythm;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class WaterEventAggregatorTest {
    @Test
    void groupsAdjacentHighConfidenceWindowsIntoOneWaterEvent() {
        Instant t = Instant.parse("2026-07-05T08:00:00Z");
        List<AcousticFeatureWindow> windows = Arrays.asList(
            AcousticFeatureWindow.sample(t, 0.92),
            AcousticFeatureWindow.sample(t.plusSeconds(10), 0.88),
            AcousticFeatureWindow.sample(t.plusSeconds(20), 0.91)
        );

        List<WaterEvent> events = new WaterEventAggregator(0.80, 15).aggregate(windows);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getDurationSeconds()).isEqualTo(30);
        assertThat(events.get(0).getAverageConfidence()).isBetween(0.89, 0.91);
    }

    @Test
    void ignoresLowConfidenceNoise() {
        Instant t = Instant.parse("2026-07-05T08:00:00Z");
        List<AcousticFeatureWindow> windows = Arrays.asList(
            AcousticFeatureWindow.sample(t, 0.20),
            AcousticFeatureWindow.sample(t.plusSeconds(10), 0.30)
        );

        assertThat(new WaterEventAggregator(0.80, 15).aggregate(windows)).isEmpty();
    }
}
```

- [ ] **步骤 2：运行测试并验证失败**

运行：`cd backend && .\mvnw.cmd test -Dtest=WaterEventAggregatorTest`

预期：失败，因为节奏类不存在。

- [ ] **步骤 3：实现最小化节奏类**

创建 `AcousticFeatureWindow.java`、`WaterEvent.java` 和 `WaterEventAggregator.java`，包含不可变字段、getter 方法，以及以下行为：

```java
public List<WaterEvent> aggregate(List<AcousticFeatureWindow> windows) {
    // 按开始时间排序。
    // 保留 flowConfidence >= threshold 的窗口。
    // 当间隔 <= maxGapSeconds 时合并相邻窗口。
    // 返回包含 startedAt、endedAt、durationSeconds、averageConfidence 的 WaterEvent。
}
```

使用测试引用的精确方法签名：

```java
public static AcousticFeatureWindow sample(Instant startedAt, double confidence)
public List<WaterEvent> aggregate(List<AcousticFeatureWindow> windows)
public int getDurationSeconds()
public double getAverageConfidence()
```

- [ ] **步骤 4：运行节奏测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=WaterEventAggregatorTest`

预期：通过。

- [ ] **步骤 5：提交**

```bash
git add backend/src/main/java/com/jiepaiqi/rhythm backend/src/test/java/com/jiepaiqi/rhythm
git commit -m "feat: aggregate acoustic features into water events"
```

## 任务 4：告警规则引擎

**文件：**

- 创建：`backend/src/main/java/com/jiepaiqi/alert/AlertType.java`
- 创建：`backend/src/main/java/com/jiepaiqi/alert/AlertCandidate.java`
- 创建：`backend/src/main/java/com/jiepaiqi/alert/AlertRuleEngine.java`
- 创建：`backend/src/main/java/com/jiepaiqi/rhythm/RhythmBaseline.java`
- 测试：`backend/src/test/java/com/jiepaiqi/alert/AlertRuleEngineTest.java`
- [ ] **步骤 1：编写失败的告警规则测试**

创建 `AlertRuleEngineTest.java`：

```java
package com.jiepaiqi.alert;

import com.jiepaiqi.rhythm.RhythmBaseline;
import com.jiepaiqi.rhythm.WaterEvent;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class AlertRuleEngineTest {
    @Test
    void createsMorningAlertWhenNoWaterAfterMorningWindow() {
        RhythmBaseline baseline = RhythmBaseline.of(LocalTime.of(6, 30), LocalTime.of(9, 0), 5.0, 900.0);
        AlertRuleEngine engine = new AlertRuleEngine();

        assertThat(engine.evaluateMorning(
            Instant.parse("2026-07-05T02:00:00Z"), baseline, Collections.emptyList()
        )).extracting(AlertCandidate::getType).contains(AlertType.NO_MORNING_WATER);
    }

    @Test
    void createsLongFlowAlertForLongContinuousEvent() {
        WaterEvent event = WaterEvent.of(
            Instant.parse("2026-07-05T08:00:00Z"),
            Instant.parse("2026-07-05T08:25:00Z"),
            0.91
        );

        assertThat(new AlertRuleEngine().evaluateLongFlow(Arrays.asList(event), 20 * 60))
            .extracting(AlertCandidate::getType)
            .contains(AlertType.LONG_CONTINUOUS_FLOW);
    }

    @Test
    void deviceOfflineIsNotLifeRhythmAlert() {
        assertThat(new AlertRuleEngine().isLifeRhythmAlert(AlertType.DEVICE_OFFLINE)).isFalse();
    }
}
```

- [ ] **步骤 2：运行测试并验证失败**

运行：`cd backend && .\mvnw.cmd test -Dtest=AlertRuleEngineTest`

预期：失败，因为告警规则类不存在。

- [ ] **步骤 3：实现告警类**

定义：

```java
public enum AlertType {
    NO_MORNING_WATER,
    LONG_CONTINUOUS_FLOW,
    LOW_DAILY_ACTIVITY,
    DEVICE_OFFLINE
}
```

定义 `AlertCandidate`，包含字段 `type`、`reason`、`suggestedAction`、`occurredAt`。

实现 `AlertRuleEngine` 方法：

```java
public List<AlertCandidate> evaluateMorning(Instant now, RhythmBaseline baseline, List<WaterEvent> todayEvents)
public List<AlertCandidate> evaluateLongFlow(List<WaterEvent> events, int maxDurationSeconds)
public List<AlertCandidate> evaluateLowDailyActivity(List<WaterEvent> todayEvents, RhythmBaseline baseline)
public boolean isLifeRhythmAlert(AlertType type)
```

- [ ] **步骤 4：运行告警测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=AlertRuleEngineTest`

预期：通过。

- [ ] **步骤 5：提交**

```bash
git add backend/src/main/java/com/jiepaiqi/alert backend/src/main/java/com/jiepaiqi/rhythm backend/src/test/java/com/jiepaiqi/alert
git commit -m "feat: add explainable alert rule engine"
```

## 任务 5：音频元数据和存储服务

**文件：**

- 创建：`backend/src/main/java/com/jiepaiqi/audio/AudioClipMetadata.java`
- 创建：`backend/src/main/java/com/jiepaiqi/audio/AudioStorageProperties.java`
- 创建：`backend/src/main/java/com/jiepaiqi/audio/AudioStorageService.java`
- 创建：`backend/src/main/java/com/jiepaiqi/audio/LocalAudioStorageService.java`
- 测试：`backend/src/test/java/com/jiepaiqi/audio/LocalAudioStorageServiceTest.java`
- [ ] **步骤 1：编写失败的存储测试**

创建 `LocalAudioStorageServiceTest.java`：

```java
package com.jiepaiqi.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class LocalAudioStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesAudioClipUnderDeviceFolder() throws Exception {
        LocalAudioStorageService service = new LocalAudioStorageService(tempDir);

        AudioClipMetadata metadata = service.store(
            "device-001",
            "2026-07-05T08-00-00Z.wav",
            "audio/wav",
            new byte[] {1, 2, 3, 4}
        );

        assertThat(metadata.getStoragePath()).contains("device-001");
        assertThat(metadata.getContentType()).isEqualTo("audio/wav");
        assertThat(metadata.getSizeBytes()).isEqualTo(4);
        assertThat(tempDir.resolve(metadata.getStoragePath())).exists();
    }
}
```

- [ ] **步骤 2：运行测试并验证失败**

运行：`cd backend && .\mvnw.cmd test -Dtest=LocalAudioStorageServiceTest`

预期：失败，因为音频存储类不存在。

- [ ] **步骤 3：实现音频存储**

实现 `AudioStorageService`：

```java
/**
 * 音频片段存储服务。
 * 负责把设备上传的原始音频保存到本地存储，并提供复盘读取能力。
 */
public interface AudioStorageService {
    /** 保存单个设备采样窗口的原始音频片段。 */
    AudioClipMetadata store(String deviceSerial, String originalFileName, String contentType, byte[] bytes);

    /** 按存储路径读取音频内容，用于授权复盘播放。 */
    byte[] load(String storagePath);
}
```

实现 `LocalAudioStorageService`，将文件写入 `{storageRoot}/{deviceSerial}/{safeFileName}` 并拒绝空字节。

- [ ] **步骤 4：运行音频存储测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=LocalAudioStorageServiceTest`

预期：通过。

- [ ] **步骤 5：提交**

```bash
git add backend/src/main/java/com/jiepaiqi/audio backend/src/test/java/com/jiepaiqi/audio
git commit -m "feat: add local audio clip storage"
```

## 任务 6：数据注入和音频上传 API

**文件：**

- 创建：`backend/src/main/java/com/jiepaiqi/ingestion/FeatureIngestionController.java`
- 创建：`backend/src/main/java/com/jiepaiqi/audio/AudioClipController.java`
- 创建：`backend/src/main/java/com/jiepaiqi/ingestion/dto/` 下的 DTO 类
- 测试：`backend/src/test/java/com/jiepaiqi/ingestion/FeatureIngestionControllerTest.java`
- 测试：`backend/src/test/java/com/jiepaiqi/audio/AudioClipControllerTest.java`
- [ ] **步骤 1：编写失败的特征注入 API 测试**

创建一个 MockMvc 测试，向 `/api/devices/device-001/features` 发送 POST 请求，包含：

```json
{
  "features": [{
    "windowStartedAt": "2026-07-05T08:00:00Z",
    "windowEndedAt": "2026-07-05T08:00:10Z",
    "averageDecibels": 42.5,
    "peakDecibels": 61.2,
    "lowBandEnergy": 0.12,
    "midBandEnergy": 0.45,
    "highBandEnergy": 0.20,
    "flowConfidence": 0.93,
    "audioClipId": null
  }]
}
```

预期响应字段：`acceptedCount: 1`，`rejectedCount: 0`。

- [ ] **步骤 2：编写失败的音频上传 API 测试**

创建一个 MockMvc multipart 测试，测试 `POST /api/devices/device-001/audio-clips`，包含文件 `sample.wav`、`windowStartedAt` 和 `windowEndedAt`。

预期响应包含 `audioClipId`、`contentType` 和 `sizeBytes`。

- [ ] **步骤 3：运行 API 测试并验证失败**

运行：`cd backend && .\mvnw.cmd test -Dtest=FeatureIngestionControllerTest,AudioClipControllerTest`

预期：失败，因为控制器不存在。

- [ ] **步骤 4：实现控制器和 DTO**

实现：

```java
POST /api/devices/{deviceId}/features
POST /api/devices/{deviceId}/audio-clips
```

验证规则：

- `flowConfidence` 必须满足 `0.0 <= value <= 1.0`。
- `windowEndedAt` 必须在 `windowStartedAt` 之后。
- 音频内容类型必须是 `audio/wav`、`audio/mpeg` 或 `audio/mp4`。
- 音频大小必须大于 0 且小于配置的限制。
- [ ] **步骤 5：运行 API 测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=FeatureIngestionControllerTest,AudioClipControllerTest`

预期：通过。

- [ ] **步骤 6：提交**

```bash
git add backend/src/main/java/com/jiepaiqi/ingestion backend/src/main/java/com/jiepaiqi/audio backend/src/test/java/com/jiepaiqi
git commit -m "feat: add feature and audio ingestion APIs"
```

## 任务 7：看板和告警 API

**文件：**

- 创建：`backend/src/main/java/com/jiepaiqi/dashboard/DashboardController.java`
- 创建：`backend/src/main/java/com/jiepaiqi/dashboard/DashboardService.java`
- 创建：`backend/src/main/java/com/jiepaiqi/alert/AlertController.java`
- 测试：`backend/src/test/java/com/jiepaiqi/dashboard/DashboardControllerTest.java`
- 测试：`backend/src/test/java/com/jiepaiqi/alert/AlertControllerTest.java`
- [ ] **步骤 1：编写失败的看板 API 测试**

编写一个 MockMvc 测试，测试 `GET /api/elders/{elderId}/dashboard`。

预期 JSON：

```json
{
  "elderName": "Grandma Li",
  "todayStatus": "NORMAL",
  "lastWaterEventAt": "2026-07-05T08:12:00Z",
  "deviceStatus": "ONLINE",
  "openAlertCount": 0
}
```

- [ ] **步骤 2：编写失败的告警工作流测试**

测试：

```text
GET /api/alerts
GET /api/alerts/{alertId}
POST /api/alerts/{alertId}/acknowledge
POST /api/alerts/{alertId}/resolve
GET /api/alerts/{alertId}/audio-clips
GET /api/audio-clips/{audioClipId}/playback
```

预期：已绑定用户可以访问其老人的告警和播放链接；未绑定用户收到 403。

- [ ] **步骤 3：实现看板服务**

返回一个移动端友好的聚合 DTO：

```java
/**
 * 老人移动端看板聚合响应。
 * 面向手机端首屏，避免前端拼接多个底层接口。
 */
public class ElderDashboardResponse {
    /** 老人姓名。 */
    public String elderName;
    /** 今日节奏状态，例如 NORMAL 或 ATTENTION。 */
    public String todayStatus;
    /** 最近一次用水事件时间。 */
    public Instant lastWaterEventAt;
    /** 设备在线状态。 */
    public String deviceStatus;
    /** 当前未处理异常数量。 */
    public int openAlertCount;
    /** 今日用水节奏时间线。 */
    public List<TimelineItem> rhythmTimeline;
    /** 当前未处理异常摘要。 */
    public List<AlertSummary> openAlerts;
}
```

- [ ] **步骤 4：实现告警控制器**

实现确认和解决状态转换：

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> RESOLVED
```

重复解决告警时返回 HTTP 409。

- [ ] **步骤 5：运行后端 API 测试**

运行：`cd backend && .\mvnw.cmd test -Dtest=DashboardControllerTest,AlertControllerTest`

预期：通过。

- [ ] **步骤 6：提交**

```bash
git add backend/src/main/java/com/jiepaiqi/dashboard backend/src/main/java/com/jiepaiqi/alert backend/src/test/java/com/jiepaiqi
git commit -m "feat: add dashboard and alert APIs"
```

## 任务 8：模拟器

**文件：**

- 创建：`simulator/package.json`
- 创建：`simulator/tsconfig.json`
- 创建：`simulator/src/index.ts`
- 创建：`simulator/src/scenarios.ts`
- 创建：`simulator/src/audio.ts`
- [ ] **步骤 1：创建模拟器包**

使用脚本：

```json
{
  "scripts": {
    "dev": "tsx src/index.ts normal",
    "scenario:normal": "tsx src/index.ts normal",
    "scenario:no-morning-water": "tsx src/index.ts no-morning-water",
    "scenario:long-flow": "tsx src/index.ts long-flow",
    "scenario:low-activity": "tsx src/index.ts low-activity"
  },
  "dependencies": {
    "tsx": "^4.20.0"
  },
  "devDependencies": {
    "typescript": "^5.5.0"
  }
}
```

- [ ] **步骤 2：实现场景**

`scenarios.ts` 导出四个场景名称：

```ts
export type ScenarioName = "normal" | "no-morning-water" | "long-flow" | "low-activity";
```

每个场景生成特征窗口和合成的类似 wav 的字节数组。不要为模拟器测试录制真实音频。

- [ ] **步骤 3：实现 API 上报**

`index.ts` 应：

1. 上传音频片段到 `/api/devices/{deviceId}/audio-clips`；
2. 接收 `audioClipId`；
3. 使用该 `audioClipId` 向 `/api/devices/{deviceId}/features` 上报特征。

- [ ] **步骤 4：手动运行模拟器**

运行：`cd simulator && npm run scenario:normal`

预期：控制台打印已接受的音频和特征数量。

- [ ] **步骤 5：提交**

```bash
git add simulator
git commit -m "feat: add acoustic feature simulator"
```

## 任务 9：移动端优先的 Next.js 应用

**文件：**

- 创建：`frontend/package.json`
- 创建：`frontend/app/layout.tsx`
- 创建：`frontend/app/login/page.tsx`
- 创建：`frontend/app/dashboard/page.tsx`
- 创建：`frontend/app/elders/[id]/page.tsx`
- 创建：`frontend/app/alerts/page.tsx`
- 创建：`frontend/app/alerts/[id]/page.tsx`
- 创建：`frontend/app/devices/page.tsx`
- 创建：`frontend/components/StatusCard.tsx`
- 创建：`frontend/components/AudioReviewPlayer.tsx`
- 创建：`frontend/lib/api.ts`
- 创建：`frontend/app/globals.css`
- [ ] **步骤 1：创建前端包**

使用脚本：

```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "lint": "next lint",
    "test": "vitest run"
  },
  "dependencies": {
    "next": "^14.2.0",
    "react": "^18.3.0",
    "react-dom": "^18.3.0"
  },
  "devDependencies": {
    "typescript": "^5.5.0",
    "vitest": "^2.0.0",
    "@testing-library/react": "^15.0.0",
    "@testing-library/jest-dom": "^6.4.0"
  }
}
```

- [ ] **步骤 2：实现 API 客户端**

`frontend/lib/api.ts` 导出：

```ts
export async function getDashboard(elderId: string): Promise<ElderDashboard>
export async function getAlerts(): Promise<AlertSummary[]>
export async function getAlert(alertId: string): Promise<AlertDetail>
export async function acknowledgeAlert(alertId: string): Promise<void>
export async function resolveAlert(alertId: string, note: string): Promise<void>
```

- [ ] **步骤 3：实现移动端看板**

看板布局规则：

- 768px 以下单列布局；
- 不自动播放音频；
- 主要卡片：状态、设备、未处理告警、最近用水事件、基线对比；
- 主要操作在移动端有全屏点击区域。
- [ ] **步骤 4：实现告警详情音频复盘**

`AudioReviewPlayer.tsx` 接收：

```ts
// 异常详情页音频复盘播放器入参。
// 只展示后端授权返回的音频播放地址，不在前端拼接存储路径。
type AudioReviewPlayerProps = {
  clips: Array<{ id: string; label: string; playbackUrl: string }>;
};
```

为每个片段渲染一个 HTML `<audio controls>`。显示文本：`Audio is for authorized review only.`

- [ ] **步骤 5：运行前端验证**

运行：

```bash
cd frontend
npm run build
```

预期：Next.js 构建成功。

- [ ] **步骤 6：提交**

```bash
git add frontend
git commit -m "feat: add mobile-first dashboard"
```

## 任务 10：端到端验证

**文件：**

- 创建：`docs/runbook.md`
- 修改：`README.md`
- [ ] **步骤 1：添加运行手册**

记录命令：

```bash
docker compose up -d postgres
cd backend && .\mvnw.cmd spring-boot:run
cd frontend && npm run dev
cd simulator && npm run scenario:normal
```

- [ ] **步骤 2：验证正常场景**

运行模拟器正常场景。

预期：

- 后端接受音频和特征；
- 看板显示正常节奏；
- 不生成生活节奏告警。
- [ ] **步骤 3：验证无晨间用水场景**

运行模拟器 no-morning-water 场景。

预期：告警列表包含 `NO_MORNING_WATER`，告警详情包含关联的音频片段。

- [ ] **步骤 4：验证长流水场景**

运行模拟器 long-flow 场景。

预期：告警列表包含 `LONG_CONTINUOUS_FLOW`。

- [ ] **步骤 5：验证低活动场景**

运行模拟器 low-activity 场景。

预期：告警列表包含 `LOW_DAILY_ACTIVITY`。

- [ ] **步骤 6：提交**

```bash
git add README.md docs/runbook.md
git commit -m "docs: add MVP runbook"
```

## 自我审查清单

- 规范覆盖：原始音频留存、30天留存、移动端优先看板、可解释告警、模拟器场景、设备离线分离、PostgreSQL 和 Spring Boot 2.7.18 的需求均已被任务覆盖。
- 占位符扫描：搜索最终计划中的预留占位符短语，并删除任何要求实现者自行补充缺失细节的说明。
- 类型一致性：在后端、模拟器和前端中一致使用 `audioClipId`、`flowConfidence`、`windowStartedAt`、`windowEndedAt`、`AlertType` 和 `WaterEvent`。
- 执行注意：依赖安装需要网络；当 `mvnw` 或 `npm install` 需要下载时，请请求升级权限。

## 执行选项

计划保存在 `docs/superpowers/plans/2026-07-05-jiepaiqi-mvp-implementation.md`。选择一种执行模式：

1. 子代理驱动（推荐）：每个任务使用新的子代理，任务之间进行审查。
2. 内联执行：在当前会话中执行任务，设置检查点。

