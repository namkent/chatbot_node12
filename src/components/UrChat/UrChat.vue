<template>
  <div class="ur-chat-app-container">
    <!-- Left Sidebar: Gemini Style -->
    <chat-sidebar
      :sessions="sessions"
      :active-session-id="activeSessionId"
      :is-collapsed="sidebarCollapsed"
      :brand-title="brandTitle"
      :user-name="userName"
      @select-session="handleSelectSession"
      @new-chat="handleNewChat"
      @delete-session="handleDeleteSession"
      @toggle-collapse="sidebarCollapsed = !sidebarCollapsed"
    />

    <!-- Right Main Chat Panel -->
    <main class="ur-chat-main-panel">
      <!-- Header -->
      <div class="ur-chat-top-header">
        <chat-header
          :bot-name="effectiveBotName"
          :status-label="statusLabel"
          :is-loading="isLoading"
          :is-streaming="isStreaming"
          :has-bot-error="hasBotError"
          :is-fullscreen="false"
          :is-expanded="false"
          :show-fullscreen-btn="false"
          :show-expand-btn="false"
          :show-close-btn="false"
          :show-sidebar-toggle="false"
          :sidebar-collapsed="sidebarCollapsed"
          @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
          @reset="promptResetChat"
        />
      </div>

      <!-- Chat Body Message List -->
      <div class="ur-chat-content-flow">
        <message-list
          ref="messageList"
          :messages="visibleMessageList"
          :hidden-earlier-count="hiddenEarlierCount"
          :thinking="thinking"
          :is-streaming="isStreaming"
          :speaking-msg-id="speakingMsgId"
          :suggested-prompts="suggestedPrompts"
          :suggested-title="suggestedTitle"
          @load-earlier="loadEarlierMessages"
          @scroll="handleBodyScroll"
          @body-click="handleBodyClick"
          @open-image="openImageModal($event.src, $event.alt)"
          @toggle-speak="toggleSpeak"
          @regenerate="regenerateMessage"
          @prev-version="prevMsgVersion"
          @next-version="nextMsgVersion"
          @send-suggested-prompt="sendSuggestedPrompt"
        />
      </div>

      <!-- Chat Input Footer -->
      <div class="ur-chat-bottom-dock">
        <chat-input
          ref="chatInput"
          v-model="inputMsg"
          :placeholder="placeholderText"
          :is-loading="isLoading"
          :is-streaming="isStreaming"
          :can-attach-file="canAttachFile"
          :pending-images="pendingImages"
          :is-thinking-active="isThinkingActive"
          :is-current-model-support-thinking="isCurrentModelSupportThinking"
          :is-current-model-support-vision="isCurrentModelSupportVision"
          :thinking-tooltip="thinkingTooltip"
          :show-model-dropdown="showModelDropdown"
          :current-model-display-name="currentModelDisplayName"
          :effective-model="effectiveModel"
          :model-list-options="modelListOptions"
          :show-scroll-bottom-btn="showScrollBottomBtn"
          @send="handleSendMessage"
          @stop="stopStreaming"
          @scroll-bottom="scrollToBottom(true)"
          @open-image="openImageModal($event.src, $event.alt)"
          @remove-image="removePendingImage"
          @files-selected="handleFilesSelected"
          @toggle-thinking="toggleThinkingActive"
          @toggle-model-dropdown="toggleModelDropdown"
          @select-model="selectModel"
        />
      </div>
    </main>

    <!-- Modals -->
    <reset-confirm-modal
      :visible="showResetConfirm"
      @cancel="cancelReset"
      @confirm="confirmReset"
    />

    <image-lightbox-modal
      :visible="activeImageModal.visible"
      :src="activeImageModal.src"
      :alt="activeImageModal.alt"
      @close="closeImageModal"
    />

    <mermaid-lightbox-modal
      :visible="activeMermaidModal.visible"
      :svg-html="activeMermaidModal.svgHtml"
      @close="closeMermaidModal"
    />
  </div>
</template>

<script>
import './styles/index.scss';

import ChatSidebar from './components/ChatSidebar.vue';
import ChatHeader from './components/ChatHeader.vue';
import MessageList from './components/MessageList.vue';
import ChatInput from './components/ChatInput.vue';
import ResetConfirmModal from './components/ResetConfirmModal.vue';
import ImageLightboxModal from './components/ImageLightboxModal.vue';
import MermaidLightboxModal from './components/MermaidLightboxModal.vue';

import { parseAndSanitizeMarkdown } from './services/markdownParser';
import {
  renderMermaidDiagrams,
  downloadSvgElementAsPng,
  downloadSvgElementAsSvg
} from './services/mermaidEngine';
import { detectLanguage, speakText, stopSpeaking } from './services/voiceService';
import { uploadImageToMinio, deleteImageFromMinio } from './services/minioUploadService';
import {
  getAllSessions,
  saveAllSessions,
  createNewSession,
  updateSession,
  deleteSession,
  fetchSessionTitle,
  fetchRemoteSessions,
  fetchRemoteSessionDetail,
  syncSessionToRemote,
  DEFAULT_USER_ID,
  DEFAULT_USER_NAME
} from './services/chatSessionService';

export default {
  name: 'UrChat',
  components: {
    ChatSidebar,
    ChatHeader,
    MessageList,
    ChatInput,
    ResetConfirmModal,
    ImageLightboxModal,
    MermaidLightboxModal
  },
  props: {
    brandTitle: {
      type: String,
      default: 'Assistant Chat'
    },
    userId: {
      type: String,
      default: DEFAULT_USER_ID
    },
    userName: {
      type: String,
      default: DEFAULT_USER_NAME
    },
    initialSessionId: {
      type: String,
      default: ''
    },
    botName: {
      type: String,
      default: 'Astro Bot AI'
    },
    statusText: {
      type: String,
      default: 'Groq • Qwen 3.8 (27B)'
    },
    placeholderText: {
      type: String,
      default: 'Type a message (Shift + Enter for new line)...'
    },
    model: {
      type: String,
      default: ''
    },
    knowledgeBase: {
      type: String,
      default: ''
    },
    systemPrompt: {
      type: String,
      default: 'Bạn là Astro Bot AI - một trợ lý không gian thông minh am hiểu công nghệ, lập trình và khoa học.'
    },
    historyLimit: {
      type: Number,
      default: 5
    },
    apiUrl: {
      type: String,
      default: ''
    },
    virtualScroll: {
      type: Boolean,
      default: true
    },
    maxVisibleMessages: {
      type: Number,
      default: 50
    },
    attachFile: {
      type: Boolean,
      default: true
    },
    storeFile: {
      type: Boolean,
      default: false
    },
    thinking: {
      type: Boolean,
      default: false
    },
    models: {
      type: Array,
      default: () => [
        { id: 'openai/gpt-oss-120b', name: 'GPT-OSS 120B', desc: 'Lý luận sâu, toán học, code & thị giác', thinking: true, vision: true },
        { id: 'qwen/qwen3.8-27b', name: 'Qwen 3.8 27B', desc: 'Đa phương thức, hiểu ảnh & tiếng Việt', thinking: false, vision: true },
        { id: 'qwen/qwen3.6-27b', name: 'Qwen 3.6 27B', desc: 'Nhận diện ảnh nhanh, siêu tốc', thinking: false, vision: true },
        { id: 'openai/gpt-oss-20b', name: 'GPT-OSS 20B', desc: 'Suy luận nhanh, tốc độ phản hồi cao', thinking: true, vision: false }
      ]
    }
  },
  data() {
    return {
      sidebarCollapsed: false,
      sessions: [],
      activeSessionId: null,
      isUnsavedNewSession: false,
      activeImageModal: { visible: false, src: '', alt: '' },
      activeMermaidModal: { visible: false, svgHtml: '' },
      showResetConfirm: false,
      showModelDropdown: false,
      isThinkingActive: !!this.thinking,
      pendingImages: [],
      inputMsg: '',
      isLoading: false,
      isStreaming: false,
      isErrorState: false,
      effectiveModel: this.model || 'openai/gpt-oss-120b',
      effectiveBotName: this.botName,
      abortController: null,
      showScrollBottomBtn: false,
      speakingMsgId: null,
      requestStartTime: null,
      thinkingStartTime: null,
      suggestedPrompts: [],
      suggestedTitle: '',
      visibleCount: this.maxVisibleMessages || 50,
      messageList: [],
      apiMessagesHistory: []
    };
  },
  computed: {
    canAttachFile() {
      return this.attachFile || this.storeFile;
    },
    shouldStoreFile() {
      return !!this.storeFile;
    },
    hasBotError() {
      return this.isErrorState;
    },
    statusLabel() {
      if (this.hasBotError) return 'Lỗi kết nối • Click để thử lại';
      if (this.isLoading) return 'Thinking...';
      if (this.isStreaming) return 'Streaming...';
      return this.statusText || 'Online';
    },
    visibleMessageList() {
      if (!this.virtualScroll || this.messageList.length <= this.visibleCount) {
        return this.messageList;
      }
      return this.messageList.slice(-this.visibleCount);
    },
    hiddenEarlierCount() {
      if (!this.virtualScroll) return 0;
      const hidden = this.messageList.length - this.visibleCount;
      return hidden > 0 ? hidden : 0;
    },
    modelListOptions() {
      return this.models && this.models.length > 0 ? this.models : [];
    },
    currentModelDisplayName() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      return found ? found.name : this.effectiveModel;
    },
    isCurrentModelSupportVision() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      if (!found) {
        return /vision|vl|qwen|gpt-4o|gemini|claude|120b/i.test(this.effectiveModel);
      }
      if (typeof found.vision === 'boolean') return found.vision;
      if (typeof found.supportsVision === 'boolean') return found.supportsVision;
      return (found.badge === 'Vision' || /vision|vl|qwen|gpt-4o|gemini|claude|120b/i.test(found.id));
    },
    isCurrentModelSupportThinking() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      if (!found) {
        return /gpt-oss|120b|20b|deepseek-r1|reasoning|r1/i.test(this.effectiveModel);
      }
      if (typeof found.thinking === 'boolean') return found.thinking;
      if (typeof found.supportsThinking === 'boolean') return found.supportsThinking;
      return (found.badge === 'Reasoning' || /gpt-oss|120b|20b|deepseek-r1|reasoning|r1/i.test(found.id));
    },
    thinkingTooltip() {
      if (!this.isCurrentModelSupportThinking) {
        return 'Mô hình này không hỗ trợ suy luận (Reasoning not supported)';
      }
      return this.isThinkingActive
        ? 'Thinking: ĐANG BẬT (AI sẽ tư duy logic trước khi trả lời)'
        : 'Thinking: ĐANG TẮT (Bấm để bật tư duy suy luận)';
    }
  },
  watch: {
    model(val) {
      this.effectiveModel = val || 'openai/gpt-oss-120b';
    },
    thinking(val) {
      this.isThinkingActive = !!val;
    }
  },
  created() {
    this.initSessions();
  },
  mounted() {
    this._escKeyHandler = (e) => {
      if (e.key === 'Escape') {
        if (this.activeMermaidModal.visible) {
          this.closeMermaidModal();
        } else if (this.activeImageModal.visible) {
          this.closeImageModal();
        } else if (this.showResetConfirm) {
          this.cancelReset();
        }
      }
    };
    window.addEventListener('keydown', this._escKeyHandler);
    this.scheduleMermaidRender();

    this._docClickListener = (e) => {
      if (this.showModelDropdown) {
        const dd = this.$el.querySelector('.ur-chatbot-model-select-wrapper');
        if (dd && !dd.contains(e.target)) {
          this.showModelDropdown = false;
        }
      }
    };
    document.addEventListener('click', this._docClickListener);
  },
  beforeDestroy() {
    if (this._escKeyHandler) {
      window.removeEventListener('keydown', this._escKeyHandler);
    }
    if (this._docClickListener) {
      document.removeEventListener('click', this._docClickListener);
    }
    stopSpeaking();
  },
  methods: {
    startFreshNewChat() {
      if (this.isStreaming) this.stopStreaming();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      const existingEmpty = this.sessions.find(
        s => s.title === 'Cuộc trò chuyện mới' && (!s.messageList || s.messageList.filter(m => m.sender === 'user').length === 0)
      );

      let sess;
      if (existingEmpty) {
        sess = existingEmpty;
      } else {
        sess = createNewSession('Cuộc trò chuyện mới', [], this.systemPrompt, this.userId, this.userName);
        this.sessions.unshift(sess);
      }
      this.isUnsavedNewSession = true;
      this.loadSession(sess);
    },
    async initSessions() {
      // 1. Nạp tức thì từ local cache để giao diện hiển thị 0ms
      const local = getAllSessions();
      if (local && local.length > 0) {
        this.sessions = local;
      }

      // 2. Nếu có initialSessionId từ URL
      if (this.initialSessionId) {
        let targetSession = this.sessions.find(s => s.id === this.initialSessionId);

        // Thử tải chi tiết từ remote H2 DB
        try {
          const remoteDetail = await fetchRemoteSessionDetail(this.initialSessionId, this.userId);
          if (remoteDetail) {
            targetSession = remoteDetail;
            const idx = this.sessions.findIndex(s => s.id === this.initialSessionId);
            if (idx !== -1) {
              this.$set(this.sessions, idx, remoteDetail);
            } else {
              this.sessions.unshift(remoteDetail);
            }
          }
        } catch (err) {
          console.warn('[UrChat] Lỗi nạp session remote:', err);
        }

        // Tải danh sách remote sessions để hiển thị lịch sử ở sidebar
        try {
          const remoteList = await fetchRemoteSessions(this.userId);
          if (remoteList && remoteList.length > 0) {
            this.sessions = remoteList;
          }
        } catch (err) {}

        if (targetSession) {
          this.isUnsavedNewSession = false;
          this.loadSession(targetSession);
          this.$emit('session-change', targetSession.id);
        } else {
          // Session id KHÔNG tồn tại: chuyển về /chat với cuộc trò chuyện mới
          console.warn(`[UrChat] Session id '${this.initialSessionId}' không tồn tại. Chuyển về /chat với cuộc trò chuyện mới.`);
          this.startFreshNewChat();
          this.$emit('session-change', null);
        }
      } else {
        // Vào thẳng /chat không có sessionId
        this.startFreshNewChat();
        this.$emit('session-change', null);

        // Tải danh sách remote sessions để hiển thị lịch sử ở sidebar
        try {
          const remoteList = await fetchRemoteSessions(this.userId);
          if (remoteList && remoteList.length > 0) {
            this.sessions = remoteList;
          }
        } catch (err) {}
      }
    },
    loadSession(session) {
      if (!session) return;
      this.activeSessionId = session.id;
      this.suggestedPrompts = Array.isArray(session.suggestedPrompts) ? session.suggestedPrompts : [];
      this.suggestedTitle = session.suggestedTitle || '';
      const rawList = Array.isArray(session.messageList) ? session.messageList : [];
      this.messageList = rawList.map((m, idx) => {
        const isGreeting = (idx === 0 && m.id === 1 && (!rawList[1] || rawList[1].sender === 'user'));
        const normalizedSender = (m.sender === 'bot' && !isGreeting && m.id !== 1 && !m.isResetNotice) ? 'assistant' : m.sender;
        return {
          ...m,
          sender: normalizedSender,
          html: (normalizedSender === 'bot' || normalizedSender === 'assistant') && m.text ? this.renderHtml(m.text, false) : ''
        };
      });
      if (this.messageList.length === 0) {
        this.messageList = [
          {
            id: 1,
            sender: 'bot',
            text: 'Hello! 👋\nI am **Astro Bot AI**, your assistant. How can I help you today?',
            html: '',
            responseTime: ''
          }
        ];
      }
      this.apiMessagesHistory = Array.isArray(session.apiMessagesHistory) && session.apiMessagesHistory.length > 0
        ? session.apiMessagesHistory
        : [{ role: 'system', content: this.systemPrompt }];
      this.saveCurrentSession();
      this.$nextTick(() => {
        this.scrollToBottom();
        this.scheduleMermaidRender();
      });
    },
    saveCurrentSession() {
      if (!this.activeSessionId) return;
      const current = this.sessions.find(s => s.id === this.activeSessionId);
      if (current) {
        // Đặt tạm tiêu đề hiển thị nhanh nếu chưa có
        const firstUserMsg = this.messageList.find(m => m.sender === 'user');
        if (firstUserMsg && (!current.title || current.title === 'Cuộc trò chuyện mới')) {
          const rawSlice = (firstUserMsg.text || '').trim().slice(0, 24);
          current.title = rawSlice ? ('💬 ' + rawSlice) : 'Cuộc trò chuyện mới';
        }
        current.messageList = this.messageList;
        current.apiMessagesHistory = this.apiMessagesHistory;
        current.suggestedPrompts = this.suggestedPrompts;
        current.suggestedTitle = this.suggestedTitle;
        current.updatedAt = Date.now();
        current.userId = this.userId;
        current.userName = this.userName;
        saveAllSessions(this.sessions);
        syncSessionToRemote(current, this.userId, this.userName);
      }
    },
    async checkAndUpdateSessionTitle(forceReTitle = false) {
      if (!this.activeSessionId) return;
      const current = this.sessions.find(s => s.id === this.activeSessionId);
      if (!current || current.isCustomRenamed) return;

      const userMsgs = (this.messageList || []).filter(m => m.sender === 'user');
      if (userMsgs.length === 0) return;

      const isDefaultOrTemp = !current.title ||
        current.title === 'Cuộc trò chuyện mới' ||
        current.title.startsWith('💬 ');

      // Lần 1: Tin nhắn đầu tiên -> Sinh tiêu đề ngay
      const needsFirstTitle = isDefaultOrTemp;

      // Lần 2 (Đánh giá theo yêu cầu user): Nếu sau vài tin nhắn (2 - 4 tin) mà tiêu đề hiện tại
      // chỉ là câu chào tạm thời (chứa emoji 👋 hoặc lời chào ngắn) và chưa từng được user đổi tên thủ công
      const isGreetingTitle = current.title && (current.title.includes('👋') || /^(chào|xin chào|hello|hi)\b/i.test(current.title.replace(/^[\p{Emoji}\s]+/u, '')));
      const needsSmartReTitle = forceReTitle || (isGreetingTitle && userMsgs.length >= 2 && !current.hasSmartReTitled);

      if (needsFirstTitle || needsSmartReTitle) {
        try {
          const apiMsgs = (this.apiMessagesHistory || [])
            .filter(m => m.role === 'user' || m.role === 'assistant')
            .slice(0, 6);

          const generatedTitle = await fetchSessionTitle(apiMsgs, this.effectiveModel);
          if (generatedTitle && generatedTitle.trim()) {
            current.title = generatedTitle.trim();
            if (needsSmartReTitle) {
              current.hasSmartReTitled = true;
            }
            saveAllSessions(this.sessions);
            this.$forceUpdate();
          }
        } catch (e) {
          console.warn('[UrChat] Lỗi gọi tự động sinh tiêu đề:', e);
        }
      }
    },
    handleSelectSession(sessionIdOrSession) {
      const sessId = typeof sessionIdOrSession === 'object' && sessionIdOrSession ? sessionIdOrSession.id : sessionIdOrSession;
      this.switchToSession(sessId);
    },
    async switchToSession(sessionId) {
      if (!sessionId) {
        if (!this.isUnsavedNewSession) {
          this.startFreshNewChat();
        }
        return;
      }
      if (sessionId === this.activeSessionId) return;
      if (this.isStreaming) this.stopStreaming();
      this.saveCurrentSession();

      let targetSession = this.sessions.find(s => s.id === sessionId);
      if (!targetSession || !targetSession.messageList || targetSession.messageList.length === 0) {
        const detail = await fetchRemoteSessionDetail(sessionId, this.userId);
        if (detail) {
          targetSession = detail;
          const idx = this.sessions.findIndex(s => s.id === sessionId);
          if (idx !== -1) {
            this.$set(this.sessions, idx, detail);
          } else {
            this.sessions.unshift(detail);
          }
        }
      }

      if (targetSession) {
        this.isUnsavedNewSession = false;
        this.loadSession(targetSession);
        this.$emit('session-change', targetSession.id);
      } else {
        // Session ID không tồn tại: chuyển về /chat với cuộc trò chuyện mới
        console.warn(`[UrChat] Session id '${sessionId}' không tồn tại. Chuyển về /chat với cuộc trò chuyện mới.`);
        this.startFreshNewChat();
        this.$emit('session-change', null);
      }
    },
    handleNewChat() {
      if (this.isStreaming) this.stopStreaming();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.saveCurrentSession();
      this.startFreshNewChat();
      this.$emit('session-change', null);
    },
    handleDeleteSession(sessionId) {
      const remaining = deleteSession(sessionId, this.userId);
      this.sessions = remaining;
      if (this.activeSessionId === sessionId) {
        if (this.sessions.length > 0) {
          this.loadSession(this.sessions[0]);
          this.$emit('session-change', this.sessions[0].id);
        } else {
          this.handleNewChat();
        }
      }
    },
    renderHtml(text, isStreaming) {
      return parseAndSanitizeMarkdown(text, !!isStreaming);
    },
    toggleModelDropdown() {
      this.showModelDropdown = !this.showModelDropdown;
    },
    selectModel(modelId) {
      this.effectiveModel = modelId;
      this.showModelDropdown = false;
      if (this.isCurrentModelSupportThinking && this.thinking && !this.isThinkingActive) {
        this.isThinkingActive = true;
      }
      this.$emit('model-change', modelId);
    },
    toggleThinkingActive() {
      if (!this.isCurrentModelSupportThinking) return;
      this.isThinkingActive = !this.isThinkingActive;
      this.$emit('update:thinking', this.isThinkingActive);
    },
    promptResetChat() {
      if (this.isStreaming) return;
      this.showResetConfirm = true;
    },
    cancelReset() {
      this.showResetConfirm = false;
    },
    confirmReset() {
      this.showResetConfirm = false;
      this.clearMessages();
    },
    clearMessages() {
      if (this.isStreaming) this.stopStreaming();
      this.inputMsg = '';
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.messageList = [
        {
          id: Date.now(),
          sender: 'bot',
          text: 'Conversation has been reset! 🌌',
          html: this.renderHtml('Conversation has been reset! 🌌', false),
          isResetNotice: true
        }
      ];
      this.apiMessagesHistory = [{ role: 'system', content: this.systemPrompt }];
      this.saveCurrentSession();
    },
    scheduleMermaidRender() {
      if (this._mermaidRenderTimer) {
        clearTimeout(this._mermaidRenderTimer);
      }
      const delay = this.isStreaming ? 500 : 50;
      this._mermaidRenderTimer = setTimeout(() => {
        this._mermaidRenderTimer = null;
        renderMermaidDiagrams(this.$el, this.isStreaming);
      }, delay);
    },
    openImageModal(src, alt) {
      this.activeImageModal = { visible: true, src: src, alt: alt || '' };
    },
    closeImageModal() {
      this.activeImageModal.visible = false;
    },
    openMermaidModal(svgHtml) {
      if (!svgHtml) return;
      let cleanSvg = svgHtml
        .replace(/\bmax-width:\s*[^;"]+;?/gi, '')
        .replace(/\bheight:\s*auto\s*!important;?/gi, '')
        .replace(/\bheight:\s*auto;?/gi, '')
        .replace(/style="[^"]*"/i, 'style="width:100%;height:100%;display:block;"');

      if (!cleanSvg.includes('width="100%"')) {
        cleanSvg = cleanSvg.replace(/<svg\b([^>]*)>/i, '<svg$1 width="100%" height="100%">');
      }

      this.activeMermaidModal = { visible: true, svgHtml: cleanSvg };
    },
    closeMermaidModal() {
      this.activeMermaidModal.visible = false;
    },
    handleBodyClick(e) {
      const copyBtn = e.target.closest('.ur-chatbot-copy-btn, .copy-code-btn, .ur-chatbot-btn-copy-code, .btn-copy-code');
      if (copyBtn && window.__copyCodeBlock) {
        window.__copyCodeBlock(copyBtn);
        return;
      }
      const imgWrapper = e.target.closest('.ur-chatbot-image-wrapper, .ur-chatbot-code-img-item');
      if (imgWrapper) {
        const rawSrc = imgWrapper.getAttribute('data-src');
        const rawAlt = imgWrapper.getAttribute('data-alt');
        const imgEl = imgWrapper.querySelector('img');
        if (imgEl && imgEl.classList.contains('error')) return;
        const src = rawSrc || (imgEl ? imgEl.src : '');
        const alt = rawAlt ? decodeURIComponent(rawAlt) : (imgEl ? imgEl.alt : '');
        if (src) this.openImageModal(src, alt);
        return;
      }
      const toggleMermaidBtn = e.target.closest('.btn-toggle-mermaid-code');
      if (toggleMermaidBtn) {
        const card = toggleMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const codeView = card.querySelector('.ur-chatbot-mermaid-code-view');
          if (codeView) {
            const isHidden = codeView.style.display === 'none';
            codeView.style.display = isHidden ? 'block' : 'none';
            toggleMermaidBtn.classList.toggle('is-active', isHidden);
          }
        }
        return;
      }
      const copyMermaidBtn = e.target.closest('.btn-copy-mermaid');
      if (copyMermaidBtn) {
        const rawCode = copyMermaidBtn.getAttribute('data-code');
        if (rawCode) {
          const codeText = decodeURIComponent(rawCode);
          const copySuccess = () => {
            const iconCopy = copyMermaidBtn.querySelector('.ur-chatbot-mermaid-icon-copy');
            const iconCopied = copyMermaidBtn.querySelector('.ur-chatbot-mermaid-icon-copied');
            if (iconCopy && iconCopied) {
              iconCopy.style.display = 'none';
              iconCopied.style.display = 'inline-block';
            }
            copyMermaidBtn.title = 'Copied!';
            copyMermaidBtn.classList.add('copied');
            setTimeout(() => {
              if (iconCopy && iconCopied) {
                iconCopy.style.display = '';
                iconCopied.style.display = 'none';
              }
              copyMermaidBtn.title = 'Sao chép mã';
              copyMermaidBtn.classList.remove('copied');
            }, 2000);
          };
          if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(codeText).then(copySuccess).catch(() => {
              window.__copyCodeBlock ? window.__copyCodeBlock(copyMermaidBtn) : copySuccess();
            });
          } else {
            copySuccess();
          }
        }
        return;
      }
      const openMermaidBtn = e.target.closest('.btn-open-mermaid, .ur-chatbot-mermaid-preview');
      if (openMermaidBtn) {
        if (e.target.closest('.btn-toggle-mermaid-code, .btn-copy-mermaid, .btn-download-mermaid, .btn-download-mermaid-svg, .ur-chatbot-mermaid-code-view')) return;
        const card = openMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const target = card.querySelector('.ur-chatbot-mermaid-target');
          if (target && !target.classList.contains('has-error') && !target.classList.contains('is-loading')) {
            const svgEl = target.querySelector('svg');
            if (svgEl) this.openMermaidModal(svgEl.outerHTML);
          }
        }
        return;
      }
      const downloadMermaidBtn = e.target.closest('.btn-download-mermaid');
      if (downloadMermaidBtn) {
        const card = downloadMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) downloadSvgElementAsPng(svgEl);
        }
        return;
      }
      const downloadMermaidSvgBtn = e.target.closest('.btn-download-mermaid-svg');
      if (downloadMermaidSvgBtn) {
        const card = downloadMermaidSvgBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) downloadSvgElementAsSvg(svgEl);
        }
        return;
      }
    },
    handleBodyScroll(e) {
      const el = e.target;
      if (!el) return;
      const dist = el.scrollHeight - el.scrollTop - el.clientHeight;
      this.showScrollBottomBtn = dist > 60;
    },
    loadEarlierMessages() {
      const el = this.$refs.messageList ? this.$refs.messageList.getScrollContainer() : null;
      const oldScrollHeight = el ? el.scrollHeight : 0;
      const oldScrollTop = el ? el.scrollTop : 0;
      this.visibleCount += 30;
      this.$nextTick(() => {
        if (el) {
          const heightDiff = el.scrollHeight - oldScrollHeight;
          el.scrollTop = oldScrollTop + heightDiff;
        }
      });
    },
    scrollToBottom(smooth = false) {
      if (this.$refs.messageList) {
        this.$refs.messageList.scrollToBottom(smooth);
      }
    },
    toggleSpeak(msg) {
      if (this.speakingMsgId === msg.id) {
        stopSpeaking();
        this.speakingMsgId = null;
        return;
      }
      stopSpeaking();
      this.speakingMsgId = msg.id;
      speakText(
        msg.text,
        msg.lang,
        () => { this.speakingMsgId = msg.id; },
        () => { this.speakingMsgId = null; },
        () => { this.speakingMsgId = null; }
      );
    },
    sendSuggestedPrompt(promptText) {
      if (this.isLoading || this.isStreaming) return;
      this.inputMsg = promptText;
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.handleSendMessage();
    },
    handleFilesSelected(files) {
      for (let i = 0; i < files.length; i++) {
        this.processAndUploadImageFile(files[i]);
      }
    },
    processAndUploadImageFile(file) {
      if (!this.canAttachFile || !file) return;
      const tempId = 'img_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
      const reader = new FileReader();

      reader.onload = async (event) => {
        const base64Data = event.target.result;
        const imgItem = {
          id: tempId,
          name: file.name || ('image_' + Date.now() + '.png'),
          preview: base64Data,
          base64: base64Data,
          url: '',
          key: '',
          uploading: false,
          error: null
        };

        if (this.shouldStoreFile) {
          imgItem.uploading = true;
          this.pendingImages.push(imgItem);
          try {
            const data = await uploadImageToMinio(imgItem.base64, imgItem.name);
            this.$set(imgItem, 'url', data.url);
            this.$set(imgItem, 'key', data.key);
            this.$set(imgItem, 'uploading', false);
          } catch (err) {
            this.$set(imgItem, 'uploading', false);
            this.$set(imgItem, 'error', err.message);
          }
        } else {
          imgItem.uploading = false;
          this.pendingImages.push(imgItem);
        }
      };

      reader.readAsDataURL(file);
    },
    async removePendingImage(index) {
      if (index < 0 || index >= this.pendingImages.length) return;
      const removed = this.pendingImages.splice(index, 1)[0];
      if (this.shouldStoreFile && removed && removed.key) {
        try {
          await deleteImageFromMinio(removed.key);
        } catch (err) {
          console.warn('[UrChat] Error deleting image from MinIO:', err);
        }
      }
    },
    async handleSendMessage() {
      const trimmed = this.inputMsg.trim();
      const hasImages = this.pendingImages && this.pendingImages.length > 0;
      if ((!trimmed && !hasImages) || this.isLoading || this.isStreaming) return;

      this.isErrorState = false;
      const attachedImages = [...(this.pendingImages || [])];
      this.pendingImages = [];

      const userMessageId = Date.now();
      this.messageList.push({
        id: userMessageId,
        sender: 'user',
        text: trimmed,
        images: attachedImages
      });

      let apiContent;
      if (attachedImages.length > 0) {
        apiContent = [];
        const questionText = trimmed || 'Hãy phân tích chi tiết hình ảnh đính kèm này.';
        apiContent.push({ type: 'text', text: questionText });

        for (let i = 0; i < attachedImages.length; i++) {
          const aImg = attachedImages[i];
          const imgUrl = (this.shouldStoreFile && aImg.url)
            ? aImg.url
            : (aImg.base64 || aImg.url);

          apiContent.push({
            type: 'image_url',
            image_url: { url: imgUrl }
          });
        }
      } else {
        apiContent = trimmed;
      }

      this.apiMessagesHistory.push({
        role: 'user',
        content: apiContent
      });

      this.$emit('message', { role: 'user', content: apiContent });

      this.inputMsg = '';
      this.isLoading = true;
      this.isStreaming = false;
      this.requestStartTime = Date.now();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.saveCurrentSession();
      this.checkAndUpdateSessionTitle(false);

      if (this.isUnsavedNewSession || this.activeSessionId) {
        this.isUnsavedNewSession = false;
        this.$emit('session-change', this.activeSessionId);
      }

      const botMessageId = Date.now() + 1;
      const botMsgObj = {
        id: botMessageId,
        sender: 'assistant',
        text: '',
        isStreaming: true,
        responseTime: null,
        lang: null,
        thinking: '',
        versions: [],
        currentVersionIdx: 0
      };
      this.messageList.push(botMsgObj);
      this.scrollToBottom();

      const systemMsg = this.apiMessagesHistory[0] || { role: 'system', content: this.systemPrompt };
      const nonSystemHistory = this.apiMessagesHistory.filter(m => m.role !== 'system');
      const trimmedHistory = this.historyLimit > 0
        ? nonSystemHistory.slice(-this.historyLimit)
        : nonSystemHistory;
      const payloadMessages = [systemMsg, ...trimmedHistory];

      this.streamChatResponse(botMsgObj, payloadMessages, false);
    },
    async callChatApi(payload, signal) {
      const endpoint = this.apiUrl || '/api/chat';
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal
      });
      return response;
    },
    async streamChatResponse(botMsgObj, payloadMessages, isRegeneration) {
      this.abortController = new AbortController();
      this.$set(botMsgObj, 'html', '');
      this.$set(botMsgObj, 'isStreaming', true);
      this.$set(botMsgObj, 'isError', false);
      this.isLoading = true;
      this.isStreaming = false;
      this.requestStartTime = Date.now();
      this.thinkingStartTime = null;

      let rawAccumulatedContent = '';

      try {
        const chatPayload = {
          messages: payloadMessages,
          model: this.effectiveModel,
          thinking: !!(this.isCurrentModelSupportThinking && this.isThinkingActive)
        };
        if (this.knowledgeBase && typeof this.knowledgeBase === 'string' && this.knowledgeBase.trim()) {
          chatPayload.knowledgeBase = this.knowledgeBase.trim();
        }

        const response = await this.callChatApi(chatPayload, this.abortController.signal);

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          throw new Error(errData.error || `HTTP ${response.status}: Server error`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop();

          for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine || !trimmedLine.startsWith('data:')) continue;
            const dataStr = trimmedLine.replace(/^data:\s*/, '');
            if (dataStr === '[DONE]') continue;

            try {
              const parsed = JSON.parse(dataStr);

              if (Array.isArray(parsed.__suggestions__) && parsed.__suggestions__.length > 0) {
                this.suggestedPrompts = parsed.__suggestions__;
                this.suggestedTitle = parsed.__suggestions_title__ || '';
                this.scrollToBottom();
                continue;
              }

              if (parsed.__status__) {
                const st = parsed.__status__;
                if (st.type === 'tool_start') {
                  this.$set(botMsgObj, 'activeTool', {
                    name: st.tool,
                    message: st.message || `Đang gọi tool: ${st.tool}...`,
                    running: true
                  });
                } else if (st.type === 'tool_done') {
                  if (botMsgObj.activeTool) {
                    this.$set(botMsgObj.activeTool, 'running', false);
                    this.$set(botMsgObj.activeTool, 'executionTimeMs', st.executionTimeMs);
                  }
                }
                this.scrollToBottom();
                continue;
              }

              const choice = (parsed.choices && parsed.choices[0]) || null;
              const delta = (choice && choice.delta) || null;
              if (!delta) continue;

              const reasoningDelta = delta.reasoning_content || delta.reasoning || '';
              if (reasoningDelta) {
                if (this.isLoading) {
                  this.isLoading = false;
                  this.isStreaming = true;
                }
                this.thinkingStartTime = this.thinkingStartTime || Date.now();
                this.$set(botMsgObj, 'thinking', (botMsgObj.thinking || '') + reasoningDelta);
                this.throttleScrollThinking();
              }

              const contentDelta = delta.content || '';
              if (contentDelta) {
                if (this.isLoading) {
                  this.isLoading = false;
                  this.isStreaming = true;
                }

                if (botMsgObj.thinking && !botMsgObj.thinkingDuration) {
                  const durSec = Math.max(1, Math.round((Date.now() - (this.thinkingStartTime || this.requestStartTime)) / 1000));
                  this.$set(botMsgObj, 'thinkingDuration', `${durSec}s`);
                }

                rawAccumulatedContent += contentDelta;

                if (rawAccumulatedContent.indexOf('<think>') !== -1) {
                  const thinkStart = rawAccumulatedContent.indexOf('<think>') + 7;
                  const thinkEnd = rawAccumulatedContent.indexOf('</think>');
                  if (thinkEnd !== -1) {
                    const extractedThinking = rawAccumulatedContent.substring(thinkStart, thinkEnd).trim();
                    const mainText = rawAccumulatedContent.substring(thinkEnd + 8).trimStart();
                    this.$set(botMsgObj, 'thinking', extractedThinking);
                    this.$set(botMsgObj, 'text', mainText);
                  } else {
                    const currentThinking = rawAccumulatedContent.substring(thinkStart);
                    this.$set(botMsgObj, 'thinking', currentThinking);
                    this.$set(botMsgObj, 'text', '');
                    this.throttleScrollThinking();
                  }
                } else {
                  this.$set(botMsgObj, 'text', (botMsgObj.text || '') + contentDelta);
                }

                this.throttleUpdateHtml(botMsgObj);
              }
            } catch (e) {}
          }
        }

        this.finishStream(botMsgObj, isRegeneration);
      } catch (err) {
        if (this._htmlRafPending) {
          this._htmlRafPending = false;
        }

        if (err.name === 'AbortError' || (this.abortController && this.abortController.signal && this.abortController.signal.aborted)) {
          this.$set(botMsgObj, 'isStreaming', false);
          if (botMsgObj.text) {
            this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text, false));
          }
          this.isStreaming = false;
          this.isLoading = false;
          return;
        }

        console.error('[UrChat] Stream error:', err);
        this.isLoading = false;
        this.isStreaming = false;
        this.isErrorState = true;
        this.$emit('error', err);

        let errorMessage = 'Unable to connect to Chatbot AI service.';
        if (err.message) errorMessage = err.message;

        if (botMsgObj.text) {
          this.$set(botMsgObj, 'text', botMsgObj.text + `\n\n⚠️ *Connection error: ${errorMessage}*`);
          this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text, false));
          this.$set(botMsgObj, 'isStreaming', false);
        } else {
          this.$set(botMsgObj, 'isError', true);
          this.$set(botMsgObj, 'isStreaming', false);
          this.$set(botMsgObj, 'text', `⚠️ ${errorMessage}`);
          this.$set(botMsgObj, 'html', this.renderHtml(`⚠️ ${errorMessage}`, false));
        }
      } finally {
        this.abortController = null;
        this.scrollToBottom();
        this.saveCurrentSession();
      }
    },
    throttleScrollThinking() {
      if (this._thinkingScrollPending) return;
      this._thinkingScrollPending = true;
      requestAnimationFrame(() => {
        this._thinkingScrollPending = false;
        this.scrollToBottom();
      });
    },
    throttleUpdateHtml(botMsgObj) {
      if (this._htmlRafPending) return;
      this._htmlRafPending = true;
      requestAnimationFrame(() => {
        this._htmlRafPending = false;
        if (!botMsgObj) return;
        this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text || '', true));
        this.scrollToBottom();
      });
    },
    finishStream(botMsgObj, isRegeneration) {
      if (this._htmlRafPending) this._htmlRafPending = false;
      this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text || '', false));
      this.$set(botMsgObj, 'isStreaming', false);
      this.isStreaming = false;
      this.isLoading = false;

      if (this.requestStartTime) {
        const elapsed = ((Date.now() - this.requestStartTime) / 1000).toFixed(2) + 's';
        this.$set(botMsgObj, 'responseTime', elapsed);
      }

      if (botMsgObj.thinking && !botMsgObj.thinkingDuration) {
        const durSec = Math.max(1, Math.round((Date.now() - (this.thinkingStartTime || this.requestStartTime)) / 1000));
        this.$set(botMsgObj, 'thinkingDuration', `${durSec}s`);
      }

      this.extractSuggestionsFromText(botMsgObj);

      if (botMsgObj.text) {
        this.$set(botMsgObj, 'lang', detectLanguage(botMsgObj.text));

        if (!botMsgObj.versions || !Array.isArray(botMsgObj.versions)) {
          this.$set(botMsgObj, 'versions', []);
        }

        const newVersionData = {
          text: botMsgObj.text,
          html: botMsgObj.html,
          responseTime: botMsgObj.responseTime || '',
          lang: botMsgObj.lang || '',
          thinking: botMsgObj.thinking || '',
          thinkingDuration: botMsgObj.thinkingDuration || ''
        };

        if (isRegeneration) {
          botMsgObj.versions.push(newVersionData);
          this.$set(botMsgObj, 'currentVersionIdx', botMsgObj.versions.length - 1);
          this.syncActiveVersionToHistory(botMsgObj);
        } else {
          botMsgObj.versions = [newVersionData];
          this.$set(botMsgObj, 'currentVersionIdx', 0);
          this.apiMessagesHistory.push({
            role: 'assistant',
            content: botMsgObj.text
          });
        }

        this.$emit('message', { role: 'assistant', content: botMsgObj.text });
        this.saveCurrentSession();
        this.checkAndUpdateSessionTitle(false);
        this.$nextTick(() => {
          this.scheduleMermaidRender();
        });
      }
      this.scrollToBottom();
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    extractSuggestionsFromText(botMsgObj) {
      if (!botMsgObj || !botMsgObj.text) return;
      const rawText = botMsgObj.text;
      const jsonBlockRegex = /(?:(?:\n|^)(?:[#*_\-\s]*(?:Gợi ý|Gợi ý tiếp theo|Follow-up questions?|Các câu hỏi gợi ý)[#*_\-\s:]*\n?)?)```(?:json)?\s*(\{[\s\S]*?"questions"\s*:\s*\[[\s\S]*?\}\s*)\s*```\s*$/i;
      let match = rawText.match(jsonBlockRegex);
      if (!match) {
        const rawJsonRegex = /(?:(?:\n|^)(?:[#*_\-\s]*(?:Gợi ý|Gợi ý tiếp theo|Follow-up questions?|Các câu hỏi gợi ý)[#*_\-\s:]*\n?)?)(\{[\s\S]*?"questions"\s*:\s*\[[\s\S]*?\}\s*)$/i;
        match = rawText.match(rawJsonRegex);
      }
      if (match) {
        try {
          const jsonStr = match[1].trim();
          const parsed = JSON.parse(jsonStr);
          if (Array.isArray(parsed.questions) && parsed.questions.length > 0) {
            this.suggestedPrompts = parsed.questions.slice(0, 3).map(q => String(q).trim()).filter(Boolean);
            if (parsed.title && typeof parsed.title === 'string' && parsed.title.trim()) {
              this.suggestedTitle = parsed.title.trim();
            }
            const cleanedText = rawText.substring(0, match.index).trimEnd();
            if (cleanedText) {
              this.$set(botMsgObj, 'text', cleanedText);
              this.$set(botMsgObj, 'html', this.renderHtml(cleanedText, false));
              if (botMsgObj.versions && botMsgObj.versions[botMsgObj.currentVersionIdx]) {
                botMsgObj.versions[botMsgObj.currentVersionIdx].text = cleanedText;
                botMsgObj.versions[botMsgObj.currentVersionIdx].html = this.renderHtml(cleanedText, false);
              }
            }
          }
        } catch (e) {}
      }
    },
    regenerateMessage(msg) {
      if (this.isStreaming || this.isLoading || !msg) return;
      const msgIdx = this.messageList.indexOf(msg);
      if (msgIdx < 0) return;

      let userMsg = null;
      for (let i = msgIdx - 1; i >= 0; i--) {
        if (this.messageList[i].sender === 'user') {
          userMsg = this.messageList[i];
          break;
        }
      }
      if (!userMsg) return;

      if (!msg.versions || !Array.isArray(msg.versions) || msg.versions.length === 0) {
        this.$set(msg, 'versions', [
          {
            text: msg.text,
            html: msg.html || this.renderHtml(msg.text, false),
            responseTime: msg.responseTime || '',
            lang: msg.lang || '',
            thinking: msg.thinking || '',
            thinkingDuration: msg.thinkingDuration || ''
          }
        ]);
        this.$set(msg, 'currentVersionIdx', 0);
      }

      const payloadMessages = [{ role: 'system', content: this.systemPrompt }];
      for (let i = 0; i < this.messageList.length; i++) {
        const item = this.messageList[i];
        if (item === msg) break;
        if (item.sender === 'user') {
          if (item.images && item.images.length > 0) {
            const content = [{ type: 'text', text: item.text || 'Hãy phân tích chi tiết hình ảnh đính kèm này.' }];
            for (let k = 0; k < item.images.length; k++) {
              const aImg = item.images[k];
              const imgUrl = (this.shouldStoreFile && aImg.url) ? aImg.url : (aImg.base64 || aImg.url);
              content.push({ type: 'image_url', image_url: { url: imgUrl } });
            }
            payloadMessages.push({ role: 'user', content: content });
          } else if (item.text) {
            payloadMessages.push({ role: 'user', content: item.text });
          }
        } else if ((item.sender === 'bot' || item.sender === 'assistant') && !item.isResetNotice && item.text) {
          payloadMessages.push({ role: 'assistant', content: item.text });
        }
      }

      let finalHistory = payloadMessages.slice(1);
      if (this.historyLimit > 0) finalHistory = finalHistory.slice(-this.historyLimit);
      const finalPayload = [payloadMessages[0], ...finalHistory];

      this.$set(msg, 'text', '');
      this.$set(msg, 'html', '');
      this.$set(msg, 'thinking', '');
      this.$set(msg, 'thinkingDuration', '');
      this.$set(msg, 'responseTime', null);
      this.$set(msg, 'isError', false);

      this.streamChatResponse(msg, finalPayload, true);
    },
    syncActiveVersionToHistory(botMsgObj) {
      if (!botMsgObj || !botMsgObj.text) return;
      let botIndex = 0;
      for (let i = 0; i < this.messageList.length; i++) {
        const m = this.messageList[i];
        if (m === botMsgObj) break;
        if ((m.sender === 'bot' || m.sender === 'assistant') && !m.isResetNotice && m.text) botIndex++;
      }

      let historyBotIndex = 0;
      for (let j = 0; j < this.apiMessagesHistory.length; j++) {
        if (this.apiMessagesHistory[j].role === 'assistant') {
          if (historyBotIndex === botIndex) {
            this.apiMessagesHistory[j].content = botMsgObj.text;
            return;
          }
          historyBotIndex++;
        }
      }
    },
    prevMsgVersion(msg) {
      const cur = typeof msg.currentVersionIdx === 'number' ? msg.currentVersionIdx : 0;
      if (cur > 0) this.setMsgVersion(msg, cur - 1);
    },
    nextMsgVersion(msg) {
      const cur = typeof msg.currentVersionIdx === 'number' ? msg.currentVersionIdx : 0;
      const count = (msg.versions && Array.isArray(msg.versions)) ? msg.versions.length : 1;
      if (cur < count - 1) this.setMsgVersion(msg, cur + 1);
    },
    setMsgVersion(msg, targetIndex) {
      if (!msg || !msg.versions || targetIndex < 0 || targetIndex >= msg.versions.length) return;
      this.$set(msg, 'currentVersionIdx', targetIndex);
      const v = msg.versions[targetIndex];
      this.$set(msg, 'text', v.text);
      this.$set(msg, 'html', v.html || this.renderHtml(v.text, false));
      this.$set(msg, 'responseTime', v.responseTime || '');
      this.$set(msg, 'lang', v.lang || detectLanguage(v.text));
      this.$set(msg, 'thinking', v.thinking || '');
      this.$set(msg, 'thinkingDuration', v.thinkingDuration || '');
      this.syncActiveVersionToHistory(msg);
      this.saveCurrentSession();
      this.$nextTick(() => {
        this.scheduleMermaidRender();
      });
    },
    stopStreaming() {
      if (this.abortController) {
        this.abortController.abort();
        this.abortController = null;
      }
      this.isStreaming = false;
      this.isLoading = false;

      const lastMsg = this.messageList[this.messageList.length - 1];
      if (lastMsg && lastMsg.isStreaming) {
        this.$set(lastMsg, 'isStreaming', false);
      }
    }
  }
};
</script>
