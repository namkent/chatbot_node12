<template>
  <div class="ur-chatbot-header chat-header">
    <div class="ur-chatbot-header-info chat-header-info">
      <!-- Nút toggle sidebar nếu được kích hoạt (chế độ UrChat Assistant) -->
      <button
        v-if="showSidebarToggle"
        type="button"
        class="ur-chatbot-btn-header-action ur-chatbot-btn-sidebar-toggle"
        :title="sidebarCollapsed ? 'Mở thanh bên' : 'Thu gọn thanh bên'"
        @click="$emit('toggle-sidebar')"
      >
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
      </button>

      <div class="ur-chatbot-header-text-group">
        <h4>{{ botName }}</h4>
        <span>
          <span :class="['ur-chatbot-online-indicator online-indicator', { busy: isLoading || isStreaming, error: hasBotError }]"></span>
          {{ statusLabel }}
        </span>
      </div>
    </div>

    <div class="ur-chatbot-header-actions header-actions">
      <!-- Nút Làm mới / Reset cuộc trò chuyện -->
      <button
        class="ur-chatbot-btn-header-action btn-header-action"
        title="Reset conversation"
        :disabled="isStreaming"
        @click="$emit('reset')"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
          <path d="M3 3v5h5"></path>
        </svg>
      </button>

      <!-- Nút Toàn màn hình (Fullscreen) -->
      <button
        v-if="showFullscreenBtn"
        class="ur-chatbot-btn-header-action ur-chatbot-btn-fullscreen btn-header-action"
        :title="isFullscreen ? 'Exit full screen (Esc)' : 'Full screen'"
        @click="$emit('toggle-fullscreen')"
      >
        <svg v-if="!isFullscreen" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"></path>
        </svg>
        <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 3v3a2 2 0 0 1-2 2H3m18 0h-3a2 2 0 0 1-2-2V3m0 18v-3a2 2 0 0 1 2-2h3M3 16h3a2 2 0 0 1 2 2v3"></path>
        </svg>
      </button>

      <!-- Nút Mở rộng / Thu nhỏ ngang -->
      <button
        v-if="showExpandBtn && !isFullscreen"
        class="ur-chatbot-btn-header-action ur-chatbot-btn-expand btn-header-action btn-expand"
        :title="isExpanded ? 'Collapse window' : 'Expand window'"
        @click="$emit('toggle-expand')"
      >
        <svg v-if="!isExpanded" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 3 21 3 21 9"></polyline>
          <polyline points="9 21 3 21 3 15"></polyline>
          <line x1="21" y1="3" x2="14" y2="10"></line>
          <line x1="3" y1="21" x2="10" y2="14"></line>
        </svg>
        <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="4 14 10 14 10 20"></polyline>
          <polyline points="20 10 14 10 14 4"></polyline>
          <line x1="14" y1="10" x2="21" y2="3"></line>
          <line x1="3" y1="21" x2="10" y2="14"></line>
        </svg>
      </button>

      <!-- Nút Đóng (chỉ hiển thị cho widget) -->
      <button
        v-if="showCloseBtn"
        class="ur-chatbot-btn-close btn-close-chat"
        title="Close window"
        @click.stop="$emit('close')"
      >
        ✕
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatHeader',
  props: {
    botName: {
      type: String,
      default: 'Astro Bot AI'
    },
    statusLabel: {
      type: String,
      default: 'Online'
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    isStreaming: {
      type: Boolean,
      default: false
    },
    hasBotError: {
      type: Boolean,
      default: false
    },
    isFullscreen: {
      type: Boolean,
      default: false
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    showFullscreenBtn: {
      type: Boolean,
      default: true
    },
    showExpandBtn: {
      type: Boolean,
      default: true
    },
    showCloseBtn: {
      type: Boolean,
      default: true
    },
    showSidebarToggle: {
      type: Boolean,
      default: false
    },
    sidebarCollapsed: {
      type: Boolean,
      default: false
    }
  }
};
</script>
