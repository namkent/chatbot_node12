<template>
  <div class="demo-page">
    <div class="hero-section">
      <div class="badge">VUE 2 + WEBPACK 4 • GROQ LPU • MODULAR URCHAT</div>
      <h1 class="title">Astronaut Neon Bot</h1>
      <p class="subtitle">
        Hệ thống AI Agent phi hành gia thông minh kết hợp <strong>Backend Java Spring Boot</strong>,
        <strong>H2 Database</strong>, hỗ trợ <strong>Dynamic Tool Calling</strong>, <strong>Kho Tri Thức</strong> và <strong>Streaming Markdown</strong>.
      </p>

      <!-- CÁC NÚT ĐIỀU HƯỚNG QUẢN TRỊ TRÊN MÀN HÌNH CHÍNH -->
      <div class="hero-action-buttons">
        <router-link to="/chat" class="btn-hero-action primary">
          <span class="action-icon">✨</span>
          <span>Mở UrChat Assistant (Sidebar Gemini)</span>
        </router-link>
        <router-link to="/studio" class="btn-hero-action secondary">
          <span class="action-icon">⚡</span>
          <span>Studio Quản Trị Tri Thức (Mini Dify)</span>
        </router-link>
      </div>

      <div class="instructions-card">
        <h3>📌 Cập nhật kiến trúc UrChat mới:</h3>
        <ul>
          <li>✨ <strong>Component UrChat mới</strong>: Giao diện AI Chat Assistant chuyên nghiệp với Left Sidebar phong cách Google Gemini, quản lý đa phiên trò chuyện (sessions).</li>
          <li>📦 <strong>Cấu trúc Module hóa</strong>: Toàn bộ code hơn 7,800 dòng được tách thành các module gọn gàng trong <code>src/components/UrChat/</code> (components, services, styles).</li>
          <li>🔄 <strong>Tương thích 100%</strong>: Hỗ trợ import linh hoạt <code>import { UrChat, UrChatbot } from './components/UrChat'</code> hoặc giữ nguyên <code>import UrChatbot from './components/UrChatbot.vue'</code>.</li>
          <li>📊 <strong>Biểu đồ Mermaid trực quan</strong>: Tự động vẽ lưu đồ (flowchart), sơ đồ tuần tự (sequence), Gantt, Pie chart, Mindmap với chế độ Zoom/Pan lightbox và tải ảnh PNG/SVG.</li>
        </ul>
      </div>
    </div>

    <!-- WIDGET CHATBOT LUÔN HOẠT ĐỘNG SẴN SÀNG KHI Ở VIEW DEMO -->
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
import { UrChatbot } from '../components/UrChat';

export default {
  name: 'HomeView',
  components: {
    UrChatbot
  }
};
</script>
