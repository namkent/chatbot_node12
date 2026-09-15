<template>
  <form
    class="ur-chatbot-footer chat-footer"
    :class="{ 'is-dragover': isDraggingOver }"
    @submit.prevent="handleSubmit"
    @dragover.prevent="onDragOver"
    @dragenter.prevent="onDragEnter"
    @dragleave.prevent="onDragLeave"
    @drop.prevent="onDrop"
  >
    <!-- Nút Cuộn Xuống Dưới Cùng: Nổi phía trên đỉnh khung input chat -->
    <button
      v-if="showScrollBottomBtn"
      type="button"
      class="ur-chatbot-btn-scroll-bottom btn-scroll-bottom"
      title="Cuộn xuống dưới cùng"
      @click.stop.prevent="$emit('scroll-bottom')"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
        <line x1="12" y1="5" x2="12" y2="19"></line>
        <polyline points="19 12 12 19 5 12"></polyline>
      </svg>
    </button>

    <!-- Input file ẩn để chọn ảnh từ máy tính -->
    <input
      v-if="canAttachFile"
      ref="imageFileInput"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleFileInputChange"
    />

    <!-- Khung Card bo góc tròn hiện đại phong cách Gemini -->
    <div class="ur-chatbot-input-card">
      <!-- Dải thumbnail ảnh đính kèm (các ô vuông nhỏ) ở vị trí TOP của khung input -->
      <div v-if="canAttachFile && pendingImages && pendingImages.length > 0" class="ur-chatbot-attachments-strip">
        <div
          v-for="(img, idx) in pendingImages"
          :key="img.id || idx"
          :class="['ur-chatbot-thumb-box', { 'is-uploading': img.uploading, 'is-error': !!img.error }]"
          :title="img.name || 'Ảnh ' + (idx + 1)"
        >
          <img
            :src="img.preview || img.url || img.base64"
            alt="thumbnail"
            class="ur-chatbot-thumb-img"
            @click="$emit('open-image', { src: img.preview || img.url || img.base64, alt: img.name })"
          />

          <!-- Spinner quay khi đang upload -->
          <div v-if="img.uploading" class="ur-chatbot-thumb-loader" title="Đang tải lên...">
            <svg class="ur-chatbot-thumb-spin-icon" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="3" stroke-dasharray="14 14"></circle>
            </svg>
          </div>

          <!-- Nút X nhỏ để xoá ảnh -->
          <button
            type="button"
            class="ur-chatbot-thumb-btn-remove"
            title="Xóa ảnh này"
            :disabled="img.uploading"
            @click.stop.prevent="$emit('remove-image', idx)"
          >
            <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- Textarea nhập nội dung tin nhắn -->
      <textarea
        ref="chatInput"
        :value="value"
        rows="1"
        class="ur-chatbot-input ur-chatbot-textarea"
        :placeholder="isStreaming ? 'Generating response...' : ((pendingImages && pendingImages.length > 0) ? 'Hỏi điều gì về ảnh (hoặc bấm gửi)...' : placeholder)"
        :disabled="isLoading || isStreaming"
        @input="onInput"
        @keydown="handleKeyDown"
        @paste="handlePaste"
      ></textarea>

      <!-- Dòng thanh công cụ dưới: Nút dấu + & Nút Thinking bên trái, Select Model & nút gửi bên phải -->
      <div class="ur-chatbot-card-bottom-bar">
        <div class="ur-chatbot-card-bottom-left">
          <!-- Nút dấu + đính kèm ảnh -->
          <button
            v-if="canAttachFile"
            type="button"
            class="ur-chatbot-btn-plus"
            :class="{ 'is-disabled': !isCurrentModelSupportVision }"
            :title="isCurrentModelSupportVision ? 'Đính kèm ảnh (hoặc kéo thả / dán Ctrl+V)' : 'Mô hình này không hỗ trợ thị giác (Vision not supported)'"
            :disabled="isLoading || isStreaming || !isCurrentModelSupportVision"
            @click="triggerFileInput"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </button>

          <!-- Nút bật / tắt Thinking -->
          <button
            type="button"
            :class="['ur-chatbot-btn-thinking-toggle', { 'is-active': isThinkingActive && isCurrentModelSupportThinking }]"
            :title="thinkingTooltip"
            :disabled="isLoading || isStreaming || !isCurrentModelSupportThinking"
            @click="$emit('toggle-thinking')"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
              <path d="M8.5 14.8C7.2 13.5 6 11.6 6 9.2a6 6 0 1 1 12 0c0 2.4-1.2 4.3-2.5 5.6l-.7 2.2H9.2l-.7-2.2z" />
              <path d="M9.5 19.5h5" />
              <path d="M10.3 7.8c0-1.1.7-1.8 1.7-1.8s1.7.7 1.7 1.8c0 1.2-1.7 1.8-1.7 3.2" stroke-width="2.3" />
              <circle cx="12" cy="13.3" r="1" fill="currentColor" stroke="none" />
              <line x1="2.8" y1="3.8" x2="4.6" y2="5.6" />
              <line x1="1.2" y1="9.5" x2="3.4" y2="9.5" />
              <line x1="2.8" y1="15.2" x2="4.6" y2="13.6" />
              <line x1="21.2" y1="3.8" x2="19.4" y2="5.6" />
              <line x1="22.8" y1="9.5" x2="20.6" y2="9.5" />
              <line x1="21.2" y1="15.2" x2="19.4" y2="13.6" />
            </svg>
          </button>
        </div>

        <div class="ur-chatbot-card-bottom-right">
          <!-- Select list model dropdown popover -->
          <div class="ur-chatbot-model-select-wrapper" ref="modelSelectDropdown">
            <button
              type="button"
              class="ur-chatbot-model-select-btn"
              :title="'Mô hình đang chọn: ' + currentModelDisplayName + ' (Bấm để đổi)'"
              :disabled="isLoading || isStreaming"
              @click.stop="$emit('toggle-model-dropdown')"
            >
              <span class="ur-chatbot-model-select-name">{{ currentModelDisplayName }}</span>
              <svg
                :class="['ur-chatbot-model-chevron', { 'is-open': showModelDropdown }]"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </button>

            <transition name="ur-chatbot-dropdown-popover">
              <div v-if="showModelDropdown" class="ur-chatbot-model-menu" @click.stop>
                <div class="ur-chatbot-model-menu-list">
                  <div
                    v-for="mItem in modelListOptions"
                    :key="mItem.id"
                    :class="['ur-chatbot-model-option', { 'is-selected': effectiveModel === mItem.id }]"
                    @click="$emit('select-model', mItem.id)"
                  >
                    <div class="ur-chatbot-model-option-main">
                      <div class="ur-chatbot-model-option-name">
                        <span>{{ mItem.name }}</span>
                        <span
                          v-for="(b, bIdx) in getModelBadges(mItem)"
                          :key="bIdx"
                          :class="['ur-chatbot-model-tag', b.type]"
                        >
                          {{ b.label }}
                        </span>
                      </div>
                      <div class="ur-chatbot-model-option-desc">{{ mItem.desc }}</div>
                    </div>
                    <svg v-if="effectiveModel === mItem.id" class="ur-chatbot-model-check-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- Nút Dừng khi đang stream -->
          <button
            v-if="isStreaming"
            type="button"
            class="ur-chatbot-btn-stop-gemini"
            title="Stop generating"
            @click="$emit('stop')"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
              <rect x="4" y="4" width="16" height="16" rx="3" ry="3" />
            </svg>
          </button>

          <!-- Nút Gửi hình tròn có mũi tên lên (↑) kiểu Gemini -->
          <button
            v-else
            type="submit"
            class="ur-chatbot-btn-send-gemini"
            title="Send message (Enter to send, Shift + Enter for new line)"
            :disabled="isLoading || isUploadingAnyImage || (!value.trim() && (!pendingImages || pendingImages.length === 0))"
          >
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="19" x2="12" y2="5"></line>
              <polyline points="5 12 12 5 19 12"></polyline>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </form>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: 'Type a message (Shift + Enter for new line)...'
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    isStreaming: {
      type: Boolean,
      default: false
    },
    canAttachFile: {
      type: Boolean,
      default: false
    },
    pendingImages: {
      type: Array,
      default: () => []
    },
    isThinkingActive: {
      type: Boolean,
      default: false
    },
    isCurrentModelSupportThinking: {
      type: Boolean,
      default: true
    },
    isCurrentModelSupportVision: {
      type: Boolean,
      default: true
    },
    thinkingTooltip: {
      type: String,
      default: ''
    },
    showModelDropdown: {
      type: Boolean,
      default: false
    },
    currentModelDisplayName: {
      type: String,
      default: ''
    },
    effectiveModel: {
      type: String,
      default: ''
    },
    modelListOptions: {
      type: Array,
      default: () => []
    },
    showScrollBottomBtn: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isDraggingOver: false
    };
  },
  computed: {
    isUploadingAnyImage() {
      return this.pendingImages && this.pendingImages.some(img => img.uploading);
    }
  },
  methods: {
    focus() {
      if (this.$refs.chatInput) {
        this.$refs.chatInput.focus();
      }
    },
    getModelBadges(mItem) {
      if (!mItem) return [];
      const badges = [];
      const hasThinking = typeof mItem.thinking === 'boolean'
        ? mItem.thinking
        : (typeof mItem.supportsThinking === 'boolean'
          ? mItem.supportsThinking
          : (mItem.badge === 'Reasoning' || /gpt-oss|120b|20b|deepseek-r1|reasoning|r1|think/i.test(mItem.id)));

      const hasVision = typeof mItem.vision === 'boolean'
        ? mItem.vision
        : (typeof mItem.supportsVision === 'boolean'
          ? mItem.supportsVision
          : (mItem.badge === 'Vision' || /vision|vl|qwen|gpt-4o|gemini|claude/i.test(mItem.id)));

      if (hasThinking) {
        badges.push({ label: 'Reasoning', type: 'badge-reasoning' });
      }
      if (hasVision) {
        badges.push({ label: 'Vision', type: 'badge-vision' });
      }
      if (badges.length === 0 && mItem.badge) {
        badges.push({ label: mItem.badge, type: mItem.badgeType || 'badge-default' });
      }
      return badges;
    },
    triggerFileInput() {
      if (this.$refs.imageFileInput) {
        this.$refs.imageFileInput.click();
      }
    },
    onInput(e) {
      this.$emit('input', e.target.value);
      this.autoResizeInput();
    },
    autoResizeInput() {
      const el = this.$refs.chatInput;
      if (!el) return;
      el.style.height = 'auto';
      const maxHeight = 160;
      el.style.height = Math.min(el.scrollHeight, maxHeight) + 'px';
      el.style.overflowY = el.scrollHeight > maxHeight ? 'auto' : 'hidden';
    },
    handleKeyDown(e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.handleSubmit();
      }
    },
    handleSubmit() {
      if (this.isLoading || this.isStreaming || this.isUploadingAnyImage) return;
      if (!this.value.trim() && (!this.pendingImages || this.pendingImages.length === 0)) return;
      this.$emit('send');
      this.$nextTick(() => {
        this.autoResizeInput();
      });
    },
    handleFileInputChange(e) {
      const files = Array.from(e.target.files || []);
      if (files.length > 0) {
        this.$emit('files-selected', files);
      }
      e.target.value = '';
    },
    onDragOver(e) {
      if (!this.canAttachFile) return;
      this.isDraggingOver = true;
    },
    onDragEnter(e) {
      if (!this.canAttachFile) return;
      this.isDraggingOver = true;
    },
    onDragLeave(e) {
      this.isDraggingOver = false;
    },
    onDrop(e) {
      this.isDraggingOver = false;
      if (!this.canAttachFile) return;
      const files = Array.from(e.dataTransfer.files || []);
      if (files.length > 0) {
        this.$emit('files-selected', files);
      }
    },
    handlePaste(e) {
      if (!this.canAttachFile) return;
      const items = (e.clipboardData || e.originalEvent.clipboardData).items;
      const files = [];
      for (let i = 0; i < items.length; i++) {
        if (items[i].type.indexOf('image') !== -1) {
          const file = items[i].getAsFile();
          if (file) files.push(file);
        }
      }
      if (files.length > 0) {
        this.$emit('files-selected', files);
      }
    }
  }
};
</script>
