/**
 * 场景名称类型。
 * 支持的场景：
 * - normal: 正常用水节奏
 * - no-morning-water: 晨间无用水
 * - long-flow: 长时间流水
 * - low-activity: 低活动量
 */
export type ScenarioName = "normal" | "no-morning-water" | "long-flow" | "low-activity";

/**
 * 特征窗口接口。
 * 表示设备在一个时间窗口内采集的声学特征。
 */
export interface FeatureWindow {
  /** 窗口开始时间 */
  windowStartedAt: string;
  /** 窗口结束时间 */
  windowEndedAt: string;
  /** 平均分贝值 */
  averageDecibels: number;
  /** 峰值分贝值 */
  peakDecibels: number;
  /** 低频带能量 */
  lowBandEnergy: number;
  /** 中频带能量 */
  midBandEnergy: number;
  /** 高频带能量 */
  highBandEnergy: number;
  /** 流水置信度（0-1） */
  flowConfidence: number;
}

/**
 * 音频片段输入接口。
 */
export interface AudioClipInput {
  /** 文件名 */
  fileName: string;
  /** 窗口开始时间 */
  windowStartedAt: string;
  /** 窗口结束时间 */
  windowEndedAt: string;
}

/**
 * 采集接口。
 * 包含一个音频片段和关联的特征窗口列表。
 */
export interface Capture {
  /** 音频片段信息 */
  audioClip: AudioClipInput;
  /** 关联的特征窗口列表 */
  features: FeatureWindow[];
}

/**
 * 场景接口。
 * 定义一个完整的模拟场景，包含多个采集和独立特征。
 */
export interface Scenario {
  /** 场景名称 */
  name: ScenarioName;
  /** 采集列表（音频+特征） */
  captures: Capture[];
  /** 独立特征列表（不关联音频） */
  standaloneFeatures?: FeatureWindow[];
}

/**
 * 创建特征窗口。
 * @param scenarioDate 场景日期
 * @param hour 小时
 * @param minute 分钟
 * @param seconds 秒
 * @param confidence 流水置信度
 * @returns 特征窗口对象
 */
function createWindow(
  scenarioDate: Date,
  hour: number,
  minute: number,
  seconds: number,
  confidence: number
): FeatureWindow {
  const start = new Date(scenarioDate);
  start.setHours(hour, minute, seconds, 0);
  const end = new Date(start.getTime() + 10000); // 窗口持续10秒

  return {
    windowStartedAt: start.toISOString(),
    windowEndedAt: end.toISOString(),
    averageDecibels: 40 + Math.random() * 20, // 40-60分贝
    peakDecibels: 55 + Math.random() * 15, // 55-70分贝
    lowBandEnergy: 0.1 + Math.random() * 0.2, // 0.1-0.3
    midBandEnergy: 0.3 + Math.random() * 0.3, // 0.3-0.6
    highBandEnergy: 0.15 + Math.random() * 0.15, // 0.15-0.3
    flowConfidence: confidence
  };
}

/**
 * 创建采集对象。
 * @param fileName 文件名
 * @param features 特征窗口列表
 * @returns 采集对象
 */
function createCapture(fileName: string, features: FeatureWindow[]): Capture {
  if (features.length === 0) {
    throw new Error(`Capture ${fileName} must contain at least one feature`);
  }

  return {
    audioClip: {
      fileName,
      windowStartedAt: features[0].windowStartedAt,
      windowEndedAt: features[features.length - 1].windowEndedAt
    },
    features
  };
}

/**
 * 获取指定名称的场景配置。
 * @param name 场景名称
 * @returns 场景配置
 */
export function getScenario(name: ScenarioName): Scenario {
  const scenarioDate = new Date();

  switch (name) {
    // 正常用水节奏场景
    case "normal": {
      // 晨间用水（7:30-8:10）
      const morningFeatures = [
        ...Array.from({ length: 15 }, (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.9)),
        ...Array.from({ length: 10 }, (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.85))
      ];
      // 午餐用水（12:00-12:20）
      const lunchFeatures = Array.from(
        { length: 20 },
        (_, i) => createWindow(scenarioDate, 12, 0, i * 10, 0.88)
      );
      // 晚餐用水（18:00-18:15）
      const dinnerFeatures = Array.from(
        { length: 15 },
        (_, i) => createWindow(scenarioDate, 18, 0, i * 10, 0.92)
      );
      // 晚间低置信度特征（20:00-20:01）
      const standaloneFeatures = [
        ...Array.from({ length: 8 }, (_, i) => createWindow(scenarioDate, 20, 0, i * 10, 0.8)),
        createWindow(scenarioDate, 20, 1, 0, 0.2),
        createWindow(scenarioDate, 20, 1, 10, 0.15)
      ];

      return {
        name,
        captures: [
          createCapture("morning.wav", morningFeatures),
          createCapture("lunch.wav", lunchFeatures),
          createCapture("dinner.wav", dinnerFeatures)
        ],
        standaloneFeatures
      };
    }

    // 晨间无用水场景（触发 NO_MORNING_WATER 异常）
    case "no-morning-water": {
      // 晨间低置信度特征（7:00-8:05）
      const morningFeatures = [
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 7, 0, i * 10, 0.1)),
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.05)),
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.08))
      ];
      // 午餐用水（12:00-12:20）
      const lunchFeatures = Array.from(
        { length: 20 },
        (_, i) => createWindow(scenarioDate, 12, 0, i * 10, 0.88)
      );
      // 晚间用水（18:00-18:15）
      const eveningFeatures = Array.from(
        { length: 15 },
        (_, i) => createWindow(scenarioDate, 18, 0, i * 10, 0.92)
      );

      return {
        name,
        captures: [createCapture("lunch.wav", lunchFeatures)],
        standaloneFeatures: [...morningFeatures, ...eveningFeatures]
      };
    }

    // 长时间流水场景（触发 LONG_CONTINUOUS_FLOW 异常）
    case "long-flow": {
      // 晨间用水（7:30-7:45）
      const morningFeatures = Array.from(
        { length: 15 },
        (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.9)
      );
      // 长时间流水（8:00-11:00，180个窗口，共30分钟）
      const longFlowFeatures = Array.from(
        { length: 180 },
        (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.95)
      );
      // 独立特征（11:00-11:10）
      const standaloneFeatures = Array.from(
        { length: 10 },
        (_, i) => createWindow(scenarioDate, 11, 0, i * 10, 0.85)
      );

      return {
        name,
        captures: [
          createCapture("morning.wav", morningFeatures),
          createCapture("long-flow.wav", longFlowFeatures)
        ],
        standaloneFeatures
      };
    }

    // 低活动量场景（触发 LOW_DAILY_ACTIVITY 异常）
    case "low-activity": {
      // 少量晨间用水（8:00-8:05）
      const morningFeatures = Array.from(
        { length: 5 },
        (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.85)
      );
      // 极少量独立特征（12:00-12:02）
      const standaloneFeatures = [
        createWindow(scenarioDate, 12, 0, 0, 0.7),
        createWindow(scenarioDate, 12, 0, 10, 0.65)
      ];

      return {
        name,
        captures: [createCapture("minimal.wav", morningFeatures)],
        standaloneFeatures
      };
    }

    default:
      throw new Error(`Unknown scenario: ${name}`);
  }
}
