<template>
  <div v-if="visible" class="tools-modal-overlay" @click.self="$emit('close')">
    <div class="tools-modal-container">
      <!-- Modal Header -->
      <div class="tools-modal-header">
        <div class="header-title-box">
          <div class="header-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0284c7" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
            </svg>
          </div>
          <div>
            <h3>Quản lý Công cụ Agent (Dynamic Tools)</h3>
            <p class="header-subtitle">H2DB SQL • Canteen Markdown • Qdrant Vector Search • REST API</p>
          </div>
        </div>
        <div class="header-btn-group">
          <button class="btn-refresh" title="Tải lại danh sách" @click="fetchTools">
            <svg :class="{ spinning: isLoading }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
            </svg>
          </button>
          <button class="btn-close" title="Đóng modal" @click="$emit('close')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- Modal Body -->
      <div class="tools-modal-body">
        <div v-if="isLoading" class="tools-loading">
          <div class="spinner"></div>
          <span>Đang tải danh sách công cụ...</span>
        </div>

        <div v-else-if="tools.length === 0" class="tools-empty">
          <p>Chưa có công cụ nào được khởi tạo trong H2 Database.</p>
        </div>

        <div v-else class="tools-grid">
          <div
            v-for="tool in tools"
            :key="tool.id"
            class="tool-card"
            :class="{ disabled: !tool.enabled }"
          >
            <div class="tool-card-top">
              <div class="tool-meta">
                <span :class="['badge-type', `type-${tool.toolType.toLowerCase()}`]">
                  {{ formatToolType(tool.toolType) }}
                </span>
                <span class="tool-name">{{ tool.name }}</span>
              </div>
              <div class="tool-toggle-box">
                <label class="switch" :title="tool.enabled ? 'Đang bật - Click để tắt' : 'Đang tắt - Click để bật'">
                  <input
                    type="checkbox"
                    :checked="tool.enabled"
                    @change="toggleTool(tool)"
                  />
                  <span class="slider"></span>
                </label>
              </div>
            </div>

            <p class="tool-desc">{{ tool.description }}</p>

            <!-- Chi tiết cấu hình thu gọn -->
            <div class="tool-detail-section">
              <div class="detail-header" @click="toggleDetail(tool.id)">
                <span>⚙ Cấu hình & Tham số (Schema)</span>
                <span class="arrow" :class="{ open: expandedDetails[tool.id] }">▼</span>
              </div>
              <div v-if="expandedDetails[tool.id]" class="detail-content">
                <div class="detail-block">
                  <div class="detail-label">Parameters Schema (JSON):</div>
                  <pre><code>{{ tool.parametersSchema }}</code></pre>
                </div>
                <div class="detail-block">
                  <div class="detail-label">Config Data:</div>
                  <pre><code>{{ tool.configData }}</code></pre>
                </div>
              </div>
            </div>

            <!-- Panel Test Run -->
            <div class="tool-test-section">
              <button class="btn-test-toggle" @click="openTest(tool)">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="5 3 19 12 5 21 5 3"></polygon>
                </svg>
                <span>Thử nghiệm công cụ</span>
              </button>

              <div v-if="testingToolId === tool.id" class="test-panel">
                <div class="test-input-row">
                  <label>Tham số gọi (JSON):</label>
                  <textarea
                    v-model="testInputJson"
                    rows="3"
                    placeholder='{"city": "Hà Nội"}'
                  ></textarea>
                </div>
                <div class="test-actions">
                  <button class="btn-run" :disabled="isTesting" @click="runToolTest(tool)">
                    {{ isTesting ? 'Đang thực thi...' : 'Chạy thử nghiệm' }}
                  </button>
                  <button class="btn-cancel" @click="testingToolId = null">Đóng test</button>
                </div>

                <div v-if="testResult" class="test-result-box" :class="{ error: !testResult.success }">
                  <div class="result-header">
                    <span>Kết quả ({{ testResult.executionTimeMs }}ms)</span>
                    <span :class="testResult.success ? 'status-ok' : 'status-err'">
                      {{ testResult.success ? '✓ Thành công' : '✕ Thất bại' }}
                    </span>
                  </div>
                  <pre><code>{{ testResult.output }}</code></pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="tools-modal-footer">
        <div class="footer-info">
          <span class="dot-active"></span>
          <span>H2 Database: Active (./data/chatbotdb)</span>
        </div>
        <button class="btn-modal-close" @click="$emit('close')">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ToolsModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      tools: [],
      isLoading: false,
      expandedDetails: {},
      testingToolId: null,
      testInputJson: '{}',
      isTesting: false,
      testResult: null
    };
  },
  watch: {
    visible(val) {
      if (val) {
        this.fetchTools();
      }
    }
  },
  methods: {
    formatToolType(type) {
      switch (type) {
        case 'STATIC_MARKDOWN':
          return 'MARKDOWN CACHE';
        case 'SQL_QUERY':
          return 'H2 SQL QUERY';
        case 'QDRANT_VECTOR':
          return 'QDRANT VECTOR';
        case 'HTTP_API':
          return 'REST HTTP';
        default:
          return type;
      }
    },
    toggleDetail(id) {
      this.$set(this.expandedDetails, id, !this.expandedDetails[id]);
    },
    openTest(tool) {
      if (this.testingToolId === tool.id) {
        this.testingToolId = null;
        return;
      }
      this.testingToolId = tool.id;
      this.testResult = null;
      if (tool.name === 'get_canteen_menu') {
        this.testInputJson = JSON.stringify({ date: 'today' }, null, 2);
      } else if (tool.name === 'get_weather_info') {
        this.testInputJson = JSON.stringify({ city: 'Hà Nội' }, null, 2);
      } else if (tool.name === 'search_production_legal') {
        this.testInputJson = JSON.stringify({ query: 'an toàn lao động' }, null, 2);
      } else {
        this.testInputJson = '{}';
      }
    },
    async fetchTools() {
      this.isLoading = true;
      try {
        const endpoints = ['/api/tools', 'http://localhost:3001/api/tools'];
        let res = null;
        for (const ep of endpoints) {
          try {
            res = await fetch(ep);
            if (res.ok) break;
          } catch (e) {
            // try next
          }
        }
        if (res && res.ok) {
          this.tools = await res.json();
        }
      } catch (err) {
        console.error('[ToolsModal] Lỗi lấy danh sách tools:', err);
      } finally {
        this.isLoading = false;
      }
    },
    async toggleTool(tool) {
      try {
        const endpoints = [`/api/tools/${tool.id}/toggle`, `http://localhost:3001/api/tools/${tool.id}/toggle`];
        let res = null;
        for (const ep of endpoints) {
          try {
            res = await fetch(ep, { method: 'POST' });
            if (res.ok) break;
          } catch (e) {
            // try next
          }
        }
        if (res && res.ok) {
          const updated = await res.json();
          tool.enabled = updated.enabled;
        }
      } catch (err) {
        console.error('[ToolsModal] Lỗi toggle tool:', err);
      }
    },
    async runToolTest(tool) {
      this.isTesting = true;
      this.testResult = null;
      try {
        let parsedParams = {};
        try {
          parsedParams = JSON.parse(this.testInputJson);
        } catch (e) {
          this.testResult = {
            success: false,
            output: 'JSON tham số không hợp lệ: ' + e.message,
            executionTimeMs: 0
          };
          this.isTesting = false;
          return;
        }

        const endpoints = [`/api/tools/${tool.id}/test`, `http://localhost:3001/api/tools/${tool.id}/test`];
        let res = null;
        for (const ep of endpoints) {
          try {
            res = await fetch(ep, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(parsedParams)
            });
            if (res.ok) break;
          } catch (e) {
            // try next
          }
        }
        if (res && res.ok) {
          this.testResult = await res.json();
        } else {
          this.testResult = {
            success: false,
            output: `Lỗi HTTP ${res ? res.status : 'không có phản hồi'}`,
            executionTimeMs: 0
          };
        }
      } catch (err) {
        this.testResult = {
          success: false,
          output: 'Lỗi thực thi: ' + err.message,
          executionTimeMs: 0
        };
      } finally {
        this.isTesting = false;
      }
    }
  }
};
</script>

<style scoped>
/* LỚP OVERLAY TOÀN MÀN HÌNH - BẬT POINTER EVENTS */
.tools-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 100000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  pointer-events: auto !important;
  cursor: default;
  animation: toolsModalFadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-sizing: border-box;
}

/* KHUNG MODAL PHONG CÁCH SÁNG ĐỒNG BỘ VỚI CHATBOT WIDGET */
.tools-modal-container {
  width: 100%;
  max-width: 760px;
  max-height: 86vh;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18), 0 0 0 1px rgba(226, 232, 240, 0.8);
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #0f172a;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  pointer-events: auto !important;
  user-select: text;
}

/* HEADER CỬA SỔ */
.tools-modal-header {
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  pointer-events: auto !important;
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0284c7;
}

.header-title-box h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.header-subtitle {
  font-size: 12px;
  color: #64748b;
  margin: 3px 0 0 0;
}

.header-btn-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-refresh, .btn-close {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer !important;
  pointer-events: auto !important;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-refresh:hover, .btn-close:hover {
  background: #f1f5f9;
  color: #0284c7;
  border-color: #cbd5e1;
}

.spinning {
  animation: spin 0.8s linear infinite;
}

/* BODY */
.tools-modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  background: #f8fafc;
  pointer-events: auto !important;
}

.tools-loading, .tools-empty {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e2e8f0;
  border-top-color: #0284c7;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px auto;
}

.tools-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* THẺ CÔNG CỤ (TOOL CARD) */
.tool-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;
  pointer-events: auto !important;
}

.tool-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.tool-card.disabled {
  opacity: 0.65;
  background: #f8fafc;
}

.tool-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge-type {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: 0.5px;
}

.type-static_markdown {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.type-sql_query {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #bfdbfe;
}

.type-qdrant_vector {
  background: #faf5ff;
  color: #9333ea;
  border: 1px solid #e9d5ff;
}

.type-http_api {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

.tool-name {
  font-weight: 700;
  font-size: 14px;
  color: #0f172a;
  font-family: Consolas, Monaco, monospace;
}

/* SWITCH TOGGLE */
.switch {
  position: relative;
  display: inline-block;
  width: 38px;
  height: 22px;
  cursor: pointer !important;
  pointer-events: auto !important;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: auto !important;
}

.slider {
  position: absolute;
  cursor: pointer !important;
  pointer-events: auto !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.25s;
  border-radius: 20px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.25s;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

input:checked + .slider {
  background-color: #0284c7;
}

input:checked + .slider:before {
  transform: translateX(16px);
}

.tool-desc {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0 0 12px 0;
}

/* SECTION DETAIL COLLAPSIBLE */
.tool-detail-section {
  margin-top: 8px;
  border-top: 1px solid #f1f5f9;
  padding-top: 8px;
}

.detail-header {
  font-size: 12px;
  color: #64748b;
  cursor: pointer !important;
  pointer-events: auto !important;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 6px;
  user-select: none;
  background: #f8fafc;
  transition: all 0.15s;
}

.detail-header:hover {
  color: #0f172a;
  background: #f1f5f9;
}

.detail-header .arrow {
  font-size: 9px;
  transition: transform 0.2s;
}

.detail-header .arrow.open {
  transform: rotate(180deg);
}

.detail-content {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-block {
  background: #0f172a;
  border-radius: 8px;
  padding: 10px 12px;
  border: 1px solid #1e293b;
}

.detail-label {
  font-size: 11px;
  font-weight: 600;
  color: #38bdf8;
  margin-bottom: 5px;
}

pre {
  margin: 0;
  font-size: 11px;
  color: #f1f5f9;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, Monaco, monospace;
}

/* TEST SECTION */
.tool-test-section {
  margin-top: 10px;
}

.btn-test-toggle {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  color: #0284c7;
  border-radius: 6px;
  font-size: 12px;
  padding: 6px 12px;
  cursor: pointer !important;
  pointer-events: auto !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-test-toggle:hover {
  background: #e0f2fe;
  border-color: #7dd3fc;
}

.test-panel {
  margin-top: 10px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
  border-radius: 8px;
  padding: 12px;
  pointer-events: auto !important;
}

.test-input-row label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 5px;
}

.test-input-row textarea {
  width: 100%;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  color: #0f172a;
  border-radius: 6px;
  padding: 8px;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  resize: vertical;
  outline: none;
  pointer-events: auto !important;
  box-sizing: border-box;
}

.test-input-row textarea:focus {
  border-color: #0284c7;
  box-shadow: 0 0 0 2px rgba(2, 132, 199, 0.15);
}

.test-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.btn-run {
  background: #0284c7;
  color: #ffffff;
  border: none;
  font-weight: 600;
  font-size: 12px;
  padding: 7px 16px;
  border-radius: 6px;
  cursor: pointer !important;
  pointer-events: auto !important;
  transition: background 0.2s;
}

.btn-run:hover:not(:disabled) {
  background: #0369a1;
}

.btn-run:disabled {
  opacity: 0.6;
  cursor: not-allowed !important;
}

.btn-cancel {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 12px;
  padding: 7px 12px;
  border-radius: 6px;
  cursor: pointer !important;
  pointer-events: auto !important;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.test-result-box {
  margin-top: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  padding: 10px;
  color: #166534;
}

.test-result-box.error {
  background: #fef2f2;
  border-color: #fecaca;
  color: #991b1b;
}

.result-header {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 6px;
}

.status-ok {
  color: #15803d;
  font-weight: 700;
}

.status-err {
  color: #dc2626;
  font-weight: 700;
}

.test-result-box pre {
  background: transparent;
  border: none;
  padding: 0;
  color: inherit;
}

/* MODAL FOOTER */
.tools-modal-footer {
  padding: 14px 20px;
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  pointer-events: auto !important;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.dot-active {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.6);
}

.btn-modal-close {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #334155;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer !important;
  pointer-events: auto !important;
  transition: all 0.2s;
}

.btn-modal-close:hover {
  background: #e2e8f0;
  color: #0f172a;
}

@keyframes toolsModalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.96);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
