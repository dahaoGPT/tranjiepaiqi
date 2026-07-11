import './globals.css';
import { Inter } from 'next/font/google';

// 使用 Google Fonts 的 Inter 字体
const inter = Inter({ subsets: ['latin'] });

/**
 * 根布局组件。
 * 定义应用的整体结构，包含顶部导航栏和主内容区域。
 */
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body className={inter.className}>
        {/* 顶部固定导航栏 */}
        <header className="bg-white shadow-sm sticky top-0 z-10">
          <div className="container">
            <h1 className="text-xl font-bold py-3">节拍器 - 老人生活节奏守护</h1>
          </div>
        </header>
        {/* 主内容区域 */}
        <main className="container">
          {children}
        </main>
      </body>
    </html>
  );
}