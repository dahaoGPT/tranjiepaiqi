import { AudioClipInput, FeatureWindow, Scenario } from "./scenarios";

export interface ScenarioUploadGateway {
  uploadAudioClip(audioClip: AudioClipInput): Promise<string | undefined>;
  uploadFeatures(features: FeatureWindow[], audioClipId?: string): Promise<unknown>;
}

export interface ScenarioExecutionOptions {
  batchSize?: number;
  batchDelayMs?: number;
}

async function uploadFeatureBatches(
  features: FeatureWindow[],
  audioClipId: string | undefined,
  gateway: ScenarioUploadGateway,
  batchSize: number,
  batchDelayMs: number
): Promise<void> {
  for (let i = 0; i < features.length; i += batchSize) {
    await gateway.uploadFeatures(features.slice(i, i + batchSize), audioClipId);
    if (batchDelayMs > 0) {
      await new Promise(resolve => setTimeout(resolve, batchDelayMs));
    }
  }
}

export async function executeScenario(
  scenario: Scenario,
  gateway: ScenarioUploadGateway,
  options: ScenarioExecutionOptions = {}
): Promise<void> {
  const batchSize = options.batchSize ?? 50;
  const batchDelayMs = options.batchDelayMs ?? 100;

  if (!Number.isInteger(batchSize) || batchSize <= 0 || batchSize > 50) {
    throw new Error("batchSize must be between 1 and 50");
  }

  for (const capture of scenario.captures) {
    const clipId = await gateway.uploadAudioClip(capture.audioClip);
    if (!clipId) {
      throw new Error(`Audio upload failed: ${capture.audioClip.fileName}`);
    }
    await uploadFeatureBatches(capture.features, clipId, gateway, batchSize, batchDelayMs);
  }

  await uploadFeatureBatches(
    scenario.standaloneFeatures ?? [],
    undefined,
    gateway,
    batchSize,
    batchDelayMs
  );
}
