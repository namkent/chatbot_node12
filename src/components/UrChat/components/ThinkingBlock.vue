<template>
  <div
    v-if="thinking && msg.thinking"
    class="ur-chatbot-thinking-block"
    :class="{ 'is-collapsed': isCollapsed, 'is-streaming': isStreamingThinking }"
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

    <!-- Khi đang stream suy luận: Chỉ hiển thị step gần nhất -->
    <div
      v-if="!isCollapsed && isStreamingThinking"
      class="ur-chatbot-thinking-content is-streaming-view"
    >
      <div
        v-if="latestStep"
        :key="`stream_step_${latestStepIndex}`"
        class="ur-chatbot-thinking-step-item is-active-step"
      >
        <div class="ur-chatbot-thinking-bullet is-pulsing">
          <span class="ur-chatbot-thinking-pulse-dot"></span>
        </div>
        <div class="ur-chatbot-thinking-step-body">
          <div v-if="steps.length > 1 || latestStep.title" class="ur-chatbot-thinking-step-title-row">
            <span v-if="steps.length > 1" class="ur-chatbot-thinking-step-badge">Bước {{ steps.length }}</span>
            <span v-if="latestStep.title" class="ur-chatbot-thinking-step-title">{{ latestStep.title }}</span>
          </div>
          <div v-if="latestStep.desc" class="ur-chatbot-thinking-step-desc">
            {{ latestStep.desc }}<span class="ur-chatbot-thinking-cursor"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Khi đã hoàn tất ([DONE]): Hiển thị toàn bộ các step khi người dùng bấm mở ra -->
    <div
      v-else-if="!isCollapsed"
      class="ur-chatbot-thinking-content"
    >
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
    isStreamingThinking() {
      return Boolean(this.msg.isStreaming && !this.msg.text && this.msg.thinking);
    },
    isCollapsed() {
      if (this.userCollapsed !== null) {
        return this.userCollapsed;
      }
      // Khi đang stream suy luận (chưa có text trả lời chính), tự động mở để hiển thị step hiện tại
      if (this.isStreamingThinking) {
        return false;
      }
      // Khi đã stream xong ([DONE]) hoặc đã có text trả lời chính, mặc định thu gọn lại
      return true;
    },
    headerLabel() {
      if (this.isStreamingThinking) {
        return this.steps.length > 1 ? `Thinking (Bước ${this.steps.length})...` : 'Thinking...';
      }
      const dur = this.msg.thinkingDuration || (this.msg.responseTime ? this.msg.responseTime.replace(/\..*$/, 's') : '3s');
      return `Thought for ${dur}`;
    },
    steps() {
      const text = this.msg.thinking;
      if (!text || typeof text !== 'string') return [];
      const clean = text.replace(/\r\n/g, '\n').trim();
      if (!clean) return [];

      let rawBlocks = [];

      // 1. Tách theo ngắt đoạn (\n\n) hoặc ngắt dòng (\n)
      if (clean.indexOf('\n\n') !== -1) {
        rawBlocks = clean.split(/\n{2,}/).map(s => s.trim()).filter(Boolean);
      } else if (clean.indexOf('\n') !== -1) {
        rawBlocks = clean.split(/\n+/).map(s => s.trim()).filter(Boolean);
      } else {
        rawBlocks = [clean];
      }

      // 2. Chỉ khi text là một khối duy nhất không có xuống dòng (như GPT-OSS 120B),
      // mới cần tách thông minh theo câu suy luận logic. Còn nếu model đã tự ngắt đoạn (DeepSeek-R1, Qwen...)
      // thì giữ nguyên 100% cấu trúc đoạn của model!
      let expandedBlocks = [];
      if (rawBlocks.length === 1 && rawBlocks[0].length > 80) {
        const sentenceRegex = /(?<=[.!?])\s+(?=[A-Z0-9\u00C0-\u024F\u1EA0-\u1EF9])/g;
        const parts = rawBlocks[0].split(sentenceRegex).map(s => s.trim()).filter(Boolean);
        if (parts.length > 1) {
          let currentGroup = '';
          for (let j = 0; j < parts.length; j++) {
            const p = parts[j];
            if (!currentGroup) {
              currentGroup = p;
            } else if (currentGroup.length < 50 || p.length < 35) {
              currentGroup += ' ' + p;
            } else {
              expandedBlocks.push(currentGroup);
              currentGroup = p;
            }
          }
          if (currentGroup) {
            expandedBlocks.push(currentGroup);
          }
        } else {
          expandedBlocks = rawBlocks;
        }
      } else {
        expandedBlocks = rawBlocks;
      }

      // 3. Phân tách title & desc cho từng block
      const result = [];
      for (let i = 0; i < expandedBlocks.length; i++) {
        let b = expandedBlocks[i].trim();
        if (!b) continue;

        b = b.replace(/^[\*\-\•]\s+/, '').replace(/^\d+[\.\)]\s+/, '');

        const boldMatch = b.match(/^\*\*([^*]+)\*\*[:\s]*([\s\S]*)$/);
        if (boldMatch) {
          result.push({
            title: boldMatch[1].trim(),
            desc: boldMatch[2].trim()
          });
          continue;
        }

        const headerMatch = b.match(/^#{1,4}\s+([^\n]+)\n*([\s\S]*)$/);
        if (headerMatch) {
          result.push({
            title: headerMatch[1].trim(),
            desc: headerMatch[2].trim()
          });
          continue;
        }

        const colonMatch = b.match(/^([A-Z\u00C0-\u024F\u1EA0-\u1EF9][a-zA-Z0-9\s_\-]{2,25}):\s*([\s\S]+)$/);
        if (colonMatch) {
          result.push({
            title: colonMatch[1].trim(),
            desc: colonMatch[2].trim()
          });
          continue;
        }

        const lines = b.split('\n');
        if (lines.length > 1 && lines[0].trim().length <= 70 && !lines[0].trim().endsWith('.')) {
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

      return result.length > 0 ? result : [{ title: '', desc: clean }];
    },
    latestStep() {
      if (!this.steps || this.steps.length === 0) return null;
      return this.steps[this.steps.length - 1];
    },
    latestStepIndex() {
      return this.steps ? this.steps.length - 1 : 0;
    }
  },
  watch: {
    'msg.thinking'() {
      if (this.msg.isStreaming) {
        this.$emit('scroll-bottom');
      }
    }
  },
  methods: {
    toggleCollapse() {
      this.userCollapsed = !this.isCollapsed;
    }
  }
};
</script>
