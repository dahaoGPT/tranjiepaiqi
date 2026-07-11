'use client';

import Link from 'next/link';

/**
 * 首页组件。
 * 展示系统介绍和功能特点，提供登录入口。
 */
export default function HomePage() {
  return (
    <div className="text-center py-10">
      {/* 标题 */}
      <h2 className="text-2xl font-bold mb-6">节拍器 - 老人生活节奏守护</h2>
      {/* 简介 */}
      <p className="text-gray-600 mb-8 max-w-md mx-auto">
        通过水龙头声感传感器识别用水节奏，不采集图像，帮助家属发现异常情况。
      </p>
      {/* 登录按钮和功能特点 */}
      <div className="space-y-4">
        {/* 登录按钮 */}
        <Link href="/login" className="btn btn-primary">
          登录系统
        </Link>
        {/* 功能特点 */}
        <div className="mt-8">
          <h3 className="font-bold mb-4">功能特点</h3>
          <div className="grid">
            {/* 非侵入式特点 */}
            <div className="card">
              <h4 className="font-bold">非侵入式</h4>
              <p className="text-sm text-gray-600 mt-1">只采集声音特征，不采集图像</p>
            </div>
            {/* 异常提醒特点 */}
            <div className="card">
              <h4 className="font-bold">异常提醒</h4>
              <p className="text-sm text-gray-600 mt-1">晨间无用水、长时间流水等异常</p>
            </div>
            {/* 音频复盘特点 */}
            <div className="card">
              <h4 className="font-bold">音频复盘</h4>
              <p className="text-sm text-gray-600 mt-1">保留原始音频用于人工核实</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}