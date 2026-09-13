<template>
  <div class="studio-container">
    <!-- STUDIO TOPBAR -->
    <header class="studio-header">
      <div class="studio-brand">
        <div class="brand-logo">⚡</div>
        <div class="brand-text">
          <h2>AI Agent Studio</h2>
          <span>Mini Dify & Workflow Hub • Spring Boot + H2DB</span>
        </div>
      </div>

      <nav class="studio-nav">
        <button
          :class="['nav-item', { active: currentTab === 'knowledge' }]"
          @click="currentTab = 'knowledge'"
        >
          <span class="nav-icon">📚</span>
          <span>Kho Tri Thức</span>
          <span v-if="knowledgeBases.length > 0" class="nav-count">{{ knowledgeBases.length }}</span>
        </button>

        <button
          :class="['nav-item', { active: currentTab === 'tools' }]"
          @click="currentTab = 'tools'"
        >
          <span class="nav-icon">🛠️</span>
          <span>Công Cụ Agent</span>
          <span v-if="tools.length > 0" class="nav-count">{{ tools.length }}</span>
        </button>

        <button
          :class="['nav-item', { active: currentTab === 'database' }]"
          @click="currentTab = 'database'"
        >
          <span class="nav-icon">🗄️</span>
          <span>H2 Database</span>
        </button>
      </nav>

      <div class="studio-actions">
        <button class="btn-switch-chat" @click="$emit('switch-view', 'chat')">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
          <span>Trải Nghiệm Chatbot</span>
        </button>
      </div>
    </header>

    <!-- MAIN CONTENT -->
    <main class="studio-main">
      <!-- ========================================================
           TAB 1: KHO TRI THỨC (KNOWLEDGE STUDIO)
           ======================================================== -->
      <section v-if="currentTab === 'knowledge'" class="tab-content">
        <div class="content-header">
          <div>
            <h3>Quản Lý Kho Tri Thức (Datasets)</h3>
            <p>Tạo kho tài liệu, tự động phân đoạn (Chunking) và cung cấp tri thức cho AI Agent tra cứu.</p>
          </div>
          <button class="btn-primary" @click="openCreateKbModal">
            <span>+ Tạo Kho Tri Thức</span>
          </button>
        </div>

        <div v-if="isLoadingKb" class="loading-box">
          <div class="spinner"></div>
          <span>Đang tải danh sách kho tri thức...</span>
        </div>

        <div v-else-if="knowledgeBases.length === 0" class="empty-box">
          <div class="empty-icon">📁</div>
          <h4>Chưa có kho tri thức nào</h4>
          <p>Tạo kho tri thức đầu tiên để nạp tài liệu quy chuẩn kỹ thuật, nội quy hoặc luật cho AI.</p>
          <button class="btn-primary" @click="openCreateKbModal">+ Tạo Kho Tri Thức</button>
        </div>

        <div v-else class="kb-grid">
          <div v-for="kb in knowledgeBases" :key="kb.id" class="kb-card">
            <div class="kb-card-header">
              <div class="kb-icon-box">{{ kb.icon || '📚' }}</div>
              <div class="kb-title-box">
                <h4>{{ kb.name }}</h4>
                <span class="kb-date">Tạo: {{ formatDate(kb.createdAt) }}</span>
              </div>
              <button class="btn-icon-danger" title="Xóa kho tri thức" @click="deleteKb(kb.id)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>

            <p class="kb-desc">{{ kb.description || 'Không có mô tả chi tiết' }}</p>

            <div class="kb-stats-row">
              <div class="stat-pill">
                <span class="stat-num">{{ kb.documentCount }}</span>
                <span class="stat-label">Tài liệu</span>
              </div>
              <div class="stat-pill">
                <span class="stat-num">{{ kb.chunkCount }}</span>
                <span class="stat-label">Đoạn Chunks</span>
              </div>
            </div>

            <div class="kb-actions-row">
              <button class="btn-action" @click="openDocumentsModal(kb)">
                <span>📄 Quản lý Tài liệu</span>
              </button>
              <button class="btn-action" @click="openSearchPlayground(kb)">
                <span>🔍 Thử Semantic Search</span>
              </button>
              <button class="btn-action-primary" title="Tạo Agent Tool từ kho tri thức này" @click="createToolFromKb(kb)">
                <span>⚡ Liên kết Agent Tool</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- ========================================================
           TAB 2: THIẾT KẾ CÔNG CỤ (TOOLS STUDIO)
           ======================================================== -->
      <section v-if="currentTab === 'tools'" class="tab-content">
        <div class="content-header">
          <div>
            <h3>Thiết Kế & Quản Lý Công Cụ (Agent Tools)</h3>
            <p>Tạo các công cụ SQL, Markdown Cache, Vector Search hoặc HTTP REST API để AI tự động kích hoạt.</p>
          </div>
          <button class="btn-primary" @click="openCreateToolModal">
            <span>+ Tạo Công Cụ Mới</span>
          </button>
        </div>

        <div v-if="isLoadingTools" class="loading-box">
          <div class="spinner"></div>
          <span>Đang tải danh sách công cụ...</span>
        </div>

        <div v-else class="tools-table-container">
          <table class="studio-table">
            <thead>
              <tr>
                <th>Loại</th>
                <th>Tên Công Cụ</th>
                <th>Mô Tả Chức Năng</th>
                <th>Trạng Thái</th>
                <th>Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in tools" :key="t.id">
                <td>
                  <span :class="['badge-type', `type-${t.toolType.toLowerCase()}`]">
                    {{ formatToolType(t.toolType) }}
                  </span>
                </td>
                <td class="cell-name"><code>{{ t.name }}</code></td>
                <td class="cell-desc">{{ t.description }}</td>
                <td>
                  <label class="switch">
                    <input type="checkbox" :checked="t.enabled" @change="toggleTool(t)" />
                    <span class="slider"></span>
                  </label>
                </td>
                <td>
                  <div class="table-actions">
                    <button class="btn-table-action" @click="openToolTest(t)">▶ Test</button>
                    <button class="btn-table-action danger" @click="deleteTool(t.id)">✕ Xóa</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- ========================================================
           TAB 3: CƠ SỞ DỮ LIỆU H2 (H2 DATABASE EXPLORER)
           ======================================================== -->
      <section v-if="currentTab === 'database'" class="tab-content">
        <div class="content-header">
          <div>
            <h3>Trình Duyệt Cơ Sở Dữ Liệu (H2 Database Explorer)</h3>
            <p>Theo dõi dữ liệu thực tế đang lưu trữ trong file <code>./data/chatbotdb</code>.</p>
          </div>
          <button class="btn-secondary" @click="fetchDbTables">
            <span>🔄 Làm mới</span>
          </button>
        </div>

        <div class="db-explorer-layout">
          <!-- Danh sách bảng -->
          <div class="db-tables-list">
            <h4>Bảng Dữ Liệu</h4>
            <div
              v-for="tbl in dbTables"
              :key="tbl.tableName"
              :class="['table-item', { active: selectedTable === tbl.tableName }]"
              @click="selectTable(tbl.tableName)"
            >
              <span class="tbl-name">{{ tbl.tableName }}</span>
              <span class="tbl-badge">{{ tbl.rowCount }} dòng</span>
            </div>
          </div>

          <!-- Dữ liệu bảng được chọn -->
          <div class="db-table-viewer">
            <div v-if="isLoadingTableData" class="loading-box">
              <div class="spinner"></div>
              <span>Đang đọc dữ liệu bảng {{ selectedTable }}...</span>
            </div>
            <div v-else-if="!currentTableData || currentTableData.rows.length === 0" class="empty-box">
              <p>Bảng <strong>{{ selectedTable }}</strong> hiện chưa có bản ghi nào.</p>
            </div>
            <div v-else class="db-data-scroll">
              <table class="studio-table data-table">
                <thead>
                  <tr>
                    <th v-for="col in currentTableData.columns" :key="col">{{ col }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, rIdx) in currentTableData.rows" :key="rIdx">
                    <td v-for="col in currentTableData.columns" :key="col">
                      <span class="cell-data">{{ row[col] }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- ========================================================
         MODALS / DIALOGS
         ======================================================== -->

    <!-- MODAL 1: TẠO KHO TRI THỨC MỚI -->
    <div v-if="showCreateKbModal" class="modal-backdrop" @click.self="showCreateKbModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <h4>Tạo Kho Tri Thức Mới (Dataset)</h4>
          <button class="btn-close-modal" @click="showCreateKbModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Tên Kho Tri Thức:</label>
            <input v-model="newKb.name" placeholder="Ví dụ: Quy Chuẩn Sản Xuất ISO" />
          </div>
          <div class="form-group">
            <label>Icon Biểu Tượng:</label>
            <input v-model="newKb.icon" placeholder="📚, 🏭, ⚖️, 🛠️..." />
          </div>
          <div class="form-group">
            <label>Mô Tả Tri Thức:</label>
            <textarea v-model="newKb.description" rows="3" placeholder="Mô tả nội dung để AI nhận biết khi nào cần tra cứu..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showCreateKbModal = false">Hủy</button>
          <button class="btn-primary" :disabled="!newKb.name" @click="saveNewKb">Lưu Kho Tri Thức</button>
        </div>
      </div>
    </div>

    <!-- MODAL 2: QUẢN LÝ TÀI LIỆU TRONG KHO (UPLOAD & CHUNKING) -->
    <div v-if="activeKbDocs" class="modal-backdrop" @click.self="activeKbDocs = null">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <div>
            <h4>Tài Liệu: {{ activeKbDocs.name }}</h4>
            <p class="modal-sub">Nạp tài liệu văn bản, tự động cắt thành các đoạn Chunks tối ưu ngữ cảnh.</p>
          </div>
          <button class="btn-close-modal" @click="activeKbDocs = null">✕</button>
        </div>
        <div class="modal-body">
          <!-- Form nạp tài liệu -->
          <div class="doc-upload-box">
            <h5>+ Thêm Tài Liệu Mới</h5>
            <div class="form-row">
              <input v-model="newDoc.title" placeholder="Tiêu đề tài liệu (ví dụ: Quy trình an toàn lao động)" />
              <select v-model="newDoc.sourceType">
                <option value="TEXT">Văn bản thường (Text)</option>
                <option value="MARKDOWN">Định dạng Markdown</option>
                <option value="POLICY">Chính sách / Quy định</option>
              </select>
            </div>
            <textarea
              v-model="newDoc.content"
              rows="5"
              placeholder="Dán nội dung tài liệu văn bản vào đây để hệ thống tự động phân đoạn (Chunking)..."
            ></textarea>
            <div class="doc-upload-actions">
              <button class="btn-primary" :disabled="isUploadingDoc || !newDoc.content" @click="uploadDoc">
                {{ isUploadingDoc ? 'Đang phân đoạn & lưu...' : 'Nạp & Phân Đoạn (Chunking)' }}
              </button>
            </div>
          </div>

          <!-- Danh sách tài liệu hiện có -->
          <div class="doc-list-box">
            <h5>Danh Sách Tài Liệu Đã Nạp ({{ currentKbDocuments.length }})</h5>
            <div v-if="currentKbDocuments.length === 0" class="empty-sub">Chưa có tài liệu nào trong kho này.</div>
            <div v-else class="doc-items">
              <div v-for="doc in currentKbDocuments" :key="doc.id" class="doc-item">
                <div class="doc-item-info">
                  <span class="doc-icon">📄</span>
                  <div>
                    <strong>{{ doc.title }}</strong>
                    <div class="doc-meta">
                      <span>{{ doc.charCount }} ký tự</span> •
                      <span>{{ doc.chunkCount }} đoạn chunks</span> •
                      <span>{{ formatDate(doc.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="activeKbDocs = null">Đóng</button>
        </div>
      </div>
    </div>

    <!-- MODAL 3: SEMANTIC SEARCH PLAYGROUND -->
    <div v-if="activeSearchKb" class="modal-backdrop" @click.self="activeSearchKb = null">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <div>
            <h4>Thử Nghiệm Semantic Search: {{ activeSearchKb.name }}</h4>
            <p class="modal-sub">Gõ câu hỏi để kiểm tra độ chính xác của bộ máy tìm kiếm ngữ nghĩa vector.</p>
          </div>
          <button class="btn-close-modal" @click="activeSearchKb = null">✕</button>
        </div>
        <div class="modal-body">
          <div class="search-input-box">
            <input
              v-model="searchQuery"
              placeholder="Nhập câu hỏi hoặc từ khóa tìm kiếm..."
              @keyup.enter="runSemanticSearch"
            />
            <button class="btn-primary" :disabled="isSearching || !searchQuery" @click="runSemanticSearch">
              {{ isSearching ? 'Đang tìm...' : 'Tìm kiếm' }}
            </button>
          </div>

          <div v-if="searchResults" class="search-results-box">
            <div class="results-header">
              <span>Tìm thấy <strong>{{ searchResults.totalResults }}</strong> đoạn văn liên quan ({{ searchResults.executionTimeMs }}ms):</span>
            </div>
            <div v-if="searchResults.results.length === 0" class="empty-sub">
              Không tìm thấy đoạn văn bản nào phù hợp với câu hỏi này.
            </div>
            <div v-else class="result-list">
              <div v-for="(item, idx) in searchResults.results" :key="idx" class="result-card">
                <div class="result-card-top">
                  <span class="chunk-badge">Chunk #{{ item.chunkIndex }}</span>
                  <span class="score-badge">Độ khớp: {{ Math.round(item.score * 100) }}%</span>
                </div>
                <p class="chunk-text">{{ item.content }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="activeSearchKb = null">Đóng</button>
        </div>
      </div>
    </div>

    <!-- MODAL 4: TẠO CÔNG CỤ MỚI (TOOLS BUILDER WIZARD) -->
    <div v-if="showCreateToolModal" class="modal-backdrop" @click.self="showCreateToolModal = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <h4>Tạo Công Cụ AI Mới (Tool Builder Wizard)</h4>
          <button class="btn-close-modal" @click="showCreateToolModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group flex-1">
              <label>Tên Công Cụ (snake_case):</label>
              <input v-model="newTool.name" placeholder="Ví dụ: get_factory_stats" />
            </div>
            <div class="form-group flex-1">
              <label>Loại Công Cụ:</label>
              <select v-model="newTool.toolType">
                <option value="SQL_QUERY">H2 SQL Query</option>
                <option value="STATIC_MARKDOWN">Markdown Cache</option>
                <option value="QDRANT_VECTOR">Vector / Kho Tri Thức</option>
                <option value="HTTP_API">REST HTTP API</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>Mô Tả Chức Năng Cho AI (Rất quan trọng):</label>
            <input v-model="newTool.description" placeholder="Giải thích cho AI biết công cụ này cung cấp thông tin gì..." />
          </div>

          <!-- Trực quan tham số Parameters Schema -->
          <div class="form-group">
            <div class="label-row">
              <label>Tham Số Gọi (Parameters Schema JSON):</label>
              <button class="btn-link" @click="generateDefaultSchema">Tạo Schema Mẫu</button>
            </div>
            <textarea v-model="newTool.parametersSchema" rows="4" class="code-font"></textarea>
          </div>

          <!-- Cấu hình thực thi Config Data -->
          <div class="form-group">
            <label>
              {{ newTool.toolType === 'SQL_QUERY' ? 'Câu Lệnh SQL Query (H2 DB):' :
                 newTool.toolType === 'STATIC_MARKDOWN' ? 'Nội Dung Markdown Trả Về:' :
                 newTool.toolType === 'HTTP_API' ? 'Cấu Hình Endpoint HTTP:' : 'Cấu Hình Vector Search:' }}
            </label>
            <textarea v-model="newTool.configData" rows="4" class="code-font"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showCreateToolModal = false">Hủy</button>
          <button class="btn-primary" :disabled="!newTool.name || !newTool.configData" @click="saveNewTool">
            Lưu Công Cụ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudioDashboard',
  data() {
    return {
      currentTab: 'knowledge', // 'knowledge' | 'tools' | 'database'

      // Knowledge Base State
      knowledgeBases: [],
      isLoadingKb: false,
      showCreateKbModal: false,
      newKb: { name: '', description: '', icon: '📚' },
      activeKbDocs: null,
      currentKbDocuments: [],
      isUploadingDoc: false,
      newDoc: { title: '', content: '', sourceType: 'TEXT' },
      activeSearchKb: null,
      searchQuery: '',
      isSearching: false,
      searchResults: null,

      // Tools Studio State
      tools: [],
      isLoadingTools: false,
      showCreateToolModal: false,
      newTool: {
        name: '',
        description: '',
        toolType: 'SQL_QUERY',
        parametersSchema: '{\n  "type": "object",\n  "properties": {\n    "query": {\n      "type": "string",\n      "description": "Tham số tìm kiếm"\n    }\n  },\n  "required": ["query"]\n}',
        configData: 'SELECT * FROM weather_logs LIMIT 5',
        enabled: true
      },

      // Database Explorer State
      dbTables: [],
      selectedTable: 'weather_logs',
      currentTableData: null,
      isLoadingTableData: false
    };
  },
  mounted() {
    this.fetchKnowledgeBases();
    this.fetchTools();
    this.fetchDbTables();
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return '';
      try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
      } catch (e) {
        return dateStr;
      }
    },
    formatToolType(type) {
      switch (type) {
        case 'STATIC_MARKDOWN': return 'MARKDOWN CACHE';
        case 'SQL_QUERY': return 'H2 SQL QUERY';
        case 'QDRANT_VECTOR': return 'VECTOR SEARCH';
        case 'HTTP_API': return 'REST HTTP';
        default: return type;
      }
    },

    // 1. Knowledge Base API Calls
    async fetchKnowledgeBases() {
      this.isLoadingKb = true;
      try {
        const res = await fetch('/api/knowledge');
        if (res.ok) {
          this.knowledgeBases = await res.json();
        }
      } catch (e) {
        console.error('Fetch KBs error:', e);
      } finally {
        this.isLoadingKb = false;
      }
    },
    openCreateKbModal() {
      this.newKb = { name: '', description: '', icon: '📚' };
      this.showCreateKbModal = true;
    },
    async saveNewKb() {
      try {
        const res = await fetch('/api/knowledge', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.newKb)
        });
        if (res.ok) {
          this.showCreateKbModal = false;
          await this.fetchKnowledgeBases();
          await this.fetchDbTables();
        }
      } catch (e) {
        console.error('Save KB error:', e);
      }
    },
    async deleteKb(id) {
      if (!confirm('Bạn có chắc chắn muốn xóa kho tri thức này và tất cả tài liệu bên trong?')) return;
      try {
        await fetch(`/api/knowledge/${id}`, { method: 'DELETE' });
        await this.fetchKnowledgeBases();
        await this.fetchDbTables();
      } catch (e) {
        console.error('Delete KB error:', e);
      }
    },
    async openDocumentsModal(kb) {
      this.activeKbDocs = kb;
      this.newDoc = { title: '', content: '', sourceType: 'TEXT' };
      try {
        const res = await fetch(`/api/knowledge/${kb.id}/documents`);
        if (res.ok) {
          this.currentKbDocuments = await res.json();
        }
      } catch (e) {
        console.error('Fetch docs error:', e);
      }
    },
    async uploadDoc() {
      if (!this.activeKbDocs || !this.newDoc.content) return;
      this.isUploadingDoc = true;
      try {
        const res = await fetch(`/api/knowledge/${this.activeKbDocs.id}/documents`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.newDoc)
        });
        if (res.ok) {
          this.newDoc = { title: '', content: '', sourceType: 'TEXT' };
          // Reload docs & KBs
          const docRes = await fetch(`/api/knowledge/${this.activeKbDocs.id}/documents`);
          if (docRes.ok) this.currentKbDocuments = await docRes.json();
          await this.fetchKnowledgeBases();
          await this.fetchDbTables();
        }
      } catch (e) {
        console.error('Upload doc error:', e);
      } finally {
        this.isUploadingDoc = false;
      }
    },
    openSearchPlayground(kb) {
      this.activeSearchKb = kb;
      this.searchQuery = '';
      this.searchResults = null;
    },
    async runSemanticSearch() {
      if (!this.activeSearchKb || !this.searchQuery.trim()) return;
      this.isSearching = true;
      try {
        const res = await fetch(`/api/knowledge/${this.activeSearchKb.id}/search`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ query: this.searchQuery, topK: 5 })
        });
        if (res.ok) {
          this.searchResults = await res.json();
        }
      } catch (e) {
        console.error('Search error:', e);
      } finally {
        this.isSearching = false;
      }
    },
    async createToolFromKb(kb) {
      try {
        const res = await fetch(`/api/knowledge/${kb.id}/create-tool`, { method: 'POST' });
        if (res.ok) {
          const tool = await res.json();
          alert(`Đã liên kết thành công kho tri thức thành Tool: '${tool.name}' cho AI Agent!`);
          await this.fetchTools();
          await this.fetchDbTables();
        }
      } catch (e) {
        alert('Lỗi khi tạo Tool: ' + e.message);
      }
    },

    // 2. Tools Studio API Calls
    async fetchTools() {
      this.isLoadingTools = true;
      try {
        const res = await fetch('/api/tools');
        if (res.ok) {
          this.tools = await res.json();
        }
      } catch (e) {
        console.error('Fetch tools error:', e);
      } finally {
        this.isLoadingTools = false;
      }
    },
    openCreateToolModal() {
      this.newTool = {
        name: '',
        description: '',
        toolType: 'SQL_QUERY',
        parametersSchema: '{\n  "type": "object",\n  "properties": {\n    "query": {\n      "type": "string",\n      "description": "Tham số tìm kiếm"\n    }\n  },\n  "required": ["query"]\n}',
        configData: 'SELECT * FROM weather_logs LIMIT 5',
        enabled: true
      };
      this.showCreateToolModal = true;
    },
    generateDefaultSchema() {
      this.newTool.parametersSchema = JSON.stringify({
        type: 'object',
        properties: {
          keyword: {
            type: 'string',
            description: 'Từ khóa hoặc câu hỏi cần tra cứu'
          }
        },
        required: ['keyword']
      }, null, 2);
    },
    async saveNewTool() {
      try {
        const res = await fetch('/api/tools', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.newTool)
        });
        if (res.ok) {
          this.showCreateToolModal = false;
          await this.fetchTools();
          await this.fetchDbTables();
        }
      } catch (e) {
        console.error('Save tool error:', e);
      }
    },
    async toggleTool(t) {
      try {
        const res = await fetch(`/api/tools/${t.id}/toggle`, { method: 'POST' });
        if (res.ok) {
          const updated = await res.json();
          t.enabled = updated.enabled;
        }
      } catch (e) {
        console.error('Toggle tool error:', e);
      }
    },
    async deleteTool(id) {
      if (!confirm('Bạn có chắc muốn xóa công cụ này?')) return;
      try {
        await fetch(`/api/tools/${id}`, { method: 'DELETE' });
        await this.fetchTools();
        await this.fetchDbTables();
      } catch (e) {
        console.error('Delete tool error:', e);
      }
    },
    openToolTest(t) {
      const paramVal = prompt(`Nhập tham số JSON để test tool '${t.name}':`, '{"city": "Hà Nội"}');
      if (paramVal === null) return;
      let body = {};
      try { body = JSON.parse(paramVal); } catch (e) { alert('JSON không hợp lệ'); return; }
      fetch(`/api/tools/${t.id}/test`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      })
      .then(r => r.json())
      .then(res => {
        alert(`Kết quả (${res.executionTimeMs}ms):\n\n${res.output}`);
      })
      .catch(err => alert('Lỗi: ' + err.message));
    },

    // 3. Database Explorer API Calls
    async fetchDbTables() {
      try {
        const res = await fetch('/api/db/tables');
        if (res.ok) {
          this.dbTables = await res.json();
          if (this.dbTables.length > 0 && !this.selectedTable) {
            this.selectTable(this.dbTables[0].tableName);
          } else {
            this.selectTable(this.selectedTable);
          }
        }
      } catch (e) {
        console.error('Fetch tables error:', e);
      }
    },
    async selectTable(name) {
      this.selectedTable = name;
      this.isLoadingTableData = true;
      try {
        const res = await fetch(`/api/db/tables/${name}`);
        if (res.ok) {
          this.currentTableData = await res.json();
        }
      } catch (e) {
        console.error('Fetch table data error:', e);
      } finally {
        this.isLoadingTableData = false;
      }
    }
  }
};
</script>

<style scoped>
.studio-container {
  min-height: 100vh;
  background: #090d16;
  color: #f1f5f9;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  display: flex;
  flex-direction: column;
}

/* TOPBAR */
.studio-header {
  height: 64px;
  background: #0d1322;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.studio-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 38px;
  height: 38px;
  background: rgba(0, 240, 255, 0.12);
  border: 1px solid rgba(0, 240, 255, 0.35);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.2);
}

.brand-text h2 {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: #ffffff;
  letter-spacing: 0.2px;
}

.brand-text span {
  font-size: 11px;
  color: #94a3b8;
}

.studio-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  background: transparent;
  border: 1px solid transparent;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #ffffff;
}

.nav-item.active {
  background: rgba(0, 240, 255, 0.1);
  border-color: rgba(0, 240, 255, 0.35);
  color: #00f0ff;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
}

.nav-count {
  background: rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  padding: 1px 6px;
  border-radius: 10px;
  font-size: 11px;
}

.btn-switch-chat {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #00f0ff;
  color: #020617;
  border: none;
  font-weight: 700;
  font-size: 12px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.3);
}

.btn-switch-chat:hover {
  background: #38bdf8;
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.5);
}

/* MAIN */
.studio-main {
  flex: 1;
  padding: 32px 40px;
  max-width: 1360px;
  margin: 0 auto;
  width: 100%;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.content-header h3 {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 4px 0;
  color: #ffffff;
}

.content-header p {
  color: #94a3b8;
  font-size: 13px;
  margin: 0;
}

.btn-primary {
  background: #00f0ff;
  color: #020617;
  border: none;
  font-weight: 700;
  font-size: 13px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #38bdf8;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f1f5f9;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.14);
}

/* GRID & CARDS */
.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.kb-card {
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  transition: all 0.2s ease;
}

.kb-card:hover {
  border-color: rgba(0, 240, 255, 0.4);
  box-shadow: 0 12px 32px rgba(0, 240, 255, 0.1);
}

.kb-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.kb-icon-box {
  font-size: 26px;
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.kb-title-box {
  flex: 1;
}

.kb-title-box h4 {
  font-size: 16px;
  margin: 0;
  color: #ffffff;
}

.kb-date {
  font-size: 11px;
  color: #64748b;
}

.btn-icon-danger {
  background: transparent;
  border: none;
  color: #ef4444;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
}

.btn-icon-danger:hover {
  background: rgba(239, 68, 68, 0.15);
}

.kb-desc {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
  margin: 0 0 16px 0;
  flex: 1;
}

.kb-stats-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-pill {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 8px;
  border-radius: 8px;
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #00f0ff;
}

.stat-label {
  font-size: 11px;
  color: #64748b;
}

.kb-actions-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-action {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  font-size: 12px;
  font-weight: 600;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
}

.btn-action:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.btn-action-primary {
  background: rgba(0, 240, 255, 0.15);
  border: 1px solid rgba(0, 240, 255, 0.35);
  color: #00f0ff;
  font-size: 12px;
  font-weight: 700;
  padding: 9px;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
}

.btn-action-primary:hover {
  background: rgba(0, 240, 255, 0.25);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
}

/* TABLES */
.tools-table-container {
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.studio-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 13px;
}

.studio-table th {
  background: #090d16;
  padding: 12px 16px;
  color: #94a3b8;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.studio-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: #cbd5e1;
}

.cell-name code {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
  padding: 3px 6px;
  border-radius: 4px;
}

.cell-desc {
  max-width: 320px;
  font-size: 12px;
  line-height: 1.4;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.btn-table-action {
  background: rgba(255, 255, 255, 0.08);
  border: none;
  color: #f1f5f9;
  font-size: 11px;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-table-action.danger {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
}

/* DB EXPLORER */
.db-explorer-layout {
  display: flex;
  gap: 20px;
  height: 600px;
}

.db-tables-list {
  width: 260px;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.db-tables-list h4 {
  font-size: 13px;
  color: #94a3b8;
  margin: 0 0 8px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid transparent;
  transition: all 0.2s;
}

.table-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.table-item.active {
  background: rgba(0, 240, 255, 0.12);
  border-color: rgba(0, 240, 255, 0.4);
  color: #00f0ff;
}

.tbl-badge {
  font-size: 10px;
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
  color: #cbd5e1;
}

.db-table-viewer {
  flex: 1;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.db-data-scroll {
  overflow: auto;
  flex: 1;
}

.data-table th {
  white-space: nowrap;
}

.cell-data {
  font-family: Consolas, monospace;
  font-size: 12px;
  color: #e2e8f0;
}

/* BADGES */
.badge-type {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
}
.type-static_markdown { background: rgba(16, 185, 129, 0.2); color: #10b981; }
.type-sql_query { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
.type-qdrant_vector { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
.type-http_api { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }

/* SWITCH */
.switch { position: relative; display: inline-block; width: 34px; height: 18px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #334155; transition: .3s; border-radius: 20px; }
.slider:before { position: absolute; content: ""; height: 12px; width: 12px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; }
input:checked + .slider { background-color: #00f0ff; }
input:checked + .slider:before { transform: translateX(16px); background-color: #020617; }

/* MODALS */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.75);
  backdrop-filter: blur(8px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-box {
  width: 100%;
  max-width: 520px;
  background: #0f172a;
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 14px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-box.modal-lg {
  max-width: 800px;
}

.modal-header {
  padding: 16px 20px;
  background: #090d16;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
  color: #ffffff;
}

.modal-sub {
  font-size: 12px;
  color: #94a3b8;
  margin: 2px 0 0 0;
}

.btn-close-modal {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 16px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 70vh;
  overflow-y: auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.flex-1 {
  flex: 1;
}

.form-row {
  display: flex;
  gap: 12px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-link {
  background: transparent;
  border: none;
  color: #00f0ff;
  font-size: 11px;
  cursor: pointer;
  text-decoration: underline;
}

.form-group label {
  font-size: 12px;
  font-weight: 600;
  color: #cbd5e1;
}

.form-group input, .form-group textarea, .form-group select, .search-input-box input {
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f1f5f9;
  padding: 10px;
  border-radius: 8px;
  outline: none;
  font-family: inherit;
  font-size: 13px;
}

.form-group input:focus, .form-group textarea:focus, .form-group select:focus {
  border-color: #00f0ff;
}

.code-font {
  font-family: Consolas, monospace !important;
  font-size: 12px !important;
}

.modal-footer {
  padding: 14px 20px;
  background: #090d16;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* SEARCH PLAYGROUND */
.search-input-box {
  display: flex;
  gap: 10px;
}

.search-input-box input {
  flex: 1;
}

.search-results-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.results-header {
  font-size: 12px;
  color: #94a3b8;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-card {
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  padding: 12px;
}

.result-card-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.chunk-badge {
  font-size: 11px;
  color: #38bdf8;
  font-weight: 600;
}

.score-badge {
  font-size: 11px;
  color: #10b981;
  font-weight: 700;
  background: rgba(16, 185, 129, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.chunk-text {
  font-size: 13px;
  color: #cbd5e1;
  margin: 0;
  line-height: 1.5;
  white-space: pre-wrap;
}

/* DOC UPLOAD BOX */
.doc-upload-box {
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.doc-upload-box h5, .doc-list-box h5 {
  margin: 0;
  font-size: 13px;
  color: #00f0ff;
}

.doc-upload-actions {
  display: flex;
  justify-content: flex-end;
}

.doc-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 240px;
  overflow-y: auto;
}

.doc-item {
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doc-item-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.doc-icon {
  font-size: 20px;
}

.doc-meta {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

/* COMMONS */
.loading-box, .empty-box {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-box h4 {
  color: #ffffff;
  margin: 0 0 6px 0;
}

.empty-box p {
  font-size: 13px;
  max-width: 400px;
  margin: 0 auto 16px auto;
}

.empty-sub {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 12px;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 240, 255, 0.2);
  border-top-color: #00f0ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px auto;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
