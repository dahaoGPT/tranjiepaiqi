# jiepaiqi MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first end-to-end MVP of `jiepaiqi`: Spring Boot 2.7.18 backend, PostgreSQL persistence, raw audio retention for review, simulator ingestion, and a mobile-first Next.js dashboard.

**Architecture:** Use a frontend/backend split. The backend is one Spring Boot modular monolith with packages for identity, elder, device, ingestion, audio, rhythm, alert, and dashboard. The frontend is a Next.js app that consumes REST APIs and prioritizes phone use.

**Tech Stack:** Java 8, Spring Boot 2.7.18, Maven Wrapper, PostgreSQL, Flyway, MyBatis, JUnit 5, Next.js, TypeScript, npm, Playwright or React Testing Library for frontend checks.

---

## Source Specs

- `docs/superpowers/specs/2026-07-02-jiepaiqi-requirements.md`
- `docs/superpowers/specs/2026-07-02-jiepaiqi-design.md`
- `docs/superpowers/specs/2026-07-05-jiepaiqi-er-diagram.md`

## Target File Structure

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

## Commenting Standard

- SQL migration files must include Chinese comments for every business table and important business columns.
- Java classes must include a short Chinese class-level comment when the class represents a domain concept, API boundary, or service.
- Complex Java methods must include a short Chinese comment describing the business rule, especially rhythm aggregation, alert generation, audio storage, and authorization.
- TypeScript components and API types must include short Chinese comments for user-facing business concepts.
- Comments should explain domain intent, not restate obvious syntax.

## Task 1: Repository Bootstrap

**Files:**
- Create: `.gitignore`
- Create: `docker-compose.yml`
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/jiepaiqi/JiepaiqiApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `frontend/package.json`
- Create: `simulator/package.json`

- [ ] **Step 1: Initialize git if needed**

Run: `git status`
Expected: either existing status output or `fatal: not a git repository`.

If not a repository, run:

```bash
git init
```

Expected: initialized repository.

- [ ] **Step 2: Create ignore rules**

Create `.gitignore`:

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

- [ ] **Step 3: Add local PostgreSQL service**

Create `docker-compose.yml`:

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

- [ ] **Step 4: Create Spring Boot Maven project**

Create `backend/pom.xml`:

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

- [ ] **Step 5: Add application entrypoint and config**

Create `backend/src/main/java/com/jiepaiqi/JiepaiqiApplication.java`:

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

Create `backend/src/main/resources/application.yml`:

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

- [ ] **Step 6: Verify backend skeleton**

Run: `cd backend && .\mvnw.cmd test`

If Maven Wrapper does not exist yet, generate it during execution with Maven installed or create wrapper files before this step.

Expected: build succeeds with zero or one context-load test.

- [ ] **Step 7: Commit**

```bash
git add .gitignore docker-compose.yml backend frontend simulator
git commit -m "chore: bootstrap jiepaiqi workspace"
```

## Task 2: Database Migration and MyBatis Mapper Skeletons

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__initial_schema.sql`
- Create: plain Java model files under `identity`, `elder`, `device`, `ingestion`, `audio`, `rhythm`, `alert`
- Create: MyBatis mapper interfaces under each backend module package
- Create: MyBatis XML mapper files under `backend/src/main/resources/mapper/`
- Test: `backend/src/test/java/com/jiepaiqi/db/SchemaSmokeTest.java`

- [ ] **Step 1: Write schema smoke test**

Create `SchemaSmokeTest.java`:

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

- [ ] **Step 2: Run test and observe failure**

Run: `cd backend && .\mvnw.cmd test -Dtest=SchemaSmokeTest`

Expected: FAIL because database schema and/or test datasource is not configured.

- [ ] **Step 3: Add test datasource profile**

Create `backend/src/test/resources/application.yml`:

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

- [ ] **Step 4: Create initial Flyway schema**

Create `V1__initial_schema.sql` with tables:

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

- [ ] **Step 5: Append feature, event, baseline, alert tables**

Append to the migration:

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

- [ ] **Step 6: Run schema test**

Run: `cd backend && .\mvnw.cmd test -Dtest=SchemaSmokeTest`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/resources/db/migration backend/src/test/resources backend/src/test/java
git commit -m "feat: add initial database schema"
```

## Task 3: Rhythm Domain Logic

**Files:**
- Create: `backend/src/main/java/com/jiepaiqi/rhythm/AcousticFeatureWindow.java`
- Create: `backend/src/main/java/com/jiepaiqi/rhythm/WaterEvent.java`
- Create: `backend/src/main/java/com/jiepaiqi/rhythm/WaterEventAggregator.java`
- Test: `backend/src/test/java/com/jiepaiqi/rhythm/WaterEventAggregatorTest.java`

- [ ] **Step 1: Write failing aggregation tests**

Create `WaterEventAggregatorTest.java`:

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

- [ ] **Step 2: Run tests and verify failure**

Run: `cd backend && .\mvnw.cmd test -Dtest=WaterEventAggregatorTest`

Expected: FAIL because rhythm classes do not exist.

- [ ] **Step 3: Implement minimal rhythm classes**

Create `AcousticFeatureWindow.java`, `WaterEvent.java`, and `WaterEventAggregator.java` with immutable fields, getters, and this behavior:

```java
public List<WaterEvent> aggregate(List<AcousticFeatureWindow> windows) {
    // Sort by start time.
    // Keep windows where flowConfidence >= threshold.
    // Merge adjacent windows when the gap is <= maxGapSeconds.
    // Return WaterEvent with startedAt, endedAt, durationSeconds, averageConfidence.
}
```

Use the exact method signatures referenced by the tests:

```java
public static AcousticFeatureWindow sample(Instant startedAt, double confidence)
public List<WaterEvent> aggregate(List<AcousticFeatureWindow> windows)
public int getDurationSeconds()
public double getAverageConfidence()
```

- [ ] **Step 4: Run rhythm tests**

Run: `cd backend && .\mvnw.cmd test -Dtest=WaterEventAggregatorTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/jiepaiqi/rhythm backend/src/test/java/com/jiepaiqi/rhythm
git commit -m "feat: aggregate acoustic features into water events"
```

## Task 4: Alert Rule Engine

**Files:**
- Create: `backend/src/main/java/com/jiepaiqi/alert/AlertType.java`
- Create: `backend/src/main/java/com/jiepaiqi/alert/AlertCandidate.java`
- Create: `backend/src/main/java/com/jiepaiqi/alert/AlertRuleEngine.java`
- Create: `backend/src/main/java/com/jiepaiqi/rhythm/RhythmBaseline.java`
- Test: `backend/src/test/java/com/jiepaiqi/alert/AlertRuleEngineTest.java`

- [ ] **Step 1: Write failing alert rule tests**

Create `AlertRuleEngineTest.java`:

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

- [ ] **Step 2: Run tests and verify failure**

Run: `cd backend && .\mvnw.cmd test -Dtest=AlertRuleEngineTest`

Expected: FAIL because alert rule classes do not exist.

- [ ] **Step 3: Implement alert classes**

Define:

```java
public enum AlertType {
    NO_MORNING_WATER,
    LONG_CONTINUOUS_FLOW,
    LOW_DAILY_ACTIVITY,
    DEVICE_OFFLINE
}
```

Define `AlertCandidate` with fields `type`, `reason`, `suggestedAction`, `occurredAt`.

Implement `AlertRuleEngine` methods:

```java
public List<AlertCandidate> evaluateMorning(Instant now, RhythmBaseline baseline, List<WaterEvent> todayEvents)
public List<AlertCandidate> evaluateLongFlow(List<WaterEvent> events, int maxDurationSeconds)
public List<AlertCandidate> evaluateLowDailyActivity(List<WaterEvent> todayEvents, RhythmBaseline baseline)
public boolean isLifeRhythmAlert(AlertType type)
```

- [ ] **Step 4: Run alert tests**

Run: `cd backend && .\mvnw.cmd test -Dtest=AlertRuleEngineTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/jiepaiqi/alert backend/src/main/java/com/jiepaiqi/rhythm backend/src/test/java/com/jiepaiqi/alert
git commit -m "feat: add explainable alert rule engine"
```

## Task 5: Audio Metadata and Storage Service

**Files:**
- Create: `backend/src/main/java/com/jiepaiqi/audio/AudioClipMetadata.java`
- Create: `backend/src/main/java/com/jiepaiqi/audio/AudioStorageProperties.java`
- Create: `backend/src/main/java/com/jiepaiqi/audio/AudioStorageService.java`
- Create: `backend/src/main/java/com/jiepaiqi/audio/LocalAudioStorageService.java`
- Test: `backend/src/test/java/com/jiepaiqi/audio/LocalAudioStorageServiceTest.java`

- [ ] **Step 1: Write failing storage test**

Create `LocalAudioStorageServiceTest.java`:

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

- [ ] **Step 2: Run test and verify failure**

Run: `cd backend && .\mvnw.cmd test -Dtest=LocalAudioStorageServiceTest`

Expected: FAIL because audio storage classes do not exist.

- [ ] **Step 3: Implement audio storage**

Implement `AudioStorageService`:

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

Implement `LocalAudioStorageService` to write under `{storageRoot}/{deviceSerial}/{safeFileName}` and reject empty bytes.

- [ ] **Step 4: Run audio storage test**

Run: `cd backend && .\mvnw.cmd test -Dtest=LocalAudioStorageServiceTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/jiepaiqi/audio backend/src/test/java/com/jiepaiqi/audio
git commit -m "feat: add local audio clip storage"
```

## Task 6: Ingestion and Audio Upload APIs

**Files:**
- Create: `backend/src/main/java/com/jiepaiqi/ingestion/FeatureIngestionController.java`
- Create: `backend/src/main/java/com/jiepaiqi/audio/AudioClipController.java`
- Create: DTO classes in `backend/src/main/java/com/jiepaiqi/ingestion/dto/`
- Test: `backend/src/test/java/com/jiepaiqi/ingestion/FeatureIngestionControllerTest.java`
- Test: `backend/src/test/java/com/jiepaiqi/audio/AudioClipControllerTest.java`

- [ ] **Step 1: Write failing feature ingestion API test**

Create a MockMvc test that posts to `/api/devices/device-001/features` with:

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

Expected response fields: `acceptedCount: 1`, `rejectedCount: 0`.

- [ ] **Step 2: Write failing audio upload API test**

Create a MockMvc multipart test for `POST /api/devices/device-001/audio-clips` with file `sample.wav`, `windowStartedAt`, and `windowEndedAt`.

Expected response includes `audioClipId`, `contentType`, and `sizeBytes`.

- [ ] **Step 3: Run API tests and verify failure**

Run: `cd backend && .\mvnw.cmd test -Dtest=FeatureIngestionControllerTest,AudioClipControllerTest`

Expected: FAIL because controllers do not exist.

- [ ] **Step 4: Implement controllers and DTOs**

Implement:

```java
POST /api/devices/{deviceId}/features
POST /api/devices/{deviceId}/audio-clips
```

Validation rules:

- `flowConfidence` must be `0.0 <= value <= 1.0`.
- `windowEndedAt` must be after `windowStartedAt`.
- audio content type must be `audio/wav`, `audio/mpeg`, or `audio/mp4`.
- audio size must be greater than 0 and below configured limit.

- [ ] **Step 5: Run API tests**

Run: `cd backend && .\mvnw.cmd test -Dtest=FeatureIngestionControllerTest,AudioClipControllerTest`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/jiepaiqi/ingestion backend/src/main/java/com/jiepaiqi/audio backend/src/test/java/com/jiepaiqi
git commit -m "feat: add feature and audio ingestion APIs"
```

## Task 7: Dashboard and Alert APIs

**Files:**
- Create: `backend/src/main/java/com/jiepaiqi/dashboard/DashboardController.java`
- Create: `backend/src/main/java/com/jiepaiqi/dashboard/DashboardService.java`
- Create: `backend/src/main/java/com/jiepaiqi/alert/AlertController.java`
- Test: `backend/src/test/java/com/jiepaiqi/dashboard/DashboardControllerTest.java`
- Test: `backend/src/test/java/com/jiepaiqi/alert/AlertControllerTest.java`

- [ ] **Step 1: Write failing dashboard API test**

Write a MockMvc test for `GET /api/elders/{elderId}/dashboard`.

Expected JSON:

```json
{
  "elderName": "Grandma Li",
  "todayStatus": "NORMAL",
  "lastWaterEventAt": "2026-07-05T08:12:00Z",
  "deviceStatus": "ONLINE",
  "openAlertCount": 0
}
```

- [ ] **Step 2: Write failing alert workflow test**

Test:

```text
GET /api/alerts
GET /api/alerts/{alertId}
POST /api/alerts/{alertId}/acknowledge
POST /api/alerts/{alertId}/resolve
GET /api/alerts/{alertId}/audio-clips
GET /api/audio-clips/{audioClipId}/playback
```

Expected: bound users can access their elder alerts and playback links; unbound users receive 403.

- [ ] **Step 3: Implement dashboard service**

Return a mobile-friendly aggregate DTO:

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

- [ ] **Step 4: Implement alert controller**

Implement acknowledge and resolve as status transitions:

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> RESOLVED
```

Reject resolving an alert twice with HTTP 409.

- [ ] **Step 5: Run backend API tests**

Run: `cd backend && .\mvnw.cmd test -Dtest=DashboardControllerTest,AlertControllerTest`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/jiepaiqi/dashboard backend/src/main/java/com/jiepaiqi/alert backend/src/test/java/com/jiepaiqi
git commit -m "feat: add dashboard and alert APIs"
```

## Task 8: Simulator

**Files:**
- Create: `simulator/package.json`
- Create: `simulator/tsconfig.json`
- Create: `simulator/src/index.ts`
- Create: `simulator/src/scenarios.ts`
- Create: `simulator/src/audio.ts`

- [ ] **Step 1: Create simulator package**

Use scripts:

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

- [ ] **Step 2: Implement scenarios**

`scenarios.ts` exports four scenario names:

```ts
export type ScenarioName = "normal" | "no-morning-water" | "long-flow" | "low-activity";
```

Each scenario produces feature windows and small synthetic wav-like byte arrays. Do not record real audio for simulator tests.

- [ ] **Step 3: Implement API posting**

`index.ts` should:

1. upload audio clip to `/api/devices/{deviceId}/audio-clips`;
2. receive `audioClipId`;
3. post features to `/api/devices/{deviceId}/features` with that `audioClipId`.

- [ ] **Step 4: Run simulator manually**

Run: `cd simulator && npm run scenario:normal`

Expected: console prints accepted audio and feature counts.

- [ ] **Step 5: Commit**

```bash
git add simulator
git commit -m "feat: add acoustic feature simulator"
```

## Task 9: Mobile-First Next.js App

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/app/layout.tsx`
- Create: `frontend/app/login/page.tsx`
- Create: `frontend/app/dashboard/page.tsx`
- Create: `frontend/app/elders/[id]/page.tsx`
- Create: `frontend/app/alerts/page.tsx`
- Create: `frontend/app/alerts/[id]/page.tsx`
- Create: `frontend/app/devices/page.tsx`
- Create: `frontend/components/StatusCard.tsx`
- Create: `frontend/components/AudioReviewPlayer.tsx`
- Create: `frontend/lib/api.ts`
- Create: `frontend/app/globals.css`

- [ ] **Step 1: Create frontend package**

Use scripts:

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

- [ ] **Step 2: Implement API client**

`frontend/lib/api.ts` exports:

```ts
export async function getDashboard(elderId: string): Promise<ElderDashboard>
export async function getAlerts(): Promise<AlertSummary[]>
export async function getAlert(alertId: string): Promise<AlertDetail>
export async function acknowledgeAlert(alertId: string): Promise<void>
export async function resolveAlert(alertId: string, note: string): Promise<void>
```

- [ ] **Step 3: Implement mobile dashboard**

Dashboard layout rules:

- single column below 768px;
- no autoplay audio;
- main cards: status, device, open alerts, last water event, baseline comparison;
- primary actions have full-width tap targets on mobile.

- [ ] **Step 4: Implement alert detail audio review**

`AudioReviewPlayer.tsx` receives:

```ts
// 异常详情页音频复盘播放器入参。
// 只展示后端授权返回的音频播放地址，不在前端拼接存储路径。
type AudioReviewPlayerProps = {
  clips: Array<{ id: string; label: string; playbackUrl: string }>;
};
```

Render one HTML `<audio controls>` per clip. Show text: `Audio is for authorized review only.`

- [ ] **Step 5: Run frontend verification**

Run:

```bash
cd frontend
npm run build
```

Expected: Next.js build succeeds.

- [ ] **Step 6: Commit**

```bash
git add frontend
git commit -m "feat: add mobile-first dashboard"
```

## Task 10: End-to-End Verification

**Files:**
- Create: `docs/runbook.md`
- Modify: `README.md`

- [ ] **Step 1: Add runbook**

Document commands:

```bash
docker compose up -d postgres
cd backend && .\mvnw.cmd spring-boot:run
cd frontend && npm run dev
cd simulator && npm run scenario:normal
```

- [ ] **Step 2: Verify normal scenario**

Run simulator normal scenario.

Expected:

- backend accepts audio and features;
- dashboard shows normal rhythm;
- no life-rhythm alert is generated.

- [ ] **Step 3: Verify no-morning-water scenario**

Run simulator no-morning-water scenario.

Expected: alert list contains `NO_MORNING_WATER` and alert detail has associated audio clips.

- [ ] **Step 4: Verify long-flow scenario**

Run simulator long-flow scenario.

Expected: alert list contains `LONG_CONTINUOUS_FLOW`.

- [ ] **Step 5: Verify low-activity scenario**

Run simulator low-activity scenario.

Expected: alert list contains `LOW_DAILY_ACTIVITY`.

- [ ] **Step 6: Commit**

```bash
git add README.md docs/runbook.md
git commit -m "docs: add MVP runbook"
```

## Self-Review Checklist

- Spec coverage: requirements for raw audio retention, 30-day retention, mobile-first dashboard, explainable alerts, simulator scenarios, device offline separation, PostgreSQL, and Spring Boot 2.7.18 are covered by tasks.
- Placeholder scan: search final plan for reserved placeholder phrases and remove any instruction that asks the implementer to invent missing details.
- Type consistency: use `audioClipId`, `flowConfidence`, `windowStartedAt`, `windowEndedAt`, `AlertType`, and `WaterEvent` consistently across backend, simulator, and frontend.
- Execution caution: dependency installation requires network; request escalation when `mvnw` or `npm install` needs downloads.

## Execution Options

Plan is saved at `docs/superpowers/plans/2026-07-05-jiepaiqi-mvp-implementation.md`. Choose one execution mode:

1. Subagent-Driven (recommended): fresh subagent per task, review between tasks.
2. Inline Execution: execute tasks in this session with checkpoints.

