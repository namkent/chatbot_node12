<template>
  <div id="app">
    <div class="demo-page">
      <div class="hero-section">
        <div class="badge">VUE 2 + VITE • GROQ LPU</div>
        <h1 class="title">Astronaut Neon Bot</h1>
        <p class="subtitle">
          Widget Chatbot AI phi hành gia không gian tích hợp <strong>Groq LPU siêu tốc</strong> (Qwen 3.8 27B),
          hỗ trợ <strong>Streaming thời gian thực</strong>, <strong>Markdown</strong> và <strong>Highlight code</strong>.
        </p>
        <div class="instructions-card">
          <h3>📌 Tính năng mới & Hướng dẫn:</h3>
          <ul>
            <li>⚡ <strong>Tốc độ phản hồi tức thì với Groq LPU</strong>: Hàng trăm token mỗi giây, gần như không có độ trễ!</li>
            <li>💻 <strong>Highlight cú pháp Code</strong>: Tự động phát hiện ngôn ngữ, tô màu cú pháp và có sẵn nút <strong>Copy</strong> tiện lợi.</li>
            <li>📝 <strong>Định dạng Markdown</strong>: Hỗ trợ tiêu đề, in đậm/nghiêng, danh sách, trích dẫn và bảng biểu.</li>
            <li>📊 <strong>Biểu đồ Mermaid trực quan</strong>: Tự động vẽ lưu đồ (flowchart), sơ đồ tuần tự (sequence), Gantt, Pie chart, Mindmap kèm nút xem mã nguồn & sao chép.</li>
            <li>⏹️ <strong>Nút Dừng (Stop)</strong>: Cho phép ngắt luồng streaming bất kỳ lúc nào.</li>
            <li>✨ <em>Gợi ý thử nghiệm:</em> Gõ <code>Vẽ sơ đồ quy trình đăng nhập bằng Mermaid</code> hoặc <code>Viết hàm QuickSort Python</code>!</li>
          </ul>
        </div>
      </div>

      <!-- Chatbot Component -->
      <UrChatbot
        bot-name="Astro Bot AI"
        status-text="Groq • GPT-OSS 120B (Reasoning)"
        placeholder-text="Hỏi giải thuật, code, kiến thức..."
        :local-storage="true"
        :attach-file="true"
        :store-file="false"
        :thinking="true"
      />
    </div>
  </div>
</template>

<script>
import UrChatbot from './components/UrChatbot.vue';

export default {
  name: 'App',
  components: {
    UrChatbot
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

.demo-page {
  min-height: 100vh;
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
