export type ScenarioName = "normal" | "no-morning-water" | "long-flow" | "low-activity";

interface FeatureWindow {
  windowStartedAt: string;
  windowEndedAt: string;
  averageDecibels: number;
  peakDecibels: number;
  lowBandEnergy: number;
  midBandEnergy: number;
  highBandEnergy: number;
  flowConfidence: number;
}

export interface Scenario {
  name: ScenarioName;
  features: FeatureWindow[];
  audioClips: Array<{ fileName: string; windowStartedAt: string; windowEndedAt: string }>;
}

function createWindow(hour: number, minute: number, seconds: number, confidence: number): FeatureWindow {
  const start = new Date();
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

export function getScenario(name: ScenarioName): Scenario {
  switch (name) {
    case "normal":
      return {
        name: "normal",
        features: [
          ...Array.from({ length: 15 }, (_, i) => createWindow(7, 30, i * 10, 0.9)),
          ...Array.from({ length: 10 }, (_, i) => createWindow(8, 0, i * 10, 0.85)),
          ...Array.from({ length: 20 }, (_, i) => createWindow(12, 0, i * 10, 0.88)),
          ...Array.from({ length: 15 }, (_, i) => createWindow(18, 0, i * 10, 0.92)),
          ...Array.from({ length: 8 }, (_, i) => createWindow(20, 0, i * 10, 0.8)),
          createWindow(20, 1, 0, 0.2),
          createWindow(20, 1, 10, 0.15)
        ],
        audioClips: [
          { fileName: "morning.wav", windowStartedAt: new Date(Date.now() - 8 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 7 * 3600000).toISOString() },
          { fileName: "lunch.wav", windowStartedAt: new Date(Date.now() - 4 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 3 * 3600000).toISOString() },
          { fileName: "dinner.wav", windowStartedAt: new Date(Date.now() - 1 * 3600000).toISOString(), windowEndedAt: new Date(Date.now()).toISOString() }
        ]
      };

    case "no-morning-water":
      return {
        name: "no-morning-water",
        features: [
          ...Array.from({ length: 5 }, (_, i) => createWindow(7, 0, i * 10, 0.1)),
          ...Array.from({ length: 5 }, (_, i) => createWindow(7, 30, i * 10, 0.05)),
          ...Array.from({ length: 5 }, (_, i) => createWindow(8, 0, i * 10, 0.08)),
          ...Array.from({ length: 20 }, (_, i) => createWindow(12, 0, i * 10, 0.88)),
          ...Array.from({ length: 15 }, (_, i) => createWindow(18, 0, i * 10, 0.92))
        ],
        audioClips: [
          { fileName: "lunch.wav", windowStartedAt: new Date(Date.now() - 4 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 3 * 3600000).toISOString() }
        ]
      };

    case "long-flow":
      return {
        name: "long-flow",
        features: [
          ...Array.from({ length: 15 }, (_, i) => createWindow(7, 30, i * 10, 0.9)),
          ...Array.from({ length: 180 }, (_, i) => createWindow(8, 0, i * 10, 0.95)),
          ...Array.from({ length: 10 }, (_, i) => createWindow(11, 0, i * 10, 0.85))
        ],
        audioClips: [
          { fileName: "morning.wav", windowStartedAt: new Date(Date.now() - 8 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 7 * 3600000).toISOString() },
          { fileName: "long-flow.wav", windowStartedAt: new Date(Date.now() - 5 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 2 * 3600000).toISOString() }
        ]
      };

    case "low-activity":
      return {
        name: "low-activity",
        features: [
          ...Array.from({ length: 5 }, (_, i) => createWindow(8, 0, i * 10, 0.85)),
          createWindow(12, 0, 0, 0.7),
          createWindow(12, 0, 10, 0.65)
        ],
        audioClips: [
          { fileName: "minimal.wav", windowStartedAt: new Date(Date.now() - 6 * 3600000).toISOString(), windowEndedAt: new Date(Date.now() - 5 * 3600000).toISOString() }
        ]
      };

    default:
      throw new Error(`Unknown scenario: ${name}`);
  }
}