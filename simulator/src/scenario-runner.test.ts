import test from "node:test";
import assert from "node:assert/strict";
import { executeScenario, ScenarioUploadGateway } from "./scenario-runner";
import { FeatureWindow, Scenario } from "./scenarios";

/**
 * 创建特征窗口辅助函数。
 * @param second 秒数（相对于 2026-07-11 08:00:00 UTC）
 * @returns 特征窗口对象
 */
function feature(second: number): FeatureWindow {
  const start = new Date(Date.UTC(2026, 6, 11, 8, 0, second));
  return {
    windowStartedAt: start.toISOString(),
    windowEndedAt: new Date(start.getTime() + 10000).toISOString(), // 窗口持续10秒
    averageDecibels: 50,
    peakDecibels: 60,
    lowBandEnergy: 0.2,
    midBandEnergy: 0.4,
    highBandEnergy: 0.2,
    flowConfidence: 0.9
  };
}

/**
 * 创建测试场景辅助函数。
 * 包含两个采集，共3个特征窗口。
 * @returns 场景对象
 */
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

/**
 * 测试：每个采集的特征使用其自己的音频片段ID上传。
 */
test("每个采集的特征使用其自己的音频片段ID上传", async () => {
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

/**
 * 测试：独立特征不关联音频片段ID上传。
 */
test("独立特征不关联音频片段ID上传", async () => {
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

/**
 * 测试：当音频上传失败时，停止上传该采集的特征。
 */
test("音频上传失败时停止上传该采集的特征", async () => {
  let featureUploadCount = 0;
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async () => "", // 返回空字符串表示上传失败
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

/**
 * 测试：特征上传按批次拆分，每批最多50个。
 */
test("特征上传按批次拆分，每批最多50个", async () => {
  const input = scenario();
  // 创建101个特征窗口，验证分批上传
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

/**
 * 测试：拒绝配置的批次大小超过50。
 */
test("拒绝配置的批次大小超过50", async () => {
  const gateway: ScenarioUploadGateway = {
    uploadAudioClip: async () => "clip-id",
    uploadFeatures: async () => undefined
  };

  await assert.rejects(
    executeScenario(scenario(), gateway, { batchSize: 51, batchDelayMs: 0 }),
    /batchSize must be between 1 and 50/
  );
});
