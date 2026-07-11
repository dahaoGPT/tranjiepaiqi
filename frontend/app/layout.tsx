import './globals.css';
import { Inter } from 'next/font/google';

const inter = Inter({ subsets: ['latin'] });

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body className={inter.className}>
        <header className="bg-white shadow-sm sticky top-0 z-10">
          <div className="container">
            <h1 className="text-xl font-bold py-3">节拍器 - 老人生活节奏守护</h1>
          </div>
        </header>
        <main className="container">
          {children}
        </main>
      </body>
    </html>
  );
}