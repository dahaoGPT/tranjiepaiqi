interface AudioClip {
  id: string;
  label: string;
  playbackUrl: string;
}

interface AudioReviewPlayerProps {
  clips: AudioClip[];
}

export default function AudioReviewPlayer({ clips }: AudioReviewPlayerProps) {
  if (clips.length === 0) {
    return <p className="text-gray-500">暂无关联音频</p>;
  }

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