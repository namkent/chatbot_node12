<template>
  <div :class="['ur-chatbot-wrapper chatbot-widget-container', { 'is-fullscreen': isOpen && isFullscreen }]">
    <!-- CỬA SỔ CHAT NỀN SÁNG -->
    <div :class="['ur-chatbot-window chat-window', { open: isOpen, expanded: isExpanded, fullscreen: isOpen && isFullscreen }]">
      <!-- SVG Bot bay lơ lửng ở Topbar -->
      <astro-bot
        variant="topbar"
        :is-loading="isLoading"
        :is-streaming="isStreaming"
        :has-error="hasBotError"
        @retry="retryLastAction"
      />

      <!-- Topbar Header -->
      <chat-header
        :bot-name="effectiveBotName"
        :status-label="statusLabel"
        :is-loading="isLoading"
        :is-streaming="isStreaming"
        :has-bot-error="hasBotError"
        :is-fullscreen="isFullscreen"
        :is-expanded="isExpanded"
        :show-fullscreen-btn="true"
        :show-expand-btn="true"
        :show-close-btn="true"
        @reset="promptResetChat"
        @toggle-fullscreen="toggleFullscreen"
        @toggle-expand="toggleExpand"
        @close="toggleChat(false)"
      />

      <!-- Khung chat body -->
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

      <!-- Footer nhập tin nhắn -->
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

      <!-- Modal xác nhận Reset cuộc trò chuyện -->
      <reset-confirm-modal
        :visible="showResetConfirm"
        @cancel="cancelReset"
        @confirm="confirmReset"
      />

      <!-- Interactive Zero-G Bot khi Fullscreen -->
      <div
        v-if="isOpen && isFullscreen"
        :class="['ur-chatbot-zerog-bot', { 'bot-error-state': hasBotError }]"
        :style="{
          transform: `translate3d(${zeroGBot.x}px, ${zeroGBot.y}px, 0) rotate(${zeroGBot.rot}deg)`,
          cursor: zeroGBot.isDragging ? 'grabbing' : (hasBotError ? 'pointer' : 'grab')
        }"
        :title="hasBotError ? 'Bot error! Click to retry' : 'Drag and toss Astro Bot in zero-gravity!'"
        @mousedown="onZeroGBotMouseDown"
        @touchstart="onZeroGBotTouchStart"
        @click="onZeroGBotClick"
      >
        <astro-bot
          variant="topbar"
          :is-loading="isLoading"
          :is-streaming="isStreaming"
          :has-error="hasBotError"
          @retry="retryLastAction"
        />
      </div>
    </div>

    <!-- NÚT TRIGGER WIDGET -->
    <div
      :class="['ur-chatbot-trigger-btn chat-trigger-btn', { 'is-hidden': isOpen }]"
      title="Chat with Astro Bot"
      @click="toggleChat()"
    >
      <astro-bot variant="trigger" />
    </div>

    <!-- Modal phóng to ảnh -->
    <image-lightbox-modal
      :visible="activeImageModal.visible"
      :src="activeImageModal.src"
      :alt="activeImageModal.alt"
      @close="closeImageModal"
    />

    <!-- Modal phóng to biểu đồ Mermaid -->
    <mermaid-lightbox-modal
      :visible="activeMermaidModal.visible"
      :svg-html="activeMermaidModal.svgHtml"
      @close="closeMermaidModal"
    />
  </div>
</template>

<script>
import './styles/index.scss';

import ChatHeader from './components/ChatHeader.vue';
import MessageList from './components/MessageList.vue';
import ChatInput from './components/ChatInput.vue';
import ResetConfirmModal from './components/ResetConfirmModal.vue';
import ImageLightboxModal from './components/ImageLightboxModal.vue';
import MermaidLightboxModal from './components/MermaidLightboxModal.vue';
import AstroBot from './components/AstroBot.vue';

import { parseAndSanitizeMarkdown } from './services/markdownParser';
import {
  renderMermaidDiagrams,
  downloadSvgElementAsPng,
  downloadSvgElementAsSvg
} from './services/mermaidEngine';
import { detectLanguage, speakText, stopSpeaking } from './services/voiceService';
import { uploadImageToMinio, deleteImageFromMinio } from './services/minioUploadService';

export default {
  name: 'UrChatbot',
  components: {
    ChatHeader,
    MessageList,
    ChatInput,
    ResetConfirmModal,
    ImageLightboxModal,
    MermaidLightboxModal,
    AstroBot
  },
  props: {
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
    initialOpen: {
      type: Boolean,
      default: false
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
    localStorage: {
      type: Boolean,
      default: false
    },
    storageKey: {
      type: String,
      default: ''
    },
    maxStoredMessages: {
      type: Number,
      default: 40
    },
    attachFile: {
      type: Boolean,
      default: false
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
        { id: 'openai/gpt-oss-120b', name: 'GPT-OSS 120B', desc: 'Lý luận sâu, toán học & code', badge: 'Reasoning', badgeType: 'badge-reasoning', supportsThinking: true },
        { id: 'qwen/qwen3.8-27b', name: 'Qwen 3.8 27B', desc: 'Đa phương thức, hiểu ảnh & tiếng Việt', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
        { id: 'qwen/qwen3.6-27b', name: 'Qwen 3.6 27B', desc: 'Nhận diện ảnh nhanh, siêu tốc', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
        { id: 'openai/gpt-oss-20b', name: 'GPT-OSS 20B', desc: 'Suy luận nhanh, tốc độ phản hồi cao', badge: 'Reasoning', badgeType: 'badge-reasoning', supportsThinking: true }
      ]
    }
  },
  data() {
    return {
      isOpen: this.initialOpen,
      activeImageModal: {
        visible: false,
        src: '',
        alt: ''
      },
      activeMermaidModal: {
        visible: false,
        svgHtml: ''
      },
      isExpanded: false,
      isFullscreen: false,
      showResetConfirm: false,
      showModelDropdown: false,
      isThinkingActive: !!this.thinking,
      zeroGBot: {
        x: 220,
        y: 120,
        vx: 1.2,
        vy: 0.8,
        rot: 0,
        vRot: 0.6,
        isDragging: false
      },
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
      messageList: [
        {
          id: 1,
          sender: 'bot',
          text: 'Hello! 👋\nI am **Astro Bot AI**, your space assistant. How can I help you today?',
          html: '',
          responseTime: ''
        }
      ],
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
    isCurrentModelSupportThinking() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      if (!found) {
        return /gpt-oss|120b|20b|deepseek-r1|reasoning|r1/i.test(this.effectiveModel);
      }
      if (/gpt-oss-20b|gpt-oss-120b|deepseek-r1/i.test(found.id)) {
        return true;
      }
      if (typeof found.supportsThinking === 'boolean') {
        return found.supportsThinking;
      }
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
    },
    botName(val) {
      this.effectiveBotName = val;
    },
    maxVisibleMessages(val) {
      this.visibleCount = val;
    },
    isFullscreen(val) {
      if (val && this.isOpen) {
        this.startZeroGBot();
      } else {
        this.stopZeroGBot();
      }
    }
  },
  created() {
    this.initHistory();
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
        } else if (this.isFullscreen) {
          this.toggleFullscreen();
        }
      }
    };
    window.addEventListener('keydown', this._escKeyHandler);
    this.loadFromLocalStorage();

    this.messageList.forEach(m => {
      if (m.sender === 'bot' && !m.html && m.text) {
        this.$set(m, 'html', this.renderHtml(m.text, false));
      }
    });
    this.scheduleMermaidRender();
    if (this.isFullscreen) {
      this.startZeroGBot();
    }
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
    if (this._mermaidRenderTimer) {
      clearTimeout(this._mermaidRenderTimer);
      this._mermaidRenderTimer = null;
    }
    if (this._escKeyHandler) {
      window.removeEventListener('keydown', this._escKeyHandler);
    }
    if (this._docClickListener) {
      document.removeEventListener('click', this._docClickListener);
    }
    this.stopZeroGBot();
    stopSpeaking();
  },
  methods: {
    initHistory() {
      this.apiMessagesHistory = [
        { role: 'system', content: this.systemPrompt }
      ];
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
      this.saveToLocalStorage();
    },
    toggleThinkingActive() {
      if (!this.isCurrentModelSupportThinking) return;
      this.isThinkingActive = !this.isThinkingActive;
      this.$emit('update:thinking', this.isThinkingActive);
      this.saveToLocalStorage();
    },
    toggleChat(forceState) {
      if (typeof forceState === 'boolean') {
        this.isOpen = forceState;
      } else {
        this.isOpen = !this.isOpen;
      }
      if (!this.isOpen) {
        if (this.isFullscreen) {
          this.isFullscreen = false;
          this.$emit('fullscreen', false);
        }
        if (typeof document !== 'undefined' && document.body) {
          document.body.style.overflow = '';
        }
        this.stopZeroGBot();
      }
      this.$emit('toggle', this.isOpen);
    },
    toggleExpand() {
      this.isExpanded = !this.isExpanded;
      this.$emit('expand', this.isExpanded);
      this.scrollToBottom();
    },
    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
      this.$emit('fullscreen', this.isFullscreen);
      this.scrollToBottom();
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
      if (this.isStreaming) {
        this.stopStreaming();
      }
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
      this.initHistory();
      this.clearLocalStorage();
      this.saveToLocalStorage();
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
      this.activeImageModal = {
        visible: true,
        src: src,
        alt: alt || ''
      };
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

      this.activeMermaidModal = {
        visible: true,
        svgHtml: cleanSvg
      };
    },
    closeMermaidModal() {
      this.activeMermaidModal.visible = false;
    },
    handleBodyClick(e) {
      // 1. Copy code block
      const copyBtn = e.target.closest('.ur-chatbot-copy-btn');
      if (copyBtn && window.__copyCodeBlock) {
        window.__copyCodeBlock(copyBtn);
        return;
      }

      // 2. Open image lightbox
      const imgWrapper = e.target.closest('.ur-chatbot-image-wrapper, .ur-chatbot-code-img-item');
      if (imgWrapper) {
        const rawSrc = imgWrapper.getAttribute('data-src');
        const rawAlt = imgWrapper.getAttribute('data-alt');
        const imgEl = imgWrapper.querySelector('img');
        if (imgEl && imgEl.classList.contains('error')) return;
        const src = rawSrc || (imgEl ? imgEl.src : '');
        const alt = rawAlt ? decodeURIComponent(rawAlt) : (imgEl ? imgEl.alt : '');
        if (src) {
          this.openImageModal(src, alt);
        }
        return;
      }

      // 3. Toggle Mermaid source code view
      const toggleMermaidBtn = e.target.closest('.btn-toggle-mermaid-code');
      if (toggleMermaidBtn) {
        const card = toggleMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const codeView = card.querySelector('.ur-chatbot-mermaid-code-view');
          if (codeView) {
            const isHidden = codeView.style.display === 'none';
            codeView.style.display = isHidden ? 'block' : 'none';
            toggleMermaidBtn.classList.toggle('is-active', isHidden);
            toggleMermaidBtn.title = isHidden ? 'Hide source code' : 'View source code';
          }
        }
        return;
      }

      // 4. Copy Mermaid code
      const copyMermaidBtn = e.target.closest('.btn-copy-mermaid');
      if (copyMermaidBtn) {
        const rawCode = copyMermaidBtn.getAttribute('data-code');
        if (rawCode) {
          const text = decodeURIComponent(rawCode);
          navigator.clipboard.writeText(text).then(() => {
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
              copyMermaidBtn.title = 'Copy code';
              copyMermaidBtn.classList.remove('copied');
            }, 2000);
          });
        }
        return;
      }

      // 5. Open Mermaid Fullscreen modal
      const openMermaidBtn = e.target.closest('.btn-open-mermaid, .ur-chatbot-mermaid-preview');
      if (openMermaidBtn) {
        if (e.target.closest('.btn-toggle-mermaid-code, .btn-copy-mermaid, .btn-download-mermaid, .btn-download-mermaid-svg, .ur-chatbot-mermaid-code-view')) {
          return;
        }
        const card = openMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const target = card.querySelector('.ur-chatbot-mermaid-target');
          if (target && !target.classList.contains('has-error') && !target.classList.contains('is-loading')) {
            const svgEl = target.querySelector('svg');
            if (svgEl) {
              this.openMermaidModal(svgEl.outerHTML);
            }
          }
        }
        return;
      }

      // 6. Download Mermaid PNG
      const downloadMermaidBtn = e.target.closest('.btn-download-mermaid');
      if (downloadMermaidBtn) {
        const card = downloadMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) {
            downloadSvgElementAsPng(svgEl);
          }
        }
        return;
      }

      // 7. Download Mermaid SVG
      const downloadMermaidSvgBtn = e.target.closest('.btn-download-mermaid-svg');
      if (downloadMermaidSvgBtn) {
        const card = downloadMermaidSvgBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) {
            downloadSvgElementAsSvg(svgEl);
          }
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
    retryLastAction() {
      if (this.isStreaming || this.isLoading) return;
      for (let i = this.messageList.length - 1; i >= 0; i--) {
        if (this.messageList[i].sender === 'user') {
          const userPrompt = this.messageList[i].text;
          if (i + 1 < this.messageList.length && this.messageList[i + 1].sender === 'bot') {
            this.messageList.splice(i + 1, 1);
          }
          this.inputMsg = userPrompt;
          this.isErrorState = false;
          this.handleSendMessage();
          return;
        }
      }
      this.isErrorState = false;
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
          console.warn('[UrChatbot] Error deleting image from MinIO:', err);
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
      this.saveToLocalStorage();

      const botMessageId = Date.now() + 1;
      const botMsgObj = {
        id: botMessageId,
        sender: 'bot',
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
                  }
                } else {
                  this.$set(botMsgObj, 'text', (botMsgObj.text || '') + contentDelta);
                }

                this.throttleUpdateHtml(botMsgObj);
              }
            } catch (e) {
              // chunk json fragment
            }
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

        console.error('[UrChatbot] Stream error:', err);
        this.isLoading = false;
        this.isStreaming = false;
        this.isErrorState = true;
        this.$emit('error', err);

        let errorMessage = 'Unable to connect to Chatbot AI service.';
        if (err.message) {
          errorMessage = err.message;
        }

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
        this.saveToLocalStorage();
      }
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
      if (this._htmlRafPending) {
        this._htmlRafPending = false;
      }
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
        this.saveToLocalStorage();
        this.$nextTick(() => {
          this.scheduleMermaidRender();
        });
      }
      this.scrollToBottom();
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
        } else if (item.sender === 'bot' && !item.isResetNotice && item.text) {
          payloadMessages.push({ role: 'assistant', content: item.text });
        }
      }

      let finalHistory = payloadMessages.slice(1);
      if (this.historyLimit > 0) {
        finalHistory = finalHistory.slice(-this.historyLimit);
      }
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
        if (m.sender === 'bot' && !m.isResetNotice && m.text) {
          botIndex++;
        }
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
      if (cur > 0) {
        this.setMsgVersion(msg, cur - 1);
      }
    },
    nextMsgVersion(msg) {
      const cur = typeof msg.currentVersionIdx === 'number' ? msg.currentVersionIdx : 0;
      const count = (msg.versions && Array.isArray(msg.versions)) ? msg.versions.length : 1;
      if (cur < count - 1) {
        this.setMsgVersion(msg, cur + 1);
      }
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
      this.saveToLocalStorage();
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
    },
    getStorageKey() {
      if (this.storageKey) return this.storageKey;
      if (typeof window === 'undefined') return 'ur_chatbot_history_default';
      const path = window.location.pathname || '/';
      return `ur_chatbot_history_${encodeURIComponent(path)}`;
    },
    loadFromLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        const raw = window.localStorage.getItem(this.getStorageKey());
        if (!raw) return;
        const data = JSON.parse(raw);
        if (data && Array.isArray(data.messageList) && data.messageList.length > 0) {
          this.messageList = data.messageList.map(m => {
            const hasVersions = Array.isArray(m.versions) && m.versions.length > 0;
            const currentIdx = typeof m.currentVersionIdx === 'number' ? m.currentVersionIdx : 0;
            const activeVersion = hasVersions && m.versions[currentIdx] ? m.versions[currentIdx] : null;
            const activeText = activeVersion ? activeVersion.text : (m.text || '');
            const activeThinking = activeVersion ? (activeVersion.thinking || '') : (m.thinking || '');
            const activeThinkingDuration = activeVersion ? (activeVersion.thinkingDuration || '') : (m.thinkingDuration || '');
            return {
              ...m,
              text: activeText,
              thinking: activeThinking,
              thinkingDuration: activeThinkingDuration,
              isStreaming: false,
              html: m.sender === 'bot' ? this.renderHtml(activeText, false) : '',
              versions: hasVersions ? m.versions : (m.sender === 'bot' && activeText ? [{
                text: activeText,
                html: this.renderHtml(activeText, false),
                responseTime: m.responseTime || '',
                lang: m.lang || '',
                thinking: activeThinking,
                thinkingDuration: activeThinkingDuration
              }] : []),
              currentVersionIdx: currentIdx
            };
          });
        }
        if (data && Array.isArray(data.apiMessagesHistory) && data.apiMessagesHistory.length > 0) {
          this.apiMessagesHistory = data.apiMessagesHistory;
        }
        if (data && data.effectiveModel) {
          this.effectiveModel = data.effectiveModel;
        }
        if (data && typeof data.isThinkingActive === 'boolean') {
          this.isThinkingActive = data.isThinkingActive;
        }
      } catch (err) {
        console.warn('[UrChatbot] Error loading localStorage:', err);
      }
    },
    saveToLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        const key = this.getStorageKey();
        let msgsToSave = (this.messageList || []).map(m => ({
          id: m.id,
          sender: m.sender,
          text: m.text,
          responseTime: m.responseTime || '',
          isError: !!m.isError,
          isResetNotice: !!m.isResetNotice,
          images: m.images || undefined,
          thinking: m.thinking || '',
          thinkingDuration: m.thinkingDuration || '',
          versions: m.versions || undefined,
          currentVersionIdx: m.currentVersionIdx !== undefined ? m.currentVersionIdx : undefined
        }));

        if (msgsToSave.length > this.maxStoredMessages) {
          const firstWelcome = msgsToSave[0] && msgsToSave[0].sender === 'bot' ? [msgsToSave[0]] : [];
          const recent = msgsToSave.slice(-this.maxStoredMessages);
          msgsToSave = firstWelcome.concat(recent.filter(m => !firstWelcome[0] || m.id !== firstWelcome[0].id));
        }

        let apiHistoryToSave = (this.apiMessagesHistory || []).slice(-this.maxStoredMessages);

        let attempts = 0;
        while (attempts < 5) {
          try {
            const payload = JSON.stringify({
              version: 1,
              updatedAt: Date.now(),
              messageList: msgsToSave,
              apiMessagesHistory: apiHistoryToSave,
              effectiveModel: this.effectiveModel,
              isThinkingActive: this.isThinkingActive
            });

            if (payload.length > 1500000 && msgsToSave.length > 6) {
              msgsToSave.splice(1, 5);
              apiHistoryToSave.splice(0, 5);
              attempts++;
              continue;
            }

            window.localStorage.setItem(key, payload);
            break;
          } catch (err) {
            if (err.name === 'QuotaExceededError' || err.code === 22 || err.code === 1014) {
              if (msgsToSave.length > 6) {
                msgsToSave.splice(1, 6);
                apiHistoryToSave.splice(0, 6);
                attempts++;
              } else {
                window.localStorage.removeItem(key);
                break;
              }
            } else {
              throw err;
            }
          }
        }
      } catch (err) {
        console.warn('[UrChatbot] Error saving localStorage:', err);
      }
    },
    clearLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        window.localStorage.removeItem(this.getStorageKey());
      } catch (e) {}
    },
    startZeroGBot() {
      this.stopZeroGBot();
      const winW = typeof window !== 'undefined' ? window.innerWidth : 1000;
      const winH = typeof window !== 'undefined' ? window.innerHeight : 700;
      this.zeroGBot.x = Math.max(50, Math.min(winW - 130, winW / 2 + 220));
      this.zeroGBot.y = Math.max(80, Math.min(winH - 180, 140));
      this.zeroGBot.vx = (Math.random() - 0.5) * 2 + 1;
      this.zeroGBot.vy = (Math.random() - 0.5) * 2;
      this.zeroGBot.rot = 0;
      this.zeroGBot.vRot = 0.8;
      this.zeroGBot.isDragging = false;
      this._hasZeroGDragged = false;

      const loop = () => {
        if (!this.isFullscreen) return;
        this.stepZeroGBot();
        this._zeroGRaf = requestAnimationFrame(loop);
      };
      this._zeroGRaf = requestAnimationFrame(loop);
    },
    stopZeroGBot() {
      if (this._zeroGRaf) {
        cancelAnimationFrame(this._zeroGRaf);
        this._zeroGRaf = null;
      }
      if (this._zeroGMouseMove) {
        window.removeEventListener('mousemove', this._zeroGMouseMove);
        this._zeroGMouseMove = null;
      }
      if (this._zeroGMouseUp) {
        window.removeEventListener('mouseup', this._zeroGMouseUp);
        this._zeroGMouseUp = null;
      }
    },
    stepZeroGBot() {
      if (this.zeroGBot.isDragging) return;
      const bot = this.zeroGBot;
      bot.x += bot.vx;
      bot.y += bot.vy;
      bot.rot += bot.vRot;

      const winW = typeof window !== 'undefined' ? window.innerWidth : 1000;
      const winH = typeof window !== 'undefined' ? window.innerHeight : 700;
      const minX = 15;
      const maxX = winW - 110;
      const minY = 60;
      const maxY = winH - 160;
      const restitution = 0.82;

      if (bot.x <= minX) {
        bot.x = minX;
        bot.vx = Math.abs(bot.vx) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      } else if (bot.x >= maxX) {
        bot.x = maxX;
        bot.vx = -Math.abs(bot.vx) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      }

      if (bot.y <= minY) {
        bot.y = minY;
        bot.vy = Math.abs(bot.vy) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      } else if (bot.y >= maxY) {
        bot.y = maxY;
        bot.vy = -Math.abs(bot.vy) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      }

      bot.vx *= 0.995;
      bot.vy *= 0.995;
      bot.vRot *= 0.996;

      const speed = Math.hypot(bot.vx, bot.vy);
      if (speed < 0.35) {
        bot.vx += (Math.random() - 0.5) * 0.08;
        bot.vy += (Math.random() - 0.5) * 0.08;
        bot.vRot += (Math.random() - 0.5) * 0.06;
      }
    },
    onZeroGBotMouseDown(e) {
      if (e.button !== 0) return;
      e.preventDefault();
      this.zeroGBot.isDragging = true;
      this._hasZeroGDragged = false;

      const startMouseX = e.clientX;
      const startMouseY = e.clientY;
      const initialBotX = this.zeroGBot.x;
      const initialBotY = this.zeroGBot.y;
      const mouseHistory = [{ x: e.clientX, y: e.clientY, t: Date.now() }];

      const onMouseMove = (moveEv) => {
        const dx = moveEv.clientX - startMouseX;
        const dy = moveEv.clientY - startMouseY;
        if (Math.hypot(dx, dy) > 5) {
          this._hasZeroGDragged = true;
        }
        this.zeroGBot.x = initialBotX + dx;
        this.zeroGBot.y = initialBotY + dy;

        const now = Date.now();
        mouseHistory.push({ x: moveEv.clientX, y: moveEv.clientY, t: now });
        if (mouseHistory.length > 6) {
          mouseHistory.shift();
        }
      };

      const onMouseUp = () => {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
        this._zeroGMouseMove = null;
        this._zeroGMouseUp = null;
        this.zeroGBot.isDragging = false;

        if (mouseHistory.length >= 2) {
          const first = mouseHistory[0];
          const last = mouseHistory[mouseHistory.length - 1];
          const dt = Math.max(16, last.t - first.t) / 1000;
          const rawVx = (last.x - first.x) / (dt * 60);
          const rawVy = (last.y - first.y) / (dt * 60);

          this.zeroGBot.vx = Math.max(-20, Math.min(20, rawVx));
          this.zeroGBot.vy = Math.max(-20, Math.min(20, rawVy));
          this.zeroGBot.vRot = Math.max(-10, Math.min(10, (rawVx - rawVy) * 0.35));
        }
      };

      this._zeroGMouseMove = onMouseMove;
      this._zeroGMouseUp = onMouseUp;
      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    },
    onZeroGBotTouchStart(e) {
      if (e.touches.length !== 1) return;
      const touch = e.touches[0];
      this.zeroGBot.isDragging = true;
      this._hasZeroGDragged = false;
      const startX = touch.clientX;
      const startY = touch.clientY;
      const initX = this.zeroGBot.x;
      const initY = this.zeroGBot.y;
      const history = [{ x: startX, y: startY, t: Date.now() }];

      const onTouchMove = (moveEv) => {
        if (moveEv.touches.length !== 1) return;
        const tMove = moveEv.touches[0];
        const dx = tMove.clientX - startX;
        const dy = tMove.clientY - startY;
        if (Math.hypot(dx, dy) > 5) {
          this._hasZeroGDragged = true;
        }
        this.zeroGBot.x = initX + dx;
        this.zeroGBot.y = initY + dy;
        history.push({ x: tMove.clientX, y: tMove.clientY, t: Date.now() });
        if (history.length > 6) history.shift();
      };

      const onTouchEnd = () => {
        window.removeEventListener('touchmove', onTouchMove);
        window.removeEventListener('touchend', onTouchEnd);
        this.zeroGBot.isDragging = false;
        if (history.length >= 2) {
          const first = history[0];
          const last = history[history.length - 1];
          const dt = Math.max(16, last.t - first.t) / 1000;
          const rawVx = (last.x - first.x) / (dt * 60);
          const rawVy = (last.y - first.y) / (dt * 60);
          this.zeroGBot.vx = Math.max(-20, Math.min(20, rawVx));
          this.zeroGBot.vy = Math.max(-20, Math.min(20, rawVy));
          this.zeroGBot.vRot = Math.max(-10, Math.min(10, (rawVx - rawVy) * 0.35));
        }
      };

      window.addEventListener('touchmove', onTouchMove, { passive: true });
      window.addEventListener('touchend', onTouchEnd);
    },
    onZeroGBotClick() {
      if (this._hasZeroGDragged) {
        this._hasZeroGDragged = false;
        return;
      }
      if (this.hasBotError) {
        this.retryLastAction();
      } else {
        this.zeroGBot.vRot += (Math.random() > 0.5 ? 4 : -4);
        this.zeroGBot.vx += (Math.random() - 0.5) * 3;
        this.zeroGBot.vy = -3.5;
      }
    }
  }
};
</script>
