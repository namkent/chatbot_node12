<template>
  <div class="studio-view-wrapper">
    <StudioDashboard @switch-view="handleSwitchView" />

    <!-- Widget Chatbot hỗ trợ khi quản trị -->
    <UrChatbot
      bot-name="Astro Bot AI"
      status-text="Groq • GPT-OSS 120B (Reasoning)"
      placeholder-text="Hỏi giải thuật, thực đơn, thời tiết, luật..."
      :local-storage="true"
      :attach-file="true"
      :store-file="false"
      :thinking="true"
    />
  </div>
</template>

<script>
import StudioDashboard from '../components/StudioDashboard.vue';
import { UrChatbot } from '../components/UrChat';

export default {
  name: 'StudioView',
  components: {
    StudioDashboard,
    UrChatbot
  },
  methods: {
    handleSwitchView(view) {
      if (view === 'chat') this.$router.push('/');
      else if (view === 'urchat') this.$router.push('/chat');
      else if (view === 'studio') this.$router.push('/studio');
    }
  }
};
</script>
