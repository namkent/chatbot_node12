<template>
  <div v-if="visible" class="tools-modal-overlay" @click.self="$emit('close')">
    <div class="tools-modal-container">
      <!-- Modal Header -->
      <div class="tools-modal-header">
        <div class="header-title-box">
          <div class="header-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#00f0ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
            <svg :class="{ spinning: isLoading }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
            </svg>
          </button>
          <button class="btn-close" @click="$emit('close')">✕</button>
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
                <span>⚙ Cấu hình & Schema</span>
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
                  <button class="btn-cancel" @click="testingToolId = null">Đóng</button>
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
.tools-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(2, 6, 23, 0.75);
  backdrop-filter: blur(8px);
  z-index: 999999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: modalFadeIn 0.25s ease-out;
}

.tools-modal-container {
  width: 100%;
  max-width: 780px;
  max-height: 88vh;
  background: #090d16;
  border: 1px solid rgba(0, 240, 255, 0.25);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.6), 0 0 32px rgba(0, 240, 255, 0.12);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #f1f5f9;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.tools-modal-header {
  padding: 16px 20px;
  background: rgba(15, 23, 42, 0.85);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title-box h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: #ffffff;
  letter-spacing: 0.3px;
}

.header-subtitle {
  font-size: 12px;
  color: #94a3b8;
  margin: 2px 0 0 0;
}

.header-btn-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-refresh, .btn-close {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-refresh:hover, .btn-close:hover {
  background: rgba(0, 240, 255, 0.15);
  color: #00f0ff;
  border-color: rgba(0, 240, 255, 0.4);
}

.spinning {
  animation: spin 1s linear infinite;
}

.tools-modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.tools-loading, .tools-empty {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(0, 240, 255, 0.2);
  border-top-color: #00f0ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px auto;
}

.tools-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tool-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  transition: border-color 0.2s;
}

.tool-card:hover {
  border-color: rgba(0, 240, 255, 0.3);
}

.tool-card.disabled {
  opacity: 0.65;
  background: rgba(15, 23, 42, 0.3);
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
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.type-sql_query {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.type-qdrant_vector {
  background: rgba(168, 85, 247, 0.15);
  color: #c084fc;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

.type-http_api {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.tool-name {
  font-weight: 600;
  font-size: 14px;
  color: #ffffff;
  font-family: monospace;
}

/* Switch Toggle */
.switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #334155;
  transition: 0.3s;
  border-radius: 20px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #00f0ff;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
}

input:checked + .slider:before {
  transform: translateX(16px);
  background-color: #020617;
}

.tool-desc {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
  margin: 0 0 12px 0;
}

/* Section Detail Collapsible */
.tool-detail-section {
  margin-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 8px;
}

.detail-header {
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  user-select: none;
}

.detail-header:hover {
  color: #94a3b8;
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
  background: #040810;
  border-radius: 6px;
  padding: 8px 10px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-label {
  font-size: 11px;
  color: #00f0ff;
  margin-bottom: 4px;
}

pre {
  margin: 0;
  font-size: 11px;
  color: #cbd5e1;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, monospace;
}

/* Test Section */
.tool-test-section {
  margin-top: 10px;
}

.btn-test-toggle {
  background: rgba(0, 240, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  border-radius: 6px;
  font-size: 12px;
  padding: 5px 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-test-toggle:hover {
  background: rgba(0, 240, 255, 0.18);
  border-color: rgba(0, 240, 255, 0.4);
}

.test-panel {
  margin-top: 10px;
  background: #040810;
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 8px;
  padding: 12px;
}

.test-input-row label {
  display: block;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.test-input-row textarea {
  width: 100%;
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f1f5f9;
  border-radius: 6px;
  padding: 8px;
  font-family: Consolas, monospace;
  font-size: 12px;
  resize: vertical;
  outline: none;
}

.test-input-row textarea:focus {
  border-color: #00f0ff;
}

.test-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.btn-run {
  background: #00f0ff;
  color: #020617;
  border: none;
  font-weight: 600;
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-run:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  border: none;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.test-result-box {
  margin-top: 10px;
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 6px;
  padding: 10px;
}

.test-result-box.error {
  background: rgba(239, 68, 68, 0.05);
  border-color: rgba(239, 68, 68, 0.3);
}

.result-header {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.status-ok {
  color: #10b981;
  font-weight: 600;
}

.status-err {
  color: #ef4444;
  font-weight: 600;
}

/* Modal Footer */
.tools-modal-footer {
  padding: 12px 20px;
  background: rgba(15, 23, 42, 0.9);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.dot-active {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}

.btn-modal-close {
  background: rgba(255, 255, 255, 0.1);
  color: #f1f5f9;
  border: none;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-modal-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

@keyframes modalFadeIn {
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
