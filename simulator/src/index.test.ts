import test from "node:test";
import assert from "node:assert/strict";
import { getScenario, ScenarioName } from "./scenarios";

/**
 * 场景配置预期值。
 * 定义每个场景应该生成的特征数量和音频片段数量。
 */
const expectations: Array<{ name: ScenarioName; featureCount: number; clipCount: number }> = [
  { name: "normal", featureCount: 70, clipCount: 3 },
  { name: "no-morning-water", featureCount: 50, clipCount: 1 },
  { name: "long-flow", featureCount: 205, clipCount: 2 },
  { name: "low-activity", featureCount: 7, clipCount: 1 }
];

/**
 * 测试所有场景配置的正确性。
 * 验证：
 * 1. 音频片段数量符合预期
 * 2. 特征总数符合预期且无重复
 * 3. 每个特征的时间窗口都在关联音频的时间范围内
 */
for (const expectation of expectations) {
  test(`${expectation.name} 场景：每个特征最多分配给一个采集`, () => {
    // 获取场景配置
    const scenario = getScenario(expectation.name);

    // 验证音频片段数量
    assert.equal(scenario.captures.length, expectation.clipCount);
    // 验证场景对象结构正确
    assert.ok(!("features" in scenario));
    assert.ok(!("audioClips" in scenario));

    // 计算所有特征（关联特征 + 独立特征）
    const associatedFeatures = scenario.captures.flatMap(capture => capture.features);
    const allFeatures = [...associatedFeatures, ...(scenario.standaloneFeatures ?? [])];
    // 验证特征总数
    assert.equal(allFeatures.length, expectation.featureCount);
    // 验证无重复特征
    assert.equal(new Set(allFeatures).size, expectation.featureCount);

    // 验证每个特征的时间窗口在关联音频的时间范围内
    for (const capture of scenario.captures) {
      const clipStart = Date.parse(capture.audioClip.windowStartedAt);
      const clipEnd = Date.parse(capture.audioClip.windowEndedAt);
      for (const feature of capture.features) {
        assert.ok(Date.parse(feature.windowStartedAt) >= clipStart);
        assert.ok(Date.parse(feature.windowEndedAt) <= clipEnd);
      }
    }
  });
}
