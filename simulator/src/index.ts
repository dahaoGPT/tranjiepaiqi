import { AudioClipInput, FeatureWindow, getScenario, ScenarioName } from "./scenarios";
import { executeScenario } from "./scenario-runner";
import * as http from "http";

/** 后端服务基础地址 */
const BASE_URL = "http://localhost:8080";
/** 模拟设备ID */
const DEVICE_ID = "sim-device-001";

/**
 * 上传模拟音频片段到后端。
 * @param fileName 文件名
 * @param windowStartedAt 窗口开始时间
 * @param windowEndedAt 窗口结束时间
 * @returns 音频片段ID
 */
async function uploadAudioClip(fileName: string, windowStartedAt: string, windowEndedAt: string): Promise<string> {
  // 生成1KB的模拟音频数据（静音波形）
  const syntheticAudio = Buffer.alloc(1024, 0x80);
  // 构建 multipart/form-data 请求体
  const boundary = "----WebKitFormBoundary" + Math.random().toString(36).substr(2);
  const crlf = "\r\n";
  
  const parts: Buffer[] = [];
  
  // 添加文件字段
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="file"; filename="${fileName}"${crlf}`));
  parts.push(Buffer.from(`Content-Type: audio/wav${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(syntheticAudio);
  parts.push(Buffer.from(crlf));
  
  // 添加窗口开始时间字段
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="windowStartedAt"${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(Buffer.from(windowStartedAt));
  parts.push(Buffer.from(crlf));
  
  // 添加窗口结束时间字段
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="windowEndedAt"${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(Buffer.from(windowEndedAt));
  parts.push(Buffer.from(crlf));
  
  // 添加结束边界
  parts.push(Buffer.from(`--${boundary}--${crlf}`));
  
  const body = Buffer.concat(parts);
  
  return new Promise((resolve) => {
    const req = http.request(
      {
        hostname: "localhost",
        port: 8080,
        path: `/api/devices/${DEVICE_ID}/audio-clips`,
        method: "POST",
        headers: {
          "Content-Type": `multipart/form-data; boundary=${boundary}`,
          "Content-Length": body.length
        }
      },
      (res) => {
        let data = "";
        res.on("data", (chunk) => {
          data += chunk;
        });
        res.on("end", () => {
          if (res.statusCode === 200) {
            try {
              // 解析响应，提取 audioClipId
              const json = JSON.parse(data);
              console.log(`音频上传成功: ${json.audioClipId}`);
              resolve(json.audioClipId);
            } catch {
              console.warn(`音频上传响应解析失败: ${data}`);
              resolve("");
            }
          } else {
            console.warn(`音频上传失败: ${res.statusCode} - ${data}`);
            resolve("");
          }
        });
      }
    );
    
    req.on("error", (e) => {
      console.warn(`音频上传请求失败: ${e.message}`);
      resolve("");
    });
    
    req.write(body);
    req.end();
  });
}

/**
 * 上传声学特征数据到后端。
 * @param features 特征窗口列表
 * @param audioClipId 关联的音频片段ID（可选）
 * @returns 上传结果
 */
async function uploadFeatures(features: FeatureWindow[], audioClipId?: string) {
  const payload = {
    features: features.map(f => ({
      ...f,
      audioClipId: audioClipId || null
    }))
  };

  const response = await fetch(`${BASE_URL}/api/devices/${DEVICE_ID}/features`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  const data = await response.json();
  console.log(`特征上传: 接受 ${data.acceptedCount}, 拒绝 ${data.rejectedCount}`);
  return data;
}

/**
 * 运行指定的模拟场景。
 * @param scenarioName 场景名称
 */
async function runScenario(scenarioName: ScenarioName) {
  console.log(`\n=== 运行场景: ${scenarioName} ===`);
  
  // 获取场景配置
  const scenario = getScenario(scenarioName);

  // 执行场景
  await executeScenario(scenario, {
    uploadAudioClip: (audioClip: AudioClipInput) => uploadAudioClip(
      audioClip.fileName,
      audioClip.windowStartedAt,
      audioClip.windowEndedAt
    ),
    uploadFeatures
  });
  
  console.log(`\n=== 场景 ${scenarioName} 完成 ===`);
}

/**
 * 模拟器主入口函数。
 * 从命令行参数获取场景名称，默认运行 normal 场景。
 */
async function main() {
  const scenarioName = process.argv[2] as ScenarioName || "normal";
  
  console.log("节拍器模拟器");
  console.log("正在连接后端: " + BASE_URL);

  try {
    await runScenario(scenarioName);
    process.exit(0);
  } catch (error) {
    console.error("运行失败:", error);
    process.exit(1);
  }
}

// 启动模拟器
main();
