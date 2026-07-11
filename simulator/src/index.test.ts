import test from "node:test";
import assert from "node:assert/strict";
import { getScenario, ScenarioName } from "./scenarios";

const expectations: Array<{ name: ScenarioName; featureCount: number; clipCount: number }> = [
  { name: "normal", featureCount: 70, clipCount: 3 },
  { name: "no-morning-water", featureCount: 50, clipCount: 1 },
  { name: "long-flow", featureCount: 205, clipCount: 2 },
  { name: "low-activity", featureCount: 7, clipCount: 1 }
];

for (const expectation of expectations) {
  test(`${expectation.name} assigns every feature to at most one capture`, () => {
    const scenario = getScenario(expectation.name);

    assert.equal(scenario.captures.length, expectation.clipCount);
    assert.ok(!("features" in scenario));
    assert.ok(!("audioClips" in scenario));

    const associatedFeatures = scenario.captures.flatMap(capture => capture.features);
    const allFeatures = [...associatedFeatures, ...(scenario.standaloneFeatures ?? [])];
    assert.equal(allFeatures.length, expectation.featureCount);
    assert.equal(new Set(allFeatures).size, expectation.featureCount);

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
