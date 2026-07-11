import { getScenario, ScenarioName } from "./scenarios";
import * as http from "http";
import * as fs from "fs";

const BASE_URL = "http://localhost:8080";
const DEVICE_ID = "sim-device-001";

async function uploadAudioClip(fileName: string, windowStartedAt: string, windowEndedAt: string): Promise<string> {
  const syntheticAudio = Buffer.alloc(1024, 0x80);// 生成1KB的模拟音频数据
  // ... 添加 file、windowStartedAt、windowEndedAt 字段
  const boundary = "----WebKitFormBoundary" + Math.random().toString(36).substr(2);
  const crlf = "\r\n";
  
  const parts: Buffer[] = [];
  
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="file"; filename="${fileName}"${crlf}`));
  parts.push(Buffer.from(`Content-Type: audio/wav${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(syntheticAudio);
  parts.push(Buffer.from(crlf));
  
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="windowStartedAt"${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(Buffer.from(windowStartedAt));
  parts.push(Buffer.from(crlf));
  
  parts.push(Buffer.from(`--${boundary}${crlf}`));
  parts.push(Buffer.from(`Content-Disposition: form-data; name="windowEndedAt"${crlf}`));
  parts.push(Buffer.from(crlf));
  parts.push(Buffer.from(windowEndedAt));
  parts.push(Buffer.from(crlf));
  
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
              // 解析响应，提取 audioClipId的类型
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

async function uploadFeatures(features: any[], audioClipId?: string) {
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

async function runScenario(scenarioName: ScenarioName) {
  console.log(`\n=== 运行场景: ${scenarioName} ===`);
  
  const scenario = getScenario(scenarioName);
  
  for (const audioClip of scenario.audioClips) {
    const clipId = await uploadAudioClip(audioClip.fileName, audioClip.windowStartedAt, audioClip.windowEndedAt);
    if (clipId) {
      const batchSize = 50;
      for (let i = 0; i < scenario.features.length; i += batchSize) {
        const batch = scenario.features.slice(i, i + batchSize);
        await uploadFeatures(batch, clipId);
        await new Promise(resolve => setTimeout(resolve, 100));
      }
    }
  }
  
  console.log(`\n=== 场景 ${scenarioName} 完成 ===`);
}

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

main();