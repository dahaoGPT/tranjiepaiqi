export type ScenarioName = "normal" | "no-morning-water" | "long-flow" | "low-activity";

export interface FeatureWindow {
  windowStartedAt: string;
  windowEndedAt: string;
  averageDecibels: number;
  peakDecibels: number;
  lowBandEnergy: number;
  midBandEnergy: number;
  highBandEnergy: number;
  flowConfidence: number;
}

export interface AudioClipInput {
  fileName: string;
  windowStartedAt: string;
  windowEndedAt: string;
}

export interface Capture {
  audioClip: AudioClipInput;
  features: FeatureWindow[];
}

export interface Scenario {
  name: ScenarioName;
  captures: Capture[];
  standaloneFeatures?: FeatureWindow[];
}

function createWindow(
  scenarioDate: Date,
  hour: number,
  minute: number,
  seconds: number,
  confidence: number
): FeatureWindow {
  const start = new Date(scenarioDate);
  start.setHours(hour, minute, seconds, 0);
  const end = new Date(start.getTime() + 10000);

  return {
    windowStartedAt: start.toISOString(),
    windowEndedAt: end.toISOString(),
    averageDecibels: 40 + Math.random() * 20,
    peakDecibels: 55 + Math.random() * 15,
    lowBandEnergy: 0.1 + Math.random() * 0.2,
    midBandEnergy: 0.3 + Math.random() * 0.3,
    highBandEnergy: 0.15 + Math.random() * 0.15,
    flowConfidence: confidence
  };
}

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

export function getScenario(name: ScenarioName): Scenario {
  const scenarioDate = new Date();

  switch (name) {
    case "normal": {
      const morningFeatures = [
        ...Array.from({ length: 15 }, (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.9)),
        ...Array.from({ length: 10 }, (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.85))
      ];
      const lunchFeatures = Array.from(
        { length: 20 },
        (_, i) => createWindow(scenarioDate, 12, 0, i * 10, 0.88)
      );
      const dinnerFeatures = Array.from(
        { length: 15 },
        (_, i) => createWindow(scenarioDate, 18, 0, i * 10, 0.92)
      );
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

    case "no-morning-water": {
      const morningFeatures = [
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 7, 0, i * 10, 0.1)),
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.05)),
        ...Array.from({ length: 5 }, (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.08))
      ];
      const lunchFeatures = Array.from(
        { length: 20 },
        (_, i) => createWindow(scenarioDate, 12, 0, i * 10, 0.88)
      );
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

    case "long-flow": {
      const morningFeatures = Array.from(
        { length: 15 },
        (_, i) => createWindow(scenarioDate, 7, 30, i * 10, 0.9)
      );
      const longFlowFeatures = Array.from(
        { length: 180 },
        (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.95)
      );
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

    case "low-activity": {
      const morningFeatures = Array.from(
        { length: 5 },
        (_, i) => createWindow(scenarioDate, 8, 0, i * 10, 0.85)
      );
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
