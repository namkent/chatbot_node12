<template>
  <div
    class="ur-chatbot-body chat-body"
    ref="chatBody"
    @scroll="onScroll"
    @click="$emit('body-click', $event)"
  >
    <!-- Nút tải thêm tin nhắn trước đó khi bật Virtual Scroll -->
    <div v-if="hiddenEarlierCount > 0" class="ur-chatbot-virtual-history virtual-history-wrapper">
      <button type="button" class="ur-chatbot-btn-load-earlier btn-load-earlier" @click="$emit('load-earlier')">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="18 15 12 9 6 15"></polyline>
        </svg>
        Load {{ hiddenEarlierCount }} earlier messages
      </button>
    </div>

    <!-- Danh sách tin nhắn -->
    <div
      v-for="msg in messages"
      :key="msg.id"
      :class="[
        'ur-chatbot-msg-bubble msg-bubble',
        msg.sender === 'user' ? 'ur-chatbot-msg-user msg-user' : 'ur-chatbot-msg-bot msg-bot',
        { 'ur-chatbot-msg-error msg-error': msg.isError }
      ]"
    >
      <!-- Tin nhắn của User -->
      <div v-if="msg.sender === 'user'" class="ur-chatbot-msg-user-wrapper">
        <!-- Hiển thị ảnh đính kèm của User (nếu có) -->
        <div v-if="msg.images && msg.images.length > 0" class="ur-chatbot-user-imgs-row">
          <div
            v-for="(uImg, uIdx) in msg.images"
            :key="uIdx"
            class="ur-chatbot-user-img-card"
            :title="uImg.name || 'Ảnh đính kèm (click để phóng to)'"
            @click="$emit('open-image', { src: uImg.preview || uImg.url || uImg.base64, alt: uImg.name })"
          >
            <img :src="uImg.preview || uImg.url || uImg.base64" :alt="uImg.name || 'Attachment'" />
            <span v-if="uImg.sendMode" class="ur-chatbot-user-img-mode-badge">{{ uImg.sendMode === 'url' ? 'S3' : 'B64' }}</span>
          </div>
        </div>

        <div
          v-if="msg.text"
          :class="[
            'ur-chatbot-msg-user-text msg-user-text',
            { 'is-collapsed': isMsgCollapsed(msg) }
          ]"
          :ref="`userMsg_${msg.id}`"
        >{{ msg.text }}</div>

        <!-- Nút Chevron Thu gọn / Mở rộng nếu nội dung dài hơn 3 dòng -->
        <div v-if="isMsgCollapsible(msg)" :class="['ur-chatbot-user-expand-row', { 'is-expanded': isMsgExpanded(msg) }]">
          <button
            type="button"
            class="ur-chatbot-btn-user-expand"
            :title="isMsgExpanded(msg) ? 'Collapse' : 'Expand'"
            @click="toggleUserMsgExpand(msg.id)"
          >
            <svg
              :class="['ur-chatbot-icon-chevron', { 'is-expanded': isMsgExpanded(msg) }]"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
        </div>

        <!-- Toolbar bên dưới câu hỏi: Nút Copy -->
        <div class="ur-chatbot-user-actions-toolbar">
          <button
            type="button"
            class="ur-chatbot-btn-action btn-msg-action"
            :title="copiedMsgId === msg.id ? 'Copied!' : 'Copy question'"
            @click="handleCopy(msg)"
          >
            <svg v-if="copiedMsgId !== msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </button>
        </div>
      </div>

      <!-- Tin nhắn của Bot có render Markdown & Highlight Code -->
      <div v-else class="ur-chatbot-msg-bot-wrapper msg-bot-wrapper">
        <!-- Badge trạng thái Tool Calling khi AI truy xuất dữ liệu ngoài -->
        <div v-if="msg.activeTool" class="ur-chatbot-tool-status-badge" :class="{ 'is-running': msg.activeTool.running }">
          <span v-if="msg.activeTool.running" class="ur-chatbot-tool-spinner">⚡</span>
          <span v-else class="ur-chatbot-tool-check">✓</span>
          <span class="ur-chatbot-tool-msg">
            {{ msg.activeTool.running ? msg.activeTool.message : ('Đã nạp dữ liệu từ công cụ: ' + msg.activeTool.name + (msg.activeTool.executionTimeMs != null ? ' (' + msg.activeTool.executionTimeMs + 'ms)' : '')) }}
          </span>
        </div>

        <!-- Hiệu ứng typing 3 chấm hiển thị NGAY LẬP TỨC khi bot chưa có chữ và không gọi tool -->
        <div v-if="!msg.text && (!msg.thinking || !thinking) && !msg.activeTool" class="ur-chatbot-typing-indicator msg-typing-indicator">
          <span class="ur-chatbot-typing-dots typing-dots">
            <span class="ur-chatbot-dot dot"></span>
            <span class="ur-chatbot-dot dot"></span>
            <span class="ur-chatbot-dot dot"></span>
          </span>
        </div>

        <!-- Khi có nội dung hoặc có suy nghĩ (thinking): Render -->
        <div v-else>
          <!-- Khối Thinking -->
          <thinking-block
            v-if="thinking && msg.thinking"
            :msg="msg"
            :thinking="thinking"
          />

          <!-- Nội dung câu trả lời chính của Bot -->
          <div
            v-if="msg.text"
            :class="['ur-chatbot-markdown markdown-content', { 'is-streaming': msg.isStreaming }]"
            v-html="msg.html || defaultRenderHtml(msg.text, msg.isStreaming)"
          ></div>

          <!-- Thanh action bar dưới câu trả lời của Bot -->
          <div v-if="!msg.isStreaming && !msg.isError && !msg.isResetNotice && (!msg.text || msg.text.indexOf('Conversation has been reset!') === -1)" class="ur-chatbot-actions-toolbar msg-actions-toolbar">
            <!-- Nút Prev / Next phân trang phiên bản -->
            <div v-if="getMsgVersionsCount(msg) > 1" class="ur-chatbot-version-nav">
              <button
                type="button"
                class="ur-chatbot-btn-version-nav"
                title="Câu trả lời trước"
                :disabled="isStreaming || getMsgCurrentVersionIndex(msg) <= 0"
                @click="$emit('prev-version', msg)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
              </button>
              <span class="ur-chatbot-version-label">
                {{ getMsgCurrentVersionIndex(msg) + 1 }} / {{ getMsgVersionsCount(msg) }}
              </span>
              <button
                type="button"
                class="ur-chatbot-btn-version-nav"
                title="Câu trả lời sau"
                :disabled="isStreaming || getMsgCurrentVersionIndex(msg) >= getMsgVersionsCount(msg) - 1"
                @click="$emit('next-version', msg)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </button>
            </div>

            <!-- Nút Copy response -->
            <button
              type="button"
              class="ur-chatbot-btn-action btn-msg-action"
              :title="copiedMsgId === msg.id ? 'Copied!' : 'Copy response'"
              @click="handleCopy(msg)"
            >
              <svg v-if="copiedMsgId !== msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
              <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </button>

            <!-- Nút Đọc to (Read aloud) -->
            <button
              type="button"
              :class="['ur-chatbot-btn-action btn-msg-action', { 'is-speaking': speakingMsgId === msg.id }]"
              :title="speakingMsgId === msg.id ? 'Stop speaking' : 'Read aloud'"
              @click="$emit('toggle-speak', msg)"
            >
              <svg v-if="speakingMsgId === msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                <line x1="23" y1="9" x2="17" y2="15"></line>
                <line x1="17" y1="9" x2="23" y2="15"></line>
              </svg>
              <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
              </svg>
            </button>

            <!-- Nút Regenerate response -->
            <button
              type="button"
              class="ur-chatbot-btn-action btn-msg-action"
              title="Regenerate response (Tạo thêm câu trả lời khác)"
              :disabled="isStreaming"
              @click="$emit('regenerate', msg)"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10"></polyline>
                <polyline points="23 20 23 14 17 14"></polyline>
                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
              </svg>
            </button>

            <!-- Thời gian phản hồi -->
            <span v-if="msg.responseTime" class="ur-chatbot-duration-badge msg-duration-badge">{{ msg.responseTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Thanh câu hỏi ngắn gợi ý (Suggested Prompts) -->
    <div
      v-if="suggestedPrompts && suggestedPrompts.length > 0 && !isStreaming"
      class="ur-chatbot-suggested-prompts suggested-prompts-in-body"
    >
      <div v-if="suggestedTitle" class="ur-chatbot-suggested-title">
        <svg class="ur-chatbot-suggested-title-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
        <span>{{ suggestedTitle }}</span>
      </div>
      <div class="ur-chatbot-suggested-track suggested-prompts-track">
        <button
          v-for="(prompt, idx) in suggestedPrompts"
          :key="idx"
          type="button"
          class="ur-chatbot-prompt-pill prompt-pill"
          :title="prompt"
          @click="$emit('send-suggested-prompt', prompt)"
        >
          <span class="ur-chatbot-prompt-pill-text prompt-pill-text">{{ prompt }}</span>
          <svg class="ur-chatbot-prompt-pill-arrow prompt-pill-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import ThinkingBlock from './ThinkingBlock.vue';
import { parseAndSanitizeMarkdown } from '../services/markdownParser';

export default {
  name: 'MessageList',
  components: {
    ThinkingBlock
  },
  props: {
    messages: {
      type: Array,
      default: () => []
    },
    hiddenEarlierCount: {
      type: Number,
      default: 0
    },
    thinking: {
      type: Boolean,
      default: false
    },
    isStreaming: {
      type: Boolean,
      default: false
    },
    speakingMsgId: {
      type: [Number, String],
      default: null
    },
    suggestedPrompts: {
      type: Array,
      default: () => []
    },
    suggestedTitle: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      copiedMsgId: null,
      userMsgExpandedMap: {},
      userMsgCollapsibleMap: {}
    };
  },
  watch: {
    messages: {
      deep: true,
      handler() {
        this.checkUserMsgHeights();
      }
    }
  },
  mounted() {
    this.checkUserMsgHeights();
  },
  updated() {
    this.checkUserMsgHeights();
  },
  methods: {
    defaultRenderHtml(text, isStreaming) {
      return parseAndSanitizeMarkdown(text, !!isStreaming);
    },
    checkUserMsgHeights() {
      this.$nextTick(() => {
        const refs = this.$refs;
        this.messages.forEach(m => {
          if (m.sender === 'user') {
            const refEl = refs[`userMsg_${m.id}`];
            const el = Array.isArray(refEl) ? refEl[0] : refEl;
            if (el) {
              const isOverflow = el.scrollHeight > 75;
              if (this.userMsgCollapsibleMap[m.id] !== isOverflow) {
                this.$set(this.userMsgCollapsibleMap, m.id, isOverflow);
              }
            }
          }
        });
      });
    },
    isMsgCollapsible(msg) {
      return !!this.userMsgCollapsibleMap[msg.id];
    },
    isMsgExpanded(msg) {
      return !!this.userMsgExpandedMap[msg.id];
    },
    isMsgCollapsed(msg) {
      return this.isMsgCollapsible(msg) && !this.isMsgExpanded(msg);
    },
    toggleUserMsgExpand(msgId) {
      this.$set(this.userMsgExpandedMap, msgId, !this.userMsgExpandedMap[msgId]);
    },
    getMsgVersionsCount(msg) {
      return (msg.versions && Array.isArray(msg.versions)) ? msg.versions.length : 1;
    },
    getMsgCurrentVersionIndex(msg) {
      return (typeof msg.currentVersionIdx === 'number') ? msg.currentVersionIdx : 0;
    },
    handleCopy(msg) {
      const textToCopy = msg.text || '';
      if (!textToCopy) return;

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textToCopy).then(() => {
          this.copiedMsgId = msg.id;
          setTimeout(() => {
            if (this.copiedMsgId === msg.id) {
              this.copiedMsgId = null;
            }
          }, 2000);
        }).catch(() => {
          this.fallbackCopy(textToCopy, msg.id);
        });
      } else {
        this.fallbackCopy(textToCopy, msg.id);
      }
    },
    fallbackCopy(text, msgId) {
      try {
        const ta = document.createElement('textarea');
        ta.value = text;
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        this.copiedMsgId = msgId;
        setTimeout(() => {
          if (this.copiedMsgId === msgId) {
            this.copiedMsgId = null;
          }
        }, 2000);
      } catch (err) {
        console.error('Copy failed:', err);
      }
    },
    onScroll(e) {
      this.$emit('scroll', e);
    },
    getScrollContainer() {
      return this.$refs.chatBody;
    },
    scrollToBottom(smooth = true) {
      this.$nextTick(() => {
        const el = this.$refs.chatBody;
        if (!el) return;
        if (smooth) {
          el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
        } else {
          el.scrollTop = el.scrollHeight;
        }
      });
    }
  }
};
</script>
