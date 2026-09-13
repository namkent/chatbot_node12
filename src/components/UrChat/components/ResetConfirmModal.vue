<template>
  <transition name="ur-chatbot-fade">
    <div v-if="visible" class="ur-chatbot-confirm-backdrop confirm-backdrop" @click.self="$emit('cancel')">
      <div class="ur-chatbot-confirm-card ur-chatbot-confirm-dialog confirm-dialog">
        <div class="ur-chatbot-confirm-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
        </div>
        <div class="ur-chatbot-confirm-title confirm-title">{{ title }}</div>
        <div class="ur-chatbot-confirm-desc confirm-desc">{{ desc }}</div>
        <div class="ur-chatbot-confirm-actions confirm-actions">
          <button type="button" class="ur-chatbot-btn-confirm-cancel confirm-btn-cancel" @click="$emit('cancel')">Huỷ</button>
          <button type="button" class="ur-chatbot-btn-confirm-ok confirm-btn-ok" @click="$emit('confirm')">Xác nhận</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'ResetConfirmModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: 'Làm mới cuộc trò chuyện?'
    },
    desc: {
      type: String,
      default: 'Toàn bộ lịch sử tin nhắn hiện tại sẽ được xoá. Hành động này không thể hoàn tác.'
    }
  }
};
</script>
