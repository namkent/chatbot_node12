<template>
  <aside class="ur-chat-sidebar" :class="{ 'is-collapsed': isCollapsed, 'is-mobile-closed': isMobileClosed }">
    <!-- 1. Header: Logo, Tên & Nút gập/mở sidebar -->
    <div class="ur-chat-sidebar-header">
      <div class="ur-chat-sidebar-brand" @click="$emit('brand-click')" :title="brandTitle">
        <img class="ur-chat-sidebar-logo" :src="logoSrc" alt="Assistant Chat" />
        <span v-if="!isCollapsed" class="ur-chat-sidebar-brand-title">{{ brandTitle }}</span>
      </div>

      <button
        type="button"
        class="ur-chat-btn-toggle-sidebar"
        :title="isCollapsed ? 'Mở thanh bên' : 'Thu gọn thanh bên'"
        @click="toggleSidebar"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <line x1="9" y1="9" x2="9" y2="21"></line>
        </svg>
      </button>
    </div>

    <!-- 2. Nút tạo cuộc trò chuyện mới (Gemini Pill) -->
    <div class="ur-chat-sidebar-actions">
      <button
        type="button"
        class="ur-chat-new-chat-btn"
        title="Tạo cuộc trò chuyện mới"
        @click="$emit('new-chat')"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span v-if="!isCollapsed" class="ur-chat-new-chat-text">Cuộc trò chuyện mới</span>
      </button>
    </div>

    <!-- 3. Thanh tìm kiếm nhanh hội thoại -->
    <div v-show="!isCollapsed" class="ur-chat-search-container">
      <div class="ur-chat-search-box">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="Tìm kiếm"
        />
        <button v-if="searchKeyword" type="button" class="ur-chat-btn-clear-search" @click="searchKeyword = ''">×</button>
      </div>
    </div>

    <!-- 4. Danh sách các phiên hội thoại ("Gần đây") - Chỉ hiển thị khi mở rộng -->
    <div v-show="!isCollapsed" class="ur-chat-sessions-scroll">
      <div class="ur-chat-sidebar-section-title">Gần đây</div>

      <!-- Trường hợp có danh sách -->
      <div
        v-for="sess in filteredSessions"
        :key="sess.id"
        class="ur-chat-session-item"
        :class="{ 'is-active': sess.id === activeSessionId }"
        :title="sess.title"
        @click="$emit('select-session', sess.id)"
      >
        <!-- Nếu có emoji ở đầu, dùng emoji làm icon riêng thay vì trùng lặp icon SVG -->
        <span v-if="getLeadingEmoji(sess.title)" class="ur-chat-session-emoji-icon" aria-hidden="true">
          {{ getLeadingEmoji(sess.title) }}
        </span>
        <svg v-else class="ur-chat-session-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>

        <span class="ur-chat-session-title">{{ getTitleWithoutLeadingEmoji(sess.title) }}</span>

        <!-- Menu thao tác (Đổi tên / Xoá) -->
        <button
          v-show="!isCollapsed"
          type="button"
          class="ur-chat-session-action-btn"
          title="Xóa cuộc trò chuyện này"
          @click.stop="$emit('delete-session', sess.id)"
        >
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </button>
      </div>

      <!-- Khi không có session nào -->
      <div v-if="filteredSessions.length === 0" style="padding: 20px 14px; text-align: center; color: #94a3b8; font-size: 13px;">
        Chưa có cuộc trò chuyện nào
      </div>
    </div>

    <!-- 5. Footer: User Profile (trái) & Settings (phải) -->
    <div class="ur-chat-sidebar-footer">
      <div class="ur-chat-user-profile" :title="userName">
        <div class="ur-chat-user-avatar">
          <img v-if="userAvatar" :src="userAvatar" :alt="userName" />
          <span v-else>{{ userInitial }}</span>
        </div>
        <div v-show="!isCollapsed" class="ur-chat-user-info-text">
          <span class="ur-chat-user-name">{{ userName }}</span>
          <span class="ur-chat-user-badge">{{ userBadge }}</span>
        </div>
      </div>

      <button
        type="button"
        class="ur-chat-btn-settings"
        title="Cài đặt hệ thống"
        @click="$emit('open-settings')"
      >
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="3"></circle>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
        </svg>
      </button>
    </div>
  </aside>
</template>

<script>
import logoSvg from '@/assets/logo.svg';

export default {
  name: 'ChatSidebar',
  props: {
    sessions: {
      type: Array,
      default: () => []
    },
    activeSessionId: {
      type: String,
      default: ''
    },
    brandTitle: {
      type: String,
      default: 'Assistant Chat'
    },
    userName: {
      type: String,
      default: 'Do Van Nam'
    },
    userRole: {
      type: String,
      default: 'Pro'
    },
    userBadge: {
      type: String,
      default: 'Pro'
    },
    userAvatar: {
      type: String,
      default: ''
    },
    isCollapsed: {
      type: Boolean,
      default: false
    },
    isMobileClosed: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      logoSrc: logoSvg,
      searchKeyword: ''
    };
  },
  computed: {
    filteredSessions() {
      if (!this.searchKeyword.trim()) {
        return this.sessions;
      }
      const kw = this.searchKeyword.toLowerCase().trim();
      return this.sessions.filter((s) => s.title && s.title.toLowerCase().includes(kw));
    },
    userInitial() {
      if (!this.userName) return 'U';
      const cleanName = this.userName.replace(/[()[\]{}]/g, '').trim();
      return cleanName ? cleanName.charAt(0).toUpperCase() : 'U';
    }
  },
  methods: {
    toggleSidebar() {
      this.$emit('toggle-collapse');
    },
    getLeadingEmoji(title) {
      if (!title || typeof title !== 'string') return null;
      try {
        const match = title.trim().match(/^(\p{Extended_Pictographic}|\p{Emoji_Presentation}|\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDE4F]|\uD83E[\uDD00-\uDDFF]|[\u2600-\u27BF])/u);
        return match ? match[1] : null;
      } catch (e) {
        return null;
      }
    },
    getTitleWithoutLeadingEmoji(title) {
      if (!title || typeof title !== 'string') return '';
      const emoji = this.getLeadingEmoji(title);
      if (emoji) {
        return title.trim().slice(emoji.length).trim();
      }
      return title;
    }
  }
};
</script>
