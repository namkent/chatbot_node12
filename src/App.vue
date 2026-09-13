<template>
  <div id="app">
    <!-- TOP NAVIGATION BAR -->
    <header class="app-global-nav">
      <div class="nav-brand">
        <span class="brand-rocket">🚀</span>
        <span class="brand-title">ASTRONAUT <strong>AI STUDIO</strong></span>
        <span class="brand-tag">SPRING BOOT + H2DB</span>
      </div>

      <div class="nav-mode-switcher">
        <button
          :class="['btn-mode', { active: currentView === 'chat' }]"
          @click="currentView = 'chat'"
        >
          <span>💬 Trải Nghiệm Chat</span>
        </button>
        <button
          :class="['btn-mode', { active: currentView === 'studio' }]"
          @click="currentView = 'studio'"
        >
          <span>⚡ Studio Quản Trị (Mini Dify)</span>
        </button>
      </div>
    </header>

    <!-- VIEW 1: TRẢI NGHIỆM CHATBOT -->
    <div v-if="currentView === 'chat'" class="demo-page">
      <div class="hero-section">
        <div class="badge">VUE 2 + VITE • GROQ LPU • DYNAMIC TOOLS</div>
        <h1 class="title">Astronaut Neon Bot</h1>
        <p class="subtitle">
          Hệ thống AI Agent phi hành gia thông minh kết hợp <strong>Backend Java Spring Boot</strong>,
          <strong>H2 Database</strong>, hỗ trợ <strong>Dynamic Tool Calling</strong>, <strong>Kho Tri Thức</strong> và <strong>Streaming Markdown</strong>.
        </p>
        <div class="instructions-card">
          <h3>📌 Tính năng mới & Hướng dẫn:</h3>
          <ul>
            <li>⚡ <strong>Tốc độ phản hồi tức thì với Groq LPU</strong>: Hàng trăm token mỗi giây, gần như không có độ trễ!</li>
            <li>🛠️ <strong>Dynamic Tool Calling (mini Dify)</strong>: Tự động truy vấn thực đơn Canteen, chạy SQL H2 Database lấy thời tiết, và tìm kiếm ngữ nghĩa Vector.</li>
            <li>📚 <strong>Studio Quản Trị Tri Thức</strong>: Bấm nút <code>⚡ Studio Quản Trị</code> trên thanh menu để nạp tài liệu và cấu hình công cụ!</li>
            <li>📊 <strong>Biểu đồ Mermaid trực quan</strong>: Tự động vẽ lưu đồ (flowchart), sơ đồ tuần tự (sequence), Gantt, Pie chart, Mindmap.</li>
            <li>✨ <em>Gợi ý thử nghiệm:</em> Gõ <code>Hôm nay Canteen có món gì?</code> hoặc <code>Thời tiết ở Hà Nội hôm nay thế nào?</code>!</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- VIEW 2: STUDIO QUẢN TRỊ (MINI DIFY) -->
    <div v-else-if="currentView === 'studio'" class="studio-view-wrapper">
      <StudioDashboard @switch-view="currentView = $event" />
    </div>

    <!-- WIDGET CHATBOT LUÔN HOẠT ĐỘNG SẴN SÀNG Ở CẢ 2 CHẾ ĐỘ -->
    <UrChatbot
      bot-name="Astro Bot AI"
      status-text="Groq • GPT-OSS 120B (Reasoning)"
      placeholder-text="Hỏi giải thuật, thực đơn, thời tiết, luật..."
      :local-storage="true"
      :attach-file="true"
      :store-file="false"
      :thinking="true"
    />
  </div>
</template>

<script>
import UrChatbot from './components/UrChatbot.vue';
import StudioDashboard from './components/StudioDashboard.vue';

export default {
  name: 'App',
  components: {
    UrChatbot,
    StudioDashboard
  },
  data() {
    return {
      currentView: 'chat' // 'chat' | 'studio'
    };
  }
};
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  margin: 0;
  min-height: 100vh;
  background: radial-gradient(circle at 80% 20%, #1e1b4b 0%, #0f172a 50%, #020617 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  color: #f8fafc;
  overflow-x: hidden;
}

/* TOPBAR */
.app-global-nav {
  height: 56px;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-rocket {
  font-size: 20px;
}

.brand-title {
  font-size: 14px;
  letter-spacing: 1px;
  color: #f1f5f9;
}

.brand-title strong {
  color: #00f0ff;
}

.brand-tag {
  font-size: 10px;
  background: rgba(0, 240, 255, 0.12);
  border: 1px solid rgba(0, 240, 255, 0.3);
  color: #00f0ff;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 700;
}

.nav-mode-switcher {
  display: flex;
  gap: 6px;
  background: rgba(0, 0, 0, 0.3);
  padding: 4px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.btn-mode {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-mode:hover {
  color: #ffffff;
}

.btn-mode.active {
  background: rgba(0, 240, 255, 0.15);
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.2);
}

.studio-view-wrapper {
  min-height: calc(100vh - 56px);
}

.demo-page {
  min-height: calc(100vh - 56px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

.hero-section {
  max-width: 680px;
  text-align: center;
  animation: fadeIn 0.8s ease-out;
}

.badge {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.3);
  color: #00f0ff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 20px;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
}

.title {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #ffffff 30%, #00f0ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 16px;
  line-height: 1.6;
  color: #94a3b8;
  margin-bottom: 32px;
}

.instructions-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 24px;
  text-align: left;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.instructions-card h3 {
  font-size: 15px;
  color: #00f0ff;
  margin-bottom: 12px;
}

.instructions-card ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.instructions-card li {
  font-size: 14px;
  color: #cbd5e1;
  position: relative;
  padding-left: 20px;
}

.instructions-card li::before {
  content: "✦";
  position: absolute;
  left: 0;
  color: #00f0ff;
  font-size: 12px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
