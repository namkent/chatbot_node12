<template>
  <div class="urchat-view-wrapper">
    <UrChat
      ref="chatRef"
      bot-name="Astro Assistant"
      brand-title="Assistant Chat"
      user-id="nam.dovan"
      user-name="Do Van Nam"
      :initial-session-id="$route.params.sessionId"
      status-text="Groq • GPT-OSS 120B (Reasoning)"
      placeholder-text="Nhập tin nhắn (Shift + Enter để xuống dòng)..."
      :thinking="true"
      :attach-file="true"
      @session-change="handleSessionChange"
    />
  </div>
</template>

<script>
import { UrChat } from '../components/UrChat';

export default {
  name: 'ChatView',
  components: {
    UrChat
  },
  methods: {
    handleSessionChange(sessionId) {
      if (sessionId) {
        if (this.$route.params.sessionId !== sessionId) {
          this.$router.replace(`/chat/${sessionId}`).catch(() => {});
        }
      } else {
        if (this.$route.params.sessionId) {
          this.$router.replace('/chat').catch(() => {});
        }
      }
    }
  },
  watch: {
    '$route.params.sessionId'(newId) {
      if (this.$refs.chatRef && typeof this.$refs.chatRef.switchToSession === 'function') {
        this.$refs.chatRef.switchToSession(newId);
      }
    }
  }
};
</script>

<style scoped>
.urchat-view-wrapper {
  height: calc(100vh - 56px);
  width: 100%;
  overflow: hidden;
  background: #ffffff;
}
</style>
