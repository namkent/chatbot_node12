<template>
  <div
    v-if="thinking && msg.thinking"
    class="ur-chatbot-thinking-block"
    :class="{ 'is-collapsed': isCollapsed, 'is-streaming': msg.isStreaming && !msg.text }"
  >
    <div class="ur-chatbot-thinking-header" @click="toggleCollapse">
      <span class="ur-chatbot-thinking-title">{{ headerLabel }}</span>
      <svg
        class="ur-chatbot-thinking-chevron"
        :class="{ 'is-collapsed': isCollapsed }"
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.4"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <polyline points="18 15 12 9 6 15"></polyline>
      </svg>
    </div>
    <div v-show="!isCollapsed" class="ur-chatbot-thinking-content">
      <div
        v-for="(step, sIdx) in steps"
        :key="sIdx"
        class="ur-chatbot-thinking-step-item"
      >
        <div class="ur-chatbot-thinking-bullet">•</div>
        <div class="ur-chatbot-thinking-step-body">
          <div v-if="step.title" class="ur-chatbot-thinking-step-title">{{ step.title }}</div>
          <div v-if="step.desc" class="ur-chatbot-thinking-step-desc">{{ step.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ThinkingBlock',
  props: {
    msg: {
      type: Object,
      required: true
    },
    thinking: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      userCollapsed: null
    };
  },
  computed: {
    isCollapsed() {
      if (this.userCollapsed !== null) {
        return this.userCollapsed;
      }
      if (this.msg.isStreaming) {
        return false;
      }
      return true;
    },
    headerLabel() {
      if (this.msg.isStreaming && !this.msg.text) {
        return 'Thinking...';
      }
      const dur = this.msg.thinkingDuration || (this.msg.responseTime ? this.msg.responseTime.replace(/\..*$/, 's') : '5s');
      return `Thought for ${dur}`;
    },
    steps() {
      const text = this.msg.thinking;
      if (!text || typeof text !== 'string') return [];
      const clean = text.replace(/\r\n/g, '\n').trim();
      if (!clean) return [];

      const rawBlocks = clean.split(/\n{2,}/);
      const result = [];

      for (let i = 0; i < rawBlocks.length; i++) {
        let b = rawBlocks[i].trim();
        if (!b) continue;

        b = b.replace(/^[\*\-\•]\s+/, '').replace(/^\d+\.\s+/, '');

        const boldMatch = b.match(/^\*\*([^*]+)\*\*[:\s]*([\s\S]*)$/);
        if (boldMatch) {
          result.push({
            title: boldMatch[1].trim(),
            desc: boldMatch[2].trim()
          });
          continue;
        }

        const lines = b.split('\n');
        if (lines.length > 1) {
          result.push({
            title: lines[0].trim(),
            desc: lines.slice(1).join('\n').trim()
          });
        } else {
          result.push({
            title: '',
            desc: b
          });
        }
      }

      return result;
    }
  },
  methods: {
    toggleCollapse() {
      this.userCollapsed = !this.isCollapsed;
    }
  }
};
</script>
