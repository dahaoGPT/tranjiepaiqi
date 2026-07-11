import test from "node:test";
import assert from "node:assert/strict";
import { executeScenario, ScenarioUploadGateway } from "./scenario-runner";
import { FeatureWindow, Scenario } from "./scenarios";

function feature(second: number): FeatureWindow {
  const start = new Date(Date.UTC(2026, 6, 11, 8, 0, second));
  return {
    windowStartedAt: start.toISOString(),
    windowEndedAt: new Date(start.getTime() + 10000).toISOString(),
    averageDecibels: 50,
    peakDecibels: 60,
    lowBandEnergy: 0.2,
    midBandEnergy: 0.4,
    highBandEnergy: 0.2,
    flowConfidence: 0.9
  };
}

function scenario(): Scenario {
  const first = [feature(0), feature(10)];
  const second = [feature(20)];
  return {
    name: "normal",
    captures: [
      {
        audioClip: {
          fileName: "first.wav",
          windowStartedAt: first[0].windowStartedAt,
          windowEndedAt: first[1].windowEndedAt
        },
        features: first
      },
      {
        audioClip: {
          fileName: "second.wav",
          windowStartedAt: second[0].windowStartedAt,
          windowEndedAt: second[0].windowEndedAt
        },
        features: second
      }
    ]
  };
}

test("uploads each capture's features once with its own clip ID", async () => {
  const uploaded: Array<{ seconds: number[]; clipId?: string }> = [];
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async clip => `id-${clip.fileName}`,
    uploadFeatures: async (features, clipId) => {
      uploaded.push({
        seconds: features.map(item => new Date(item.windowStartedAt).getUTCSeconds()),
        clipId
      });
    }
  };

  await executeScenario(scenario(), gateway, { batchDelayMs: 0 });

  assert.deepEqual(uploaded, [
    { seconds: [0, 10], clipId: "id-first.wav" },
    { seconds: [20], clipId: "id-second.wav" }
  ]);
});

test("uploads standalone features without a clip ID", async () => {
  const input = scenario();
  input.standaloneFeatures = [feature(30)];
  const clipIds: Array<string | undefined> = [];
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async clip => `id-${clip.fileName}`,
    uploadFeatures: async (_features, clipId) => {
      clipIds.push(clipId);
    }
  };

  await executeScenario(input, gateway, { batchDelayMs: 0 });

  assert.deepEqual(clipIds, ["id-first.wav", "id-second.wav", undefined]);
});

test("stops before uploading capture features when its audio upload fails", async () => {
  let featureUploadCount = 0;
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async () => "",
    uploadFeatures: async () => {
      featureUploadCount++;
    }
  };

  await assert.rejects(
    executeScenario(scenario(), gateway, { batchDelayMs: 0 }),
    /Audio upload failed: first\.wav/
  );
  assert.equal(featureUploadCount, 0);
});

test("splits feature uploads into batches of at most 50", async () => {
  const input = scenario();
  const features = Array.from({ length: 101 }, (_, i) => feature(i * 10));
  input.captures = [{
    audioClip: {
      fileName: "large.wav",
      windowStartedAt: features[0].windowStartedAt,
      windowEndedAt: features[features.length - 1].windowEndedAt
    },
    features
  }];
  const batchSizes: number[] = [];
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async () => "large-id",
    uploadFeatures: async batch => {
      batchSizes.push(batch.length);
    }
  };

  await executeScenario(input, gateway, { batchDelayMs: 0 });

  assert.deepEqual(batchSizes, [50, 50, 1]);
});

test("rejects a configured batch size greater than 50", async () => {
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async () => "clip-id",
    uploadFeatures: async () => undefined
  };

  await assert.rejects(
    executeScenario(scenario(), gateway, { batchSize: 51, batchDelayMs: 0 }),
    /batchSize must be between 1 and 50/
  );
});
