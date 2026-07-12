/**
 * 音频片段接口。
 * 包含音频的唯一标识、显示标签和播放地址。
 */
interface AudioClip {
  /** 音频片段ID */
  id: string;
  /** 显示标签 */
  label: string;
  /** 播放地址 */
  playbackUrl: string;
}

/**
 * 音频复盘播放器组件属性接口。
 */
interface AudioReviewPlayerProps {
  /** 音频片段列表 */
  clips: AudioClip[];
}

/**
 * 音频复盘播放器组件。
 * 用于异常详情页展示关联的原始音频，支持人工核实。
 */
export default function AudioReviewPlayer({ clips }: AudioReviewPlayerProps) {
  // 如果没有音频片段，显示提示信息
  if (clips.length === 0) {
    return <p className="text-gray-500">暂无关联音频</p>;
  }

  // 渲染音频片段列表
  return (
    <div className="space-y-4">
      {clips.map((clip) => (
        <div key={clip.id} className="p-3 bg-gray-50 rounded-lg">
          <p className="text-sm font-medium mb-2">{clip.label}</p>
          <audio controls src={clip.playbackUrl} className="w-full">
            您的浏览器不支持音频播放
          </audio>
        </div>
      ))}
    </div>
  );
}