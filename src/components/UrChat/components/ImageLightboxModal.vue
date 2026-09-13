<template>
  <transition name="ur-chatbot-fade">
    <div
      v-if="modal && modal.visible"
      class="ur-chatbot-image-modal-backdrop"
      @click.self="$emit('close')"
    >
      <div class="ur-chatbot-image-modal-content">
        <button
          type="button"
          class="ur-chatbot-image-modal-close"
          title="Đóng (Esc)"
          @click="$emit('close')"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <img :src="modal.src" :alt="modal.alt" class="ur-chatbot-image-modal-img" />
        <div class="ur-chatbot-image-modal-footer">
          <div class="ur-chatbot-image-modal-caption">
            {{ modal.alt || 'Hình ảnh' }}
          </div>
          <div class="ur-chatbot-image-modal-actions">
            <a :href="modal.src" target="_blank" rel="noopener noreferrer" class="ur-chatbot-image-modal-btn">
              Mở link gốc ↗
            </a>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'ImageLightboxModal',
  props: {
    modal: {
      type: Object,
      default: () => ({ visible: false, src: '', alt: '' })
    }
  }
};
</script>
