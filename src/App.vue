<template>
  <div id="app">
    <!-- TOP NAVIGATION BAR -->
    <header class="app-global-nav">
      <div class="nav-brand" @click="$router.push('/')" style="cursor: pointer;">
        <span class="brand-rocket">🚀</span>
        <span class="brand-title">ASTRONAUT <strong>AI STUDIO</strong></span>
        <span class="brand-tag">SPRING BOOT + H2DB</span>
      </div>

      <div class="nav-right-section">
        <div class="nav-mode-switcher">
          <router-link
            to="/"
            exact
            class="btn-mode"
            exact-active-class="active"
          >
            <span>💬 Bot Widget</span>
          </router-link>
          <router-link
            to="/chat"
            class="btn-mode"
            active-class="active"
          >
            <span>✨ Assistant Chat</span>
          </router-link>
          <router-link
            to="/studio"
            class="btn-mode"
            active-class="active"
          >
            <span>⚡ Studio Quản Trị</span>
          </router-link>
        </div>

        <!-- NÚT QUẢN LÝ CÔNG CỤ DÀNH CHO ADMIN (TÁCH KHỎI CHATBOT) -->
        <button
          class="btn-nav-tools"
          title="Quản lý & Cấu hình Công cụ Agent (Dành riêng cho Quản trị viên)"
          @click="isToolsModalOpen = true"
        >
          <span class="btn-icon">🛠️</span>
          <span>Cấu Hình Tools</span>
        </button>
      </div>
    </header>

    <!-- ROUTED VIEWS -->
    <main class="app-router-content">
      <router-view />
    </main>

    <!-- MODAL QUẢN LÝ TOOLS AGENT (ĐẶT Ở CẤP ROOT DÀNH CHO ADMIN) -->
    <ToolsModal :visible="isToolsModalOpen" @close="isToolsModalOpen = false" />
  </div>
</template>

<script>
import ToolsModal from './components/ToolsModal.vue';

export default {
  name: 'App',
  components: {
    ToolsModal
  },
  data() {
    return {
      isToolsModalOpen: false
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
  text-decoration: none;
  display: inline-flex;
  align-items: center;
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

.nav-right-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-nav-tools {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(2, 132, 199, 0.15);
  border: 1px solid rgba(2, 132, 199, 0.4);
  color: #38bdf8;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-nav-tools:hover {
  background: rgba(2, 132, 199, 0.3);
  border-color: #38bdf8;
  color: #ffffff;
  box-shadow: 0 0 12px rgba(56, 189, 248, 0.35);
  transform: translateY(-1px);
}

.btn-nav-tools .btn-icon {
  font-size: 13px;
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
  margin-bottom: 24px;
}

/* HERO ADMIN ACTIONS */
.hero-action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.btn-hero-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  text-decoration: none;
}

.btn-hero-action.primary {
  background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
  color: #ffffff;
  border: 1px solid rgba(56, 189, 248, 0.4);
  box-shadow: 0 4px 14px rgba(2, 132, 199, 0.3);
}

.btn-hero-action.primary:hover {
  background: linear-gradient(135deg, #0369a1 0%, #0284c7 100%);
  border-color: #38bdf8;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 189, 248, 0.45);
}

.btn-hero-action.secondary {
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.btn-hero-action.secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #00f0ff;
  border-color: rgba(0, 240, 255, 0.3);
  transform: translateY(-2px);
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
