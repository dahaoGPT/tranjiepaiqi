'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

/**
 * 登录页面组件。
 * 提供用户登录界面，MVP 版本支持任意用户名密码登录。
 */
export default function LoginPage() {
  // 用户名状态
  const [username, setUsername] = useState('');
  // 密码状态
  const [password, setPassword] = useState('');
  // 加载状态
  const [loading, setLoading] = useState(false);
  // 路由导航
  const router = useRouter();

  /**
   * 处理登录表单提交。
   * @param e 表单事件
   */
  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      // 登录成功后跳转到看板页面
      if (response.ok) {
        router.push('/dashboard');
      }
    } catch (error) {
      console.error('登录失败:', error);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-md mx-auto mt-10">
      <div className="card text-center">
        <h2 className="text-2xl font-bold mb-6">节拍器登录</h2>
        {/* 登录表单 */}
        <form onSubmit={handleSubmit}>
          {/* 用户名输入 */}
          <div className="form-group">
            <label className="form-label">用户名</label>
            <input
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名"
              required
            />
          </div>
          {/* 密码输入 */}
          <div className="form-group">
            <label className="form-label">密码</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码"
              required
            />
          </div>
          {/* 登录按钮 */}
          <button type="submit" className="btn btn-primary w-full" disabled={loading}>
            {loading ? '登录中...' : '登录'}
          </button>
        </form>
        {/* MVP 版本提示 */}
        <p className="mt-4 text-sm text-gray-500">MVP 版本：任意用户名密码即可登录</p>
      </div>
    </div>
  );
}