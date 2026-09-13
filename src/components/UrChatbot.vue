<template>
  <ur-chatbot-impl ref="bot" v-bind="$attrs" v-on="$listeners" />
</template>

<script>
import UrChatbotImpl from './UrChat/UrChatbot.vue';

export default {
  name: 'UrChatbot',
  inheritAttrs: false,
  components: {
    UrChatbotImpl
  },
  methods: {
    clearMessages() {
      if (this.$refs.bot) this.$refs.bot.clearMessages();
    },
    stopStreaming() {
      if (this.$refs.bot) this.$refs.bot.stopStreaming();
    },
    toggleChat(force) {
      if (this.$refs.bot) this.$refs.bot.toggleChat(force);
    },
    toggleFullscreen() {
      if (this.$refs.bot) this.$refs.bot.toggleFullscreen();
    },
    toggleExpand() {
      if (this.$refs.bot) this.$refs.bot.toggleExpand();
    },
    scrollToBottom(smooth) {
      if (this.$refs.bot) this.$refs.bot.scrollToBottom(smooth);
    }
  }
};
</script>
