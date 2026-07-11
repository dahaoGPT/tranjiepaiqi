import { AudioClipInput, FeatureWindow, Scenario } from "./scenarios";

/**
 * 场景上传网关接口。
 * 定义音频片段和特征数据的上传方法。
 */
export interface ScenarioUploadGateway {
  /**
   * 上传音频片段。
   * @param audioClip 音频片段输入
   * @returns 音频片段ID，上传失败返回 undefined
   */
  uploadAudioClip(audioClip: AudioClipInput): Promise<string | undefined>;

  /**
   * 上传特征数据。
   * @param features 特征窗口列表
   * @param audioClipId 关联的音频片段ID（可选）
   * @returns 上传结果
   */
  uploadFeatures(features: FeatureWindow[], audioClipId?: string): Promise<unknown>;
}

/**
 * 场景执行选项接口。
 */
export interface ScenarioExecutionOptions {
  /** 每批上传的特征数量，默认50 */
  batchSize?: number;
  /** 批次之间的延迟（毫秒），默认100 */
  batchDelayMs?: number;
}

/**
 * 分批上传特征数据。
 * @param features 特征窗口列表
 * @param audioClipId 关联的音频片段ID
 * @param gateway 上传网关
 * @param batchSize 每批大小
 * @param batchDelayMs 批次间隔延迟
 */
async function uploadFeatureBatches(
  features: FeatureWindow[],
  audioClipId: string | undefined,
  gateway: ScenarioUploadGateway,
  batchSize: number,
  batchDelayMs: number
): Promise<void> {
  for (let i = 0; i < features.length; i += batchSize) {
    await gateway.uploadFeatures(features.slice(i, i + batchSize), audioClipId);
    // 如果设置了延迟，在批次之间等待
    if (batchDelayMs > 0) {
      await new Promise(resolve => setTimeout(resolve, batchDelayMs));
    }
  }
}

/**
 * 执行场景。
 * 按照场景定义，依次上传音频片段和关联的特征数据。
 * @param scenario 场景定义
 * @param gateway 上传网关
 * @param options 执行选项
 */
export async function executeScenario(
  scenario: Scenario,
  gateway: ScenarioUploadGateway,
  options: ScenarioExecutionOptions = {}
): Promise<void> {
  // 获取批次大小和延迟，使用默认值
  const batchSize = options.batchSize ?? 50;
  const batchDelayMs = options.batchDelayMs ?? 100;

  // 验证批次大小参数
  if (!Number.isInteger(batchSize) || batchSize <= 0 || batchSize > 50) {
    throw new Error("batchSize must be between 1 and 50");
  }

  // 处理每个采集（音频+特征）
  for (const capture of scenario.captures) {
    // 先上传音频片段
    const clipId = await gateway.uploadAudioClip(capture.audioClip);
    if (!clipId) {
      throw new Error(`Audio upload failed: ${capture.audioClip.fileName}`);
    }
    // 然后上传关联的特征数据
    await uploadFeatureBatches(capture.features, clipId, gateway, batchSize, batchDelayMs);
  }

  // 处理独立的特征数据（不关联音频）
  await uploadFeatureBatches(
    scenario.standaloneFeatures ?? [],
    undefined,
    gateway,
    batchSize,
    batchDelayMs
  );
}
