<template>
  <!-- TOPBAR VARIANT -->
  <svg
    v-if="variant === 'topbar' && !hasError"
    class="ur-chatbot-topbar-bot bottom-topbar-bot"
    viewBox="0 0 100 100"
  >
    <defs>
      <radialGradient id="sphereGloss" cx="40%" cy="30%" r="65%">
        <stop offset="0%" stop-color="#ffffff" />
        <stop offset="60%" stop-color="#e2e8f0" />
        <stop offset="100%" stop-color="#94a3b8" />
      </radialGradient>
      <linearGradient id="metalDark" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#475569" />
        <stop offset="100%" stop-color="#0f172a" />
      </linearGradient>
      <filter id="neonGlow" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="2" result="coloredBlur" />
        <feMerge>
          <feMergeNode in="coloredBlur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
      <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="5" stdDeviation="4" flood-color="#000000" flood-opacity="0.4" />
      </filter>
      <clipPath id="visorClipStream">
        <rect x="20" y="18" width="60" height="32" rx="16" />
      </clipPath>
      <linearGradient id="scannerLaserGrad" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#00f0ff" stop-opacity="0" />
        <stop offset="50%" stop-color="#00f0ff" stop-opacity="0.9" />
        <stop offset="100%" stop-color="#00f0ff" stop-opacity="0" />
      </linearGradient>
    </defs>

    <g class="anim-astro-float" filter="url(#dropShadow)">
      <g class="astro-leg-l">
        <circle cx="39" cy="76" r="4" fill="url(#metalDark)" />
        <rect x="36" y="76" width="6" height="13" rx="3" fill="url(#sphereGloss)" stroke="#64748b" stroke-width="0.6" />
        <path d="M 33.5 88 C 33.5 85, 44.5 85, 44.5 88 L 44.5 91 C 44.5 93, 33.5 93, 33.5 91 Z" fill="url(#metalDark)" stroke="#94a3b8" stroke-width="0.8" />
      </g>
      <g class="astro-leg-r">
        <circle cx="61" cy="76" r="4" fill="url(#metalDark)" />
        <rect x="58" y="76" width="6" height="13" rx="3" fill="url(#sphereGloss)" stroke="#64748b" stroke-width="0.6" />
        <path d="M 55.5 88 C 55.5 85, 66.5 85, 66.5 88 L 66.5 91 C 66.5 93, 55.5 93, 55.5 91 Z" fill="url(#metalDark)" stroke="#94a3b8" stroke-width="0.8" />
      </g>
      <rect class="astro-arm-l" x="25" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss)" stroke="#94a3b8" stroke-width="0.6" />
      <rect class="astro-arm-r" x="68.5" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss)" stroke="#94a3b8" stroke-width="0.6" />
      <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDark)" />
      <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGloss)" stroke="#cbd5e1" stroke-width="0.8" />
      <circle cx="50" cy="67" r="5" fill="#020617" />
      <circle :class="['anim-core-pulse', { 'anim-core-fast': isLoading || isStreaming }]" cx="50" cy="67" r="3" fill="none" stroke="#00f0ff" stroke-width="1.8" filter="url(#neonGlow)" />
      <circle cx="50" cy="33" r="32" fill="url(#sphereGloss)" stroke="#f8fafc" stroke-width="1.5" />
      <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDark)" stroke="#00f0ff" stroke-width="1.2" filter="url(#neonGlow)" />

      <g transform="rotate(-20 24 16)">
        <rect x="23" y="2" width="2" height="14" rx="1" fill="url(#metalDark)" />
        <circle cx="24" cy="0" r="3.5" fill="#00f0ff" filter="url(#neonGlow)" />
        <circle class="anim-antenna-signal" cx="24" cy="0" r="4.5" stroke="#00f0ff" fill="none" />
      </g>
      <g transform="rotate(20 76 16)">
        <rect x="75" y="2" width="2" height="14" rx="1" fill="url(#metalDark)" />
        <circle cx="76" cy="0" r="3.5" fill="#00f0ff" filter="url(#neonGlow)" />
        <circle class="anim-antenna-signal" cx="76" cy="0" r="4.5" stroke="#00f0ff" fill="none" style="animation-delay: 0.2s;" />
      </g>

      <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDark)" />
      <ellipse cx="15" cy="33" rx="2" ry="6" fill="#94a3b8" />
      <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDark)" />
      <ellipse cx="85" cy="33" rx="2" ry="6" fill="#94a3b8" />

      <rect x="20" y="18" width="60" height="32" rx="16" fill="#020617" stroke="#334155" stroke-width="1.6" />

      <g v-if="isLoading || isStreaming" clip-path="url(#visorClipStream)">
        <rect x="20" y="18" width="60" height="32" fill="#030f20" />
        <text class="anim-binary-stream-1" x="22" y="26" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">101100101011010010101001</text>
        <text class="anim-binary-stream-2" x="22" y="34" fill="#38bdf8" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">010101101001101011001110</text>
        <text class="anim-binary-stream-3" x="22" y="42" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">110100101010110110100011</text>
        <rect class="anim-visor-scanner" x="20" y="18" width="10" height="32" fill="url(#scannerLaserGrad)" />
        <g class="anim-cyber-eyes">
          <circle cx="36" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
          <circle cx="36" cy="34" r="2" fill="#00f0ff" filter="url(#neonGlow)" />
          <circle cx="64" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
          <circle cx="64" cy="34" r="2" fill="#00f0ff" filter="url(#neonGlow)" />
        </g>
      </g>
      <g v-else>
        <g class="anim-eye-blink">
          <circle cx="36" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow)" />
          <circle cx="64" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow)" />
        </g>
        <path d="M 46 44 Q 50 47.5 54 44" fill="none" stroke="#00f0ff" stroke-width="1.8" stroke-linecap="round" filter="url(#neonGlow)" />
      </g>
    </g>
  </svg>

  <!-- TOPBAR ERROR VARIANT -->
  <svg
    v-else-if="variant === 'topbar' && hasError"
    class="ur-chatbot-topbar-bot bottom-topbar-bot bot-error-state"
    viewBox="0 0 100 100"
    title="Bot issue (click to retry)"
    @click="$emit('retry')"
  >
    <defs>
      <radialGradient id="sphereGlossErr" cx="40%" cy="30%" r="65%">
        <stop offset="0%" stop-color="#ffffff" />
        <stop offset="60%" stop-color="#fee2e2" />
        <stop offset="100%" stop-color="#fca5a5" />
      </radialGradient>
      <linearGradient id="metalDarkErr" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#450a0a" />
        <stop offset="100%" stop-color="#1c1917" />
      </linearGradient>
      <filter id="neonGlowErr" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="2.2" result="coloredBlur" />
        <feMerge>
          <feMergeNode in="coloredBlur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
      <filter id="dropShadowErr" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="5" stdDeviation="4" flood-color="#ef4444" flood-opacity="0.35" />
      </filter>
    </defs>

    <g class="anim-astro-error" filter="url(#dropShadowErr)">
      <g class="astro-leg-l">
        <circle cx="39" cy="76" r="4" fill="url(#metalDarkErr)" />
        <rect x="36" y="76" width="6" height="13" rx="3" fill="url(#sphereGlossErr)" stroke="#ef4444" stroke-width="0.8" />
        <path d="M 33.5 88 C 33.5 85, 44.5 85, 44.5 88 L 44.5 91 C 44.5 93, 33.5 93, 33.5 91 Z" fill="url(#metalDarkErr)" stroke="#f87171" stroke-width="0.8" />
      </g>
      <g class="astro-leg-r">
        <circle cx="61" cy="76" r="4" fill="url(#metalDarkErr)" />
        <rect x="58" y="76" width="6" height="13" rx="3" fill="url(#sphereGlossErr)" stroke="#ef4444" stroke-width="0.8" />
        <path d="M 55.5 88 C 55.5 85, 66.5 85, 66.5 88 L 66.5 91 C 66.5 93, 55.5 93, 55.5 91 Z" fill="url(#metalDarkErr)" stroke="#f87171" stroke-width="0.8" />
      </g>
      <rect class="astro-arm-l" x="25" y="62" width="6.5" height="15" rx="3.2" fill="url(#sphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(8 28 62)" />
      <rect class="astro-arm-r" x="68.5" y="62" width="6.5" height="15" rx="3.2" fill="url(#sphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(-8 71 62)" />
      <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDarkErr)" />
      <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGlossErr)" stroke="#fca5a5" stroke-width="0.8" />
      <circle cx="50" cy="67" r="5" fill="#1c1917" />
      <circle class="anim-core-warning" cx="50" cy="67" r="3.2" fill="#ef4444" filter="url(#neonGlowErr)" />
      <circle cx="50" cy="33" r="32" fill="url(#sphereGlossErr)" stroke="#fee2e2" stroke-width="1.5" />
      <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDarkErr)" stroke="#ef4444" stroke-width="1.2" filter="url(#neonGlowErr)" />

      <g transform="rotate(-20 24 16)">
        <rect x="23" y="2" width="2" height="14" rx="1" fill="url(#metalDarkErr)" />
        <circle cx="24" cy="0" r="3.5" fill="#ef4444" filter="url(#neonGlowErr)" />
        <circle class="anim-antenna-error" cx="24" cy="0" r="5" stroke="#ef4444" fill="none" />
      </g>
      <g transform="rotate(20 76 16)">
        <rect x="75" y="2" width="2" height="14" rx="1" fill="url(#metalDarkErr)" />
        <circle cx="76" cy="0" r="3.5" fill="#ef4444" filter="url(#neonGlowErr)" />
        <circle class="anim-antenna-error" cx="76" cy="0" r="5" stroke="#ef4444" fill="none" style="animation-delay: 0.25s;" />
      </g>

      <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDarkErr)" />
      <ellipse cx="15" cy="33" rx="2" ry="6" fill="#f87171" />
      <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDarkErr)" />
      <ellipse cx="85" cy="33" rx="2" ry="6" fill="#f87171" />

      <rect x="20" y="18" width="60" height="32" rx="16" fill="#180404" stroke="#7f1d1d" stroke-width="1.8" />
      <g class="anim-eye-error" filter="url(#neonGlowErr)">
        <line x1="32" y1="30" x2="40" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
        <line x1="40" y1="30" x2="32" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
        <line x1="60" y1="30" x2="68" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
        <line x1="68" y1="30" x2="60" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
      </g>
      <path d="M 44 46 Q 50 42 56 46" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" filter="url(#neonGlowErr)" />
      <path class="anim-sweat-drop" d="M 75 22 Q 77 26 75 28 Q 73 26 75 22 Z" fill="#38bdf8" opacity="0.85" />
    </g>
  </svg>

  <!-- TRIGGER BUTTON VARIANT -->
  <svg
    v-else-if="variant === 'trigger'"
    class="ur-chatbot-trigger-svg trigger-svg"
    viewBox="0 0 100 100"
  >
    <defs>
      <radialGradient id="sphereGloss2" cx="40%" cy="30%" r="65%">
        <stop offset="0%" stop-color="#ffffff" />
        <stop offset="60%" stop-color="#e2e8f0" />
        <stop offset="100%" stop-color="#94a3b8" />
      </radialGradient>
      <linearGradient id="metalDark2" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#475569" />
        <stop offset="100%" stop-color="#0f172a" />
      </linearGradient>
      <filter id="neonGlow2" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="2.5" result="coloredBlur" />
        <feMerge>
          <feMergeNode in="coloredBlur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
      <filter id="btnShadow" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="5" stdDeviation="5" flood-color="#0f172a" flood-opacity="0.4" />
      </filter>
    </defs>

    <g class="state-chat-bubble" filter="url(#btnShadow)">
      <g class="anim-astro-float">
        <circle cx="50" cy="50" r="44" fill="#0f172a" opacity="0.3" filter="blur(6px)" />
        <circle cx="50" cy="50" r="42" fill="url(#sphereGloss2)" stroke="#f8fafc" stroke-width="1.5" />
        <ellipse cx="50" cy="11" rx="18" ry="5" fill="url(#metalDark2)" stroke="#00f0ff" stroke-width="1.5" filter="url(#neonGlow2)" />
        <rect x="5" y="38" width="8" height="24" rx="4" fill="url(#metalDark2)" />
        <rect x="87" y="38" width="8" height="24" rx="4" fill="url(#metalDark2)" />
        <rect x="14" y="30" width="72" height="42" rx="21" fill="#020617" stroke="#334155" stroke-width="2" />
        <g class="anim-eye-blink-head">
          <circle cx="34" cy="50" r="9" fill="none" stroke="#00f0ff" stroke-width="3.5" filter="url(#neonGlow2)" />
          <circle cx="66" cy="50" r="9" fill="none" stroke="#00f0ff" stroke-width="3.5" filter="url(#neonGlow2)" />
        </g>
        <path d="M 45 63 Q 50 67.5 55 63" fill="none" stroke="#00f0ff" stroke-width="2" stroke-linecap="round" filter="url(#neonGlow2)" />
      </g>
    </g>

    <g class="state-bot-cute" filter="url(#btnShadow)">
      <g class="anim-astro-float">
        <g class="astro-leg-l">
          <circle cx="39" cy="76" r="4" fill="url(#metalDark2)" />
          <rect x="36" y="76" width="6" height="13" rx="3" fill="url(#sphereGloss2)" stroke="#64748b" stroke-width="0.6" />
          <path d="M 33.5 88 C 33.5 85, 44.5 85, 44.5 88 L 44.5 91 C 44.5 93, 33.5 93, 33.5 91 Z" fill="url(#metalDark2)" stroke="#94a3b8" stroke-width="0.8" />
        </g>
        <g class="astro-leg-r">
          <circle cx="61" cy="76" r="4" fill="url(#metalDark2)" />
          <rect x="58" y="76" width="6" height="13" rx="3" fill="url(#sphereGloss2)" stroke="#64748b" stroke-width="0.6" />
          <path d="M 55.5 88 C 55.5 85, 66.5 85, 66.5 88 L 66.5 91 C 66.5 93, 55.5 93, 55.5 91 Z" fill="url(#metalDark2)" stroke="#94a3b8" stroke-width="0.8" />
        </g>
        <rect class="astro-arm-l" x="25" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss2)" stroke="#94a3b8" stroke-width="0.6" />
        <rect class="astro-arm-r" x="68.5" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss2)" stroke="#94a3b8" stroke-width="0.6" />
        <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDark2)" />
        <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGloss2)" stroke="#cbd5e1" stroke-width="0.8" />
        <circle cx="50" cy="67" r="5" fill="#020617" />
        <circle cx="50" cy="67" r="3" fill="none" stroke="#00f0ff" stroke-width="1.8" filter="url(#neonGlow2)" />
        <circle cx="50" cy="33" r="32" fill="url(#sphereGloss2)" stroke="#f8fafc" stroke-width="1.5" />
        <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDark2)" stroke="#00f0ff" stroke-width="1.2" filter="url(#neonGlow2)" />
        <g transform="rotate(-20 24 16)">
          <rect x="23" y="2" width="2" height="14" rx="1" fill="url(#metalDark2)" />
          <circle cx="24" cy="0" r="3.5" fill="#00f0ff" filter="url(#neonGlow2)" />
          <circle class="anim-antenna-signal" cx="24" cy="0" r="4.5" stroke="#00f0ff" fill="none" />
        </g>
        <g transform="rotate(20 76 16)">
          <rect x="75" y="2" width="2" height="14" rx="1" fill="url(#metalDark2)" />
          <circle cx="76" cy="0" r="3.5" fill="#00f0ff" filter="url(#neonGlow2)" />
          <circle class="anim-antenna-signal" cx="76" cy="0" r="4.5" stroke="#00f0ff" fill="none" style="animation-delay: 0.2s;" />
        </g>
        <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDark2)" />
        <ellipse cx="15" cy="33" rx="2" ry="6" fill="#94a3b8" />
        <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDark2)" />
        <ellipse cx="85" cy="33" rx="2" ry="6" fill="#94a3b8" />
        <rect x="20" y="18" width="60" height="32" rx="16" fill="#020617" stroke="#334155" stroke-width="1.6" />
        <g class="anim-eye-blink">
          <circle cx="36" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow2)" />
          <circle cx="64" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow2)" />
        </g>
        <path d="M 46 44 Q 50 47.5 54 44" fill="none" stroke="#00f0ff" stroke-width="1.8" stroke-linecap="round" filter="url(#neonGlow2)" />
      </g>
    </g>
  </svg>
</template>

<script>
export default {
  name: 'AstroBot',
  props: {
    variant: {
      type: String,
      default: 'topbar' // 'topbar', 'trigger', 'zerog'
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    isStreaming: {
      type: Boolean,
      default: false
    },
    hasError: {
      type: Boolean,
      default: false
    }
  }
};
</script>
