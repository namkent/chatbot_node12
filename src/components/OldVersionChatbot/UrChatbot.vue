<template>
  <div :class="['ur-chatbot-wrapper chatbot-widget-container', { 'is-fullscreen': isOpen && isFullscreen }]">
    <!-- CỬA SỔ CHAT NỀN SÁNG (LIGHT THEME) -->
    <div :class="['ur-chatbot-window chat-window', { open: isOpen, expanded: isExpanded, fullscreen: isOpen && isFullscreen }]">
      <!-- SVG 3A: Bot bình thường bay lơ lửng ở Topbar -->
      <svg v-if="!hasBotError" class="ur-chatbot-topbar-bot bottom-topbar-bot" viewBox="0 0 100 100">
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

        <!-- Trôi nổi toàn thân -->
        <g class="anim-astro-float" filter="url(#dropShadow)">
          <!-- CHÂN -->
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

          <!-- TAY TRÔI NỔI -->
          <rect class="astro-arm-l" x="25" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss)" stroke="#94a3b8" stroke-width="0.6" />
          <rect class="astro-arm-r" x="68.5" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss)" stroke="#94a3b8" stroke-width="0.6" />

          <!-- CỔ VÀ THÂN -->
          <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDark)" />
          <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGloss)" stroke="#cbd5e1" stroke-width="0.8" />

          <!-- Lõi năng lượng ngực -->
          <circle cx="50" cy="67" r="5" fill="#020617" />
          <circle :class="['anim-core-pulse', { 'anim-core-fast': isLoading || isStreaming }]" cx="50" cy="67" r="3" fill="none" stroke="#00f0ff" stroke-width="1.8" filter="url(#neonGlow)" />

          <!-- ĐẦU HÌNH TRÒN -->
          <circle cx="50" cy="33" r="32" fill="url(#sphereGloss)" stroke="#f8fafc" stroke-width="1.5" />
          <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDark)" stroke="#00f0ff" stroke-width="1.2" filter="url(#neonGlow)" />

          <!-- ĂNG-TEN ĐỈNH TAI -->
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

          <!-- ỐP TAI NGHE -->
          <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDark)" />
          <ellipse cx="15" cy="33" rx="2" ry="6" fill="#94a3b8" />

          <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDark)" />
          <ellipse cx="85" cy="33" rx="2" ry="6" fill="#94a3b8" />

          <!-- MẶT NẠ KÍNH TỐI MÀU -->
          <rect x="20" y="18" width="60" height="32" rx="16" fill="#020617" stroke="#334155" stroke-width="1.6" />

          <!-- TRẠNG THÁI 1: KHI AI ĐANG SUY NGHĨ / TRẢ LỜI (MÃ NHỊ PHÂN & LASER SCANNER VIỄN TƯỞNG) -->
          <g v-if="isLoading || isStreaming" clip-path="url(#visorClipStream)">
            <rect x="20" y="18" width="60" height="32" fill="#030f20" />

            <!-- Chuỗi mã nhị phân chạy ngang 3 tầng -->
            <text class="anim-binary-stream-1" x="22" y="26" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">101100101011010010101001</text>
            <text class="anim-binary-stream-2" x="22" y="34" fill="#38bdf8" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">010101101001101011001110</text>
            <text class="anim-binary-stream-3" x="22" y="42" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6" font-weight="bold" letter-spacing="1">110100101010110110100011</text>

            <!-- Vệt sáng Laser Scanner quét qua lại trên mắt -->
            <rect class="anim-visor-scanner" x="20" y="18" width="10" height="32" fill="url(#scannerLaserGrad)" />

            <!-- Hai tâm mắt AI Cyber-Optics nhấp nháy xoay radar -->
            <g class="anim-cyber-eyes">
              <circle cx="36" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
              <circle cx="36" cy="34" r="2" fill="#00f0ff" filter="url(#neonGlow)" />
              <circle cx="64" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
              <circle cx="64" cy="34" r="2" fill="#00f0ff" filter="url(#neonGlow)" />
            </g>
          </g>

          <!-- TRẠNG THÁI 2: CHẾ ĐỘ NGHỈ (IDLE) - MẮT NEON XANH THÂN THIỆN -->
          <g v-else>
            <g class="anim-eye-blink">
              <circle cx="36" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow)" />
              <circle cx="64" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow)" />
            </g>
            <path d="M 46 44 Q 50 47.5 54 44" fill="none" stroke="#00f0ff" stroke-width="1.8" stroke-linecap="round" filter="url(#neonGlow)" />
          </g>
        </g>
      </svg>

      <!-- SVG 3B: Bot trạng thái LỖI (Hết token, server ngắt kết nối, lỗi mạng) -->
      <svg
        v-else
        class="ur-chatbot-topbar-bot bottom-topbar-bot bot-error-state"
        viewBox="0 0 100 100"
        title="Bot issue (token limit or server disconnect)! Click to retry"
        @click="retryLastAction"
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

        <!-- Toàn thân trôi nổi kèm rung báo động lỗi -->
        <g class="anim-astro-error" filter="url(#dropShadowErr)">
          <!-- CHÂN -->
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

          <!-- TAY TRÔI NỔI BUÔNG XUỐNG BỐI RỐI -->
          <rect class="astro-arm-l" x="25" y="62" width="6.5" height="15" rx="3.2" fill="url(#sphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(8 28 62)" />
          <rect class="astro-arm-r" x="68.5" y="62" width="6.5" height="15" rx="3.2" fill="url(#sphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(-8 71 62)" />

          <!-- CỔ VÀ THÂN -->
          <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDarkErr)" />
          <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGlossErr)" stroke="#fca5a5" stroke-width="0.8" />

          <!-- Lõi năng lượng báo lỗi màu đỏ chớp tắt -->
          <circle cx="50" cy="67" r="5" fill="#1c1917" />
          <circle class="anim-core-warning" cx="50" cy="67" r="3.2" fill="#ef4444" filter="url(#neonGlowErr)" />

          <!-- ĐẦU HÌNH TRÒN -->
          <circle cx="50" cy="33" r="32" fill="url(#sphereGlossErr)" stroke="#fee2e2" stroke-width="1.5" />
          <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDarkErr)" stroke="#ef4444" stroke-width="1.2" filter="url(#neonGlowErr)" />

          <!-- ĂNG-TEN BÁO ĐỘNG ĐỎ PHÁT TÍN HIỆU -->
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

          <!-- ỐP TAI NGHE -->
          <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDarkErr)" />
          <ellipse cx="15" cy="33" rx="2" ry="6" fill="#f87171" />

          <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDarkErr)" />
          <ellipse cx="85" cy="33" rx="2" ry="6" fill="#f87171" />

          <!-- MẶT NẠ KÍNH TỐI MÀU BÁO LỖI -->
          <rect x="20" y="18" width="60" height="32" rx="16" fill="#180404" stroke="#7f1d1d" stroke-width="1.8" />

          <!-- MẮT LỖI X X NEON ĐỎ -->
          <g class="anim-eye-error" filter="url(#neonGlowErr)">
            <line x1="32" y1="30" x2="40" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
            <line x1="40" y1="30" x2="32" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
            <line x1="60" y1="30" x2="68" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
            <line x1="68" y1="30" x2="60" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
          </g>

          <!-- MIỆNG BUỒN / GẬP NÉT ZIGZAG CỦA BOT KHI GẶP LỖI -->
          <path d="M 44 46 Q 50 42 56 46" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" filter="url(#neonGlowErr)" />

          <!-- GIỌT MỒ HÔI LO LẮNG BÊN THÁI DƯƠNG -->
          <path class="anim-sweat-drop" d="M 75 22 Q 77 26 75 28 Q 73 26 75 22 Z" fill="#38bdf8" opacity="0.85" />
        </g>
      </svg>

      <!-- Topbar Header -->
      <div class="ur-chatbot-header chat-header">
        <div class="ur-chatbot-header-info chat-header-info">
          <h4>{{ effectiveBotName }}</h4>
          <span>
            <span :class="['ur-chatbot-online-indicator online-indicator', { busy: isLoading || isStreaming, error: hasBotError }]"></span>
            {{ statusLabel }}
          </span>
        </div>
        <div class="ur-chatbot-header-actions header-actions">

          <!-- Nút Làm mới / Reset cuộc trò chuyện -->
          <button
            class="ur-chatbot-btn-header-action btn-header-action"
            title="Reset conversation"
            :disabled="isStreaming"
            @click="promptResetChat"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
              <path d="M3 3v5h5"></path>
            </svg>
          </button>

          <!-- Nút Toàn màn hình (Fullscreen) -->
          <button
            class="ur-chatbot-btn-header-action ur-chatbot-btn-fullscreen btn-header-action"
            :title="isFullscreen ? 'Exit full screen (Esc)' : 'Full screen'"
            @click="toggleFullscreen"
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
            v-if="!isFullscreen"
            class="ur-chatbot-btn-header-action ur-chatbot-btn-expand btn-header-action btn-expand"
            :title="isExpanded ? 'Collapse window' : 'Expand window'"
            @click="toggleExpand"
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

          <!-- Nút Đóng -->
          <button
            class="ur-chatbot-btn-close btn-close-chat"
            title="Close window"
            @click.stop="toggleChat(false)"
          >
            ✕
          </button>
        </div>
      </div>

      <!-- Khung chat nền sáng (Hỗ trợ Virtual Scroll mượt mà) -->
      <div class="ur-chatbot-body chat-body" ref="chatBody" @scroll="handleBodyScroll" @click="handleBodyClick">
        <!-- Nút tải thêm tin nhắn trước đó khi bật Virtual Scroll -->
        <div v-if="hiddenEarlierCount > 0" class="ur-chatbot-virtual-history virtual-history-wrapper">
          <button type="button" class="ur-chatbot-btn-load-earlier btn-load-earlier" @click="loadEarlierMessages">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="18 15 12 9 6 15"></polyline>
            </svg>
            Load {{ hiddenEarlierCount }} earlier messages
          </button>
        </div>

        <div
          v-for="msg in visibleMessageList"
          :key="msg.id"
          :class="[
            'ur-chatbot-msg-bubble msg-bubble',
            msg.sender === 'user' ? 'ur-chatbot-msg-user msg-user' : 'ur-chatbot-msg-bot msg-bot',
            { 'ur-chatbot-msg-error msg-error': msg.isError }
          ]"
        >
          <!-- Tin nhắn của User -->
          <div v-if="msg.sender === 'user'" class="ur-chatbot-msg-user-wrapper">
            <!-- Hiển thị ảnh đính kèm của User (nếu có) -->
            <div v-if="msg.images && msg.images.length > 0" class="ur-chatbot-user-imgs-row">
              <div
                v-for="(uImg, uIdx) in msg.images"
                :key="uIdx"
                class="ur-chatbot-user-img-card"
                :title="uImg.name || 'Ảnh đính kèm (click để phóng to)'"
                @click="openImageModal(uImg.preview || uImg.url || uImg.base64, uImg.name)"
              >
                <img :src="uImg.preview || uImg.url || uImg.base64" :alt="uImg.name || 'Attachment'" />
                <span v-if="uImg.sendMode" class="ur-chatbot-user-img-mode-badge">{{ uImg.sendMode === 'url' ? 'S3' : 'B64' }}</span>
              </div>
            </div>

            <div
              v-if="msg.text"
              :class="[
                'ur-chatbot-msg-user-text msg-user-text',
                { 'is-collapsed': isMsgCollapsed(msg) }
              ]"
              :ref="`userMsg_${msg.id}`"
            >{{ msg.text }}</div>

            <!-- Nút Chevron Thu gọn / Mở rộng nếu nội dung dài hơn 3 dòng -->
            <div v-if="isMsgCollapsible(msg)" class="ur-chatbot-user-expand-row">
              <button
                type="button"
                class="ur-chatbot-btn-user-expand"
                :title="isMsgExpanded(msg) ? 'Collapse' : 'Expand'"
                @click="toggleUserMsgExpand(msg.id)"
              >
                <svg
                  :class="['ur-chatbot-icon-chevron', { 'is-expanded': isMsgExpanded(msg) }]"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </button>
            </div>

            <!-- Toolbar bên dưới câu hỏi: Nút Copy (đã bỏ nút Regenerate question) -->
            <div class="ur-chatbot-user-actions-toolbar">
              <button
                type="button"
                class="ur-chatbot-btn-action btn-msg-action"
                :title="copiedMsgId === msg.id ? 'Copied!' : 'Copy question'"
                @click="copyMessageContent(msg)"
              >
                <svg v-if="copiedMsgId !== msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
                <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </button>
            </div>
          </div>

          <!-- Tin nhắn của Bot có render Markdown & Highlight Code -->
          <div v-else class="ur-chatbot-msg-bot-wrapper msg-bot-wrapper">
            <!-- Badge trạng thái Tool Calling khi AI truy xuất dữ liệu ngoài -->
            <div v-if="msg.activeTool" class="ur-chatbot-tool-status-badge" :class="{ 'is-running': msg.activeTool.running }">
              <span v-if="msg.activeTool.running" class="ur-chatbot-tool-spinner">⚡</span>
              <span v-else class="ur-chatbot-tool-check">✓</span>
              <span class="ur-chatbot-tool-msg">
                {{ msg.activeTool.running ? msg.activeTool.message : ('Đã nạp dữ liệu từ công cụ: ' + msg.activeTool.name + (msg.activeTool.executionTimeMs != null ? ' (' + msg.activeTool.executionTimeMs + 'ms)' : '')) }}
              </span>
            </div>

            <!-- Hiệu ứng typing 3 chấm hiển thị NGAY LẬP TỨC khi bot chưa có chữ và không gọi tool -->
            <div v-if="!msg.text && (!msg.thinking || !thinking) && !msg.activeTool" class="ur-chatbot-typing-indicator msg-typing-indicator">
              <span class="ur-chatbot-typing-dots typing-dots">
                <span class="ur-chatbot-dot dot"></span>
                <span class="ur-chatbot-dot dot"></span>
                <span class="ur-chatbot-dot dot"></span>
              </span>
            </div>

            <!-- Khi có nội dung hoặc có suy nghĩ (thinking): Render -->
            <div v-else>
              <!-- Khối Thinking (hiển thị khi props thinking = true và bot có nội dung thinking) -->
              <div
                v-if="thinking && msg.thinking"
                class="ur-chatbot-thinking-block"
                :class="{ 'is-collapsed': isThinkingCollapsed(msg), 'is-streaming': msg.isStreaming && !msg.text }"
              >
                <div class="ur-chatbot-thinking-header" @click="toggleThinkingCollapse(msg)">
                  <span class="ur-chatbot-thinking-title">{{ getThinkingHeaderLabel(msg) }}</span>
                  <svg
                    class="ur-chatbot-thinking-chevron"
                    :class="{ 'is-collapsed': isThinkingCollapsed(msg) }"
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
                <div v-show="!isThinkingCollapsed(msg)" class="ur-chatbot-thinking-content">
                  <div
                    v-for="(step, sIdx) in getThinkingSteps(msg.thinking)"
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

              <!-- Nội dung câu trả lời chính của Bot -->
              <div
                v-if="msg.text"
                :class="['ur-chatbot-markdown markdown-content', { 'is-streaming': msg.isStreaming }]"
                v-html="msg.html || renderHtml(msg.text, msg.isStreaming)"
              ></div>

              <!-- Thanh action bar dưới câu trả lời của Bot (Ẩn khi là isResetNotice hoặc thông báo reset) -->
              <div v-if="!msg.isStreaming && !msg.isError && !msg.isResetNotice && (!msg.text || msg.text.indexOf('Conversation has been reset!') === -1)" class="ur-chatbot-actions-toolbar msg-actions-toolbar">
                <!-- Nút Prev / Next phân trang phiên bản (chỉ xuất hiện khi có >= 2 phiên bản trả lời) -->
                <div v-if="getMsgVersionsCount(msg) > 1" class="ur-chatbot-version-nav">
                  <button
                    type="button"
                    class="ur-chatbot-btn-version-nav"
                    title="Câu trả lời trước"
                    :disabled="isStreaming || getMsgCurrentVersionIndex(msg) <= 0"
                    @click="prevMsgVersion(msg)"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="15 18 9 12 15 6"></polyline>
                    </svg>
                  </button>
                  <span class="ur-chatbot-version-label">
                    {{ getMsgCurrentVersionIndex(msg) + 1 }} / {{ getMsgVersionsCount(msg) }}
                  </span>
                  <button
                    type="button"
                    class="ur-chatbot-btn-version-nav"
                    title="Câu trả lời sau"
                    :disabled="isStreaming || getMsgCurrentVersionIndex(msg) >= getMsgVersionsCount(msg) - 1"
                    @click="nextMsgVersion(msg)"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                  </button>
                </div>

                <!-- Nút Copy response -->
                <button
                  type="button"
                  class="ur-chatbot-btn-action btn-msg-action"
                  :title="copiedMsgId === msg.id ? 'Copied!' : 'Copy response'"
                  @click="copyMessageContent(msg)"
                >
                  <svg v-if="copiedMsgId !== msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                  </svg>
                  <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                </button>

                <!-- Nút Đọc to (Read aloud) -->
                <button
                  type="button"
                  :class="['ur-chatbot-btn-action btn-msg-action', { 'is-speaking': speakingMsgId === msg.id }]"
                  :title="speakingMsgId === msg.id ? 'Stop speaking' : 'Read aloud'"
                  @click="toggleSpeak(msg)"
                >
                  <!-- Icon loa đang phát -->
                  <svg v-if="speakingMsgId === msg.id" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                    <line x1="23" y1="9" x2="17" y2="15"></line>
                    <line x1="17" y1="9" x2="23" y2="15"></line>
                  </svg>
                  <!-- Icon loa tắt -->
                  <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                    <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                  </svg>
                </button>

                <!-- Nút Regenerate response -->
                <button
                  type="button"
                  class="ur-chatbot-btn-action btn-msg-action"
                  title="Regenerate response (Tạo thêm câu trả lời khác)"
                  :disabled="isStreaming"
                  @click="regenerateMessage(msg)"
                >
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="1 4 1 10 7 10"></polyline>
                    <polyline points="23 20 23 14 17 14"></polyline>
                    <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
                  </svg>
                </button>

                <!-- Thời gian phản hồi -->
                <span v-if="msg.responseTime" class="ur-chatbot-duration-badge msg-duration-badge">{{ msg.responseTime }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Thanh câu hỏi ngắn gợi ý (Suggested Prompts) gắn ở cuối cùng nội dung chat -->
        <div
          v-if="suggestedPrompts && suggestedPrompts.length > 0 && !isStreaming"
          class="ur-chatbot-suggested-prompts suggested-prompts-in-body"
        >
          <div v-if="suggestedTitle" class="ur-chatbot-suggested-title">
            <svg class="ur-chatbot-suggested-title-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <span>{{ suggestedTitle }}</span>
          </div>
          <div class="ur-chatbot-suggested-track suggested-prompts-track">
            <button
              v-for="(prompt, idx) in suggestedPrompts"
              :key="idx"
              type="button"
              class="ur-chatbot-prompt-pill prompt-pill"
              :title="prompt"
              @click="sendSuggestedPrompt(prompt)"
            >
              <span class="ur-chatbot-prompt-pill-text prompt-pill-text">{{ prompt }}</span>
              <svg class="ur-chatbot-prompt-pill-arrow prompt-pill-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Footer nhập tin nhắn -->
      <form
        class="ur-chatbot-footer chat-footer"
        :class="{ 'is-dragover': isDraggingOver }"
        @submit.prevent="handleSendMessage"
        @dragover.prevent="onDragOver"
        @dragenter.prevent="onDragEnter"
        @dragleave.prevent="onDragLeave"
        @drop.prevent="onDrop"
      >
        <!-- Nút Cuộn Xuống Dưới Cùng: Nổi phía trên đỉnh khung input chat, không che nội dung input -->
        <button
          v-if="showScrollBottomBtn"
          type="button"
          class="ur-chatbot-btn-scroll-bottom btn-scroll-bottom"
          title="Cuộn xuống dưới cùng"
          @click.stop.prevent="scrollToBottom(true)"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <polyline points="19 12 12 19 5 12"></polyline>
          </svg>
        </button>

        <!-- Input file ẩn để chọn ảnh từ máy tính -->
        <input
          v-if="canAttachFile"
          ref="imageFileInput"
          type="file"
          accept="image/*"
          multiple
          style="display: none"
          @change="handleFileInputChange"
        />

        <!-- Khung Card bo góc tròn hiện đại phong cách Gemini -->
        <div class="ur-chatbot-input-card">
          <!-- Dải thumbnail ảnh đính kèm (các ô vuông nhỏ) ở vị trí TOP của khung input -->
          <div v-if="canAttachFile && pendingImages.length > 0" class="ur-chatbot-attachments-strip">
            <div
              v-for="(img, idx) in pendingImages"
              :key="img.id || idx"
              :class="['ur-chatbot-thumb-box', { 'is-uploading': img.uploading, 'is-error': !!img.error }]"
              :title="img.name || 'Ảnh ' + (idx + 1)"
            >
              <img :src="img.preview || img.url || img.base64" alt="thumbnail" class="ur-chatbot-thumb-img" @click="openImageModal(img.preview || img.url || img.base64, img.name)" />

              <!-- Spinner quay khi đang upload lên MinIO -->
              <div v-if="img.uploading" class="ur-chatbot-thumb-loader" title="Đang tải lên MinIO...">
                <svg class="ur-chatbot-thumb-spin-icon" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="3" stroke-dasharray="14 14"></circle>
                </svg>
              </div>

              <!-- Nút X nhỏ để xoá ảnh (đồng thời xoá luôn ở MinIO nếu storeFile=true) -->
              <button
                type="button"
                class="ur-chatbot-thumb-btn-remove"
                title="Xóa ảnh này"
                :disabled="img.uploading"
                @click.stop.prevent="removePendingImage(idx)"
              >
                <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          </div>

          <!-- Textarea nhập nội dung tin nhắn -->
          <textarea
            ref="chatInput"
            v-model="inputMsg"
            rows="1"
            class="ur-chatbot-input ur-chatbot-textarea"
            :placeholder="isStreaming ? 'Generating response...' : (pendingImages.length > 0 ? 'Hỏi điều gì về ảnh (hoặc bấm gửi)...' : placeholderText)"
            :disabled="isLoading || isStreaming"
            @keydown="handleInputKeyDown"
            @input="autoResizeInput"
            @paste="handlePaste"
          ></textarea>

          <!-- Dòng thanh công cụ dưới: Nút dấu + & Nút Thinking bên trái, Select Model & nút gửi bên phải -->
          <div class="ur-chatbot-card-bottom-bar">
            <div class="ur-chatbot-card-bottom-left">
              <!-- Nút dấu + đính kèm ảnh (chỉ hiển thị khi canAttachFile = true) -->
              <button
                v-if="canAttachFile"
                type="button"
                class="ur-chatbot-btn-plus"
                title="Đính kèm ảnh (hoặc kéo thả / dán Ctrl+V)"
                :disabled="isLoading || isStreaming"
                @click="$refs.imageFileInput.click()"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
              </button>

              <!-- Nút bật / tắt Thinking (Icon bóng đèn phát sáng có dấu hỏi chấm) -->
              <button
                type="button"
                :class="['ur-chatbot-btn-thinking-toggle', { 'is-active': isThinkingActive && isCurrentModelSupportThinking }]"
                :title="thinkingTooltip"
                :disabled="isLoading || isStreaming || !isCurrentModelSupportThinking"
                @click="toggleThinkingActive"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
                  <!-- Thân bóng đèn lớn, rõ nét -->
                  <path d="M8.5 14.8C7.2 13.5 6 11.6 6 9.2a6 6 0 1 1 12 0c0 2.4-1.2 4.3-2.5 5.6l-.7 2.2H9.2l-.7-2.2z" />
                  <!-- Đui đèn -->
                  <path d="M9.5 19.5h5" />
                  <!-- Dấu hỏi chấm to, rõ nét ngay giữa bóng đèn -->
                  <path d="M10.3 7.8c0-1.1.7-1.8 1.7-1.8s1.7.7 1.7 1.8c0 1.2-1.7 1.8-1.7 3.2" stroke-width="2.3" />
                  <circle cx="12" cy="13.3" r="1" fill="currentColor" stroke="none" />
                  <!-- 6 tia sáng xung quanh (3 bên trái, 3 bên phải) -->
                  <line x1="2.8" y1="3.8" x2="4.6" y2="5.6" />
                  <line x1="1.2" y1="9.5" x2="3.4" y2="9.5" />
                  <line x1="2.8" y1="15.2" x2="4.6" y2="13.6" />
                  <line x1="21.2" y1="3.8" x2="19.4" y2="5.6" />
                  <line x1="22.8" y1="9.5" x2="20.6" y2="9.5" />
                  <line x1="21.2" y1="15.2" x2="19.4" y2="13.6" />
                </svg>
              </button>
            </div>

            <div class="ur-chatbot-card-bottom-right">
              <!-- Select list model dropdown (như trong ảnh mẫu) -->
              <div class="ur-chatbot-model-select-wrapper" ref="modelSelectDropdown">
                <button
                  type="button"
                  class="ur-chatbot-model-select-btn"
                  :title="'Mô hình đang chọn: ' + currentModelDisplayName + ' (Bấm để đổi)'"
                  :disabled="isLoading || isStreaming"
                  @click.stop="toggleModelDropdown"
                >
                  <span class="ur-chatbot-model-select-name">{{ currentModelDisplayName }}</span>
                  <svg
                    :class="['ur-chatbot-model-chevron', { 'is-open': showModelDropdown }]"
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </button>

                <!-- Popover upward menu (danh sách trực tiếp không tiêu đề) -->
                <transition name="ur-chatbot-dropdown-popover">
                  <div v-if="showModelDropdown" class="ur-chatbot-model-menu" @click.stop>
                    <div class="ur-chatbot-model-menu-list">
                      <div
                        v-for="mItem in modelListOptions"
                        :key="mItem.id"
                        :class="['ur-chatbot-model-option', { 'is-selected': effectiveModel === mItem.id }]"
                        @click="selectModel(mItem.id)"
                      >
                        <div class="ur-chatbot-model-option-main">
                          <div class="ur-chatbot-model-option-name">
                            <span>{{ mItem.name }}</span>
                            <span v-if="mItem.badge" :class="['ur-chatbot-model-tag', mItem.badgeType || 'badge-default']">
                              {{ mItem.badge }}
                            </span>
                          </div>
                          <div class="ur-chatbot-model-option-desc">{{ mItem.desc }}</div>
                        </div>
                        <svg v-if="effectiveModel === mItem.id" class="ur-chatbot-model-check-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>

              <!-- Nút Dừng khi đang stream -->
              <button
                v-if="isStreaming"
                type="button"
                class="ur-chatbot-btn-stop-gemini"
                title="Stop generating"
                @click="stopStreaming"
              >
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="4" y="4" width="16" height="16" rx="3" ry="3" />
                </svg>
              </button>

              <!-- Nút Gửi hình tròn có mũi tên lên (↑) kiểu Gemini -->
              <button
                v-else
                type="submit"
                class="ur-chatbot-btn-send-gemini"
                title="Send message (Enter to send, Shift + Enter for new line)"
                :disabled="isLoading || isUploadingAnyImage || (!inputMsg.trim() && pendingImages.length === 0)"
              >
                <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="19" x2="12" y2="5"></line>
                  <polyline points="5 12 12 5 19 12"></polyline>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </form>

      <!-- Modal xác nhận Reset cuộc trò chuyện -->
      <transition name="ur-chatbot-fade">
        <div
          v-if="showResetConfirm"
          class="ur-chatbot-confirm-backdrop"
          @click.self="cancelReset"
        >
          <div class="ur-chatbot-confirm-card">
            <h4 class="ur-chatbot-confirm-title">Are you sure you want to clear this conversation?</h4>
            <div class="ur-chatbot-confirm-actions">
              <button
                type="button"
                class="ur-chatbot-btn-confirm-cancel"
                @click="cancelReset"
              >
                Cancel
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-confirm-ok"
                @click="confirmReset"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      </transition>

      <!-- Bot Phi hành gia bay lơ lửng không trọng lực (Interactive Zero-Gravity) khi Fullscreen -->
      <div
        v-if="isOpen && isFullscreen"
        :class="['ur-chatbot-zerog-bot', { 'bot-error-state': hasBotError }]"
        :style="{
          transform: `translate3d(${zeroGBot.x}px, ${zeroGBot.y}px, 0) rotate(${zeroGBot.rot}deg)`,
          cursor: zeroGBot.isDragging ? 'grabbing' : (hasBotError ? 'pointer' : 'grab')
        }"
        :title="hasBotError ? 'Bot error! Click to retry' : 'Drag and toss Astro Bot in zero-gravity!'"
        @mousedown="onZeroGBotMouseDown"
        @touchstart="onZeroGBotTouchStart"
        @click="onZeroGBotClick"
      >
        <!-- SVG Zero-G 1: Bot bình thường / Streaming / Loading / Searching -->
        <svg v-if="!hasBotError" class="ur-chatbot-zerog-svg" viewBox="0 0 100 100">
          <defs>
            <radialGradient id="zgSphereGloss" cx="40%" cy="30%" r="65%">
              <stop offset="0%" stop-color="#ffffff" />
              <stop offset="60%" stop-color="#e2e8f0" />
              <stop offset="100%" stop-color="#94a3b8" />
            </radialGradient>
            <linearGradient id="zgMetalDark" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#475569" />
              <stop offset="100%" stop-color="#0f172a" />
            </linearGradient>
            <filter id="zgNeonGlow" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur stdDeviation="2" result="coloredBlur" />
              <feMerge>
                <feMergeNode in="coloredBlur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
            <filter id="zgDropShadow" x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="5" stdDeviation="4" flood-color="#000000" flood-opacity="0.4" />
            </filter>
            <clipPath id="zgVisorClipStream">
              <rect x="20" y="18" width="60" height="32" rx="16" />
            </clipPath>
            <linearGradient id="zgScannerLaserGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00f0ff" stop-opacity="0" />
              <stop offset="50%" stop-color="#00f0ff" stop-opacity="0.9" />
              <stop offset="100%" stop-color="#00f0ff" stop-opacity="0" />
            </linearGradient>
          </defs>

          <!-- Trôi nổi toàn thân -->
          <g class="anim-astro-float" filter="url(#zgDropShadow)">
            <!-- CHÂN -->
            <g class="astro-leg-l">
              <circle cx="39" cy="76" r="4" fill="url(#zgMetalDark)" />
              <rect x="36" y="76" width="6" height="13" rx="3" fill="url(#zgSphereGloss)" stroke="#64748b" stroke-width="0.6" />
              <path d="M 33.5 88 C 33.5 85, 44.5 85, 44.5 88 L 44.5 91 C 44.5 93, 33.5 93, 33.5 91 Z" fill="url(#zgMetalDark)" stroke="#94a3b8" stroke-width="0.8" />
            </g>
            <g class="astro-leg-r">
              <circle cx="61" cy="76" r="4" fill="url(#zgMetalDark)" />
              <rect x="58" y="76" width="6" height="13" rx="3" fill="url(#zgSphereGloss)" stroke="#64748b" stroke-width="0.6" />
              <path d="M 55.5 88 C 55.5 85, 66.5 85, 66.5 88 L 66.5 91 C 66.5 93, 55.5 93, 55.5 91 Z" fill="url(#zgMetalDark)" stroke="#94a3b8" stroke-width="0.8" />
            </g>

            <!-- TAY TRÔI NỔI -->
            <rect class="astro-arm-l" x="25" y="60" width="6.5" height="15" rx="3.2" fill="url(#zgSphereGloss)" stroke="#94a3b8" stroke-width="0.6" />
            <rect class="astro-arm-r" x="68.5" y="60" width="6.5" height="15" rx="3.2" fill="url(#zgSphereGloss)" stroke="#94a3b8" stroke-width="0.6" />

            <!-- CỔ VÀ THÂN -->
            <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#zgMetalDark)" />
            <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#zgSphereGloss)" stroke="#cbd5e1" stroke-width="0.8" />

            <!-- Lõi năng lượng ngực -->
            <circle cx="50" cy="67" r="5" fill="#020617" />
            <circle :class="['anim-core-pulse', { 'anim-core-fast': isLoading || isStreaming }]" cx="50" cy="67" r="3" fill="none" stroke="#00f0ff" stroke-width="1.8" filter="url(#zgNeonGlow)" />

            <!-- ĐẦU HÌNH TRÒN -->
            <circle cx="50" cy="33" r="32" fill="url(#zgSphereGloss)" stroke="#f8fafc" stroke-width="1.5" />
            <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#zgMetalDark)" stroke="#00f0ff" stroke-width="1.2" filter="url(#zgNeonGlow)" />

            <!-- ĂNG-TEN ĐỈNH TAI -->
            <g transform="rotate(-20 24 16)">
              <rect x="23" y="2" width="2" height="14" rx="1" fill="url(#zgMetalDark)" />
              <circle cx="24" cy="0" r="3.5" fill="#00f0ff" filter="url(#zgNeonGlow)" />
              <circle class="anim-antenna-signal" cx="24" cy="0" r="4.5" stroke="#00f0ff" fill="none" />
            </g>

            <g transform="rotate(20 76 16)">
              <rect x="75" y="2" width="2" height="14" rx="1" fill="url(#zgMetalDark)" />
              <circle cx="76" cy="0" r="3.5" fill="#00f0ff" filter="url(#zgNeonGlow)" />
              <circle class="anim-antenna-signal" cx="76" cy="0" r="4.5" stroke="#00f0ff" fill="none" style="animation-delay: 0.2s;" />
            </g>

            <!-- ỐP TAI NGHE -->
            <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#zgMetalDark)" />
            <ellipse cx="15" cy="33" rx="2" ry="6" fill="#94a3b8" />

            <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#zgMetalDark)" />
            <ellipse cx="85" cy="33" rx="2" ry="6" fill="#94a3b8" />

            <!-- MẶT NẠ KÍNH TỐI MÀU -->
            <rect x="20" y="18" width="60" height="32" rx="16" fill="#020617" stroke="#334155" stroke-width="1.6" />

            <!-- TRẠNG THÁI 1: KHI AI ĐANG SUY NGHĨ / TRẢ LỜI / TÌM KIẾM (MÃ NHỊ PHÂN & LASER SCANNER) -->
            <g v-if="isLoading || isStreaming" clip-path="url(#zgVisorClipStream)">
              <rect x="20" y="18" width="60" height="32" fill="#030f20" />
              <text class="anim-binary-stream-1" x="22" y="26" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">101100101011010010101001</text>
              <text class="anim-binary-stream-2" x="22" y="34" fill="#38bdf8" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">010101101001101011001110</text>
              <text class="anim-binary-stream-3" x="22" y="42" fill="#00f0ff" font-family="'Courier New', monospace" font-size="6.5" font-weight="bold" letter-spacing="1">110100101010110110100011</text>
              <rect class="anim-visor-scanner" x="20" y="18" width="10" height="32" fill="url(#zgScannerLaserGrad)" />
              <g class="anim-cyber-eyes">
                <circle cx="36" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
                <circle cx="36" cy="34" r="2" fill="#00f0ff" filter="url(#zgNeonGlow)" />
                <circle cx="64" cy="34" r="5" fill="none" stroke="#00f0ff" stroke-width="1.2" stroke-dasharray="3,2" />
                <circle cx="64" cy="34" r="2" fill="#00f0ff" filter="url(#zgNeonGlow)" />
              </g>
            </g>

            <!-- TRẠNG THÁI 2: CHẾ ĐỘ NGHỈ (IDLE) - MẮT NEON XANH THÂN THIỆN -->
            <g v-else>
              <g class="anim-eye-blink">
                <circle cx="36" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#zgNeonGlow)" />
                <circle cx="64" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#zgNeonGlow)" />
              </g>
              <path d="M 46 44 Q 50 47.5 54 44" fill="none" stroke="#00f0ff" stroke-width="1.8" stroke-linecap="round" filter="url(#zgNeonGlow)" />
            </g>
          </g>
        </svg>

        <!-- SVG Zero-G 2: Bot trạng thái LỖI (Hết token, server ngắt kết nối, lỗi mạng) -->
        <svg v-else class="ur-chatbot-zerog-svg bot-error-state" viewBox="0 0 100 100">
          <defs>
            <radialGradient id="zgSphereGlossErr" cx="40%" cy="30%" r="65%">
              <stop offset="0%" stop-color="#ffffff" />
              <stop offset="60%" stop-color="#fee2e2" />
              <stop offset="100%" stop-color="#fca5a5" />
            </radialGradient>
            <linearGradient id="zgMetalDarkErr" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#450a0a" />
              <stop offset="100%" stop-color="#1c1917" />
            </linearGradient>
            <filter id="zgNeonGlowErr" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur stdDeviation="2.2" result="coloredBlur" />
              <feMerge>
                <feMergeNode in="coloredBlur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
            <filter id="zgDropShadowErr" x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="5" stdDeviation="4" flood-color="#ef4444" flood-opacity="0.35" />
            </filter>
          </defs>

          <!-- Toàn thân trôi nổi kèm rung báo động lỗi -->
          <g class="anim-astro-error" filter="url(#zgDropShadowErr)">
            <!-- CHÂN -->
            <g class="astro-leg-l">
              <circle cx="39" cy="76" r="4" fill="url(#zgMetalDarkErr)" />
              <rect x="36" y="76" width="6" height="13" rx="3" fill="url(#zgSphereGlossErr)" stroke="#ef4444" stroke-width="0.8" />
              <path d="M 33.5 88 C 33.5 85, 44.5 85, 44.5 88 L 44.5 91 C 44.5 93, 33.5 93, 33.5 91 Z" fill="url(#zgMetalDarkErr)" stroke="#f87171" stroke-width="0.8" />
            </g>
            <g class="astro-leg-r">
              <circle cx="61" cy="76" r="4" fill="url(#zgMetalDarkErr)" />
              <rect x="58" y="76" width="6" height="13" rx="3" fill="url(#zgSphereGlossErr)" stroke="#ef4444" stroke-width="0.8" />
              <path d="M 55.5 88 C 55.5 85, 66.5 85, 66.5 88 L 66.5 91 C 66.5 93, 55.5 93, 55.5 91 Z" fill="url(#zgMetalDarkErr)" stroke="#f87171" stroke-width="0.8" />
            </g>

            <!-- TAY TRÔI NỔI BUÔNG XUỐNG BỐI RỐI -->
            <rect class="astro-arm-l" x="25" y="62" width="6.5" height="15" rx="3.2" fill="url(#zgSphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(8 28 62)" />
            <rect class="astro-arm-r" x="68.5" y="62" width="6.5" height="15" rx="3.2" fill="url(#zgSphereGlossErr)" stroke="#f87171" stroke-width="0.6" transform="rotate(-8 71 62)" />

            <!-- CỔ VÀ THÂN -->
            <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#zgMetalDarkErr)" />
            <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#zgSphereGlossErr)" stroke="#fca5a5" stroke-width="0.8" />

            <!-- Lõi năng lượng báo lỗi màu đỏ chớp tắt -->
            <circle cx="50" cy="67" r="5" fill="#1c1917" />
            <circle class="anim-core-warning" cx="50" cy="67" r="3.2" fill="#ef4444" filter="url(#zgNeonGlowErr)" />

            <!-- ĐẦU HÌNH TRÒN -->
            <circle cx="50" cy="33" r="32" fill="url(#zgSphereGlossErr)" stroke="#fee2e2" stroke-width="1.5" />
            <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#zgMetalDarkErr)" stroke="#ef4444" stroke-width="1.2" filter="url(#zgNeonGlowErr)" />

            <!-- ĂNG-TEN BÁO ĐỘNG ĐỎ PHÁT TÍN HIỆU -->
            <g transform="rotate(-20 24 16)">
              <rect x="23" y="2" width="2" height="14" rx="1" fill="url(#zgMetalDarkErr)" />
              <circle cx="24" cy="0" r="3.5" fill="#ef4444" filter="url(#zgNeonGlowErr)" />
              <circle class="anim-antenna-error" cx="24" cy="0" r="5" stroke="#ef4444" fill="none" />
            </g>

            <g transform="rotate(20 76 16)">
              <rect x="75" y="2" width="2" height="14" rx="1" fill="url(#zgMetalDarkErr)" />
              <circle cx="76" cy="0" r="3.5" fill="#ef4444" filter="url(#zgNeonGlowErr)" />
              <circle class="anim-antenna-error" cx="76" cy="0" r="5" stroke="#ef4444" fill="none" style="animation-delay: 0.25s;" />
            </g>

            <!-- ỐP TAI NGHE -->
            <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#zgMetalDarkErr)" />
            <ellipse cx="15" cy="33" rx="2" ry="6" fill="#f87171" />

            <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#zgMetalDarkErr)" />
            <ellipse cx="85" cy="33" rx="2" ry="6" fill="#f87171" />

            <!-- MẶT NẠ KÍNH TỐI MÀU BÁO LỖI -->
            <rect x="20" y="18" width="60" height="32" rx="16" fill="#180404" stroke="#7f1d1d" stroke-width="1.8" />

            <!-- MẮT LỖI X X NEON ĐỎ -->
            <g class="anim-eye-error" filter="url(#zgNeonGlowErr)">
              <line x1="32" y1="30" x2="40" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
              <line x1="40" y1="30" x2="32" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
              <line x1="60" y1="30" x2="68" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
              <line x1="68" y1="30" x2="60" y2="38" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
            </g>

            <!-- MIỆNG BUỒN / GẬP NÉT ZIGZAG CỦA BOT KHI GẶP LỖI -->
            <path d="M 44 46 Q 50 42 56 46" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" filter="url(#zgNeonGlowErr)" />

            <!-- GIỌT MỒ HÔI LO LẮNG BÊN THÁI DƯƠNG -->
            <path class="anim-sweat-drop" d="M 75 22 Q 77 26 75 28 Q 73 26 75 22 Z" fill="#38bdf8" opacity="0.85" />
          </g>
        </svg>
      </div>
    </div>

    <!-- NÚT TRIGGER WIDGET (TỰ ĐỘNG ẨN KHI MỞ CỬA SỔ CHAT) -->
    <div
      :class="['ur-chatbot-trigger-btn chat-trigger-btn', { 'is-hidden': isOpen }]"
      title="Chat with Astro Bot"
      @click="toggleChat()"
    >
      <svg class="ur-chatbot-trigger-svg trigger-svg" viewBox="0 0 100 100">
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

        <!-- SVG 1: KHUÔN MẶT BOT TRÒN XOE (MẶC ĐỊNH) -->
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

        <!-- SVG 2: TOÀN THÂN BOT PHI HÀNH GIA LƠ LỬNG (HOVER) -->
        <g class="state-bot-cute" filter="url(#btnShadow)">
          <g class="anim-astro-float">
            <!-- Chân -->
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

            <!-- Tay -->
            <rect class="astro-arm-l" x="25" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss2)" stroke="#94a3b8" stroke-width="0.6" />
            <rect class="astro-arm-r" x="68.5" y="60" width="6.5" height="15" rx="3.2" fill="url(#sphereGloss2)" stroke="#94a3b8" stroke-width="0.6" />

            <!-- Cổ, Thân dưới -->
            <ellipse cx="50" cy="58" rx="7" ry="3" fill="url(#metalDark2)" />
            <path d="M 36 60 C 36 57, 64 57, 64 60 L 60 75 C 60 77, 40 77, 40 75 Z" fill="url(#sphereGloss2)" stroke="#cbd5e1" stroke-width="0.8" />
            <circle cx="50" cy="67" r="5" fill="#020617" />
            <circle cx="50" cy="67" r="3" fill="none" stroke="#00f0ff" stroke-width="1.8" filter="url(#neonGlow2)" />

            <!-- Đầu tròn -->
            <circle cx="50" cy="33" r="32" fill="url(#sphereGloss2)" stroke="#f8fafc" stroke-width="1.5" />
            <ellipse cx="50" cy="3" rx="14" ry="4" fill="url(#metalDark2)" stroke="#00f0ff" stroke-width="1.2" filter="url(#neonGlow2)" />

            <!-- Ăng-ten -->
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

            <!-- Tai nghe -->
            <rect x="15" y="24" width="6" height="18" rx="3" fill="url(#metalDark2)" />
            <ellipse cx="15" cy="33" rx="2" ry="6" fill="#94a3b8" />

            <rect x="79" y="24" width="6" height="18" rx="3" fill="url(#metalDark2)" />
            <ellipse cx="85" cy="33" rx="2" ry="6" fill="#94a3b8" />

            <!-- Mặt nạ kính -->
            <rect x="20" y="18" width="60" height="32" rx="16" fill="#020617" stroke="#334155" stroke-width="1.6" />

            <g class="anim-eye-blink">
              <circle cx="36" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow2)" />
              <circle cx="64" cy="34" r="7.5" fill="none" stroke="#00f0ff" stroke-width="3" filter="url(#neonGlow2)" />
            </g>
            <path d="M 46 44 Q 50 47.5 54 44" fill="none" stroke="#00f0ff" stroke-width="1.8" stroke-linecap="round" filter="url(#neonGlow2)" />
          </g>
        </g>
      </svg>
    </div>

    <!-- Modal phóng to ảnh (Lightbox cách ly trong ur-chatbot-wrapper) -->
    <transition name="ur-chatbot-fade">
      <div
        v-if="activeImageModal.visible"
        class="ur-chatbot-image-modal-backdrop"
        @click.self="closeImageModal"
      >
        <div class="ur-chatbot-image-modal-content">
          <button
            type="button"
            class="ur-chatbot-image-modal-close"
            title="Close (Esc)"
            @click="closeImageModal"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          <img :src="activeImageModal.src" :alt="activeImageModal.alt" class="ur-chatbot-image-modal-img" />
          <div class="ur-chatbot-image-modal-footer">
            <div class="ur-chatbot-image-modal-caption">
              {{ activeImageModal.alt || 'Image' }}
            </div>
            <div class="ur-chatbot-image-modal-actions">
              <a :href="activeImageModal.src" target="_blank" rel="noopener noreferrer" class="ur-chatbot-image-modal-btn">
                Mở link gốc ↗
              </a>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Modal phóng to biểu đồ Mermaid (Lightbox tương tác Zoom & Pan, Tải ảnh PNG) -->
    <transition name="ur-chatbot-fade">
      <div
        v-if="activeMermaidModal.visible"
        class="ur-chatbot-mermaid-modal-backdrop"
        @click.self="closeMermaidModal"
      >
        <div class="ur-chatbot-mermaid-modal-content">
          <!-- Header of Modal -->
          <div class="ur-chatbot-mermaid-modal-header">
            <div class="ur-chatbot-mermaid-modal-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#0284c7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                <polyline points="2 17 12 22 22 17"></polyline>
                <polyline points="2 12 12 17 22 12"></polyline>
              </svg>
              <span>Mermaid Diagram</span>
              <span class="ur-chatbot-mermaid-modal-zoom-badge">{{ Math.round(activeMermaidModal.zoom * 100) }}%</span>
            </div>

            <div class="ur-chatbot-mermaid-modal-actions header-actions">
              <button
                type="button"
                class="ur-chatbot-btn-header-action btn-header-action"
                title="Zoom out (-)"
                @click="zoomMermaidModal(-0.2)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                  <line x1="8" y1="11" x2="14" y2="11"></line>
                </svg>
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-header-action btn-header-action"
                title="Zoom in (+)"
                @click="zoomMermaidModal(0.2)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                  <line x1="11" y1="8" x2="11" y2="14"></line>
                  <line x1="8" y1="11" x2="14" y2="11"></line>
                </svg>
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-header-action btn-header-action"
                title="Reset view (100%)"
                @click="resetMermaidModalView"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                  <path d="M3 3v5h5"></path>
                </svg>
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-header-action btn-header-action"
                title="Download PNG"
                @click="downloadCurrentMermaidPng"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-header-action btn-header-action"
                title="Download SVG (Vector)"
                @click="downloadCurrentMermaidSvg"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                  <line x1="12" y1="18" x2="12" y2="12"></line>
                  <polyline points="9 15 12 18 15 15"></polyline>
                </svg>
              </button>
              <button
                type="button"
                class="ur-chatbot-btn-header-action ur-chatbot-btn-close btn-close-chat"
                title="Close (Esc)"
                @click="closeMermaidModal"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          </div>

          <!-- Viewport với Zoom & Pan -->
          <div
            ref="mermaidModalViewport"
            class="ur-chatbot-mermaid-modal-viewport"
            :class="{ 'is-dragging': activeMermaidModal.isDragging }"
            @wheel.prevent="handleMermaidModalWheel"
            @mousedown="handleMermaidModalMouseDown"
            @touchstart="handleMermaidModalTouchStart"
            @touchmove="handleMermaidModalTouchMove"
            @touchend="handleMermaidModalTouchEnd"
          >
            <div
              ref="mermaidModalCanvas"
              class="ur-chatbot-mermaid-modal-canvas"
              :style="mermaidCanvasStyle"
              v-html="activeMermaidModal.svgHtml"
            ></div>
          </div>

          <!-- Footer của Modal -->
          <div class="ur-chatbot-mermaid-modal-footer">
            <span class="ur-chatbot-modal-tip">💡 <strong>Tip:</strong> Scroll to zoom • Click & drag to pan • Press <strong>Esc</strong> to close</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it';
import markdownItTaskLists from 'markdown-it-task-lists';
import hljs from 'highlight.js';
import katex from 'katex';
import DOMPurify from 'dompurify';
import mermaid from '@/vendor/mermaid.js';

import 'highlight.js/styles/github.css';
import 'katex/dist/katex.min.css';

// Khởi tạo Mermaid với theme 'neutral' thanh lịch, hiện đại, tối giản
mermaid.initialize({
  startOnLoad: false,
  theme: 'neutral',
  securityLevel: 'loose',
  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, sans-serif'
});

// Khởi tạo MarkdownIt cho UrChatbot (Độc lập 100% trong component)
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
});

md.use(markdownItTaskLists, {
  enabled: true,
  label: true,
  labelAfter: true
});

// Custom table render: bọc table trong container cuộn ngang (overflow-x) giống block code và áp dụng style GitHub sáng
md.renderer.rules.table_open = function () {
  return '<div class="ur-chatbot-table-container table-container"><table class="ur-chatbot-table markdown-table">';
};
md.renderer.rules.table_close = function () {
  return '</table></div>';
};

// Custom render cho thẻ hình ảnh Markdown (với hiệu ứng, khung viền, caption và mở phóng to)
md.renderer.rules.image = function (tokens, idx, options, env, self) {
  const token = tokens[idx];
  const srcIndex = token.attrIndex('src');
  const src = srcIndex >= 0 ? token.attrs[srcIndex][1] : '';
  const alt = token.content || (token.attrs && token.attrIndex('alt') >= 0 ? token.attrs[token.attrIndex('alt')][1] : '') || '';
  const titleIndex = token.attrIndex('title');
  const title = titleIndex >= 0 ? token.attrs[titleIndex][1] : '';

  const safeSrc = md.utils.escapeHtml(src);
  const safeAlt = md.utils.escapeHtml(alt);
  const safeTitle = md.utils.escapeHtml(title || alt);

  const captionHtml = safeAlt ? `<figcaption class="ur-chatbot-image-caption image-caption">${safeAlt}</figcaption>` : '';

  return `<figure class="ur-chatbot-image-figure image-figure">
    <div class="ur-chatbot-image-wrapper image-wrapper" data-src="${safeSrc}" data-alt="${encodeURIComponent(safeAlt)}">
      <img class="ur-chatbot-image markdown-image" src="${safeSrc}" alt="${safeAlt}" title="${safeTitle}" loading="lazy" onerror="this.classList.add('error'); if(this.parentElement) this.parentElement.classList.add('has-error');" />
      <div class="ur-chatbot-image-zoom-overlay">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          <line x1="11" y1="8" x2="11" y2="14"></line>
          <line x1="8" y1="11" x2="14" y2="11"></line>
        </svg>
      </div>
      <div class="ur-chatbot-image-error-fallback">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <circle cx="8.5" cy="8.5" r="1.5"></circle>
          <polyline points="21 15 16 10 5 21"></polyline>
          <line x1="3" y1="3" x2="21" y2="21"></line>
        </svg>
        <div class="ur-chatbot-image-error-text">
          <span>Failed to load image</span>
          <a href="${safeSrc}" target="_blank" rel="noopener noreferrer">Open external link ↗</a>
        </div>
      </div>
    </div>
    ${captionHtml}
  </figure>`;
};

// Custom fence render cho block code GitHub theme & Line numbers + Live Image Preview cho Markdown ảnh
md.renderer.rules.fence = function (tokens, idx) {
  const token = tokens[idx];
  const lang = (token.info || '').trim();
  const code = token.content;

  let highlighted = '';
  if (lang && hljs.getLanguage(lang)) {
    try {
      highlighted = hljs.highlight(code, { language: lang, ignoreIllegals: true }).value;
    } catch (e) {
      highlighted = md.utils.escapeHtml(code);
    }
  } else {
    try {
      highlighted = hljs.highlightAuto(code).value;
    } catch (e) {
      highlighted = md.utils.escapeHtml(code);
    }
  }

  const langMatch = (token.info || '').trim().match(/^([a-zA-Z0-9_+#.-]+)/);
  const detectedLang = langMatch ? langMatch[1].toLowerCase() : '';
  const displayLang = detectedLang || 'code';
  const normalizedLang = detectedLang || 'plaintext';
  const safeCode = encodeURIComponent(code);

  const cleanCode = code.replace(/\n$/, '');
  const lines = cleanCode.split('\n');
  const lineCount = lines.length;
  const digits = String(lineCount).length;
  // Tự động tính toán chiều rộng theo số lượng chữ số (1 chữ số: 34px, 2 chữ số: 44px, 3 chữ số: 52px...)
  const gutterWidth = Math.max(34, digits * 9 + 22);

  let gutterHtml = '';
  if (lineCount > 0) {
    const gutterItems = Array.from({ length: lineCount }, (_, i) => `<span class="ur-chatbot-gutter-num gutter-num">${i + 1}</span>`).join('');
    gutterHtml = `<div class="ur-chatbot-code-gutter code-gutter" style="min-width: ${gutterWidth}px; width: ${gutterWidth}px;" aria-hidden="true">${gutterItems}</div>`;
  }

  // TÍCH HỢP BIỂU ĐỒ MERMAID (Flowchart, Sequence, Gantt, Class, State, Pie, Mindmap...)
  if (detectedLang === 'mermaid') {
    return `<div class="ur-chatbot-mermaid-card mermaid-card" data-mermaid-code="${safeCode}">
      <div class="ur-chatbot-mermaid-header">
        <div class="ur-chatbot-mermaid-badge">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
            <polyline points="2 17 12 22 22 17"></polyline>
            <polyline points="2 12 12 17 22 12"></polyline>
          </svg>
          <span>mermaid</span>
        </div>
        <div class="ur-chatbot-mermaid-actions">
          <button type="button" class="ur-chatbot-mermaid-btn btn-open-mermaid" title="Xem màn hình lớn (Phóng to & Di chuyển)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 3 21 3 21 9"></polyline>
              <polyline points="9 21 3 21 3 15"></polyline>
              <line x1="21" y1="3" x2="14" y2="10"></line>
              <line x1="3" y1="21" x2="10" y2="14"></line>
            </svg>
          </button>
          <button type="button" class="ur-chatbot-mermaid-btn btn-download-mermaid" title="Tải ảnh PNG">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
          </button>
          <button type="button" class="ur-chatbot-mermaid-btn btn-download-mermaid-svg" title="Tải file vector SVG">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="12" y1="18" x2="12" y2="12"></line>
              <polyline points="9 15 12 18 15 15"></polyline>
            </svg>
          </button>
          <button type="button" class="ur-chatbot-mermaid-btn btn-toggle-mermaid-code" title="Xem mã nguồn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="16 18 22 12 16 6"></polyline>
              <polyline points="8 6 2 12 8 18"></polyline>
            </svg>
          </button>
          <button type="button" class="ur-chatbot-mermaid-btn btn-copy-mermaid" data-code="${safeCode}" title="Sao chép mã">
            <svg class="ur-chatbot-mermaid-icon-copy" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <svg class="ur-chatbot-mermaid-icon-copied" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="display:none;">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </button>
        </div>
      </div>
      <div class="ur-chatbot-mermaid-viewport">
        <div class="ur-chatbot-mermaid-preview">
          <div class="ur-chatbot-mermaid-target" data-mermaid-code="${safeCode}">
            <div class="ur-chatbot-mermaid-loading">
              <span class="ur-chatbot-mermaid-spinner"></span>
              <span>Rendering Mermaid diagram...</span>
            </div>
          </div>
        </div>
        <div class="ur-chatbot-mermaid-code-view" style="display: none;">
          <div class="ur-chatbot-code-body has-gutter">
            ${gutterHtml}
            <pre class="ur-chatbot-code-pre code-pre"><code class="hljs mermaid">${highlighted}</code></pre>
          </div>
        </div>
      </div>
    </div>`;
  }

  // Tự động phát hiện nếu trong code block có chứa cú pháp hình ảnh markdown ![alt](url)
  // để hiển thị ngay ảnh xem trước trực quan cho người dùng xem
  let imagePreviewHtml = '';
  const imageRegex = /!\[(.*?)\]\((https?:\/\/[^\s\)]+)(?:\s+"(.*?)")?\)/g;
  const imageMatches = [];
  let m;
  while ((m = imageRegex.exec(code)) !== null) {
    imageMatches.push({
      alt: m[1] || '',
      url: m[2] || '',
      title: m[3] || m[1] || ''
    });
  }

  if (imageMatches.length > 0) {
    const previewItems = imageMatches.map(img => {
      const safeSrc = md.utils.escapeHtml(img.url);
      const safeAlt = md.utils.escapeHtml(img.alt);
      return `<div class="ur-chatbot-code-img-item" data-src="${safeSrc}" data-alt="${encodeURIComponent(safeAlt)}" title="Click to zoom image">
        <img src="${safeSrc}" alt="${safeAlt}" loading="lazy" class="ur-chatbot-preview-thumb" onerror="this.classList.add('error'); if(this.parentElement) this.parentElement.classList.add('has-error');" />
        <span class="ur-chatbot-code-img-desc">${safeAlt || 'Image preview'}</span>
      </div>`;
    }).join('');

    imagePreviewHtml = `<div class="ur-chatbot-code-image-preview">
      <div class="ur-chatbot-code-preview-title">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <circle cx="8.5" cy="8.5" r="1.5"></circle>
          <polyline points="21 15 16 10 5 21"></polyline>
        </svg>
        <span>Image preview (${imageMatches.length}):</span>
      </div>
      <div class="ur-chatbot-code-preview-grid">${previewItems}</div>
    </div>`;
  }

  return `<div class="ur-chatbot-code-card code-card github-theme">
    <div class="ur-chatbot-code-header code-card-header">
      <div class="ur-chatbot-code-header-left code-header-left">
        <span class="ur-chatbot-code-lang code-lang-tag">${displayLang}</span>
      </div>
      <button type="button" class="ur-chatbot-copy-btn copy-code-btn" data-code="${safeCode}" title="Copy code">
        <svg class="ur-chatbot-icon-copy icon-copy" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
        </svg>
        <svg class="ur-chatbot-icon-copied icon-copied" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="display:none;">
          <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
      </button>
    </div>
    ${imagePreviewHtml}
    <div class="ur-chatbot-code-body code-body-wrapper has-gutter">
      ${gutterHtml}
      <pre class="ur-chatbot-code-pre code-pre"><code class="hljs ${normalizedLang}">${highlighted}</code></pre>
    </div>
  </div>`;
};

const defaultLinkRender = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options);
};

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrPush(['target', '_blank']);
  tokens[idx].attrPush(['rel', 'noopener noreferrer']);
  return defaultLinkRender(tokens, idx, options, env, self);
};

// Helper: suppress console.warn during KaTeX rendering to prevent metric warnings flooding devtools
function katexRenderSilent(formula, opts) {
  const originalWarn = console.warn;
  console.warn = function() {}; // silence all warnings during render
  let result = '';
  try {
    result = katex.renderToString(formula, opts);
  } catch (e) {
    // ignore errors, throwOnError is false
  } finally {
    console.warn = originalWarn; // always restore
  }
  return result;
}

// Heuristic: check if formula looks like real math
// Returns false if content is clearly plain text to avoid spurious KaTeX processing
function looksLikeMath(formula) {
  if (!formula || formula.trim().length === 0) return false;
  var f = formula.trim();
  // Quick checks - must contain at least one of these to be math:
  if (/[+\-*/=^_{}]/.test(f)) return true;       // basic math operators
  if (/\\[a-zA-Z]/.test(f)) return true;          // LaTeX commands like \alpha
  if (/\d+\/\d+/.test(f)) return true;           // fractions like 1/2
  if (/[<>]/.test(f)) return true;                 // inequalities
  // Greek and math symbols via char codes
  var code;
  for (var i = 0; i < f.length; i++) {
    code = f.charCodeAt(i);
    // Greek: U+0391-U+03C9, Math symbols: U+2200-U+22FF
    if ((code >= 0x0391 && code <= 0x03C9) || (code >= 0x2200 && code <= 0x22FF)) return true;
  }
  return false;
}

function renderMathFormulas(content) {
  if (!content) return '';

  // 1. Tạm thời bảo vệ các khối code (fenced code blocks & inline code) để không vô tình render math bên trong code
  const codeBlocks = [];
  let textWithoutCode = content.replace(/(```[\s\S]*?```|`[^`\n]+?`)/g, (match) => {
    const placeholder = `%%UR_MATH_CODE_${codeBlocks.length}%%`;
    codeBlocks.push(match);
    return placeholder;
  });

  // 2. Render Block/Display Math:
  // 2A. Chuẩn LaTeX \[ ... \]
  textWithoutCode = textWithoutCode.replace(/\\\[([\s\S]+?)\\\]/g, (match, formula) => {
    const trimmed = formula.trim();
    if (!trimmed) return match;
    try {
      const html = katexRenderSilent(trimmed, { displayMode: true, throwOnError: false, strict: 'ignore', trust: true });
      return html ? `\n\n<div class="ur-chatbot-math-block math-block">${html}</div>\n\n` : match;
    } catch (e) {
      return match;
    }
  });

  // 2B. Chuẩn $$ ... $$
  textWithoutCode = textWithoutCode.replace(/\$\$([\s\S]+?)\$\$/g, (match, formula) => {
    const trimmed = formula.trim();
    if (!trimmed) return match;
    try {
      const html = katexRenderSilent(trimmed, { displayMode: true, throwOnError: false, strict: 'ignore', trust: true });
      return html ? `\n\n<div class="ur-chatbot-math-block math-block">${html}</div>\n\n` : match;
    } catch (e) {
      return match;
    }
  });

  // 2C. Fallback cho block math dạng [ \n ... \n ] chứa lệnh LaTeX (\frac, \times, \left, \right, \sqrt, \sum, \int, \cdot)
  textWithoutCode = textWithoutCode.replace(/(?:^|\n)\s*\[\s*\n([\s\S]+?)\n\s*\](?=\s*(?:\n|$))/g, (match, formula) => {
    const trimmed = formula.trim();
    if (!trimmed || !/\\(?:frac|times|left|right|sqrt|pm|cdot|sum|int|mathbf|alpha|beta|pi)/.test(trimmed)) return match;
    try {
      const html = katexRenderSilent(trimmed, { displayMode: true, throwOnError: false, strict: 'ignore', trust: true });
      return html ? `\n\n<div class="ur-chatbot-math-block math-block">${html}</div>\n\n` : match;
    } catch (e) {
      return match;
    }
  });

  // 3. Render Inline Math:
  // 3A. Chuẩn LaTeX \( ... \)
  textWithoutCode = textWithoutCode.replace(/\\\(([\s\S]+?)\\\)/g, (match, formula) => {
    const trimmed = formula.trim();
    if (!trimmed) return match;
    try {
      const html = katexRenderSilent(trimmed, { displayMode: false, throwOnError: false, strict: 'ignore', trust: true });
      return html || match;
    } catch (e) {
      return match;
    }
  });

  // 3B. Chuẩn $ ... $ (không phải escaped \$ và không phải giá tiền tệ $100)
  textWithoutCode = textWithoutCode.replace(/(^|[^\\])\$([^\$\n]+?)\$/g, (match, prefix, formula) => {
    const trimmed = formula.trim();
    if (/^\d+(\.\d+)?$/.test(trimmed)) return match;
    if (!looksLikeMath(trimmed)) return match;
    try {
      const rendered = katexRenderSilent(trimmed, { displayMode: false, throwOnError: false, strict: 'ignore', trust: true });
      return rendered ? `${prefix}${rendered}` : match;
    } catch (e) {
      return match;
    }
  });

  // 3C. Fallback cho inline math dạng ( ... \frac ... ) hoặc ( ... \times ... ) chứa rõ ràng lệnh LaTeX
  textWithoutCode = textWithoutCode.replace(/\(\s*([^\n()]*?\\(?:frac|times|sqrt|pm|cdot|sum|int|left|right|alpha|beta|pi)[^\n()]*?)\s*\)/g, (match, formula) => {
    const trimmed = formula.trim();
    if (!trimmed) return match;
    try {
      const rendered = katexRenderSilent(trimmed, { displayMode: false, throwOnError: false, strict: 'ignore', trust: true });
      return rendered || match;
    } catch (e) {
      return match;
    }
  });

  // 4. Khôi phục các khối code ban đầu
  for (let i = 0; i < codeBlocks.length; i++) {
    textWithoutCode = textWithoutCode.replace(`%%UR_MATH_CODE_${i}%%`, () => codeBlocks[i]);
  }

  return textWithoutCode;
}

function fixStreamingMarkdown(text, isStreaming) {
  if (!isStreaming || !text) return text;
  const codeBlockCount = (text.match(/```/g) || []).length;
  if (codeBlockCount % 2 !== 0) {
    return text + '\n```';
  }
  return text;
}

if (typeof window !== 'undefined' && !window.__copyCodeBlock) {
  window.__copyCodeBlock = function (btn) {
    const raw = btn.getAttribute('data-code');
    if (!raw) return;
    const text = decodeURIComponent(raw);
    navigator.clipboard.writeText(text).then(() => {
      const copyIcon = btn.querySelector('.icon-copy');
      const copiedIcon = btn.querySelector('.icon-copied');
      if (copyIcon && copiedIcon) {
        copyIcon.style.display = 'none';
        copiedIcon.style.display = 'inline-block';
        btn.classList.add('copied');
        setTimeout(() => {
          copyIcon.style.display = 'inline-block';
          copiedIcon.style.display = 'none';
          btn.classList.remove('copied');
        }, 2000);
      }
    }).catch(err => {
      console.error('Không thể sao chép:', err);
    });
  };
}

function parseAndSanitizeMarkdown(markdownText, isStreaming = false) {
  if (!markdownText) return '';
  try {
    const normalized = fixStreamingMarkdown(markdownText, isStreaming);
    const withMath = renderMathFormulas(normalized);
    const rawHtml = md.render(withMath);
    return DOMPurify.sanitize(rawHtml, {
      ADD_TAGS: [
        'table', 'thead', 'tbody', 'tfoot', 'tr', 'th', 'td',
        'math', 'annotation', 'semantics', 'mrow', 'mi', 'mo', 'mn', 'msup', 'msub', 'mfrac',
        'mspace', 'mtext', 'mtable', 'mtr', 'mtd', 'msqrt', 'mpadded', 'mphantom',
        'munder', 'mover', 'munderover', 'msubsup', 'menclose', 'mmultiscripts',
        'span', 'div', 'button', 'svg', 'path', 'rect', 'polyline', 'line', 'circle', 'polygon',
        'pre', 'code', 'figure', 'figcaption', 'img', 'a', 'picture', 'source',
        'input', 'label', 'del', 's', 'ins', 'mark', 'kbd', 'sup', 'sub', 'details', 'summary',
        'hr', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 'blockquote',
        'strong', 'b', 'em', 'i'
      ],
      ADD_ATTR: [
        'target', 'rel', 'data-code', 'data-src', 'data-alt', 'data-mermaid-code', 'data-rendered',
        'viewBox', 'fill', 'stroke', 'stroke-width',
        'stroke-linecap', 'stroke-linejoin', 'displayMode', 'aria-hidden', 'title', 'style', 'class',
        'src', 'alt', 'loading', 'onerror', 'cx', 'cy', 'r', 'x1', 'y1', 'x2', 'y2', 'points',
        'type', 'checked', 'disabled', 'id', 'for', 'open', 'align', 'colspan', 'rowspan',
        'encoding', 'xmlns', 'display'
      ]
    });
  } catch (err) {
    console.error('[UrChatbot Markdown] Lỗi render:', err);
    return markdownText;
  }
}

/**
 * Phát hiện ngôn ngữ của văn bản để chọn giọng TTS phù hợp.
 * Trả về BCP-47 tag: 'vi-VN', 'en-US', 'ja-JP', 'ko-KR', 'zh-CN', ...
 */
function detectLanguage(text) {
  if (!text || text.length < 8) return 'vi-VN';

  const sample = text.slice(0, 500);

  // Kiểm tra ký tự đặc trưng từng ngôn ngữ
  const hasKorean   = /[\uAC00-\uD7AF\u1100-\u11FF]/.test(sample);
  const hasJapanese = /[\u3040-\u30FF\u31F0-\u31FF]/.test(sample);
  const hasCJK      = /[\u4E00-\u9FFF\u3400-\u4DBF]/.test(sample);

  if (hasKorean)   return 'ko-KR';
  if (hasJapanese) return 'ja-JP';
  // Trung hoặc Nhật nếu chỉ có CJK (không có kana)
  if (hasCJK)      return 'zh-CN';

  // Đếm ký tự có dấu đặc trưng tiếng Việt
  const viDiacritics = (sample.match(/[àáảãạăắặẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐÀÁẢÃẠĂẮẶẲẴẶÂẤẦẨẪẬÈÉẺẼẸÊẾỀỂỄỆÌÍỈĨỊÒÓỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÙÚỦŨỤƯỨỪỬỮỰỲÝỶỸỴ]/g) || []).length;
  const viRatio = viDiacritics / sample.replace(/\s/g, '').length;

  // Nếu > 5% là dấu tiếng Việt → vi-VN
  if (viRatio > 0.05) return 'vi-VN';

  // Mặc định tiếng Anh
  return 'en-US';
}

export default {
  name: 'UrChatbot',
  props: {
    botName: {
      type: String,
      default: 'Astro Bot AI'
    },
    statusText: {
      type: String,
      default: 'Groq • Qwen 3.8 (27B)'
    },
    placeholderText: {
      type: String,
      default: 'Type a message (Shift + Enter for new line)...'
    },
    model: {
      type: String,
      default: ''
    },
    knowledgeBase: {
      type: String,
      default: ''
    },
    systemPrompt: {
      type: String,
      default: 'Bạn là Astro Bot AI - một trợ lý không gian thông minh am hiểu công nghệ, lập trình và khoa học.'
    },
    historyLimit: {
      type: Number,
      default: 5
    },
    initialOpen: {
      type: Boolean,
      default: false
    },
    apiUrl: {
      type: String,
      default: ''
    },
    virtualScroll: {
      type: Boolean,
      default: true
    },
    maxVisibleMessages: {
      type: Number,
      default: 50 // Giới hạn số lượng tin nhắn DOM hiển thị cùng lúc để chat siêu mượt, chống lag
    },
    localStorage: {
      type: Boolean,
      default: false
    },
    storageKey: {
      type: String,
      default: ''
    },
    maxStoredMessages: {
      type: Number,
      default: 40
    },
    attachFile: {
      type: Boolean,
      default: false
    },
    storeFile: {
      type: Boolean,
      default: false
    },
    thinking: {
      type: Boolean,
      default: false
    },
    models: {
      type: Array,
      default: () => [
        { id: 'openai/gpt-oss-120b', name: 'GPT-OSS 120B', desc: 'Lý luận sâu, toán học & code', badge: 'Reasoning', badgeType: 'badge-reasoning', supportsThinking: true },
        { id: 'qwen/qwen3.8-27b', name: 'Qwen 3.8 27B', desc: 'Đa phương thức, hiểu ảnh & tiếng Việt', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
        { id: 'qwen/qwen3.6-27b', name: 'Qwen 3.6 27B', desc: 'Nhận diện ảnh nhanh, siêu tốc', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
        { id: 'openai/gpt-oss-20b', name: 'GPT-OSS 20B', desc: 'Gọn nhẹ, tốc độ phản hồi cao', badge: 'Fast', badgeType: 'badge-fast', supportsThinking: false }
      ]
    }
  },
  data() {
    return {
      isOpen: this.initialOpen,
      activeImageModal: {
        visible: false,
        src: '',
        alt: ''
      },
      activeMermaidModal: {
        visible: false,
        svgHtml: '',
        zoom: 1,
        panX: 0,
        panY: 0,
        isDragging: false,
        dragStartX: 0,
        dragStartY: 0
      },
      isExpanded: false, // Mở rộng theo chiều ngang
      isFullscreen: false, // Chế độ toàn màn hình
      showResetConfirm: false, // Modal xác nhận làm mới cuộc trò chuyện
      userMsgExpandedMap: {}, // Map lưu trạng thái expand của từng tin nhắn user
      userMsgCollapsibleMap: {}, // Map lưu trạng thái có dài quá 3 dòng của từng tin nhắn user
      thinkingCollapseMap: {}, // Map lưu trạng thái đóng/mở khối suy nghĩ thinking của bot
      showModelDropdown: false, // Trạng thái mở menu dropdown chọn model
      isThinkingActive: !!this.thinking, // Trạng thái bật/tắt reasoning/thinking từ UI
      zeroGBot: {
        x: 220,
        y: 120,
        vx: 1.2,
        vy: 0.8,
        rot: 0,
        vRot: 0.6,
        isDragging: false
      },
      isDraggingOver: false,
      pendingImages: [],
      inputMsg: '',
      isLoading: false,
      isStreaming: false,
      isErrorState: false,
      effectiveModel: this.model || 'openai/gpt-oss-120b',
      effectiveBotName: this.botName,
      abortController: null,
      typingTimer: null,
      showScrollBottomBtn: false,
      copiedMsgId: null,
      speakingMsgId: null,
      requestStartTime: null,
      suggestedPrompts: [],
      suggestedTitle: '',
      visibleCount: this.maxVisibleMessages || 50,
      messageList: [
        {
          id: 1,
          sender: 'bot',
          text: 'Hello! 👋\nI am **Astro Bot AI**, your space assistant. How can I help you today?',
          html: '',
          responseTime: ''
        }
      ],
      apiMessagesHistory: []
    };
  },
  watch: {
    model(val) {
      this.effectiveModel = val || 'openai/gpt-oss-120b';
    },
    thinking(val) {
      this.isThinkingActive = !!val;
    },
    botName(val) {
      this.effectiveBotName = val;
    },
    maxVisibleMessages(val) {
      this.visibleCount = val;
    },
    isFullscreen(val) {
      if (val && this.isOpen) {
        if (typeof document !== 'undefined' && document.body) {
          document.body.style.overflow = 'hidden';
        }
        this.startZeroGBot();
      } else {
        if (typeof document !== 'undefined' && document.body) {
          document.body.style.overflow = '';
        }
        this.stopZeroGBot();
      }
    },
    isOpen(val) {
      if (!val) {
        if (this.isFullscreen) {
          this.isFullscreen = false;
          this.$emit('fullscreen', false);
        }
        if (typeof document !== 'undefined' && document.body) {
          document.body.style.overflow = '';
        }
        this.stopZeroGBot();
      }
    }
  },
  computed: {
    canAttachFile() {
      return !!this.attachFile;
    },
    shouldStoreFile() {
      return !!(this.canAttachFile && this.storeFile);
    },
    isUploadingAnyImage() {
      return (this.pendingImages || []).some(function (img) { return !!img.uploading; });
    },
    visibleMessageList() {
      if (!this.virtualScroll || this.messageList.length <= this.visibleCount) {
        return this.messageList;
      }
      return this.messageList.slice(-this.visibleCount);
    },
    hiddenEarlierCount() {
      if (!this.virtualScroll) return 0;
      return Math.max(0, this.messageList.length - this.visibleCount);
    },
    hasBotError() {
      if (this.isErrorState) return true;
      const lastMsg = this.messageList[this.messageList.length - 1];
      return !!(lastMsg && lastMsg.isError);
    },
    statusLabel() {
      if (this.hasBotError) return 'Connection issue • Bot error';
      if (this.isStreaming) return 'Typing response...';
      if (this.isLoading) return 'Connecting...';
      if (this.currentModelDisplayName) {
        const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
        const badge = found && found.badge ? ` • ${found.badge}` : '';
        return `Groq • ${this.currentModelDisplayName}${badge}`;
      }
      return this.statusText;
    },
    mermaidCanvasStyle() {
      const m = this.activeMermaidModal;
      return {
        transform: `translate(${m.panX}px, ${m.panY}px) scale(${m.zoom})`,
        transformOrigin: 'center center',
        transition: m.isDragging ? 'none' : 'transform 0.12s ease-out'
      };
    },
    modelListOptions() {
      const baseList = Array.isArray(this.models) && this.models.length > 0
        ? [...this.models]
        : [
            { id: 'openai/gpt-oss-120b', name: 'GPT-OSS 120B', desc: 'Lý luận sâu, toán học & code', badge: 'Reasoning', badgeType: 'badge-reasoning', supportsThinking: true },
            { id: 'qwen/qwen3.8-27b', name: 'Qwen 3.8 27B', desc: 'Đa phương thức, hiểu ảnh & tiếng Việt', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
            { id: 'qwen/qwen3.6-27b', name: 'Qwen 3.6 27B', desc: 'Nhận diện ảnh nhanh, siêu tốc', badge: 'Vision', badgeType: 'badge-vision', supportsThinking: false },
            { id: 'openai/gpt-oss-20b', name: 'GPT-OSS 20B', desc: 'Gọn nhẹ, tốc độ phản hồi cao', badge: 'Fast', badgeType: 'badge-fast', supportsThinking: false }
          ];
      if (this.effectiveModel && !baseList.some(m => m.id === this.effectiveModel)) {
        const isReasoning = /120b|deepseek-r1|reasoning/i.test(this.effectiveModel);
        baseList.unshift({
          id: this.effectiveModel,
          name: this.effectiveModel.split('/').pop(),
          desc: 'Mô hình tuỳ chỉnh',
          badge: isReasoning ? 'Reasoning' : 'Custom',
          badgeType: isReasoning ? 'badge-reasoning' : 'badge-default',
          supportsThinking: isReasoning
        });
      }
      return baseList;
    },
    currentModelDisplayName() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      if (found) return found.name;
      if (!this.effectiveModel) return 'Chọn Model';
      const parts = this.effectiveModel.split('/');
      return parts[parts.length - 1];
    },
    isCurrentModelSupportThinking() {
      const found = this.modelListOptions.find(m => m.id === this.effectiveModel);
      if (!found) return false;
      if (typeof found.supportsThinking === 'boolean') {
        return found.supportsThinking;
      }
      return (found.badge === 'Reasoning' || /120b|deepseek-r1|reasoning/i.test(found.id));
    },
    thinkingTooltip() {
      if (!this.isCurrentModelSupportThinking) {
        return 'Mô hình này không hỗ trợ suy luận (Reasoning not supported)';
      }
      return this.isThinkingActive
        ? 'Thinking: ĐANG BẬT (AI sẽ tư duy logic trước khi trả lời)'
        : 'Thinking: ĐANG TẮT (Bấm để bật tư duy suy luận)';
    }
  },
  created() {
    this.initHistory();
  },
  mounted() {
    this._escKeyHandler = (e) => {
      if (e.key === 'Escape') {
        if (this.activeMermaidModal.visible) {
          this.closeMermaidModal();
        } else if (this.activeImageModal.visible) {
          this.closeImageModal();
        } else if (this.showResetConfirm) {
          this.cancelReset();
        } else if (this.isFullscreen) {
          this.toggleFullscreen();
        }
      }
    };
    window.addEventListener('keydown', this._escKeyHandler);
    this.loadFromLocalStorage();
    this.checkUserMsgHeights();
    this.messageList.forEach(m => {
      if (m.sender === 'bot' && !m.html && m.text) {
        this.$set(m, 'html', this.renderHtml(m.text, false));
      }
    });
    this.scheduleMermaidRender();
    if (this.isFullscreen) {
      this.startZeroGBot();
    }
    this._docClickListener = (e) => {
      if (this.showModelDropdown && this.$refs.modelSelectDropdown) {
        if (!this.$refs.modelSelectDropdown.contains(e.target)) {
          this.showModelDropdown = false;
        }
      }
    };
    document.addEventListener('click', this._docClickListener);
  },
  updated() {
    this.checkUserMsgHeights();
    // KHÔNG gọi renderMermaidDiagrams() ở đây — updated() bị trigger
    // hàng chục lần mỗi giây khi stream, gây Mermaid render loop → lag/đơ browser.
    // Việc render được xử lý qua scheduleMermaidRender() với debounce.
  },
  beforeDestroy() {
    if (this.typingTimer) {
      clearInterval(this.typingTimer);
      this.typingTimer = null;
    }
    if (this._htmlRafPending) {
      this._htmlRafPending = false;
    }
    if (this._mermaidRenderTimer) {
      clearTimeout(this._mermaidRenderTimer);
      this._mermaidRenderTimer = null;
    }
    if (this._escKeyHandler) {
      window.removeEventListener('keydown', this._escKeyHandler);
    }
    if (this._docClickListener) {
      document.removeEventListener('click', this._docClickListener);
    }
    this.stopZeroGBot();
  },
  methods: {
    toggleModelDropdown() {
      this.showModelDropdown = !this.showModelDropdown;
    },
    selectModel(modelId) {
      this.effectiveModel = modelId;
      this.showModelDropdown = false;
      this.$emit('model-change', modelId);
      this.saveToLocalStorage();
    },
    toggleThinkingActive() {
      if (!this.isCurrentModelSupportThinking) return;
      this.isThinkingActive = !this.isThinkingActive;
      this.$emit('update:thinking', this.isThinkingActive);
      this.saveToLocalStorage();
    },
    retryLastAction() {
      if (this.isStreaming || this.isLoading) return;
      for (let i = this.messageList.length - 1; i >= 0; i--) {
        if (this.messageList[i].sender === 'user') {
          const userPrompt = this.messageList[i].text;
          if (i + 1 < this.messageList.length && this.messageList[i + 1].sender === 'bot') {
            this.messageList.splice(i + 1, 1);
          }
          this.inputMsg = userPrompt;
          this.isErrorState = false;
          this.handleSendMessage();
          return;
        }
      }
      this.isErrorState = false;
    },
    initHistory() {
      this.apiMessagesHistory = [
        { role: 'system', content: this.systemPrompt }
      ];
    },
    renderHtml(text, isStreaming) {
      return parseAndSanitizeMarkdown(text, !!isStreaming);
    },
    throttleUpdateHtml(botMsgObj) {
      if (this._htmlRafPending) return;
      this._htmlRafPending = true;
      requestAnimationFrame(() => {
        this._htmlRafPending = false;
        if (!botMsgObj) return;
        this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text || '', true));
        this.scrollToBottom();
      });
    },
    finishStream(botMsgObj, isRegeneration) {
      if (this._htmlRafPending) {
        this._htmlRafPending = false;
      }
      this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text || '', false));
      this.$set(botMsgObj, 'isStreaming', false);
      this.isStreaming = false;
      this.isLoading = false;

      if (this.requestStartTime) {
        const elapsed = ((Date.now() - this.requestStartTime) / 1000).toFixed(2) + 's';
        this.$set(botMsgObj, 'responseTime', elapsed);
      }

      if (botMsgObj.thinking && !botMsgObj.thinkingDuration) {
        const durSec = Math.max(1, Math.round((Date.now() - (this.thinkingStartTime || this.requestStartTime)) / 1000));
        this.$set(botMsgObj, 'thinkingDuration', `${durSec}s`);
      }

      // Trích xuất gợi ý nếu model xuất khối JSON câu hỏi gợi ý trong nội dung text
      this.extractSuggestionsFromText(botMsgObj);

      if (botMsgObj.text) {
        this.$set(botMsgObj, 'lang', detectLanguage(botMsgObj.text));

        // Quản lý các phiên bản câu trả lời (Versions)
        if (!botMsgObj.versions || !Array.isArray(botMsgObj.versions)) {
          this.$set(botMsgObj, 'versions', []);
        }

        const newVersionData = {
          text: botMsgObj.text,
          html: botMsgObj.html,
          responseTime: botMsgObj.responseTime || '',
          lang: botMsgObj.lang || '',
          thinking: botMsgObj.thinking || '',
          thinkingDuration: botMsgObj.thinkingDuration || ''
        };

        if (isRegeneration) {
          botMsgObj.versions.push(newVersionData);
          this.$set(botMsgObj, 'currentVersionIdx', botMsgObj.versions.length - 1);
          this.syncActiveVersionToHistory(botMsgObj);
        } else {
          botMsgObj.versions = [newVersionData];
          this.$set(botMsgObj, 'currentVersionIdx', 0);
          this.apiMessagesHistory.push({
            role: 'assistant',
            content: botMsgObj.text
          });
        }

        this.$emit('message', { role: 'assistant', content: botMsgObj.text });
        this.saveToLocalStorage();
        this.$nextTick(() => {
          this.scheduleMermaidRender();
        });
      }
      this.scrollToBottom();
    },
    scheduleMermaidRender() {
      if (this._mermaidRenderTimer) {
        clearTimeout(this._mermaidRenderTimer);
      }
      const delay = this.isStreaming ? 500 : 50;
      this._mermaidRenderTimer = setTimeout(() => {
        this._mermaidRenderTimer = null;
        this.renderMermaidDiagrams();
      }, delay);
    },
    sanitizeMermaidNode(token) {
      token = token.trim();
      if (!token) return token;

      // Giữ lại class suffix nếu có: ví dụ A[text]:::myClass
      let classSuffix = '';
      const classMatch = token.match(/:::([a-zA-Z0-9_-]+)$/);
      if (classMatch) {
        classSuffix = ':::' + classMatch[1];
        token = token.slice(0, -classSuffix.length).trim();
      }

      // Danh sách các cặp delimiter của Mermaid (ưu tiên cặp 2 ký tự trước)
      const delimPairs = [
        { open: '([', close: '])' }, // stadium
        { open: '[[', close: ']]' }, // subroutine
        { open: '[(', close: ')]' }, // cylinder
        { open: '((', close: '))' }, // circle
        { open: '{{', close: '}}' }, // hexagon
        { open: '[/', close: '/]' }, // parallelogram
        { open: '[\\', close: '\\]' }, // parallelogram alt
        { open: '>', close: ']' },   // asymmetric / flag
        { open: '[', close: ']' },   // rectangle
        { open: '(', close: ')' },   // rounded
        { open: '{', close: '}' }    // rhombus / decision
      ];

      for (let i = 0; i < delimPairs.length; i++) {
        const p = delimPairs[i];
        const closeIdx = token.lastIndexOf(p.close);
        if (closeIdx === -1 || closeIdx !== token.length - p.close.length) {
          continue;
        }

        const openIdx = token.indexOf(p.open);
        if (openIdx <= 0) {
          continue;
        }

        const rawNodeId = token.slice(0, openIdx).trim();
        if (!/^[a-zA-Z0-9_-]+$/.test(rawNodeId)) {
          continue;
        }

        let safeNodeId = rawNodeId;
        if (/^(end|subgraph|class|click|style|graph|flowchart|direction|default)$/i.test(safeNodeId)) {
          safeNodeId = 'node_' + safeNodeId;
        }

        let inner = token.slice(openIdx + p.open.length, closeIdx).trim();
        if ((inner.startsWith('"') && inner.endsWith('"')) || (inner.startsWith("'") && inner.endsWith("'"))) {
          if (inner.length >= 2) {
            inner = inner.slice(1, -1);
          }
        }

        // Thoát an toàn mọi ký tự nhạy cảm có thể làm vỡ lexer/parser của Mermaid bằng HTML entity codes
        const safeInner = inner
          .replace(/"/g, "'")
          .replace(/\[/g, '#91;')
          .replace(/\]/g, '#93;')
          .replace(/\(/g, '#40;')
          .replace(/\)/g, '#41;')
          .replace(/\{/g, '#123;')
          .replace(/\}/g, '#125;')
          .replace(/</g, '#60;')
          .replace(/>/g, '#62;');

        return `${safeNodeId}${p.open}"${safeInner}"${p.close}${classSuffix}`;
      }

      // Nếu chỉ là node ID đơn (ví dụ `end` hay `style` đứng riêng làm endpoint: E -->|no| end)
      if (/^(end|subgraph|class|click|style|graph|flowchart|direction|default)$/i.test(token)) {
        return 'node_' + token + classSuffix;
      }

      return token + classSuffix;
    },
    repairFlowchartCode(code) {
      const lines = code.split('\n');
      const arrowRegex = /(\s*(?:-->|---|==>|===|-\.->|-\.-|<-->|<==>|o--o|x--x)(?:\|[^|\n]*\|)?\s*)/g;

      const repairedLines = lines.map(line => {
        const trimmed = line.trim();
        if (!trimmed) return line;

        // Bỏ qua dòng chỉ là comment
        if (/^%%/.test(trimmed)) return line;

        // Bỏ qua dòng khai báo tiêu đề hoặc định nghĩa style / class
        if (/^\s*(?:flowchart|graph|direction|style|classDef|linkStyle|click|accTitle|accDescr)\b/i.test(trimmed)) {
          return line;
        }

        // Subgraph handling: ví dụ subgraph "Title with spaces" hoặc subgraph Sub1
        if (/^\s*subgraph\b/i.test(trimmed)) {
          const subMatch = trimmed.match(/^subgraph\s+(.+)$/i);
          if (subMatch) {
            let title = subMatch[1].trim();
            if (!/^[a-zA-Z0-9_-]+(\s*\[.*\])?$/.test(title)) {
              if (title.startsWith('"') && title.endsWith('"')) {
                title = title.slice(1, -1);
              }
              const safeTitle = title.replace(/"/g, "'");
              const id = 'sub_' + Math.random().toString(36).slice(2, 7);
              return line.replace(trimmed, `subgraph ${id} ["${safeTitle}"]`);
            }
          }
          return line;
        }

        if (/^\s*end\b/i.test(trimmed)) {
          return line;
        }

        // Tách indentation ban đầu để bảo toàn cấu trúc thụt đầu dòng
        const indentMatch = line.match(/^(\s*)/);
        const indent = indentMatch ? indentMatch[1] : '';

        // Tách các liên kết (edges) trên dòng
        const parts = trimmed.split(arrowRegex);
        if (parts.length <= 1) {
          // Dòng chỉ có 1 node đơn lẻ
          return indent + this.sanitizeMermaidNode(trimmed);
        }

        let repairedLine = '';
        for (let i = 0; i < parts.length; i++) {
          const part = parts[i];
          if (arrowRegex.test(part)) {
            // Đây là mũi tên liên kết (edge)
            // Chuẩn hoá nhãn edge nếu có ký tự đặc biệt: -->|true| hoặc -->|arr[j] < 5|
            const safeEdge = part.replace(/(\|)([^|\n]+)(\|)/g, (m, p1, edgeText, p3) => {
              if (/[<>[\](){}?=,&'"]/.test(edgeText)) {
                const s = edgeText
                  .replace(/"/g, "'")
                  .replace(/\[/g, '#91;')
                  .replace(/\]/g, '#93;')
                  .replace(/\(/g, '#40;')
                  .replace(/\)/g, '#41;')
                  .replace(/\{/g, '#123;')
                  .replace(/\}/g, '#125;')
                  .replace(/</g, '#60;')
                  .replace(/>/g, '#62;');
                return `|"${s}"|`;
              }
              return m;
            });
            repairedLine += safeEdge;
          } else {
            // Đây là cụm node (có thể là A hoặc A & B hoặc A[Text])
            if (part.includes('&')) {
              const subNodes = part.split(/\s*&\s*/).map(n => this.sanitizeMermaidNode(n));
              repairedLine += subNodes.join(' & ');
            } else {
              repairedLine += this.sanitizeMermaidNode(part);
            }
          }
        }

        return indent + repairedLine;
      });

      return repairedLines.join('\n');
    },
    repairClassDiagramCode(code) {
      // Sửa generic types List<String> thành List~String~
      return code.replace(/<([a-zA-Z0-9_,\s]+)>/g, '~$1~');
    },
    repairPieCode(code) {
      const lines = code.split('\n');
      return lines.map(line => {
        const trimmed = line.trim();
        if (/^\s*(?:pie|title|accTitle|accDescr)\b/i.test(trimmed)) return line;
        const m = trimmed.match(/^([^":\n]+)\s*:\s*([+-]?\d+(?:\.\d+)?)\s*$/);
        if (m) {
          const key = m[1].trim();
          const val = m[2].trim();
          return `    "${key}" : ${val}`;
        }
        return line;
      }).join('\n');
    },
    repairErDiagramCode(code) {
      // Sửa kiểu dữ liệu có dấu ngoặc như VARCHAR(255) -> VARCHAR_255
      return code.replace(/([a-zA-Z0-9_]+)\((\d+)\)/g, '$1_$2');
    },
    repairSequenceDiagramCode(code) {
      const lines = code.split('\n');
      let openBlocks = 0;
      for (let i = 0; i < lines.length; i++) {
        const t = lines[i].trim();
        if (/^(loop|alt|opt|par|critical|rect)\b/i.test(t)) {
          openBlocks++;
        } else if (/^end\b/i.test(t)) {
          if (openBlocks > 0) openBlocks--;
        }
      }
      let res = code;
      while (openBlocks > 0) {
        res += '\nend';
        openBlocks--;
      }
      return res;
    },
    sanitizeMermaidCode(raw) {
      if (!raw) return '';
      let code = raw.trim();

      // Bỏ markdown code fences nếu lọt vào nội dung
      code = code.replace(/^```(?:mermaid)?\s*\n?/i, '').replace(/\n?```\s*$/i, '').trim();

      // 1. Xử lý chuyên biệt cho biểu đồ xychart / xychart-beta
      if (/^\s*xychart(-beta)?\b/i.test(code)) {
        // Chuẩn hoá các nhãn trong x-axis nếu bị LLM dùng nháy đơn ['A', 'B'] -> ["A", "B"]
        code = code.replace(/(x-axis\s*\[)([\s\S]*?)(\])/gi, (m, p1, inner, p3) => {
          return p1 + inner.replace(/'/g, '"') + p3;
        });

        // Tìm giá trị max trong các mảng dữ liệu bar/line
        const dataBlocks = code.match(/(?:bar|line)\s*\[([\s\S]*?)\]/gi);
        let maxDataVal = 0;
        if (dataBlocks) {
          dataBlocks.forEach(block => {
            const numbers = block.match(/[+-]?(?:\d+(?:\.\d+)?|\.\d+)/g);
            if (numbers) {
              numbers.forEach(n => {
                const num = parseFloat(n);
                if (!isNaN(num) && num > maxDataVal) maxDataVal = num;
              });
            }
          });
        }

        // Kiểm tra xem y-axis có khai báo dải min --> max bị nhỏ hơn dữ liệu thực tế không
        const yAxisRangeMatch = code.match(/(y-axis\s+(?:"[^"]*"|'[^']*')?\s*)([+-]?\d+(?:\.\d+)?)\s*-->\s*([+-]?\d+(?:\.\d+)?)/i);
        if (yAxisRangeMatch) {
          const declaredMax = parseFloat(yAxisRangeMatch[3]);
          if (!isNaN(declaredMax) && maxDataVal > declaredMax) {
            code = code.replace(yAxisRangeMatch[0], yAxisRangeMatch[1].trim());
          }
        }

        return code;
      }

      // 2. Xử lý Flowchart / Graph (Bao gồm Bubble Sort, nested brackets, math operators, reserved IDs)
      if (/^\s*(?:flowchart|graph)\b/i.test(code)) {
        return this.repairFlowchartCode(code);
      }

      // 3. Xử lý Class Diagram
      if (/^\s*classDiagram\b/i.test(code)) {
        return this.repairClassDiagramCode(code);
      }

      // 4. Xử lý Pie Chart
      if (/^\s*pie\b/i.test(code)) {
        return this.repairPieCode(code);
      }

      // 5. Xử lý ER Diagram
      if (/^\s*erDiagram\b/i.test(code)) {
        return this.repairErDiagramCode(code);
      }

      // 6. Xử lý Sequence Diagram
      if (/^\s*sequenceDiagram\b/i.test(code)) {
        return this.repairSequenceDiagramCode(code);
      }

      return code;
    },
    aggressiveFallbackRepairMermaid(code, errMsg) {
      if (!code) return '';
      let c = code;

      // 1. Xoá ký tự điều khiển ẩn hoặc zero-width space
      c = c.replace(/[\u200B-\u200D\uFEFF]/g, '');

      // 2. Xoá dấu chấm phẩy ở cuối dòng
      c = c.replace(/;\s*$/gm, '');

      // 3. Nếu là flowchart mà vẫn bị lỗi nhãn dạng { ... } hoặc ( ... )
      if (/^\s*(?:flowchart|graph)\b/i.test(c)) {
        c = c.replace(/([a-zA-Z0-9_]+)\{([^{}\n]+)\}/g, (match, id, label) => {
          const cleanLabel = label.replace(/"/g, "'").replace(/[<>{}[\]]/g, ' ');
          return `${id}["${cleanLabel}"]`;
        });
        const lines = c.split('\n');
        c = lines.map(line => {
          const quotes = (line.match(/"/g) || []).length;
          if (quotes % 2 !== 0) {
            return line.replace(/"/g, "'");
          }
          return line;
        }).join('\n');
      }

      // 4. Nếu là sequenceDiagram mà còn thiếu end
      if (/^\s*sequenceDiagram\b/i.test(c)) {
        c = this.repairSequenceDiagramCode(c);
      }

      return c;
    },
    async renderMermaidDiagrams() {
      if (typeof window === 'undefined') return;
      if (this.isStreaming) return; // Không render khi đang stream - code chưa hoàn chỉnh
      if (this._mermaidRendering) return; // Tránh chạy song song gây xung đột SVG
      this._mermaidRendering = true;

      try {
        await this.$nextTick();
        const targets = this.$el ? this.$el.querySelectorAll('.ur-chatbot-mermaid-target:not([data-rendered="true"])') : [];
        if (!targets || targets.length === 0) return;

        for (const target of targets) {
          const rawCode = target.getAttribute('data-mermaid-code');
          if (!rawCode) continue;
          let code = decodeURIComponent(rawCode).trim();
          if (!code) continue;
          code = this.sanitizeMermaidCode(code);

          // Tạo id duy nhất chỉ chứa ký tự chữ và số
          const id = 'mmd_' + Math.random().toString(36).replace(/[^a-z0-9]/g, '').slice(0, 8);

          // Xoá sẵn temp element có thể còn sót
          const existingTemp = document.getElementById(id) || document.getElementById('d' + id);
          if (existingTemp) existingTemp.remove();

          let renderSuccess = false;
          let svgCode = '';
          let bindFunctions = null;

          // PASS 1: Render với mã đã chuẩn hoá
          try {
            const renderFn = (typeof mermaid.renderAsync === 'function')
              ? mermaid.renderAsync.bind(mermaid)
              : mermaid.render.bind(mermaid);
            const res = await renderFn(id, code);
            svgCode = typeof res === 'string' ? res : (res && res.svg ? res.svg : '');
            bindFunctions = res && res.bindFunctions;
            renderSuccess = true;
          } catch (firstErr) {
            // Xoá ngay element rác mà Mermaid có thể đã tạo ra khi throw error
            const errEl = document.getElementById(id) || document.getElementById('d' + id);
            if (errEl) errEl.remove();

            // PASS 2: Thử nghiệm Fallback Repair nâng cao
            try {
              const fallbackCode = this.aggressiveFallbackRepairMermaid(code, firstErr.message);
              if (fallbackCode) {
                const fbId = id + '_fb';
                const fbRenderFn = (typeof mermaid.renderAsync === 'function')
                  ? mermaid.renderAsync.bind(mermaid)
                  : mermaid.render.bind(mermaid);
                const fbRes = await fbRenderFn(fbId, fallbackCode);
                svgCode = typeof fbRes === 'string' ? fbRes : (fbRes && fbRes.svg ? fbRes.svg : '');
                bindFunctions = fbRes && fbRes.bindFunctions;
                renderSuccess = true;
              }
            } catch (secondErr) {
              const fbErrEl = document.getElementById(id + '_fb') || document.getElementById('d' + id + '_fb');
              if (fbErrEl) fbErrEl.remove();
            }
          }

          if (renderSuccess && target.isConnected) {
            target.innerHTML = svgCode;
            target.setAttribute('data-rendered', 'true');
            target.classList.remove('is-loading', 'has-error');
            const previewEl = target.closest('.ur-chatbot-mermaid-preview');
            if (previewEl) {
              previewEl.classList.add('is-clickable');
              previewEl.setAttribute('title', 'Nhấp để mở xem toàn màn hình (Phóng to & Di chuyển)');
            }
            if (typeof bindFunctions === 'function') {
              bindFunctions(target);
            }

            // Tự động căn chỉnh viewBox cho sơ đồ mindmap và sơ đồ thiếu viewBox
            const svgEl = target.querySelector('svg');
            if (svgEl) {
              try {
                const styleEl = svgEl.querySelector('style');
                if (styleEl && styleEl.textContent) {
                  styleEl.textContent = styleEl.textContent.replace(
                    /([#a-zA-Z0-9_.-]*\.mindmap-node[^{]*?)\s+text\b/g,
                    '$1 text, $1 foreignObject div, $1 foreignObject span, $1 .nodeLabel'
                  );
                }

                const mindmapNodes = svgEl.querySelectorAll('.mindmap-node');
                if (mindmapNodes && mindmapNodes.length > 0) {
                  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
                  mindmapNodes.forEach(node => {
                    let tx = 0, ty = 0;
                    const tf = node.getAttribute('transform') || '';
                    const m = tf.match(/translate\(\s*([-\d.]+)(?:\s*,\s*([-\d.]+))?\s*\)/);
                    if (m) {
                      tx = parseFloat(m[1]) || 0;
                      ty = parseFloat(m[2]) || 0;
                    }
                    let bx = -40, by = -20, bw = 80, bh = 40;
                    try {
                      const b = node.getBBox();
                      if (b && b.width > 0) {
                        bx = b.x; by = b.y; bw = b.width; bh = b.height;
                      }
                    } catch (e) {}
                    minX = Math.min(minX, tx + bx);
                    minY = Math.min(minY, ty + by);
                    maxX = Math.max(maxX, tx + bx + bw);
                    maxY = Math.max(maxY, ty + by + bh);
                  });

                  if (minX !== Infinity && maxX !== -Infinity) {
                    const pad = 36;
                    const vbX = Math.round(minX - pad);
                    const vbY = Math.round(minY - pad);
                    const vbW = Math.round((maxX - minX) + pad * 2);
                    const vbH = Math.round((maxY - minY) + pad * 2);
                    svgEl.setAttribute('viewBox', `${vbX} ${vbY} ${vbW} ${vbH}`);
                    svgEl.setAttribute('width', '100%');
                    svgEl.style.maxWidth = `${Math.min(760, Math.max(480, vbW))}px`;
                    svgEl.style.height = 'auto';
                    svgEl.style.overflow = 'visible';
                  }
                } else {
                  const bbox = svgEl.getBBox();
                  if (bbox && bbox.width > 0 && bbox.height > 0) {
                    const currentVb = svgEl.getAttribute('viewBox');
                    if (!currentVb) {
                      const pad = 16;
                      svgEl.setAttribute(
                        'viewBox',
                        `${bbox.x - pad} ${bbox.y - pad} ${bbox.width + pad * 2} ${bbox.height + pad * 2}`
                      );
                      svgEl.style.width = '100%';
                      svgEl.style.height = 'auto';
                    }
                  }
                }
              } catch (e) {
                // getBBox fallback
              }
            }
          } else if (!renderSuccess && !this.isStreaming && target.isConnected) {
            target.setAttribute('data-rendered', 'true');
            target.classList.add('has-error');
            target.innerHTML = `<div class="ur-chatbot-mermaid-error">
              <div class="ur-chatbot-mermaid-error-title">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>Diagram syntax is incomplete or has errors</span>
              </div>
              <div class="ur-chatbot-mermaid-error-desc">Source code view has been opened automatically below.</div>
            </div>`;

            const card = target.closest('.ur-chatbot-mermaid-card');
            if (card) {
              const codeView = card.querySelector('.ur-chatbot-mermaid-code-view');
              const toggleBtn = card.querySelector('.btn-toggle-mermaid-code');
              if (codeView) codeView.style.display = 'block';
              if (toggleBtn) {
                toggleBtn.classList.add('is-active');
              }
            }
          }
        }
      } finally {
        if (typeof document !== 'undefined') {
          try {
            const tempEls = document.querySelectorAll('body > [id^="dmmd_"], body > [id^="immd_"], body > [id^="mmd_"]');
            if (tempEls && tempEls.length > 0) {
              tempEls.forEach(el => el.remove());
            }
          } catch (e) {}
        }
        this._mermaidRendering = false;
      }
    },
    handleBodyClick(e) {
      // 1. Xử lý copy code block
      const copyBtn = e.target.closest('.ur-chatbot-copy-btn');
      if (copyBtn) {
        if (window.__copyCodeBlock) {
          window.__copyCodeBlock(copyBtn);
        }
        return;
      }

      // 2. Xử lý mở phóng to ảnh (Lightbox) từ hình ảnh markdown hoặc preview từ code block
      const imgWrapper = e.target.closest('.ur-chatbot-image-wrapper, .ur-chatbot-code-img-item');
      if (imgWrapper) {
        const rawSrc = imgWrapper.getAttribute('data-src');
        const rawAlt = imgWrapper.getAttribute('data-alt');
        const imgEl = imgWrapper.querySelector('img');
        if (imgEl && imgEl.classList.contains('error')) return;

        const src = rawSrc || (imgEl ? imgEl.src : '');
        const alt = rawAlt ? decodeURIComponent(rawAlt) : (imgEl ? imgEl.alt : '');
        if (src) {
          this.openImageModal(src, alt);
        }
        return;
      }

      // 3. Xử lý nút bật/tắt xem mã nguồn Mermaid
      const toggleMermaidBtn = e.target.closest('.btn-toggle-mermaid-code');
      if (toggleMermaidBtn) {
        const card = toggleMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const codeView = card.querySelector('.ur-chatbot-mermaid-code-view');
          if (codeView) {
            const isHidden = codeView.style.display === 'none';
            codeView.style.display = isHidden ? 'block' : 'none';
            toggleMermaidBtn.classList.toggle('is-active', isHidden);
            toggleMermaidBtn.title = isHidden ? 'Hide source code' : 'View source code';
          }
        }
        return;
      }

      // 4. Xử lý nút sao chép mã Mermaid
      const copyMermaidBtn = e.target.closest('.btn-copy-mermaid');
      if (copyMermaidBtn) {
        const rawCode = copyMermaidBtn.getAttribute('data-code');
        if (rawCode) {
          const text = decodeURIComponent(rawCode);
          navigator.clipboard.writeText(text).then(() => {
            var iconCopy = copyMermaidBtn.querySelector('.ur-chatbot-mermaid-icon-copy');
            var iconCopied = copyMermaidBtn.querySelector('.ur-chatbot-mermaid-icon-copied');
            if (iconCopy && iconCopied) {
              iconCopy.style.display = 'none';
              iconCopied.style.display = 'inline-block';
            }
            copyMermaidBtn.title = 'Copied!';
            copyMermaidBtn.classList.add('copied');
            setTimeout(() => {
              if (iconCopy && iconCopied) {
                iconCopy.style.display = '';
                iconCopied.style.display = 'none';
              }
              copyMermaidBtn.title = 'Copy code';
              copyMermaidBtn.classList.remove('copied');
            }, 2000);
          });
        }
        return;
      }

      // 5. Xử lý mở modal xem toàn màn hình biểu đồ Mermaid (Zoom & Pan)
      const openMermaidBtn = e.target.closest('.btn-open-mermaid, .ur-chatbot-mermaid-preview');
      if (openMermaidBtn) {
        // Bỏ qua nếu người dùng click vào các nút hành động khác bên trong card
        if (e.target.closest('.btn-toggle-mermaid-code, .btn-copy-mermaid, .btn-download-mermaid, .btn-download-mermaid-svg, .ur-chatbot-mermaid-code-view')) {
          return;
        }
        const card = openMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const target = card.querySelector('.ur-chatbot-mermaid-target');
          if (target && !target.classList.contains('has-error') && !target.classList.contains('is-loading')) {
            const svgEl = target.querySelector('svg');
            if (svgEl) {
              this.openMermaidModal(svgEl.outerHTML);
            }
          }
        }
        return;
      }

      // 6. Xử lý nút tải ảnh PNG biểu đồ Mermaid trực tiếp từ card
      const downloadMermaidBtn = e.target.closest('.btn-download-mermaid');
      if (downloadMermaidBtn) {
        const card = downloadMermaidBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) {
            this.downloadSvgElementAsPng(svgEl);
          }
        }
        return;
      }

      // 6b. Xử lý nút tải file vector SVG biểu đồ Mermaid trực tiếp từ card
      const downloadMermaidSvgBtn = e.target.closest('.btn-download-mermaid-svg');
      if (downloadMermaidSvgBtn) {
        const card = downloadMermaidSvgBtn.closest('.ur-chatbot-mermaid-card');
        if (card) {
          const svgEl = card.querySelector('.ur-chatbot-mermaid-target svg');
          if (svgEl) {
            this.downloadSvgElementAsSvg(svgEl);
          }
        }
        return;
      }
    },
    openImageModal(src, alt) {
      this.activeImageModal = {
        visible: true,
        src: src,
        alt: alt || ''
      };
    },
    closeImageModal() {
      this.activeImageModal.visible = false;
    },
    openMermaidModal(svgHtml) {
      if (!svgHtml) return;
      // Chuẩn hoá SVG để hiển thị toàn diện trong modal:
      // Gỡ bỏ các giới hạn max-width cố định của card chat, đảm bảo width/height co giãn theo viewport modal
      let cleanSvg = svgHtml
        .replace(/\bmax-width:\s*[^;"]+;?/gi, '')
        .replace(/\bheight:\s*auto\s*!important;?/gi, '')
        .replace(/\bheight:\s*auto;?/gi, '')
        .replace(/style="[^"]*"/i, 'style="width:100%;height:100%;display:block;"');

      if (!cleanSvg.includes('width="100%"')) {
        cleanSvg = cleanSvg.replace(/<svg\b([^>]*)>/i, '<svg$1 width="100%" height="100%">');
      }

      this.activeMermaidModal = {
        visible: true,
        svgHtml: cleanSvg,
        zoom: 1,
        panX: 0,
        panY: 0,
        isDragging: false,
        dragStartX: 0,
        dragStartY: 0
      };
    },
    closeMermaidModal() {
      this.activeMermaidModal.visible = false;
      this.activeMermaidModal.isDragging = false;
    },
    zoomMermaidModal(delta) {
      let newZoom = this.activeMermaidModal.zoom + delta;
      newZoom = Math.min(Math.max(0.2, newZoom), 5.0);
      this.activeMermaidModal.zoom = Math.round(newZoom * 100) / 100;
    },
    resetMermaidModalView() {
      this.activeMermaidModal.zoom = 1;
      this.activeMermaidModal.panX = 0;
      this.activeMermaidModal.panY = 0;
    },
    handleMermaidModalWheel(e) {
      const delta = e.deltaY < 0 ? 0.15 : -0.15;
      this.zoomMermaidModal(delta);
    },
    handleMermaidModalMouseDown(e) {
      if (e.button !== 0) return; // Chỉ nhận chuột trái
      this.activeMermaidModal.isDragging = true;
      this.activeMermaidModal.dragStartX = e.clientX - this.activeMermaidModal.panX;
      this.activeMermaidModal.dragStartY = e.clientY - this.activeMermaidModal.panY;

      const onMouseMove = (ev) => {
        if (!this.activeMermaidModal.isDragging) return;
        this.activeMermaidModal.panX = ev.clientX - this.activeMermaidModal.dragStartX;
        this.activeMermaidModal.panY = ev.clientY - this.activeMermaidModal.dragStartY;
      };

      const onMouseUp = () => {
        this.activeMermaidModal.isDragging = false;
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
      };

      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    },
    handleMermaidModalTouchStart(e) {
      if (e.touches.length === 1) {
        this.activeMermaidModal.isDragging = true;
        this.activeMermaidModal.dragStartX = e.touches[0].clientX - this.activeMermaidModal.panX;
        this.activeMermaidModal.dragStartY = e.touches[0].clientY - this.activeMermaidModal.panY;
      } else if (e.touches.length === 2) {
        this._touchDist = Math.hypot(
          e.touches[0].clientX - e.touches[1].clientX,
          e.touches[0].clientY - e.touches[1].clientY
        );
        this._startZoom = this.activeMermaidModal.zoom;
      }
    },
    handleMermaidModalTouchMove(e) {
      if (e.touches.length === 1 && this.activeMermaidModal.isDragging) {
        this.activeMermaidModal.panX = e.touches[0].clientX - this.activeMermaidModal.dragStartX;
        this.activeMermaidModal.panY = e.touches[0].clientY - this.activeMermaidModal.dragStartY;
      } else if (e.touches.length === 2 && this._touchDist) {
        const dist = Math.hypot(
          e.touches[0].clientX - e.touches[1].clientX,
          e.touches[0].clientY - e.touches[1].clientY
        );
        const factor = dist / this._touchDist;
        let newZoom = this._startZoom * factor;
        newZoom = Math.min(Math.max(0.2, newZoom), 5.0);
        this.activeMermaidModal.zoom = Math.round(newZoom * 100) / 100;
      }
    },
    handleMermaidModalTouchEnd() {
      this.activeMermaidModal.isDragging = false;
      this._touchDist = null;
    },
    downloadCurrentMermaidPng() {
      const container = this.$refs.mermaidModalCanvas;
      if (!container) return;
      const svgEl = container.querySelector('svg');
      if (svgEl) {
        this.downloadSvgElementAsPng(svgEl, 'mermaid-diagram-full.png');
      }
    },
    downloadCurrentMermaidSvg() {
      const container = this.$refs.mermaidModalCanvas;
      if (!container) return;
      const svgEl = container.querySelector('svg');
      if (svgEl) {
        this.downloadSvgElementAsSvg(svgEl, 'mermaid-diagram-full.svg');
      }
    },
    prepareSvgForExport(svgEl) {
      if (!svgEl) return null;
      const clonedSvg = svgEl.cloneNode(true);

      // Xác định kích thước chuẩn
      let width = svgEl.clientWidth || 800;
      let height = svgEl.clientHeight || 600;

      const vb = svgEl.getAttribute('viewBox');
      if (vb) {
        const parts = vb.trim().split(/[\s,]+/).map(Number);
        if (parts.length === 4 && parts[2] > 0 && parts[3] > 0) {
          width = parts[2];
          height = parts[3];
        }
      }

      clonedSvg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
      clonedSvg.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink');
      clonedSvg.setAttribute('width', width);
      clonedSvg.setAttribute('height', height);
      if (!clonedSvg.getAttribute('viewBox')) {
        clonedSvg.setAttribute('viewBox', '0 0 ' + width + ' ' + height);
      }

      // Tạo nền trắng (#ffffff) để ảnh không bị đen/trong suốt
      const bgRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      bgRect.setAttribute('x', '0');
      bgRect.setAttribute('y', '0');
      bgRect.setAttribute('width', '100%');
      bgRect.setAttribute('height', '100%');
      bgRect.setAttribute('fill', '#ffffff');
      clonedSvg.insertBefore(bgRect, clonedSvg.firstChild);

      // Chuyển đổi toàn bộ <foreignObject> thành SVG <text> để loại bỏ hoàn toàn nguy cơ Tainted Canvas trong Chromium
      const foreignObjects = Array.from(clonedSvg.querySelectorAll('foreignObject'));
      foreignObjects.forEach((fo, foIdx) => {
        const foWidth = parseFloat(fo.getAttribute('width')) || 0;
        const foHeight = parseFloat(fo.getAttribute('height')) || 0;
        const x = parseFloat(fo.getAttribute('x')) || 0;
        const y = parseFloat(fo.getAttribute('y')) || 0;

        const labelEl = fo.querySelector('.nodeLabel, span, p, div') || fo;
        const textContent = (labelEl.textContent || fo.textContent || '').trim();
        if (!textContent) {
          fo.remove();
          return;
        }

        let textColor = '#1e293b';
        let fontSize = '14px';
        let fontWeight = '500';
        let fontFamily = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';

        if (typeof window !== 'undefined') {
          try {
            const origFoList = svgEl.querySelectorAll('foreignObject');
            const origFo = origFoList[foIdx] || svgEl.querySelector(`[id="${fo.id}"]`);
            if (origFo) {
              const origLabel = origFo.querySelector('.nodeLabel, span, p, div') || origFo;
              const comp = window.getComputedStyle(origLabel);
              if (comp.color && comp.color !== 'rgba(0, 0, 0, 0)') textColor = comp.color;
              if (comp.fontSize) fontSize = comp.fontSize;
              if (comp.fontWeight) fontWeight = comp.fontWeight;
              if (comp.fontFamily) fontFamily = comp.fontFamily;
            }
          } catch (e) {}
        }

        const textEl = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        textEl.setAttribute('x', x + foWidth / 2);
        textEl.setAttribute('y', y + foHeight / 2);
        textEl.setAttribute('text-anchor', 'middle');
        textEl.setAttribute('dominant-baseline', 'central');
        textEl.setAttribute('alignment-baseline', 'middle');
        textEl.setAttribute('fill', textColor);
        textEl.setAttribute('font-size', fontSize);
        textEl.setAttribute('font-weight', fontWeight);
        textEl.setAttribute('font-family', fontFamily);

        const lines = textContent.split(/\r?\n|<br\s*\/?>/i).map(l => l.trim()).filter(Boolean);
        if (lines.length > 1) {
          const lineHeight = parseFloat(fontSize) * 1.25 || 16;
          const startY = (y + foHeight / 2) - ((lines.length - 1) * lineHeight) / 2;
          lines.forEach((lineText, idx) => {
            const tspan = document.createElementNS('http://www.w3.org/2000/svg', 'tspan');
            tspan.textContent = lineText;
            tspan.setAttribute('x', x + foWidth / 2);
            tspan.setAttribute('y', startY + idx * lineHeight);
            textEl.appendChild(tspan);
          });
        } else {
          textEl.textContent = textContent;
        }

        if (fo.parentNode) {
          fo.parentNode.replaceChild(textEl, fo);
        }
      });

      return { clonedSvg, width, height };
    },
    triggerFileDownload(href, filename) {
      if (!href) return;
      try {
        const a = document.createElement('a');
        a.style.display = 'none';
        a.setAttribute('download', filename);
        a.setAttribute('href', href);
        document.body.appendChild(a);
        a.click();
        setTimeout(() => {
          if (a.parentNode) {
            a.parentNode.removeChild(a);
          }
        }, 500);
      } catch (err) {
        console.error('[File Download Error]:', err);
      }
    },
    downloadSvgElementAsSvg(svgEl, filename) {
      if (!svgEl) return;
      try {
        const defaultName = 'mermaid-diagram-' + new Date().toISOString().slice(0, 10) + '.svg';
        const finalName = filename ? filename.replace(/\.png$/i, '.svg') : defaultName;

        let svgString = '';
        const prep = this.prepareSvgForExport(svgEl);
        if (prep && prep.clonedSvg) {
          svgString = new XMLSerializer().serializeToString(prep.clonedSvg);
        } else {
          const clonedSvg = svgEl.cloneNode(true);
          clonedSvg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
          clonedSvg.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink');
          svgString = new XMLSerializer().serializeToString(clonedSvg);
        }

        // Dùng Data URL trực tiếp (không dùng Blob URL có createObjectURL/revokeObjectURL)
        // để tránh hoàn toàn lỗi Chrome bị abort stream và lưu file không có extension
        const dataUrl = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgString);
        this.triggerFileDownload(dataUrl, finalName);
      } catch (err) {
        console.error('[Mermaid Export SVG Error]:', err);
      }
    },
    downloadSvgElementAsPng(svgEl, filename) {
      if (!svgEl) return;
      const defaultName = 'mermaid-diagram-' + new Date().toISOString().slice(0, 10) + '.png';
      const finalName = filename || defaultName;

      try {
        const prep = this.prepareSvgForExport(svgEl);
        if (!prep) {
          this.downloadSvgElementAsSvg(svgEl, finalName.replace(/\.png$/i, '.svg'));
          return;
        }
        const { clonedSvg, width, height } = prep;
        const svgString = new XMLSerializer().serializeToString(clonedSvg);

        // Chuyển sang Base64 Data URL để tránh tainting do blob url trong trình duyệt
        let base64Svg;
        try {
          base64Svg = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgString)));
        } catch (e) {
          base64Svg = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgString);
        }

        const img = new Image();
        img.crossOrigin = 'anonymous';

        img.onload = () => {
          try {
            const scale = 2;
            const canvas = document.createElement('canvas');
            canvas.width = Math.max(Math.round(width * scale), 1600);
            const ratio = canvas.width / width;
            canvas.height = Math.round(height * ratio);

            const ctx = canvas.getContext('2d');
            ctx.fillStyle = '#ffffff';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            // Dùng trực tiếp canvas.toDataURL('image/png') cho download
            // Loại bỏ hoàn toàn createObjectURL và revokeObjectURL 1s gây hủy download trên Chromium
            const pngDataUrl = canvas.toDataURL('image/png');
            this.triggerFileDownload(pngDataUrl, finalName);
          } catch (canvasErr) {
            console.warn('[Mermaid Export PNG] Canvas export security error, auto-fallback to SVG:', canvasErr);
            this.downloadSvgElementAsSvg(svgEl, finalName.replace(/\.png$/i, '.svg'));
          }
        };

        img.onerror = (e) => {
          console.warn('[Mermaid Export PNG] Image load error, auto-fallback to SVG:', e);
          this.downloadSvgElementAsSvg(svgEl, finalName.replace(/\.png$/i, '.svg'));
        };

        img.src = base64Svg;
      } catch (err) {
        console.warn('[Mermaid Export PNG] Exception occurred, auto-fallback to SVG:', err);
        this.downloadSvgElementAsSvg(svgEl, finalName.replace(/\.png$/i, '.svg'));
      }
    },
    toggleChat(forceState) {
      if (typeof forceState === 'boolean') {
        this.isOpen = forceState;
      } else {
        this.isOpen = !this.isOpen;
      }
      if (!this.isOpen) {
        if (this.isFullscreen) {
          this.isFullscreen = false;
          this.$emit('fullscreen', false);
        }
        if (typeof document !== 'undefined' && document.body) {
          document.body.style.overflow = '';
        }
        this.stopZeroGBot();
      }
      this.$emit('toggle', this.isOpen);
    },
    toggleExpand() {
      this.isExpanded = !this.isExpanded;
      this.$emit('expand', this.isExpanded);
      this.scrollToBottom();
    },
    handleBodyScroll() {
      const el = this.$refs.chatBody;
      if (!el) return;
      const dist = el.scrollHeight - el.scrollTop - el.clientHeight;
      this.showScrollBottomBtn = dist > 60;
    },
    loadEarlierMessages() {
      const el = this.$refs.chatBody;
      const oldScrollHeight = el ? el.scrollHeight : 0;
      const oldScrollTop = el ? el.scrollTop : 0;

      // Mở rộng thêm 30 tin nhắn cũ
      this.visibleCount += 30;

      this.$nextTick(() => {
        if (el) {
          // Bù trừ độ lệch chiều cao để giữ nguyên vị trí scroll mắt người dùng đang đọc
          const heightDiff = el.scrollHeight - oldScrollHeight;
          el.scrollTop = oldScrollTop + heightDiff;
        }
      });
    },
    scrollToBottom(smooth = false) {
      this.$nextTick(() => {
        const el = this.$refs.chatBody;
        if (!el) return;
        if (smooth) {
          el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
        } else {
          el.scrollTop = el.scrollHeight;
        }
      });
    },
    copyMessageContent(msg) {
      if (!msg.text) return;
      navigator.clipboard.writeText(msg.text).then(() => {
        this.copiedMsgId = msg.id;
        setTimeout(() => {
          if (this.copiedMsgId === msg.id) {
            this.copiedMsgId = null;
          }
        }, 2000);
      }).catch(err => {
        console.error('Lỗi sao chép tin nhắn:', err);
      });
    },
    toggleSpeak(msg) {
      if (!window.speechSynthesis) return;

      // Nếu đang đọc chính message này → dừng lại
      if (this.speakingMsgId === msg.id) {
        window.speechSynthesis.cancel();
        this.speakingMsgId = null;
        return;
      }

      // Dừng bất kỳ message nào đang đọc trước đó
      window.speechSynthesis.cancel();

      // Làm sạch markdown + emoji trước khi đọc
      const cleanText = (msg.text || '')
        .replace(/```[\s\S]*?```/g, '')
        .replace(/`[^`]+`/g, '')
        .replace(/!\[.*?\]\(.*?\)/g, '')
        .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
        .replace(/[#*_>~|]/g, '')
        // Xoá emoji (Unicode ranges đầy đủ)
        .replace(/[\u{1F000}-\u{1FFFF}]/gu, '')   // Emoji chính (1F600-1F9FF, symbols, flags...)
        .replace(/[\u{2600}-\u{27BF}]/gu, '')      // Misc symbols, Dingbats
        .replace(/[\u{FE00}-\u{FE0F}]/gu, '')      // Variation selectors
        .replace(/[\u{1F1E0}-\u{1F1FF}]/gu, '')    // Regional indicators (cờ quốc gia)
        .replace(/\u200D/g, '')                    // Zero-width joiner
        .replace(/[\u2702-\u27B0]/g, '')           // Dingbats legacy
        .replace(/\n{2,}/g, '. ')
        .replace(/\n/g, ' ')
        .replace(/\s{2,}/g, ' ')                   // Gom khoảng trắng thừa sau khi xoá emoji
        .trim();

      if (!cleanText) return;

      // Lang đã được detect lúc lưu message, hoặc detect lại nếu chưa có
      const targetLang = msg.lang || detectLanguage(cleanText);

      const utterance = new SpeechSynthesisUtterance(cleanText);

      // Chọn giọng tốt nhất theo thứ tự ưu tiên
      const trySpeak = () => {
        const voices = window.speechSynthesis.getVoices();
        const langCode = targetLang.split('-')[0]; // vd: 'vi', 'en'

        // Lọc tất cả voices cùng ngôn ngữ
        const sameLangVoices = voices.filter(v =>
          v.lang === targetLang || v.lang.startsWith(langCode + '-')
        );

        // Hàm kiểm tra voice chất lượng cao (Edge Neural / Azure)
        const isNatural = v => /natural|neural|online/i.test(v.name);
        const isMicrosoft = v => /microsoft/i.test(v.name);
        const isGoogle = v => /google/i.test(v.name);

        // Thứ tự ưu tiên:
        // 1. Natural/Neural/Online (Edge Azure TTS) cùng lang
        // 2. Microsoft voice cùng lang
        // 3. Google voice cùng lang
        // 4. Bất kỳ voice cùng lang
        // 5. Fallback ngôn ngữ tương tự
        const chosenVoice =
          sameLangVoices.find(isNatural) ||
          sameLangVoices.find(isMicrosoft) ||
          sameLangVoices.find(isGoogle) ||
          sameLangVoices[0] ||
          voices.find(v => v.lang.startsWith(langCode)) ||
          null;

        if (chosenVoice) {
          utterance.voice = chosenVoice;
          utterance.lang = chosenVoice.lang;
          // Natural voices không cần giảm tốc độ
          utterance.rate = isNatural(chosenVoice) ? 1.0 : 0.92;
          console.info(`[UrChatbot TTS] Sử dụng giọng: "${chosenVoice.name}" (${chosenVoice.lang}) cho ngôn ngữ ${targetLang}`);
        } else {
          utterance.lang = targetLang;
          utterance.rate = 0.92;
          console.warn(`[UrChatbot TTS] Không tìm thấy voice cho ${targetLang}, dùng browser default`);
        }

        utterance.pitch = 1;
        utterance.volume = 1;

        utterance.onstart = () => { this.speakingMsgId = msg.id; };
        utterance.onend = () => { this.speakingMsgId = null; };
        utterance.onerror = () => { this.speakingMsgId = null; };

        window.speechSynthesis.speak(utterance);
      };

      if (window.speechSynthesis.getVoices().length === 0) {
        window.speechSynthesis.onvoiceschanged = () => {
          window.speechSynthesis.onvoiceschanged = null;
          trySpeak();
        };
      } else {
        trySpeak();
      }
    },
    speakText(text) {
      // Legacy alias — gọi toggleSpeak nếu cần
      this.toggleSpeak({ id: '__legacy__', text });
    },
    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
      this.$emit('fullscreen', this.isFullscreen);
      this.scrollToBottom();
    },
    getStorageKey() {
      if (this.storageKey) return this.storageKey;
      if (typeof window === 'undefined') return 'ur_chatbot_history_default';
      const path = window.location.pathname || '/';
      return `ur_chatbot_history_${encodeURIComponent(path)}`;
    },
    loadFromLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        const raw = window.localStorage.getItem(this.getStorageKey());
        if (!raw) return;
        const data = JSON.parse(raw);
        if (data && Array.isArray(data.messageList) && data.messageList.length > 0) {
          this.messageList = data.messageList.map(m => {
            const hasVersions = Array.isArray(m.versions) && m.versions.length > 0;
            const currentIdx = typeof m.currentVersionIdx === 'number' ? m.currentVersionIdx : 0;
            const activeVersion = hasVersions && m.versions[currentIdx] ? m.versions[currentIdx] : null;
            const activeText = activeVersion ? activeVersion.text : (m.text || '');
            const activeThinking = activeVersion ? (activeVersion.thinking || '') : (m.thinking || '');
            const activeThinkingDuration = activeVersion ? (activeVersion.thinkingDuration || '') : (m.thinkingDuration || '');
            return {
              ...m,
              text: activeText,
              thinking: activeThinking,
              thinkingDuration: activeThinkingDuration,
              isStreaming: false,
              html: m.sender === 'bot' ? this.renderHtml(activeText, false) : '',
              versions: hasVersions ? m.versions : (m.sender === 'bot' && activeText ? [{
                text: activeText,
                html: this.renderHtml(activeText, false),
                responseTime: m.responseTime || '',
                lang: m.lang || '',
                thinking: activeThinking,
                thinkingDuration: activeThinkingDuration
              }] : []),
              currentVersionIdx: currentIdx
            };
          });
        }
        if (data && Array.isArray(data.apiMessagesHistory) && data.apiMessagesHistory.length > 0) {
          this.apiMessagesHistory = data.apiMessagesHistory;
        }
        if (data && data.effectiveModel) {
          this.effectiveModel = data.effectiveModel;
        }
        if (data && typeof data.isThinkingActive === 'boolean') {
          this.isThinkingActive = data.isThinkingActive;
        }
      } catch (err) {
        console.warn('[UrChatbot] Không thể nạp localStorage:', err);
      }
    },
    saveToLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        const key = this.getStorageKey();
        let msgsToSave = (this.messageList || []).map(m => ({
          id: m.id,
          sender: m.sender,
          text: m.text,
          responseTime: m.responseTime || '',
          isError: !!m.isError,
          isResetNotice: !!m.isResetNotice,
          images: m.images || undefined,
          thinking: m.thinking || '',
          thinkingDuration: m.thinkingDuration || '',
          versions: m.versions || undefined,
          currentVersionIdx: m.currentVersionIdx !== undefined ? m.currentVersionIdx : undefined
        }));

        // Giới hạn maxStoredMessages (mặc định 40 tin nhắn gần nhất)
        if (msgsToSave.length > this.maxStoredMessages) {
          const firstWelcome = msgsToSave[0] && msgsToSave[0].sender === 'bot' ? [msgsToSave[0]] : [];
          const recent = msgsToSave.slice(-this.maxStoredMessages);
          msgsToSave = firstWelcome.concat(recent.filter(m => !firstWelcome[0] || m.id !== firstWelcome[0].id));
        }

        let apiHistoryToSave = (this.apiMessagesHistory || []).slice(-this.maxStoredMessages);

        // Vòng lặp thử lưu và tự động cắt tỉa (prune) tin nhắn cũ nếu chạm hạn mức 5MB (QuotaExceededError)
        let attempts = 0;
        while (attempts < 5) {
          try {
            const payload = JSON.stringify({
              version: 1,
              updatedAt: Date.now(),
              messageList: msgsToSave,
              apiMessagesHistory: apiHistoryToSave,
              effectiveModel: this.effectiveModel,
              isThinkingActive: this.isThinkingActive
            });

            // Nếu kích thước chuỗi vượt quá 1.5MB (~3MB UTF-16) thì chủ động cắt tỉa bớt tin nhắn cũ
            if (payload.length > 1500000 && msgsToSave.length > 6) {
              msgsToSave.splice(1, 5);
              apiHistoryToSave.splice(0, 5);
              attempts++;
              continue;
            }

            window.localStorage.setItem(key, payload);
            break;
          } catch (err) {
            if (err.name === 'QuotaExceededError' || err.code === 22 || err.code === 1014) {
              if (msgsToSave.length > 6) {
                // Xoá bớt 6 tin nhắn cũ nhất và thử lại
                msgsToSave.splice(1, 6);
                apiHistoryToSave.splice(0, 6);
                attempts++;
              } else {
                window.localStorage.removeItem(key);
                break;
              }
            } else {
              throw err;
            }
          }
        }
      } catch (err) {
        console.warn('[UrChatbot] Lỗi lưu localStorage:', err);
      }
    },
    clearLocalStorage() {
      if (!this.localStorage || typeof window === 'undefined') return;
      try {
        window.localStorage.removeItem(this.getStorageKey());
      } catch (e) {}
    },
    startZeroGBot() {
      this.stopZeroGBot();
      const winW = typeof window !== 'undefined' ? window.innerWidth : 1000;
      const winH = typeof window !== 'undefined' ? window.innerHeight : 700;
      this.zeroGBot.x = Math.max(50, Math.min(winW - 130, winW / 2 + 220));
      this.zeroGBot.y = Math.max(80, Math.min(winH - 180, 140));
      this.zeroGBot.vx = (Math.random() - 0.5) * 2 + 1;
      this.zeroGBot.vy = (Math.random() - 0.5) * 2;
      this.zeroGBot.rot = 0;
      this.zeroGBot.vRot = 0.8;
      this.zeroGBot.isDragging = false;
      this._hasZeroGDragged = false;

      const loop = () => {
        if (!this.isFullscreen) return;
        this.stepZeroGBot();
        this._zeroGRaf = requestAnimationFrame(loop);
      };
      this._zeroGRaf = requestAnimationFrame(loop);
    },
    stopZeroGBot() {
      if (this._zeroGRaf) {
        cancelAnimationFrame(this._zeroGRaf);
        this._zeroGRaf = null;
      }
      if (this._zeroGMouseMove) {
        window.removeEventListener('mousemove', this._zeroGMouseMove);
        this._zeroGMouseMove = null;
      }
      if (this._zeroGMouseUp) {
        window.removeEventListener('mouseup', this._zeroGMouseUp);
        this._zeroGMouseUp = null;
      }
    },
    stepZeroGBot() {
      if (this.zeroGBot.isDragging) return;

      const bot = this.zeroGBot;
      bot.x += bot.vx;
      bot.y += bot.vy;
      bot.rot += bot.vRot;

      const winW = typeof window !== 'undefined' ? window.innerWidth : 1000;
      const winH = typeof window !== 'undefined' ? window.innerHeight : 700;

      const minX = 15;
      const maxX = winW - 110;
      const minY = 60;
      const maxY = winH - 160;

      const restitution = 0.82; // Độ nảy đàn hồi

      if (bot.x <= minX) {
        bot.x = minX;
        bot.vx = Math.abs(bot.vx) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      } else if (bot.x >= maxX) {
        bot.x = maxX;
        bot.vx = -Math.abs(bot.vx) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      }

      if (bot.y <= minY) {
        bot.y = minY;
        bot.vy = Math.abs(bot.vy) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      } else if (bot.y >= maxY) {
        bot.y = maxY;
        bot.vy = -Math.abs(bot.vy) * restitution;
        bot.vRot = -bot.vRot * 0.9 + (Math.random() - 0.5) * 0.4;
      }

      // Giảm tốc ma sát chân không rất êm
      bot.vx *= 0.995;
      bot.vy *= 0.995;
      bot.vRot *= 0.996;

      // Micro drift ngẫu nhiên nếu bot sắp dừng hẳn
      const speed = Math.hypot(bot.vx, bot.vy);
      if (speed < 0.35) {
        bot.vx += (Math.random() - 0.5) * 0.08;
        bot.vy += (Math.random() - 0.5) * 0.08;
        bot.vRot += (Math.random() - 0.5) * 0.06;
      }
    },
    onZeroGBotMouseDown(e) {
      if (e.button !== 0) return;
      e.preventDefault();
      this.zeroGBot.isDragging = true;
      this._hasZeroGDragged = false;

      const startMouseX = e.clientX;
      const startMouseY = e.clientY;
      const initialBotX = this.zeroGBot.x;
      const initialBotY = this.zeroGBot.y;

      let mouseHistory = [{ x: e.clientX, y: e.clientY, t: Date.now() }];

      const onMouseMove = (moveEv) => {
        const dx = moveEv.clientX - startMouseX;
        const dy = moveEv.clientY - startMouseY;
        if (Math.hypot(dx, dy) > 5) {
          this._hasZeroGDragged = true;
        }
        this.zeroGBot.x = initialBotX + dx;
        this.zeroGBot.y = initialBotY + dy;

        const now = Date.now();
        mouseHistory.push({ x: moveEv.clientX, y: moveEv.clientY, t: now });
        if (mouseHistory.length > 6) {
          mouseHistory.shift();
        }
      };

      const onMouseUp = () => {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
        this._zeroGMouseMove = null;
        this._zeroGMouseUp = null;
        this.zeroGBot.isDragging = false;

        if (mouseHistory.length >= 2) {
          const first = mouseHistory[0];
          const last = mouseHistory[mouseHistory.length - 1];
          const dt = Math.max(16, last.t - first.t) / 1000;
          const rawVx = (last.x - first.x) / (dt * 60);
          const rawVy = (last.y - first.y) / (dt * 60);

          this.zeroGBot.vx = Math.max(-20, Math.min(20, rawVx));
          this.zeroGBot.vy = Math.max(-20, Math.min(20, rawVy));
          this.zeroGBot.vRot = Math.max(-10, Math.min(10, (rawVx - rawVy) * 0.35));
        }
      };

      this._zeroGMouseMove = onMouseMove;
      this._zeroGMouseUp = onMouseUp;
      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    },
    onZeroGBotTouchStart(e) {
      if (e.touches.length !== 1) return;
      const touch = e.touches[0];
      this.zeroGBot.isDragging = true;
      this._hasZeroGDragged = false;
      const startX = touch.clientX;
      const startY = touch.clientY;
      const initX = this.zeroGBot.x;
      const initY = this.zeroGBot.y;
      let history = [{ x: startX, y: startY, t: Date.now() }];

      const onTouchMove = (moveEv) => {
        if (moveEv.touches.length !== 1) return;
        const tMove = moveEv.touches[0];
        const dx = tMove.clientX - startX;
        const dy = tMove.clientY - startY;
        if (Math.hypot(dx, dy) > 5) {
          this._hasZeroGDragged = true;
        }
        this.zeroGBot.x = initX + dx;
        this.zeroGBot.y = initY + dy;
        history.push({ x: tMove.clientX, y: tMove.clientY, t: Date.now() });
        if (history.length > 6) history.shift();
      };

      const onTouchEnd = () => {
        window.removeEventListener('touchmove', onTouchMove);
        window.removeEventListener('touchend', onTouchEnd);
        this.zeroGBot.isDragging = false;
        if (history.length >= 2) {
          const first = history[0];
          const last = history[history.length - 1];
          const dt = Math.max(16, last.t - first.t) / 1000;
          const rawVx = (last.x - first.x) / (dt * 60);
          const rawVy = (last.y - first.y) / (dt * 60);
          this.zeroGBot.vx = Math.max(-20, Math.min(20, rawVx));
          this.zeroGBot.vy = Math.max(-20, Math.min(20, rawVy));
          this.zeroGBot.vRot = Math.max(-10, Math.min(10, (rawVx - rawVy) * 0.35));
        }
      };

      window.addEventListener('touchmove', onTouchMove, { passive: true });
      window.addEventListener('touchend', onTouchEnd);
    },
    onZeroGBotClick() {
      if (this._hasZeroGDragged) {
        this._hasZeroGDragged = false;
        return;
      }
      if (this.hasBotError) {
        this.retryLastAction();
      } else {
        // Nudge nhẹ và quay vòng vui nhộn khi click vào bot
        this.zeroGBot.vRot += (Math.random() > 0.5 ? 4 : -4);
        this.zeroGBot.vx += (Math.random() - 0.5) * 3;
        this.zeroGBot.vy = -3.5;
      }
    },
    promptResetChat() {
      if (this.isStreaming) return;
      this.showResetConfirm = true;
    },
    cancelReset() {
      this.showResetConfirm = false;
    },
    confirmReset() {
      this.showResetConfirm = false;
      this.clearMessages();
    },
    isMsgCollapsible(msg) {
      if (!msg || msg.sender !== 'user') return false;
      if (this.userMsgCollapsibleMap[msg.id] !== undefined) {
        return this.userMsgCollapsibleMap[msg.id];
      }
      const lineCount = (msg.text || '').split('\n').length;
      return lineCount > 3 || (msg.text || '').length > 140;
    },
    isMsgExpanded(msg) {
      return !!this.userMsgExpandedMap[msg.id];
    },
    isMsgCollapsed(msg) {
      return this.isMsgCollapsible(msg) && !this.isMsgExpanded(msg);
    },
    toggleUserMsgExpand(msgId) {
      // Save scroll position BEFORE DOM changes to prevent jarring scroll jump
      const chatBody = this.$refs.chatBody;
      const savedScrollTop = chatBody ? chatBody.scrollTop : 0;
      const savedScrollHeight = chatBody ? chatBody.scrollHeight : 0;

      this.$set(this.userMsgExpandedMap, msgId, !this.userMsgExpandedMap[msgId]);

      this.$nextTick(() => {
        if (!chatBody) return;
        const newScrollHeight = chatBody.scrollHeight;
        const heightDelta = newScrollHeight - savedScrollHeight;
        // Adjust scrollTop by the height change so content above stays in place
        chatBody.scrollTop = savedScrollTop + heightDelta;
      });
    },
    checkUserMsgHeights() {
      this.$nextTick(() => {
        if (!this.visibleMessageList) return;
        this.visibleMessageList.forEach(msg => {
          if (msg.sender === 'user') {
            const refName = `userMsg_${msg.id}`;
            const el = this.$refs[refName];
            const targetEl = Array.isArray(el) ? el[0] : el;
            if (targetEl) {
              const lineCount = (msg.text || '').split('\n').length;
              const isCollapsible = targetEl.scrollHeight > 72 || lineCount > 3;
              if (this.userMsgCollapsibleMap[msg.id] !== isCollapsible) {
                this.$set(this.userMsgCollapsibleMap, msg.id, isCollapsible);
              }
            }
          }
        });
      });
    },
    retryUserQuestion(msg) {
      if (this.isStreaming || this.isLoading || !msg.text) return;
      this.inputMsg = msg.text;
      this.autoResizeInput();
      this.handleSendMessage();
    },
    handleInputKeyDown(e) {
      if (e.key === 'Enter') {
        if (e.shiftKey) {
          // Xuống dòng với Shift + Enter
          this.autoResizeInput();
          return;
        }
        // Enter gửi tin nhắn nếu không trong bộ gõ IME tiếng Việt
        if (e.isComposing || e.keyCode === 229) {
          return;
        }
        e.preventDefault();
        this.handleSendMessage();
      }
    },
    autoResizeInput() {
      this.$nextTick(() => {
        const el = this.$refs.chatInput;
        if (!el) return;
        el.style.height = 'auto';
        const minHeight = 38;
        const maxHeight = 120; // Giới hạn tối đa 5 dòng
        const targetHeight = Math.max(minHeight, Math.min(el.scrollHeight, maxHeight));
        el.style.height = targetHeight + 'px';
        el.style.overflowY = el.scrollHeight > maxHeight ? 'auto' : 'hidden';
      });
    },
    syncActiveVersionToHistory(botMsgObj) {
      if (!botMsgObj || !botMsgObj.text) return;
      let botIndex = 0;
      for (let i = 0; i < this.messageList.length; i++) {
        const m = this.messageList[i];
        if (m === botMsgObj) break;
        if (m.sender === 'bot' && !m.isResetNotice && m.text) {
          botIndex++;
        }
      }

      let historyBotIndex = 0;
      for (let j = 0; j < this.apiMessagesHistory.length; j++) {
        if (this.apiMessagesHistory[j].role === 'assistant') {
          if (historyBotIndex === botIndex) {
            this.apiMessagesHistory[j].content = botMsgObj.text;
            return;
          }
          historyBotIndex++;
        }
      }
    },
    getMsgVersionsCount(msg) {
      if (!msg) return 0;
      if (msg.versions && Array.isArray(msg.versions) && msg.versions.length > 0) {
        return msg.versions.length;
      }
      return 1;
    },
    getMsgCurrentVersionIndex(msg) {
      if (!msg || typeof msg.currentVersionIdx !== 'number') return 0;
      return msg.currentVersionIdx;
    },
    setMsgVersion(msg, targetIndex) {
      if (!msg || !msg.versions || targetIndex < 0 || targetIndex >= msg.versions.length) return;
      this.$set(msg, 'currentVersionIdx', targetIndex);
      const v = msg.versions[targetIndex];
      this.$set(msg, 'text', v.text);
      this.$set(msg, 'html', v.html || this.renderHtml(v.text, false));
      this.$set(msg, 'responseTime', v.responseTime || '');
      this.$set(msg, 'lang', v.lang || detectLanguage(v.text));
      this.$set(msg, 'thinking', v.thinking || '');
      this.$set(msg, 'thinkingDuration', v.thinkingDuration || '');
      this.syncActiveVersionToHistory(msg);
      this.saveToLocalStorage();
      this.$nextTick(() => {
        this.scheduleMermaidRender();
      });
    },
    prevMsgVersion(msg) {
      const cur = this.getMsgCurrentVersionIndex(msg);
      if (cur > 0) {
        this.setMsgVersion(msg, cur - 1);
      }
    },
    nextMsgVersion(msg) {
      const cur = this.getMsgCurrentVersionIndex(msg);
      if (cur < this.getMsgVersionsCount(msg) - 1) {
        this.setMsgVersion(msg, cur + 1);
      }
    },
    isThinkingCollapsed(msg) {
      if (!msg) return true;
      if (this.thinkingCollapseMap[msg.id] !== undefined) {
        return !!this.thinkingCollapseMap[msg.id];
      }
      // Trong khi bot đang streaming: mở ra để người dùng theo dõi quá trình suy nghĩ
      if (msg.isStreaming) {
        return false;
      }
      // Sau khi AI đã hoàn tất câu trả lời: mặc định tự động thu gọn lại
      return true;
    },
    toggleThinkingCollapse(msgOrId) {
      const msg = (typeof msgOrId === 'object' && msgOrId)
        ? msgOrId
        : (this.messageList || []).find(m => m.id === msgOrId);
      const msgId = msg ? msg.id : msgOrId;
      if (!msgId) return;
      const current = this.isThinkingCollapsed(msg);
      this.$set(this.thinkingCollapseMap, msgId, !current);
    },
    getThinkingHeaderLabel(msg) {
      if (!msg) return 'Thinking...';
      if (msg.isStreaming && !msg.text) {
        return 'Thinking...';
      }
      const dur = msg.thinkingDuration || (msg.responseTime ? msg.responseTime.replace(/\..*$/, 's') : '5s');
      return `Thought for ${dur}`;
    },
    getThinkingSteps(text) {
      if (!text || typeof text !== 'string') return [];
      const clean = text.replace(/\r\n/g, '\n').trim();
      if (!clean) return [];

      const rawBlocks = clean.split(/\n{2,}/);
      const steps = [];

      for (let i = 0; i < rawBlocks.length; i++) {
        let b = rawBlocks[i].trim();
        if (!b) continue;

        // Bỏ ký tự gạch đầu dòng Markdown nếu có
        b = b.replace(/^[\*\-\•]\s+/, '').replace(/^\d+\.\s+/, '');

        // 1. Format **Title** Description hoặc **Title**\nDescription
        const boldMatch = b.match(/^\*\*([^*]+)\*\*[:\s]*([\s\S]*)$/);
        if (boldMatch) {
          steps.push({
            title: boldMatch[1].trim(),
            desc: boldMatch[2].trim()
          });
          continue;
        }

        // 2. Format ### Title\nDescription
        const headingMatch = b.match(/^#{1,4}\s+([^\n]+)\n?([\s\S]*)$/);
        if (headingMatch) {
          steps.push({
            title: headingMatch[1].trim(),
            desc: headingMatch[2].trim()
          });
          continue;
        }

        // 3. Format Title: Description (Title ngắn <= 45 ký tự)
        const colonMatch = b.match(/^([^:\n]{3,45}):\s+([\s\S]+)$/);
        if (colonMatch) {
          steps.push({
            title: colonMatch[1].trim(),
            desc: colonMatch[2].trim()
          });
          continue;
        }

        // 4. Khối nhiều dòng và dòng đầu tiên ngắn (< 50 ký tự), không có dấu chấm kết thúc
        const lines = b.split('\n').map(l => l.trim()).filter(Boolean);
        if (lines.length > 1 && lines[0].length <= 50 && !/[.?!]$/.test(lines[0])) {
          steps.push({
            title: lines[0],
            desc: lines.slice(1).join('\n')
          });
          continue;
        }

        // 5. Nếu là đoạn văn, tách câu đầu tiên làm title nếu câu ngắn (< 55 ký tự)
        const periodMatch = b.match(/^([^\.\?\!]{5,55}[\.\?\!])\s+([\s\S]+)$/);
        if (periodMatch) {
          steps.push({
            title: periodMatch[1].trim(),
            desc: periodMatch[2].trim()
          });
          continue;
        }

        // 6. Fallback
        if (b.length <= 45) {
          steps.push({ title: b, desc: '' });
        } else {
          steps.push({ title: '', desc: b });
        }
      }

      return steps;
    },
    extractSuggestionsFromText(botMsgObj) {
      if (!botMsgObj || !botMsgObj.text) return;
      const rawText = botMsgObj.text;

      // 1. Regex tìm khối ```json { ... "questions": [...] ... } ``` ở cuối văn bản (có thể có tiêu đề Gợi ý / Suggestions phía trước)
      const jsonBlockRegex = /(?:(?:\n|^)(?:[#*_\-\s]*(?:Gợi ý|Gợi ý tiếp theo|Follow-up questions?|Các câu hỏi gợi ý)[#*_\-\s:]*\n?)?)```(?:json)?\s*(\{[\s\S]*?"questions"\s*:\s*\[[\s\S]*?\}\s*)\s*```\s*$/i;
      let match = rawText.match(jsonBlockRegex);

      // 2. Hoặc tìm khối JSON thuần { "title": ..., "questions": [...] } ở cuối văn bản
      if (!match) {
        const rawJsonRegex = /(?:(?:\n|^)(?:[#*_\-\s]*(?:Gợi ý|Gợi ý tiếp theo|Follow-up questions?|Các câu hỏi gợi ý)[#*_\-\s:]*\n?)?)(\{[\s\S]*?"questions"\s*:\s*\[[\s\S]*?\}\s*)$/i;
        match = rawText.match(rawJsonRegex);
      }

      if (match) {
        try {
          const jsonStr = match[1].trim();
          const parsed = JSON.parse(jsonStr);
          if (Array.isArray(parsed.questions) && parsed.questions.length > 0) {
            this.suggestedPrompts = parsed.questions.slice(0, 3).map(q => String(q).trim()).filter(Boolean);
            if (parsed.title && typeof parsed.title === 'string' && parsed.title.trim()) {
              this.suggestedTitle = parsed.title.trim();
            }

            // Xóa bỏ đoạn mã JSON thô này khỏi câu trả lời của bot
            const cleanedText = rawText.substring(0, match.index).trimEnd();
            if (cleanedText) {
              this.$set(botMsgObj, 'text', cleanedText);
              this.$set(botMsgObj, 'html', this.renderHtml(cleanedText, false));
              if (botMsgObj.versions && botMsgObj.versions[botMsgObj.currentVersionIdx]) {
                botMsgObj.versions[botMsgObj.currentVersionIdx].text = cleanedText;
                botMsgObj.versions[botMsgObj.currentVersionIdx].html = this.renderHtml(cleanedText, false);
              }
            }
          }
        } catch (e) {
          // ignore parse error
        }
      }
    },
    regenerateMessage(msg) {
      if (this.isStreaming || this.isLoading || !msg) return;

      const msgIdx = this.messageList.indexOf(msg);
      if (msgIdx < 0) return;

      // Tìm câu hỏi của user ngay trước tin nhắn bot này
      let userMsg = null;
      for (let i = msgIdx - 1; i >= 0; i--) {
        if (this.messageList[i].sender === 'user') {
          userMsg = this.messageList[i];
          break;
        }
      }
      if (!userMsg) return;

      // Khởi tạo hoặc đồng bộ versions hiện tại
      if (!msg.versions || !Array.isArray(msg.versions) || msg.versions.length === 0) {
        this.$set(msg, 'versions', [
          {
            text: msg.text,
            html: msg.html || this.renderHtml(msg.text, false),
            responseTime: msg.responseTime || '',
            lang: msg.lang || '',
            thinking: msg.thinking || '',
            thinkingDuration: msg.thinkingDuration || ''
          }
        ]);
        this.$set(msg, 'currentVersionIdx', 0);
      } else {
        const curIdx = msg.currentVersionIdx || 0;
        if (msg.versions[curIdx]) {
          msg.versions[curIdx] = {
            text: msg.text,
            html: msg.html || this.renderHtml(msg.text, false),
            responseTime: msg.responseTime || '',
            lang: msg.lang || '',
            thinking: msg.thinking || '',
            thinkingDuration: msg.thinkingDuration || ''
          };
        }
      }

      // Xây dựng ngữ cảnh hội thoại từ đầu đến userMsg
      const payloadMessages = [{ role: 'system', content: this.systemPrompt }];
      for (let i = 0; i < this.messageList.length; i++) {
        const item = this.messageList[i];
        if (item === msg) break;
        if (item.sender === 'user') {
          if (item.images && item.images.length > 0) {
            const content = [{ type: 'text', text: item.text || 'Hãy phân tích chi tiết hình ảnh đính kèm này.' }];
            for (let k = 0; k < item.images.length; k++) {
              const aImg = item.images[k];
              const imgUrl = (this.shouldStoreFile && aImg.url) ? aImg.url : (aImg.base64 || aImg.url);
              content.push({ type: 'image_url', image_url: { url: imgUrl } });
            }
            payloadMessages.push({ role: 'user', content: content });
          } else if (item.text) {
            payloadMessages.push({ role: 'user', content: item.text });
          }
        } else if (item.sender === 'bot' && !item.isResetNotice && item.text) {
          payloadMessages.push({ role: 'assistant', content: item.text });
        }
      }

      // Áp dụng giới hạn lịch sử
      let finalHistory = payloadMessages.slice(1);
      if (this.historyLimit > 0) {
        finalHistory = finalHistory.slice(-this.historyLimit);
      }
      const finalPayload = [payloadMessages[0], ...finalHistory];

      // Đặt lại dữ liệu hiển thị của bot message để stream phiên bản mới
      this.$set(msg, 'text', '');
      this.$set(msg, 'html', '');
      this.$set(msg, 'thinking', '');
      this.$set(msg, 'thinkingDuration', '');
      this.$set(msg, 'responseTime', null);
      this.$set(msg, 'isError', false);

      this.streamChatResponse(msg, finalPayload, true);
    },
    sendSuggestedPrompt(promptText) {
      if (this.isLoading || this.isStreaming) return;
      this.inputMsg = promptText;
      this.autoResizeInput();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.handleSendMessage();
    },
    clearMessages() {
      if (this.isStreaming) {
        this.stopStreaming();
      }
      this.inputMsg = '';
      this.autoResizeInput();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.userMsgExpandedMap = {};
      this.userMsgCollapsibleMap = {};
      this.thinkingCollapseMap = {};
      this.messageList = [
        {
          id: Date.now(),
          sender: 'bot',
          text: 'Conversation has been reset! 🌌',
          html: this.renderHtml('Conversation has been reset! 🌌', false),
          isResetNotice: true
        }
      ];
      this.initHistory();
      this.clearLocalStorage();
      this.saveToLocalStorage();
    },
    stopStreaming() {
      if (this.typingTimer) {
        clearInterval(this.typingTimer);
        this.typingTimer = null;
      }
      if (this.abortController) {
        this.abortController.abort();
        this.abortController = null;
      }
      this.isStreaming = false;
      this.isLoading = false;

      const lastMsg = this.messageList[this.messageList.length - 1];
      if (lastMsg && lastMsg.isStreaming) {
        this.$set(lastMsg, 'isStreaming', false);
      }
    },
    async callChatApi(payload, signal) {
      const endpoint = this.apiUrl || '/api/chat';

      try {
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload),
          signal
        });

        return response;
      } catch (err) {
        if (err.name === 'AbortError') throw err;
        throw err || new Error('Unable to connect to Chat API service');
      }
    },
    async callChatApi_old(payload, signal) {
      const endpoints = this.apiUrl
        ? [this.apiUrl]
        : ['/api/chat', 'http://localhost:3001/api/chat'];
      let lastError = null;
      for (const endpoint of endpoints) {
        try {
          const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload),
            signal
          });
          if (response.status === 404 && endpoint === '/api/chat') {
            continue; // Thử tiếp endpoint port 3001
          }
          return response;
        } catch (err) {
          if (err.name === 'AbortError') throw err;
          lastError = err;
        }
      }
      throw lastError || new Error('Unable to connect to Chat API service');
    },
    onDragOver(e) {
      if (!this.canAttachFile) return;
      this.isDraggingOver = true;
    },
    onDragEnter(e) {
      if (!this.canAttachFile) return;
      this.isDraggingOver = true;
    },
    onDragLeave(e) {
      this.isDraggingOver = false;
    },
    onDrop(e) {
      this.isDraggingOver = false;
      if (!this.canAttachFile) return;
      const files = e.dataTransfer && e.dataTransfer.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.type && file.type.indexOf('image') !== -1) {
          this.processAndUploadImageFile(file);
        }
      }
    },
    handlePaste(e) {
      this.autoResizeInput();
      if (!this.canAttachFile) return;
      const clipboardData = e.clipboardData || (window.clipboardData);
      if (!clipboardData || !clipboardData.items) return;

      const items = clipboardData.items;
      let hasImage = false;

      for (let i = 0; i < items.length; i++) {
        const item = items[i];
        if (item.type && item.type.indexOf('image') !== -1) {
          const file = item.getAsFile();
          if (file) {
            hasImage = true;
            this.processAndUploadImageFile(file);
          }
        }
      }

      if (hasImage) {
        e.preventDefault();
      }
    },
    handleFileInputChange(e) {
      const files = e.target.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        this.processAndUploadImageFile(files[i]);
      }
      e.target.value = '';
    },
    processAndUploadImageFile(file) {
      if (!this.canAttachFile || !file) return;
      const tempId = 'img_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
      const reader = new FileReader();

      reader.onload = (event) => {
        const base64Data = event.target.result;
        const imgItem = {
          id: tempId,
          name: file.name || ('image_' + Date.now() + '.png'),
          preview: base64Data,
          base64: base64Data,
          url: '',
          key: '',
          uploading: false,
          error: null
        };

        if (this.shouldStoreFile) {
          // Khi storeFile = true: Upload lên MinIO để lấy URL S3 lưu trữ 1 ngày
          imgItem.uploading = true;
          this.pendingImages.push(imgItem);
          this.uploadImageToMinio(imgItem);
        } else {
          // Khi storeFile = false: Gửi trực tiếp chuỗi Base64
          imgItem.uploading = false;
          this.pendingImages.push(imgItem);
        }
      };

      reader.readAsDataURL(file);
    },
    async uploadImageToMinio(imgItem) {
      try {
        const res = await fetch('/api/upload', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            image: imgItem.base64,
            name: imgItem.name
          })
        });
        const data = await res.json();
        if (!res.ok || !data.success) {
          throw new Error(data.error || 'Lỗi upload ảnh');
        }
        this.$set(imgItem, 'url', data.url);
        this.$set(imgItem, 'key', data.key);
        this.$set(imgItem, 'uploading', false);
      } catch (err) {
        console.error('[UrChatbot] Lỗi upload ảnh lên MinIO:', err);
        this.$set(imgItem, 'uploading', false);
        this.$set(imgItem, 'error', err.message);
      }
    },
    async removePendingImage(index) {
      if (index < 0 || index >= this.pendingImages.length) return;
      const removed = this.pendingImages.splice(index, 1)[0];
      if (this.shouldStoreFile && removed && removed.key) {
        try {
          await fetch('/api/upload', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ key: removed.key })
          });
          console.log('[UrChatbot] Đã xóa ảnh khỏi MinIO:', removed.key);
        } catch (err) {
          console.warn('[UrChatbot] Lỗi khi gọi xóa ảnh khỏi MinIO:', err);
        }
      }
    },
    async handleSendMessage() {
      const trimmed = this.inputMsg.trim();
      const hasImages = this.pendingImages && this.pendingImages.length > 0;
      if ((!trimmed && !hasImages) || this.isLoading || this.isStreaming || this.isUploadingAnyImage) return;

      this.isErrorState = false;

      // 1. Sao chép và xử lý danh sách ảnh đính kèm
      const attachedImages = [...(this.pendingImages || [])];
      this.pendingImages = [];

      // 2. Thêm tin nhắn của User
      const userMessageId = Date.now();
      this.messageList.push({
        id: userMessageId,
        sender: 'user',
        text: trimmed,
        images: attachedImages
      });

      // 3. Chuẩn bị payload gửi cho LLM (Vision multimodal format)
      let apiContent;
      if (attachedImages.length > 0) {
        apiContent = [];
        const questionText = trimmed || 'Hãy phân tích chi tiết hình ảnh đính kèm này.';
        apiContent.push({ type: 'text', text: questionText });

        for (let i = 0; i < attachedImages.length; i++) {
          const aImg = attachedImages[i];
          // Nếu storeFile=true và đã upload xong lên MinIO thì gửi URL S3, nếu không thì gửi base64
          const imgUrl = (this.shouldStoreFile && aImg.url)
            ? aImg.url
            : (aImg.base64 || aImg.url);

          apiContent.push({
            type: 'image_url',
            image_url: {
              url: imgUrl
            }
          });
        }
      } else {
        apiContent = trimmed;
      }

      this.apiMessagesHistory.push({
        role: 'user',
        content: apiContent
      });

      this.$emit('message', { role: 'user', content: apiContent });

      this.inputMsg = '';
      this.autoResizeInput();
      this.checkUserMsgHeights();
      this.isLoading = true;
      this.isStreaming = false;
      this.requestStartTime = Date.now();
      this.suggestedPrompts = [];
      this.suggestedTitle = '';
      this.saveToLocalStorage();

      // 2. NGAY LẬP TỨC thêm bong bóng tin nhắn Bot với hiệu ứng 3 chấm typing
      const botMessageId = Date.now() + 1;
      const botMsgObj = {
        id: botMessageId,
        sender: 'bot',
        text: '',
        isStreaming: true,
        responseTime: null,
        lang: null,
        thinking: '',
        versions: [],
        currentVersionIdx: 0
      };
      this.messageList.push(botMsgObj);
      this.scrollToBottom();

      // 3. Chuẩn bị danh sách messages gửi đi (áp dụng historyLimit để tiết kiệm token)
      const systemMsg = this.apiMessagesHistory[0] || { role: 'system', content: this.systemPrompt };
      const nonSystemHistory = this.apiMessagesHistory.filter(m => m.role !== 'system');
      const trimmedHistory = this.historyLimit > 0
        ? nonSystemHistory.slice(-this.historyLimit)
        : nonSystemHistory;
      const payloadMessages = [systemMsg, ...trimmedHistory];

      this.streamChatResponse(botMsgObj, payloadMessages, false);
    },
    async streamChatResponse(botMsgObj, payloadMessages, isRegeneration) {
      this.abortController = new AbortController();
      this.$set(botMsgObj, 'html', '');
      this.$set(botMsgObj, 'isStreaming', true);
      this.$set(botMsgObj, 'isError', false);
      this.isLoading = true;
      this.isStreaming = false;
      this.requestStartTime = Date.now();
      this.thinkingStartTime = null;

      let rawAccumulatedContent = '';

      try {
        const chatPayload = {
          messages: payloadMessages,
          model: this.effectiveModel,
          thinking: !!(this.isCurrentModelSupportThinking && this.isThinkingActive)
        };
        if (this.knowledgeBase && typeof this.knowledgeBase === 'string' && this.knowledgeBase.trim()) {
          chatPayload.knowledgeBase = this.knowledgeBase.trim();
        }

        const response = await this.callChatApi(chatPayload, this.abortController.signal);

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          throw new Error(errData.error || `HTTP ${response.status}: Server error`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop();

          for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine || !trimmedLine.startsWith('data:')) continue;
            const dataStr = trimmedLine.replace(/^data:\s*/, '');
            if (dataStr === '[DONE]') continue;

            try {
              const parsed = JSON.parse(dataStr);

              // --- Event đặc biệt: suggestions từ tool_call của server ---
              if (Array.isArray(parsed.__suggestions__) && parsed.__suggestions__.length > 0) {
                this.suggestedPrompts = parsed.__suggestions__;
                this.suggestedTitle = parsed.__suggestions_title__ || '';
                this.scrollToBottom();
                continue;
              }

              // --- Event đặc biệt: trạng thái Tool Calling từ Agent Backend ---
              if (parsed.__status__) {
                const st = parsed.__status__;
                if (st.type === 'tool_start') {
                  this.$set(botMsgObj, 'activeTool', {
                    name: st.tool,
                    message: st.message || `Đang gọi tool: ${st.tool}...`,
                    running: true
                  });
                } else if (st.type === 'tool_done') {
                  if (botMsgObj.activeTool) {
                    this.$set(botMsgObj.activeTool, 'running', false);
                    this.$set(botMsgObj.activeTool, 'executionTimeMs', st.executionTimeMs);
                  }
                }
                this.scrollToBottom();
                continue;
              }

              const choice = (parsed.choices && parsed.choices[0]) || null;
              const delta = (choice && choice.delta) || null;
              if (!delta) continue;

              // 1. Kiểm tra reasoning / thinking từ API delta
              const reasoningDelta = delta.reasoning_content || delta.reasoning || '';
              if (reasoningDelta) {
                if (this.isLoading) {
                  this.isLoading = false;
                  this.isStreaming = true;
                }
                this.thinkingStartTime = this.thinkingStartTime || Date.now();
                this.$set(botMsgObj, 'thinking', (botMsgObj.thinking || '') + reasoningDelta);
              }

              // 2. Nội dung text stream
              const contentDelta = delta.content || '';
              if (contentDelta) {
                if (this.isLoading) {
                  this.isLoading = false;
                  this.isStreaming = true;
                }

                if (botMsgObj.thinking && !botMsgObj.thinkingDuration) {
                  const durSec = Math.max(1, Math.round((Date.now() - (this.thinkingStartTime || this.requestStartTime)) / 1000));
                  this.$set(botMsgObj, 'thinkingDuration', `${durSec}s`);
                }

                rawAccumulatedContent += contentDelta;

                // Xử lý các model gửi thẻ suy nghĩ <think>...</think> trực tiếp trong content
                if (rawAccumulatedContent.indexOf('<think>') !== -1) {
                  const thinkStart = rawAccumulatedContent.indexOf('<think>') + 7;
                  const thinkEnd = rawAccumulatedContent.indexOf('</think>');
                  if (thinkEnd !== -1) {
                    const extractedThinking = rawAccumulatedContent.substring(thinkStart, thinkEnd).trim();
                    const mainText = rawAccumulatedContent.substring(thinkEnd + 8).trimStart();
                    this.$set(botMsgObj, 'thinking', extractedThinking);
                    this.$set(botMsgObj, 'text', mainText);
                  } else {
                    const currentThinking = rawAccumulatedContent.substring(thinkStart);
                    this.$set(botMsgObj, 'thinking', currentThinking);
                    this.$set(botMsgObj, 'text', '');
                  }
                } else {
                  this.$set(botMsgObj, 'text', (botMsgObj.text || '') + contentDelta);
                }

                this.throttleUpdateHtml(botMsgObj);
              }
            } catch (e) {
              // chunk json fragment
            }
          }
        }

        // Stream network kết thúc: hoàn tất hiển thị và trigger Mermaid
        this.finishStream(botMsgObj, isRegeneration);
      } catch (err) {
        if (this.typingTimer) {
          clearInterval(this.typingTimer);
          this.typingTimer = null;
        }
        if (this._htmlRafPending) {
          this._htmlRafPending = false;
        }

        if (err.name === 'AbortError' || (this.abortController && this.abortController.signal && this.abortController.signal.aborted)) {
          console.log('[UrChatbot] Đã dừng stream theo yêu cầu.');
          this.$set(botMsgObj, 'isStreaming', false);
          if (botMsgObj.text) {
            this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text, false));
          }
          this.isStreaming = false;
          this.isLoading = false;
          return;
        }

        console.error('[UrChatbot] Lỗi stream chat:', err);
        this.isLoading = false;
        this.isStreaming = false;
        this.isErrorState = true;
        this.$emit('error', err);

        let errorMessage = 'Unable to connect to Chatbot AI service.';
        if (err.message) {
          errorMessage = err.message;
        }

        if (botMsgObj.text) {
          this.$set(botMsgObj, 'text', botMsgObj.text + `\n\n⚠️ *Connection error: ${errorMessage}*`);
          this.$set(botMsgObj, 'html', this.renderHtml(botMsgObj.text, false));
          this.$set(botMsgObj, 'isStreaming', false);
        } else {
          this.$set(botMsgObj, 'isError', true);
          this.$set(botMsgObj, 'isStreaming', false);
          this.$set(botMsgObj, 'text', `⚠️ ${errorMessage}`);
          this.$set(botMsgObj, 'html', this.renderHtml(`⚠️ ${errorMessage}`, false));
        }
      } finally {
        this.abortController = null;
        this.scrollToBottom();
        this.saveToLocalStorage();
      }
    }
  }
};
</script>

<style lang="scss">
/* =========================================================
   ANIMATIONS KEYFRAMES
   ========================================================= */
@keyframes urChatbotCursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes urChatbotPulseTool {
  0% { transform: scale(0.9); opacity: 0.7; }
  100% { transform: scale(1.25); opacity: 1; }
}

.ur-chatbot-tool-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(0, 240, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  font-size: 12px;
  color: #00f0ff;
  margin-bottom: 8px;
  box-shadow: 0 2px 10px rgba(0, 240, 255, 0.1);

  &.is-running {
    background: rgba(245, 158, 11, 0.1);
    border-color: rgba(245, 158, 11, 0.4);
    color: #fbbf24;
  }

  .ur-chatbot-tool-spinner {
    display: inline-block;
    animation: urChatbotPulseTool 0.8s infinite alternate;
  }

  .ur-chatbot-tool-check {
    color: #10b981;
    font-weight: bold;
  }

  .ur-chatbot-tool-msg {
    font-size: 12px;
    font-weight: 500;
  }
}

@keyframes urChatbotPulseAura {
  0%, 100% { transform: scale(1); opacity: 0.25; }
  50% { transform: scale(1.15); opacity: 0.5; }
}

@keyframes urChatbotAstroFloatMotion {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-7px) rotate(2.5deg); }
  66% { transform: translateY(3px) rotate(-1.5deg); }
}

@keyframes urChatbotEyeBlink {
  0%, 94%, 100% { transform: scaleY(1); }
  97% { transform: scaleY(0.08); }
}

@keyframes urChatbotOccasionalSignal {
  0% { r: 3.5px; opacity: 0.9; stroke-width: 2; }
  12% { r: 16px; opacity: 0; stroke-width: 0.5; }
  15% { r: 3.5px; opacity: 0.9; stroke-width: 2; }
  27% { r: 16px; opacity: 0; stroke-width: 0.5; }
  28%, 100% { opacity: 0; }
}

@keyframes urChatbotAstroArmLeft {
  0%, 100% { transform: rotate(15deg); }
  50% { transform: rotate(35deg); }
}

@keyframes urChatbotAstroArmRight {
  0%, 100% { transform: rotate(-25deg); }
  50% { transform: rotate(-5deg); }
}

@keyframes urChatbotAstroLegLeft {
  0%, 100% { transform: rotate(-10deg); }
  50% { transform: rotate(15deg); }
}

@keyframes urChatbotAstroLegRight {
  0%, 100% { transform: rotate(20deg); }
  50% { transform: rotate(-5deg); }
}

@keyframes urChatbotAstroErrorShake {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  15% { transform: translateY(-3px) rotate(-3deg); }
  30% { transform: translateY(2px) rotate(2deg); }
  45% { transform: translateY(-2px) rotate(-1deg); }
  60% { transform: translateY(1px) rotate(1deg); }
  80% { transform: translateY(-1px) rotate(-0.5deg); }
}

@keyframes urChatbotCoreWarningBlink {
  0%, 100% { opacity: 1; transform: scale(1); filter: drop-shadow(0 0 4px #ef4444); }
  50% { opacity: 0.25; transform: scale(0.7); filter: none; }
}

@keyframes urChatbotAntennaErrorWave {
  0% { r: 3.5px; opacity: 1; stroke-width: 2.5; }
  50% { r: 16px; opacity: 0.2; stroke-width: 1; }
  100% { r: 24px; opacity: 0; stroke-width: 0.2; }
}

@keyframes urChatbotSweatDropFlow {
  0%, 20% { transform: translateY(0); opacity: 0; }
  35% { opacity: 0.9; }
  80% { transform: translateY(8px); opacity: 0.7; }
  100% { transform: translateY(12px); opacity: 0; }
}

@keyframes urChatbotEyeErrorFlicker {
  0%, 100% { opacity: 1; }
  40% { opacity: 0.3; }
  45% { opacity: 1; }
  85% { opacity: 0.4; }
}

@keyframes urChatbotBinaryStreamLeft {
  0% { transform: translateX(0); }
  100% { transform: translateX(-40px); }
}

@keyframes urChatbotBinaryStreamRight {
  0% { transform: translateX(-40px); }
  100% { transform: translateX(0); }
}

@keyframes urChatbotLaserSweep {
  0% { transform: translateX(0); opacity: 0.3; }
  50% { transform: translateX(48px); opacity: 0.95; }
  100% { transform: translateX(0); opacity: 0.3; }
}

@keyframes urChatbotCyberEyePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

@keyframes urChatbotCorePulseFast {
  0%, 100% { transform: scale(1); opacity: 1; filter: drop-shadow(0 0 6px #00f0ff); }
  50% { transform: scale(1.35); opacity: 0.7; filter: drop-shadow(0 0 12px #00f0ff); }
}

@keyframes urChatbotTypingBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.35; }
  40% { transform: scale(1.2); opacity: 1; }
}

/* Aliases for keyframes */
@keyframes pulseAura { 0%, 100% { transform: scale(1); opacity: 0.25; } 50% { transform: scale(1.15); opacity: 0.5; } }
@keyframes astroFloatMotion { 0%, 100% { transform: translateY(0px) rotate(0deg); } 33% { transform: translateY(-7px) rotate(2.5deg); } 66% { transform: translateY(3px) rotate(-1.5deg); } }
@keyframes eyeBlink { 0%, 94%, 100% { transform: scaleY(1); } 97% { transform: scaleY(0.08); } }
@keyframes occasionalSignal { 0% { r: 3.5px; opacity: 0.9; stroke-width: 2; } 12% { r: 16px; opacity: 0; stroke-width: 0.5; } 15% { r: 3.5px; opacity: 0.9; stroke-width: 2; } 27% { r: 16px; opacity: 0; stroke-width: 0.5; } 28%, 100% { opacity: 0; } }
@keyframes astroArmLeft { 0%, 100% { transform: rotate(15deg); } 50% { transform: rotate(35deg); } }
@keyframes astroArmRight { 0%, 100% { transform: rotate(-25deg); } 50% { transform: rotate(-5deg); } }
@keyframes astroLegLeft { 0%, 100% { transform: rotate(-10deg); } 50% { transform: rotate(15deg); } }
@keyframes astroLegRight { 0%, 100% { transform: rotate(20deg); } 50% { transform: rotate(-5deg); } }
@keyframes astroErrorShake { 0%, 100% { transform: translateY(0px) rotate(0deg); } 15% { transform: translateY(-3px) rotate(-3deg); } 30% { transform: translateY(2px) rotate(2deg); } 45% { transform: translateY(-2px) rotate(-1deg); } 60% { transform: translateY(1px) rotate(1deg); } 80% { transform: translateY(-1px) rotate(-0.5deg); } }
@keyframes coreWarningBlink { 0%, 100% { opacity: 1; transform: scale(1); filter: drop-shadow(0 0 4px #ef4444); } 50% { opacity: 0.25; transform: scale(0.7); filter: none; } }
@keyframes antennaErrorWave { 0% { r: 3.5px; opacity: 1; stroke-width: 2.5; } 50% { r: 16px; opacity: 0.2; stroke-width: 1; } 100% { r: 24px; opacity: 0; stroke-width: 0.2; } }
@keyframes sweatDropFlow { 0%, 20% { transform: translateY(0); opacity: 0; } 35% { opacity: 0.9; } 80% { transform: translateY(8px); opacity: 0.7; } 100% { transform: translateY(12px); opacity: 0; } }
@keyframes eyeErrorFlicker { 0%, 100% { opacity: 1; } 40% { opacity: 0.3; } 45% { opacity: 1; } 85% { opacity: 0.4; } }
@keyframes binaryStreamLeft { 0% { transform: translateX(0); } 100% { transform: translateX(-40px); } }
@keyframes binaryStreamRight { 0% { transform: translateX(-40px); } 100% { transform: translateX(0); } }
@keyframes laserSweep { 0% { transform: translateX(0); opacity: 0.3; } 50% { transform: translateX(48px); opacity: 0.95; } 100% { transform: translateX(0); opacity: 0.3; } }
@keyframes cyberEyePulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.15); } }
@keyframes corePulseFast { 0%, 100% { transform: scale(1); opacity: 1; filter: drop-shadow(0 0 6px #00f0ff); } 50% { transform: scale(1.35); opacity: 0.7; filter: drop-shadow(0 0 12px #00f0ff); } }
@keyframes typingBounce { 0%, 80%, 100% { transform: scale(0.6); opacity: 0.35; } 40% { transform: scale(1.2); opacity: 1; } }

/* =========================================================
   ROOT WRAPPER & ISOLATION NAMESPACE
   ========================================================= */
.ur-chatbot-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 9999;
  user-select: none;
  font-family: "Noto Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji";
  pointer-events: none;
  box-sizing: border-box;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  --ucb-sb-size: 8px;
  --ucb-sb-track: #f1f5f9;
  --ucb-sb-thumb: rgba(2, 132, 199, 0.35);
  --ucb-sb-thumb-hover: #0284c7;
  --ucb-sb-radius: 6px;

  &.is-fullscreen {
    position: fixed !important;
    inset: 0 !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100% !important;
    height: 100% !important;
    max-width: 100% !important;
    max-height: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    z-index: 99999 !important;
    align-items: stretch;
    overflow: hidden !important;
  }

  // =========================================================
  // BẢO VỆ & CÔ LẬP CSS CHỐNG XUNG ĐỘT TOÀN CỤC (ANT DESIGN, ELEMENT UI, BOOTSTRAP, SDC)
  // =========================================================
  *, *::before, *::after {
    box-sizing: border-box !important;
    -webkit-font-smoothing: antialiased !important;
    -moz-osx-font-smoothing: grayscale !important;
    text-rendering: optimizeLegibility !important;
  }

  button {
    margin: 0;
    padding: 0;
    border: none;
    background: transparent;
    font-family: inherit;
    font-size: inherit;
    line-height: 1;
    color: inherit;
    text-transform: none;
    text-shadow: none;
    box-shadow: none;
    outline: none;
    cursor: pointer;
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    white-space: nowrap;

    &:focus {
      outline: none;
    }
  }

  input:not([type="checkbox"]):not([type="radio"]), textarea {
    margin: 0;
    font-family: inherit;
    font-size: inherit;
    line-height: inherit;
    color: inherit;
    box-shadow: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
  }

  input[type="checkbox"] {
    -webkit-appearance: checkbox !important;
    -moz-appearance: checkbox !important;
    appearance: checkbox !important;
    accent-color: #0284c7 !important;
    width: 15px !important;
    height: 15px !important;
    min-width: 15px !important;
    min-height: 15px !important;
    margin: 0 6px 0 0 !important;
    padding: 0 !important;
    display: inline-block !important;
    vertical-align: middle !important;
    cursor: default !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  // Khóa bảo vệ SVG đa sắc của Astro Bot chống bị ghi đè fill currentColor
  .ur-chatbot-topbar-bot,
  .ur-chatbot-zerog-svg,
  .ur-chatbot-trigger-svg {
    overflow: visible;
  }

  /* ---------------------------------------------------------
     1. CỬA SỔ CHAT (WINDOW)
     --------------------------------------------------------- */
  .ur-chatbot-window,
  .chat-window {
    position: relative;
    width: 440px;
    max-width: calc(100vw - 36px);
    height: 580px;
    max-height: calc(100vh - 80px);
    margin-bottom: 0;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 12px 40px rgba(15, 23, 42, 0.14), 0 0 0 1px rgba(226, 232, 240, 0.9);
    display: flex;
    flex-direction: column;
    overflow: visible;
    opacity: 0;
    transform: translateY(20px) scale(0.92);
    pointer-events: none;
    transition: width 0.35s cubic-bezier(0.34, 1.25, 0.64, 1),
                opacity 0.35s ease,
                transform 0.35s cubic-bezier(0.34, 1.25, 0.64, 1);

    &.open,
    &.is-open {
      opacity: 1;
      transform: translateY(0) scale(1);
      pointer-events: auto;
    }

    &.expanded,
    &.is-expanded {
      width: 1024px;
      max-width: calc(100vw - 40px);
    }

    &.fullscreen {
      position: fixed !important;
      inset: 0 !important;
      top: 0 !important;
      left: 0 !important;
      right: 0 !important;
      bottom: 0 !important;
      width: 100% !important;
      height: 100% !important;
      max-width: 100% !important;
      max-height: 100% !important;
      border-radius: 0 !important;
      box-shadow: none !important;
      margin: 0 !important;
      transform: none !important;
      z-index: 10000;
      overflow: hidden;

      .ur-chatbot-header,
      .chat-header {
        border-radius: 0;
        padding-left: 16px;
        padding-right: 16px;

        @media (min-width: 840px) {
          padding-left: calc((100vw - 800px) / 2) !important;
          padding-right: calc((100vw - 800px) / 2) !important;
        }
      }

      .ur-chatbot-topbar-bot,
      .bottom-topbar-bot {
        opacity: 0 !important;
        visibility: hidden !important;
        pointer-events: none !important;
      }

      .ur-chatbot-body,
      .chat-body {
        padding-left: 16px;
        padding-right: 16px;

        @media (min-width: 840px) {
          padding-left: calc((100vw - 800px) / 2) !important;
          padding-right: calc((100vw - 800px) / 2) !important;
        }
      }

      .ur-chatbot-footer,
      .chat-footer {
        border-radius: 0;
        padding-left: 16px;
        padding-right: 16px;

        @media (min-width: 840px) {
          padding-left: calc((100vw - 800px) / 2) !important;
          padding-right: calc((100vw - 800px) / 2) !important;
        }
      }
    }
  }

  /* ---------------------------------------------------------
     2. BOT SVG TOPBAR (ASTRONAUT HOVERING)
     --------------------------------------------------------- */
  .ur-chatbot-topbar-bot,
  .bottom-topbar-bot {
    position: absolute;
    top: -30px;
    left: 14px;
    width: 100px;
    height: 100px;
    z-index: 120; // Nổi lên trên lớp nền blur của confirm modal (z-index 100)
    pointer-events: none;
    overflow: visible;

    &.bot-error-state {
      pointer-events: auto !important;
      cursor: pointer;
    }
  }

  /* ---------------------------------------------------------
     2B. BOT PHI HÀNH GIA ZERO-GRAVITY INTERACTIVE
     --------------------------------------------------------- */
  .ur-chatbot-zerog-bot {
    position: absolute;
    top: 0;
    left: 0;
    width: 96px;
    height: 96px;
    z-index: 25;
    user-select: none;
    touch-action: none;
    transition: filter 0.2s ease;
    filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.22)) drop-shadow(0 2px 6px rgba(0, 0, 0, 0.1));

    &:hover {
      filter: drop-shadow(0 12px 28px rgba(0, 240, 255, 0.55)) drop-shadow(0 4px 10px rgba(0, 0, 0, 0.15));
    }

    &.bot-error-state {
      filter: drop-shadow(0 10px 24px rgba(239, 68, 68, 0.45)) drop-shadow(0 2px 6px rgba(0, 0, 0, 0.1));
      cursor: pointer !important;

      &:hover {
        filter: drop-shadow(0 12px 28px rgba(239, 68, 68, 0.7)) drop-shadow(0 4px 10px rgba(0, 0, 0, 0.15));
      }
    }

    .ur-chatbot-zerog-svg {
      width: 100%;
      height: 100%;
      pointer-events: none;
      overflow: visible;
    }
  }

  /* ---------------------------------------------------------
     3. THANH TOPBAR HEADER
     --------------------------------------------------------- */
  .ur-chatbot-header,
  .chat-header {
    position: relative;
    padding: 16px 18px 16px 110px;
    background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 55%, #e0f2fe 100%);
    border-radius: 12px 12px 0 0;
    color: #1e293b;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 10;
    box-shadow: 0 2px 10px rgba(2, 132, 199, 0.08);
    border-bottom: 1px solid #e2e8f0;
  }

  .ur-chatbot-header-info,
  .chat-header-info {
    h4 {
      margin: 0;
      font-size: 15px;
      font-weight: 700;
      line-height: 1.2;
      color: #0284c7;
      text-shadow: none;
    }

    span {
      font-size: 11px;
      opacity: 0.9;
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 2px;
      color: #64748b;
    }
  }

  .ur-chatbot-online-indicator,
  .online-indicator {
    width: 7px;
    height: 7px;
    background-color: #10b981;
    border-radius: 50%;
    display: inline-block;
    box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
    transition: all 0.3s ease;

    &.busy {
      background-color: #0284c7;
      box-shadow: 0 0 8px #0284c7;
      animation: pulseAura 1.2s infinite;
    }

    &.error {
      background-color: #ef4444;
      box-shadow: 0 0 8px #ef4444;
      animation: pulseAura 0.8s infinite;
    }
  }

  .ur-chatbot-header-actions,
  .header-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .ur-chatbot-btn-header-action,
  .btn-header-action {
    background: #ffffff !important;
    border: 1px solid #e2e8f0 !important;
    width: 30px !important;
    height: 30px !important;
    min-width: 30px !important;
    max-width: 30px !important;
    min-height: 30px !important;
    max-height: 30px !important;
    border-radius: 6px !important;
    cursor: pointer;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    color: #475569 !important;
    transition: all 0.2s ease;
    padding: 0 !important;
    margin: 0 !important;
    line-height: 1 !important;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

    &:hover:not(:disabled) {
      background: #f0f9ff !important;
      border-color: #0284c7 !important;
      color: #0284c7 !important;
      transform: translateY(-1px);
    }

    &:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }
  }

  .ur-chatbot-btn-close,
  .btn-close-chat {
    background: #ffffff !important;
    border: 1px solid #e2e8f0 !important;
    color: #64748b !important;
    width: 30px !important;
    height: 30px !important;
    min-width: 30px !important;
    max-width: 30px !important;
    min-height: 30px !important;
    max-height: 30px !important;
    border-radius: 6px !important;
    cursor: pointer;
    font-size: 14px;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    transition: all 0.2s ease;
    padding: 0 !important;
    margin: 0 !important;
    line-height: 1 !important;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

    &:hover {
      background: #fee2e2 !important;
      border-color: #fca5a5 !important;
      color: #ef4444 !important;
      transform: translateY(-1px);
    }
  }

  /* ---------------------------------------------------------
     4. KHUNG CHAT BODY & VIRTUAL SCROLL
     --------------------------------------------------------- */
  .ur-chatbot-body,
  .chat-body {
    flex: 1;
    padding: 0 16px 16px 16px;
    overflow-y: auto;
    background-color: #f8fafc;
    display: flex;
    flex-direction: column;
    gap: 14px;
    position: relative;
    z-index: 5;
    user-select: text;

    > .ur-chatbot-msg-bubble:first-child,
    > .msg-bubble:first-child {
      margin-top: 16px;
    }

    scrollbar-width: thin;
    scrollbar-color: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35)) var(--ucb-sb-track, #f1f5f9);

    &::-webkit-scrollbar {
      width: var(--ucb-sb-size, 8px);
    }
    &::-webkit-scrollbar-track {
      background: var(--ucb-sb-track, #f1f5f9);
      border-radius: var(--ucb-sb-radius, 6px);
    }
    &::-webkit-scrollbar-thumb {
      background: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35));
      border-radius: var(--ucb-sb-radius, 6px);
      &:hover {
        background: var(--ucb-sb-thumb-hover, #0284c7);
      }
    }
    &::-webkit-scrollbar-button {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
    }
  }

  .ur-chatbot-virtual-history,
  .virtual-history-wrapper {
    display: flex;
    justify-content: center;
    padding: 10px 0 4px 0;
    width: 100%;
  }

  .ur-chatbot-btn-load-earlier,
  .btn-load-earlier {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 14px;
    background: #ffffff;
    border: 1px solid #cbd5e1;
    border-radius: 20px;
    color: #475569;
    font-size: 11.5px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

    &:hover {
      background: #f1f5f9;
      color: #0284c7;
      border-color: #0284c7;
      transform: translateY(-1px);
    }
  }

  /* ---------------------------------------------------------
     5. BONG BÓNG TIN NHẮN (MESSAGE BUBBLES)
     --------------------------------------------------------- */
  .ur-chatbot-msg-bubble,
  .msg-bubble {
    max-width: 92%;
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 13.5px;
    line-height: 1.6;
    word-break: break-word;
    position: relative;

    &.ur-chatbot-msg-user,
    &.msg-user {
      align-self: flex-end;
      background: #eef5fc;
      color: #0f172a;
      border: 1px solid #cbe0fa;
      border-radius: 18px;
      border-bottom-right-radius: 4px;
      box-shadow: 0 1px 4px rgba(15, 23, 42, 0.05);
    }

    &.ur-chatbot-msg-bot,
    &.msg-bot {
      align-self: flex-start;
      background-color: #ffffff;
      color: #1e293b;
      border-radius: 18px;
      border-bottom-left-radius: 4px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    }

    &.ur-chatbot-msg-error,
    &.msg-error {
      background-color: #fef2f2 !important;
      color: #b91c1c !important;
      border: 1px solid #fecaca !important;

      &::before {
        background: #fef2f2 !important;
      }
    }
  }

  .ur-chatbot-msg-user-wrapper {
    position: relative;
    width: 100%;
  }

  .ur-chatbot-msg-user-text,
  .msg-user-text {
    white-space: pre-wrap;
    word-break: break-word;
    tab-size: 2;
    font-family: inherit;
    line-height: 1.6;

    &.is-collapsed {
      max-height: 72px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      position: relative;
      mask-image: linear-gradient(180deg, #000 40%, rgba(0, 0, 0, 0.2) 85%, transparent 100%);
      -webkit-mask-image: linear-gradient(180deg, #000 40%, rgba(0, 0, 0, 0.2) 85%, transparent 100%);
    }
  }

  .ur-chatbot-user-expand-row {
    display: flex;
    justify-content: center;
    margin-top: -14px;
    position: relative;
    z-index: 2;
    height: 18px;
    pointer-events: auto;

    // Gradient fade overlay phía trên nút chevron, tạo hiệu ứng mờ dần
    // giúp icon không bị hoà vào dòng chữ
    &::before {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 32px; // Chiều cao của vùng fade phía trên nút
      background: linear-gradient(
        to bottom,
        transparent 0%,
        rgba(238, 245, 252, 0.85) 55%,
        rgba(238, 245, 252, 0.97) 100%
      );
      pointer-events: none;
      z-index: -1;
      border-radius: 0 0 18px 18px; // Khớp với border-radius bubble user
    }
  }

  .ur-chatbot-btn-user-expand {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0 !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
    box-shadow: none !important;
    color: #0284c7;
    cursor: pointer;
    transition: all 0.2s ease;
    opacity: 0.75;

    &:hover {
      background: transparent !important;
      border: none !important;
      color: #0369a1;
      opacity: 1;
      transform: scale(1.15);
    }

    .ur-chatbot-icon-chevron {
      transition: transform 0.25s ease;
      &.is-expanded {
        transform: rotate(180deg);
      }
    }
  }

  .ur-chatbot-user-actions-toolbar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 4px;
    margin-top: 6px;
    padding-top: 4px;
    border-top: 1px solid rgba(15, 23, 42, 0.06);
    user-select: none;
  }

  .ur-chatbot-msg-bot-wrapper,
  .msg-bot-wrapper {
    position: relative;
  }

  /* ---------------------------------------------------------
     KHỐI SUY NGHĨ (THINKING / REASONING BLOCK) - MODERN LIST STYLE
     --------------------------------------------------------- */
  .ur-chatbot-thinking-block {
    margin-bottom: 14px;
    font-size: 13px;
    user-select: text;
    transition: all 0.2s ease;

    &.is-collapsed {
      margin-bottom: 8px;
    }
  }

  .ur-chatbot-thinking-header {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    user-select: none;
    padding: 2px 0;
    color: #0284c7;
    font-weight: 600;
    font-size: 12.5px;
    line-height: 1.4;
    transition: opacity 0.15s ease, color 0.15s ease;

    &:hover {
      opacity: 0.85;
    }

    .ur-chatbot-thinking-title {
      letter-spacing: 0.2px;
    }

    .ur-chatbot-thinking-chevron {
      transition: transform 0.22s cubic-bezier(0.4, 0, 0.2, 1);
      color: #64748b;
      flex-shrink: 0;

      &.is-collapsed {
        transform: rotate(180deg);
      }
    }
  }

  .ur-chatbot-thinking-content {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 4px 0 6px 2px;
  }

  .ur-chatbot-thinking-step-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }

  .ur-chatbot-thinking-bullet {
    color: #64748b;
    font-size: 16px;
    line-height: 1.25;
    user-select: none;
    flex-shrink: 0;
  }

  .ur-chatbot-thinking-step-body {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
  }

  .ur-chatbot-thinking-step-title {
    font-weight: 600;
    font-size: 13px;
    line-height: 1.4;
    color: #0f172a;
    letter-spacing: 0.15px;
  }

  .ur-chatbot-thinking-step-desc {
    font-size: 12.5px;
    line-height: 1.55;
    color: #64748b;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  }

  .ur-chatbot-actions-toolbar,
  .msg-actions-toolbar {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 10px;
    padding-top: 6px;
    border-top: 1px solid #f1f5f9;
    user-select: none;
  }

  /* ---------------------------------------------------------
     NÚT PHÂN TRANG PHIÊN BẢN CÂU TRẢ LỜI (< 1 / 2 >)
     --------------------------------------------------------- */
  .ur-chatbot-version-nav {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    margin-right: 4px;
    user-select: none;
  }

  .ur-chatbot-btn-version-nav {
    background: transparent;
    border: none;
    color: #8c959f;
    padding: 3px 4px;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease;

    &:hover:not(:disabled) {
      background: #f1f5f9;
      color: #1e293b;
    }

    &:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }
  }

  .ur-chatbot-version-label {
    font-size: 11px;
    font-weight: 600;
    color: #64748b;
    padding: 0 3px;
    letter-spacing: 0.3px;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
    white-space: nowrap;
  }

  .ur-chatbot-btn-action,
  .btn-msg-action {
    background: transparent;
    border: none;
    color: #8c959f;
    padding: 4px 6px;
    border-radius: 5px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease;

    &:hover {
      background: #f1f5f9;
      color: #1e293b;
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }

    // Trạng thái đang đọc to
    &.is-speaking {
      color: #0284c7;
      background: rgba(2, 132, 199, 0.08);
      animation: urChatbotSpeakPulse 1.4s ease-in-out infinite;

      &:hover {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
      }
    }
  }

  @keyframes urChatbotSpeakPulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.55; }
  }

  .ur-chatbot-duration-badge,
  .msg-duration-badge {
    font-size: 11px;
    color: #94a3b8;
    margin-left: 6px;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
  }

  /* ---------------------------------------------------------
     6. NÚT SCROLL TO BOTTOM
     --------------------------------------------------------- */
  .ur-chatbot-btn-scroll-bottom,
  .btn-scroll-bottom {
    position: absolute;
    bottom: calc(100% + 10px);
    left: 50%;
    transform: translateX(-50%);
    background: #ffffff;
    border: 1px solid #d0d7de;
    border-radius: 50% !important;
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
    min-height: 34px !important;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #57606a;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
    z-index: 25;
    transition: all 0.2s cubic-bezier(0.34, 1.25, 0.64, 1);
    flex-shrink: 0;
    padding: 0;

    &:hover {
      background: #f6f8fa;
      color: #0284c7;
      border-color: #0284c7;
      transform: translateX(-50%) translateY(-2px) scale(1.05);
      box-shadow: 0 6px 18px rgba(2, 132, 199, 0.25);
    }
  }

  /* ---------------------------------------------------------
     7. TYPING INDICATOR
     --------------------------------------------------------- */
  .ur-chatbot-typing-indicator,
  .msg-typing-indicator {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 4px 6px;
    min-height: 24px;
  }

  .ur-chatbot-typing-dots,
  .typing-dots {
    display: inline-flex;
    align-items: center;
    gap: 4.5px;
  }

  .ur-chatbot-dot,
  .dot {
    width: 6.5px;
    height: 6.5px;
    border-radius: 50%;
    background-color: #0284c7;
    box-shadow: 0 0 6px rgba(2, 132, 199, 0.4);
    animation: typingBounce 1.3s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
    &:nth-child(3) { animation-delay: 0s; }
  }

  /* ---------------------------------------------------------
     8. SUGGESTED PROMPTS
     --------------------------------------------------------- */
  .ur-chatbot-suggested-prompts,
  .suggested-prompts-in-body {
    align-self: flex-start;
    margin-top: 4px;
    margin-bottom: 8px;
    max-width: 92%;
    z-index: 6;
  }

  .ur-chatbot-suggested-title {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    padding-left: 2px;
    font-size: 12.5px;
    font-weight: 600;
    color: #475569;
    line-height: 1.4;
    user-select: text;

    .ur-chatbot-suggested-title-icon {
      flex-shrink: 0;
      color: #0284c7;
    }
  }

  .ur-chatbot-suggested-track,
  .suggested-prompts-track {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .ur-chatbot-prompt-pill,
  .prompt-pill {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    color: #334155;
    border-radius: 6px;
    padding: 6px 10px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    width: fit-content;
    max-width: 100%;
    box-sizing: border-box;
    transition: all 0.15s ease;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

    &:hover {
      background: #f1f5f9;
      border-color: #0284c7;
      color: #0284c7;

      .ur-chatbot-prompt-pill-arrow,
      .prompt-pill-arrow {
        color: #0284c7;
        transform: translateX(2px);
      }
    }
  }

  .ur-chatbot-prompt-pill-text,
  .prompt-pill-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: left;
  }

  .ur-chatbot-prompt-pill-arrow,
  .prompt-pill-arrow {
    flex-shrink: 0;
    color: #94a3b8;
    transition: transform 0.15s ease, color 0.15s ease;
  }

  /* ---------------------------------------------------------
     8b. ATTACHED IMAGES (USER BUBBLE)
     --------------------------------------------------------- */
  .ur-chatbot-user-imgs-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;

    .ur-chatbot-user-img-card {
      position: relative;
      width: 58px;
      height: 58px;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      border: 1px solid #cbe0fa;
      background: #ffffff;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
      transition: transform 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;

      &:hover {
        transform: translateY(-1px) scale(1.03);
        border-color: #93c5fd;
        box-shadow: 0 3px 8px rgba(2, 132, 199, 0.15);
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }
  }

  /* ---------------------------------------------------------
     9. CHAT FOOTER & GEMINI-STYLE INPUT CARD
     --------------------------------------------------------- */
  .ur-chatbot-footer,
  .chat-footer {
    position: relative;
    padding: 10px 14px 14px 14px;
    background-color: #ffffff;
    border-top: 1px solid #e2e8f0;
    border-radius: 0 0 12px 12px;
    display: flex;
    flex-direction: column;
    z-index: 10;
    margin: 0;
    box-sizing: border-box;
    transition: background-color 0.2s ease;

    &.is-dragover {
      .ur-chatbot-input-card {
        border-color: #0284c7 !important;
        border-style: dashed !important;
        background-color: #f0f9ff !important;
        box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.15) !important;
      }
    }

    .ur-chatbot-input-card {
      background: #f8fafc;
      border: 1px solid #d0d7de;
      border-radius: 20px;
      padding: 10px 14px 8px 14px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
      transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;

      &:focus-within {
        background: #ffffff;
        border-color: #0284c7;
        box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.12);
      }

      .ur-chatbot-attachments-strip {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        padding-bottom: 4px;
        animation: ucbFadeSlideDown 0.18s cubic-bezier(0.16, 1, 0.3, 1);
      }

      .ur-chatbot-thumb-box {
        position: relative;
        width: 48px;
        height: 48px;
        border-radius: 8px;
        background: #ffffff;
        border: 1px solid #d0d7de;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        overflow: hidden;
        flex-shrink: 0;
        transition: transform 0.15s ease, border-color 0.15s ease;

        &:hover {
          transform: translateY(-1px);
          border-color: #0284c7;
        }

        &.is-uploading {
          border-style: dashed;
          border-color: #0284c7;
        }

        &.is-error {
          border-color: #ef4444;
          box-shadow: 0 1px 4px rgba(239, 68, 68, 0.2);
        }

        .ur-chatbot-thumb-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
          cursor: pointer;
        }

        .ur-chatbot-thumb-loader {
          position: absolute;
          inset: 0;
          background: rgba(255, 255, 255, 0.75);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #0284c7;

          .ur-chatbot-thumb-spin-icon {
            width: 20px;
            height: 20px;
            animation: spin 1s linear infinite;
          }
        }

        .ur-chatbot-thumb-btn-remove {
          position: absolute;
          top: 2px;
          right: 2px;
          width: 16px;
          height: 16px;
          border-radius: 50%;
          background: rgba(239, 68, 68, 0.92);
          color: #ffffff;
          border: none;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          padding: 0;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
          transition: transform 0.15s ease, background 0.15s ease;
          z-index: 5;

          &:hover:not(:disabled) {
            background: #dc2626;
            transform: scale(1.15);
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }

      .ur-chatbot-input,
      input,
      textarea {
        width: 100%;
        padding: 4px 0 !important;
        background: transparent !important;
        border: none !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        outline: none !important;
        color: #0f172a !important;
        font-size: 13.5px !important;
        line-height: 1.5 !important;
        font-family: inherit !important;
        resize: none !important;
        min-height: 30px !important;
        max-height: 120px !important;
        tab-size: 2;
        white-space: pre-wrap;
        word-break: break-word;
        overflow-y: hidden;
        box-sizing: border-box !important;

        &::placeholder {
          color: #94a3b8;
        }

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        scrollbar-width: thin;
        scrollbar-color: rgba(148, 163, 184, 0.5) transparent;

        &::-webkit-scrollbar {
          width: 5px;
        }
        &::-webkit-scrollbar-track {
          background: transparent;
        }
        &::-webkit-scrollbar-thumb {
          background: rgba(148, 163, 184, 0.5);
          border-radius: 6px;
        }
      }

      .ur-chatbot-card-bottom-bar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        margin-top: 2px;
      }

      .ur-chatbot-card-bottom-left {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .ur-chatbot-card-bottom-right {
        display: flex;
        align-items: center;
        gap: 6px;
        position: relative;
      }

      .ur-chatbot-btn-thinking-toggle {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: none;
        background: transparent;
        color: #94a3b8;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.18s ease;
        padding: 0;

        svg {
          flex-shrink: 0;
          color: inherit;
        }

        &:hover:not(:disabled) {
          background: #f1f5f9;
          color: #475569;
        }

        &.is-active:not(:disabled) {
          color: #0284c7;

          &:hover {
            background: #e0f2fe;
            color: #0369a1;
          }
        }

        &:disabled {
          opacity: 0.35;
          color: #cbd5e1;
          cursor: not-allowed;
          background: transparent;
        }
      }

      .ur-chatbot-model-select-wrapper {
        position: relative;
      }

      .ur-chatbot-model-select-btn {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        height: 28px;
        padding: 0 8px 0 10px;
        border-radius: 14px;
        border: none;
        background: transparent;
        color: #334155;
        font-size: 12px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.18s ease;
        user-select: none;

        &:hover:not(:disabled) {
          background: #f1f5f9;
          color: #0f172a;
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .ur-chatbot-model-select-name {
          max-width: 140px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          letter-spacing: -0.1px;
        }

        .ur-chatbot-model-chevron {
          transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          color: #64748b;
          flex-shrink: 0;

          &.is-open {
            transform: rotate(180deg);
          }
        }
      }

      .ur-chatbot-model-menu {
        position: absolute;
        bottom: calc(100% + 10px);
        right: 0;
        width: 280px;
        max-width: calc(100vw - 48px);
        background: rgba(255, 255, 255, 0.98);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        box-shadow: 0 14px 34px -4px rgba(15, 23, 42, 0.16), 0 4px 12px -2px rgba(15, 23, 42, 0.08);
        padding: 5px;
        z-index: 120;
        user-select: none;
      }

      .ur-chatbot-model-menu-list {
        display: flex;
        flex-direction: column;
        gap: 2px;
        max-height: 240px;
        overflow-y: auto;
      }

      .ur-chatbot-model-option {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 10px;
        border-radius: 9px;
        cursor: pointer;
        transition: all 0.15s ease;
        border: 1px solid transparent;

        &:hover {
          background: #f8fafc;
        }

        &.is-selected {
          background: #eff6ff;
          border-color: #bfdbfe;

          .ur-chatbot-model-option-name {
            color: #1d4ed8;
            font-weight: 600;
          }
        }

        .ur-chatbot-model-option-main {
          display: flex;
          flex-direction: column;
          gap: 2px;
          flex: 1;
          min-width: 0;
        }

        .ur-chatbot-model-option-name {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12.5px;
          font-weight: 500;
          color: #0f172a;
        }

        .ur-chatbot-model-option-desc {
          font-size: 11px;
          color: #64748b;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .ur-chatbot-model-tag {
          font-size: 9.5px;
          font-weight: 600;
          padding: 1px 5px;
          border-radius: 8px;
          text-transform: uppercase;
          letter-spacing: 0.3px;

          &.badge-reasoning {
            background: #f3e8ff;
            color: #7e22ce;
          }
          &.badge-vision {
            background: #e0f2fe;
            color: #0284c7;
          }
          &.badge-fast {
            background: #dcfce7;
            color: #15803d;
          }
          &.badge-default {
            background: #f1f5f9;
            color: #475569;
          }
        }

        .ur-chatbot-model-check-icon {
          flex-shrink: 0;
          margin-left: 6px;
        }
      }

      /* Animation dropdown popover */
      .ur-chatbot-dropdown-popover-enter-active,
      .ur-chatbot-dropdown-popover-leave-active {
        transition: opacity 0.18s ease, transform 0.18s cubic-bezier(0.16, 1, 0.3, 1);
      }

      .ur-chatbot-dropdown-popover-enter,
      .ur-chatbot-dropdown-popover-leave-to {
        opacity: 0;
        transform: translateY(6px) scale(0.96);
      }

      .ur-chatbot-btn-plus {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: none;
        background: transparent;
        color: #475569;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.18s ease;
        padding: 0;

        &:hover:not(:disabled) {
          background: #e2e8f0;
          color: #0f172a;
        }

        &:disabled {
          opacity: 0.4;
          cursor: not-allowed;
        }
      }

      .ur-chatbot-btn-send-gemini {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: none;
        background: #93c5fd;
        color: #0f172a;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 1px 3px rgba(147, 197, 253, 0.4);
        padding: 0;

        &:hover:not(:disabled) {
          background: #60a5fa;
          color: #020617;
          transform: translateY(-1px);
          box-shadow: 0 3px 8px rgba(96, 165, 250, 0.4);
        }

        &:disabled {
          background: #e2e8f0;
          color: #94a3b8;
          opacity: 0.7;
          cursor: not-allowed;
          box-shadow: none;
        }
      }

      .ur-chatbot-btn-stop-gemini {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: none;
        background: #ef4444;
        color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.18s ease;
        padding: 0;
        box-shadow: 0 1px 4px rgba(239, 68, 68, 0.3);

        &:hover {
          background: #dc2626;
          transform: translateY(-1px);
        }
      }
    }
  }

  /* ---------------------------------------------------------
     9B. MODAL XÁC NHẬN RESET CHAT
     --------------------------------------------------------- */
  .ur-chatbot-confirm-backdrop {
    position: absolute;
    inset: 0;
    background: rgba(15, 23, 42, 0.5);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 20px;
    border-radius: 12px;
  }

  .ur-chatbot-confirm-card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.22);
    padding: 22px 20px 18px 20px;
    max-width: 320px;
    width: 100%;
    text-align: center;
    border: 1px solid #e2e8f0;
    animation: urChatbotModalZoomIn 0.22s ease-out;
  }

  .ur-chatbot-confirm-icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: #fee2e2;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px auto;
  }

  .ur-chatbot-confirm-title {
    margin: 0 0 8px 0;
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
  }

  .ur-chatbot-confirm-desc {
    margin: 0 0 18px 0;
    font-size: 12.5px;
    line-height: 1.5;
    color: #64748b;
  }

  .ur-chatbot-confirm-actions {
    display: flex;
    gap: 10px;
    justify-content: center;
  }

  .ur-chatbot-btn-confirm-cancel {
    flex: 1;
    padding: 8px 12px;
    border-radius: 6px;
    background: #f1f5f9;
    border: 1px solid #cbd5e1;
    color: #475569;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #e2e8f0;
      color: #1e293b;
    }
  }

  .ur-chatbot-btn-confirm-ok {
    flex: 1;
    padding: 8px 12px;
    border-radius: 6px;
    background: #ef4444;
    border: 1px solid #dc2626;
    color: #ffffff;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #dc2626;
      box-shadow: 0 2px 8px rgba(239, 68, 68, 0.35);
    }
  }

  /* ---------------------------------------------------------
     10. NÚT TRIGGER WIDGET
     --------------------------------------------------------- */
  .ur-chatbot-trigger-btn,
  .chat-trigger-btn {
    position: relative;
    width: 75px;
    height: 75px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    pointer-events: auto;
    margin-top: 12px;
    transition: opacity 0.3s ease,
                transform 0.35s cubic-bezier(0.34, 1.25, 0.64, 1),
                height 0.35s ease,
                margin 0.35s ease;

    &.is-hidden {
      opacity: 0;
      pointer-events: none;
      transform: scale(0.4) translateY(20px);
      height: 0;
      margin-top: 0;
      visibility: hidden;
    }

    &::after {
      content: "";
      position: absolute;
      width: 66px;
      height: 66px;
      border-radius: 50%;
      background: rgba(0, 240, 255, 0.25);
      filter: blur(14px);
      z-index: 1;
      transition: all 0.5s ease;
      animation: pulseAura 3s ease-in-out infinite;
    }

    &:hover::after {
      width: 80px;
      height: 80px;
      background: rgba(0, 240, 255, 0.45);
      filter: blur(18px);
    }
  }

  .ur-chatbot-trigger-svg,
  .trigger-svg {
    width: 100%;
    height: 100%;
    position: relative;
    z-index: 2;
    overflow: visible;
  }

  .state-chat-bubble,
  .state-bot-cute {
    transform-origin: 50px 50px;
    transition: transform 0.6s cubic-bezier(0.34, 1.4, 0.64, 1),
      opacity 0.4s ease;
  }

  .state-chat-bubble {
    transform: scale(1);
    opacity: 1;
  }

  .state-bot-cute {
    transform: scale(0.4);
    opacity: 0;
    pointer-events: none;
  }

  .ur-chatbot-trigger-btn:hover,
  .chat-trigger-btn:hover {
    .state-chat-bubble {
      transform: scale(0.4) rotate(-15deg);
      opacity: 0;
    }

    .state-bot-cute {
      transform: scale(1);
      opacity: 1;
      pointer-events: auto;
    }
  }

  /* Bot SVG Parts Animation Bindings */
  .anim-astro-float {
    animation: astroFloatMotion 6s ease-in-out infinite;
    transform-origin: center;
  }

  .anim-eye-blink {
    animation: eyeBlink 4s ease-in-out infinite;
    transform-origin: 50px 34px;
  }

  .anim-eye-blink-head {
    animation: eyeBlink 4s ease-in-out infinite;
    transform-origin: 50px 50px;
  }

  .anim-antenna-signal {
    animation: occasionalSignal 4.5s ease-out infinite;
  }

  .astro-arm-l {
    transform-origin: 28px 62px;
    animation: astroArmLeft 5s ease-in-out infinite;
  }

  .astro-arm-r {
    transform-origin: 72px 62px;
    animation: astroArmRight 5.5s ease-in-out infinite;
  }

  .astro-leg-l {
    transform-origin: 39px 76px;
    animation: astroLegLeft 6s ease-in-out infinite;
  }

  .astro-leg-r {
    transform-origin: 61px 76px;
    animation: astroLegRight 5.8s ease-in-out infinite;
  }

  .anim-astro-error {
    animation: astroErrorShake 2.5s ease-in-out infinite;
    transform-origin: 50px 50px;
  }

  .anim-core-warning {
    animation: coreWarningBlink 0.9s ease-in-out infinite;
    transform-origin: 50px 67px;
  }

  .anim-antenna-error {
    animation: antennaErrorWave 1.2s ease-out infinite;
  }

  .anim-sweat-drop {
    animation: sweatDropFlow 2s ease-in infinite;
  }

  .anim-eye-error {
    animation: eyeErrorFlicker 1.8s ease-in-out infinite;
  }

  .anim-binary-stream-1 {
    animation: binaryStreamLeft 2.4s linear infinite;
  }

  .anim-binary-stream-2 {
    animation: binaryStreamRight 1.9s linear infinite;
  }

  .anim-binary-stream-3 {
    animation: binaryStreamLeft 2.8s linear infinite;
  }

  .anim-visor-scanner {
    animation: laserSweep 1.5s ease-in-out infinite;
  }

  .anim-cyber-eyes {
    transform-origin: 50px 34px;
    animation: cyberEyePulse 1.2s ease-in-out infinite;
  }

  .anim-core-fast {
    animation: corePulseFast 0.6s ease-in-out infinite !important;
    transform-origin: 50px 67px;
  }

  /* ---------------------------------------------------------
     11. MARKDOWN TYPOGRAPHY & ELEMENTS
     --------------------------------------------------------- */
  .ur-chatbot-markdown,
  .markdown-content {
    color: #1e293b;
    line-height: 1.65;

    /* Con trỏ nhấp nháy hiện đại khi đang streaming trực tiếp */
    &.is-streaming {
      & > *:last-child::after,
      &:not(:has(*))::after {
        content: '▋';
        display: inline-block;
        margin-left: 3px;
        color: #0284c7;
        animation: urChatbotCursorBlink 0.8s infinite;
        vertical-align: baseline;
      }
    }

    p {
      margin: 0 0 10px 0;
      color: #334155;
      &:last-child {
        margin-bottom: 0;
      }
    }

    strong {
      color: #0f172a;
      font-weight: 700;
    }

    h1, h2, h3, h4 {
      color: #0f172a;
      margin: 14px 0 6px 0;
      font-weight: 700;
    }
    h1 { font-size: 16px; }
    h2 { font-size: 15px; }
    h3 { font-size: 14px; }
    h4 { font-size: 13.5px; }

    /* Danh sách có thứ tự & không có thứ tự */
    ul:not(.contains-task-list) {
      list-style-type: disc !important;
      margin: 8px 0 8px 22px !important;
      padding: 0 !important;

      ul {
        list-style-type: circle !important;
        margin: 4px 0 4px 18px !important;
      }
      ol {
        list-style-type: decimal !important;
        margin: 4px 0 4px 18px !important;
      }
    }

    ol {
      list-style-type: decimal !important;
      margin: 8px 0 8px 22px !important;
      padding: 0 !important;

      ol {
        list-style-type: lower-alpha !important;
        margin: 4px 0 4px 18px !important;
      }
      ul {
        list-style-type: disc !important;
        margin: 4px 0 4px 18px !important;
      }
    }

    li {
      display: list-item !important;
      margin-bottom: 4px !important;
      line-height: 1.6 !important;
      color: #1e293b !important;
    }

    a {
      color: #0284c7 !important;
      text-decoration: underline !important;
      &:hover {
        color: #0369a1 !important;
      }
    }

    blockquote {
      margin: 8px 0 !important;
      padding: 8px 14px !important;
      background: #f1f5f9 !important;
      border-left: 3.5px solid #0284c7 !important;
      border-radius: 4px !important;
      color: #475569 !important;
      font-style: italic !important;

      p {
        margin: 0 !important;
      }
    }

    /* Checklists / Task lists (- [ ] và - [x]) */
    ul.contains-task-list {
      list-style: none !important;
      list-style-type: none !important;
      padding-left: 2px !important;
      margin: 8px 0 !important;
    }

    li.task-list-item {
      display: flex !important;
      align-items: flex-start !important;
      list-style: none !important;
      list-style-type: none !important;
      gap: 8px !important;
      margin-bottom: 6px !important;
      line-height: 1.5 !important;

      &::before, &::marker {
        content: none !important;
        display: none !important;
      }

      input.task-list-item-checkbox,
      input[type="checkbox"] {
        -webkit-appearance: checkbox !important;
        -moz-appearance: checkbox !important;
        appearance: checkbox !important;
        accent-color: #0284c7 !important;
        width: 16px !important;
        height: 16px !important;
        min-width: 16px !important;
        min-height: 16px !important;
        margin: 3px 0 0 0 !important;
        padding: 0 !important;
        cursor: default !important;
        flex-shrink: 0 !important;
        display: inline-block !important;
        opacity: 1 !important;
        visibility: visible !important;
      }

      label.task-list-item-label {
        display: inline !important;
        margin: 0 !important;
        cursor: default !important;
        color: #1e293b !important;
      }
    }

    hr {
      border: none !important;
      border-top: 1px solid #e2e8f0 !important;
      margin: 12px 0 !important;
      height: 1px !important;
    }

    del, s {
      text-decoration: line-through !important;
      color: #94a3b8 !important;
    }

    mark {
      background: #fef08a !important;
      color: #854d0e !important;
      padding: 1px 4px !important;
      border-radius: 3px !important;
    }

    kbd {
      background: #f1f5f9 !important;
      border: 1px solid #cbd5e1 !important;
      border-radius: 4px !important;
      box-shadow: 0 1px 0 #94a3b8 !important;
      color: #334155 !important;
      display: inline-block !important;
      font-size: 11px !important;
      padding: 2px 5px !important;
      line-height: 1 !important;
    }

    details {
      background: #f8fafc !important;
      border: 1px solid #e2e8f0 !important;
      border-radius: 6px !important;
      padding: 8px 12px !important;
      margin: 8px 0 !important;

      summary {
        cursor: pointer !important;
        font-weight: 600 !important;
        color: #0284c7 !important;
        outline: none !important;
      }
    }

    :not(pre) > code {
      background-color: #f1f5f9;
      color: #0f172a;
      padding: 2px 5px;
      border-radius: 4px;
      font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
      font-size: 12px;
      border: 1px solid #e2e8f0;
    }
  }

  /* Math Block KaTeX */
  .ur-chatbot-math-block,
  .math-block {
    overflow-x: auto;
    padding: 8px 0;
    margin: 8px 0;
    text-align: center;
  }

  /* ---------------------------------------------------------
     12. BẢNG (TABLE) GITHUB LIGHT THEME VỚI CUỘN NGANG
     --------------------------------------------------------- */
  .ur-chatbot-table-container,
  .table-container {
    width: 100%;
    max-width: 100%;
    overflow-x: auto;
    margin: 12px 0;
    border: 1px solid #d0d7de;
    background-color: #ffffff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);

    scrollbar-width: thin;
    scrollbar-color: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35)) var(--ucb-sb-track, #f1f5f9);

    &::-webkit-scrollbar {
      height: var(--ucb-sb-size, 8px);
      width: var(--ucb-sb-size, 8px);
    }
    &::-webkit-scrollbar-track {
      background: var(--ucb-sb-track, #f1f5f9);
      border-radius: 0 0 var(--ucb-sb-radius, 6px) var(--ucb-sb-radius, 6px);
    }
    &::-webkit-scrollbar-thumb {
      background: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35));
      border-radius: var(--ucb-sb-radius, 6px);
      &:hover {
        background: var(--ucb-sb-thumb-hover, #0284c7);
      }
    }

    table,
    .ur-chatbot-table,
    .markdown-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
      line-height: 1.5;
      color: #24292f;
      background-color: #ffffff;
      border-spacing: 0;
    }

    th, td {
      border: 1px solid #d0d7de;
      padding: 8px 14px;
      text-align: left;
      white-space: nowrap;
    }

    th {
      background-color: #f6f8fa;
      color: #1f2328;
      font-weight: 600;
      border-bottom: 2px solid #d0d7de;
    }

    tbody tr:nth-child(even) {
      background-color: #fbfcfd;
    }

    tbody tr:hover {
      background-color: #f3f4f6;
    }
  }

  /* ---------------------------------------------------------
     12B. BIỂU ĐỒ MERMAID (LIGHT THEME, RESPONSIVE, ZOOMABLE)
     --------------------------------------------------------- */
  .ur-chatbot-mermaid-card,
  .mermaid-card {
    position: relative;
    margin: 14px 0;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      border-color: #cbd5e1;
      box-shadow: 0 4px 14px rgba(2, 132, 199, 0.08);
    }
  }

  .ur-chatbot-mermaid-header {
    position: sticky;
    top: 0;
    z-index: 20;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 12px;
    background: #f6f8fa;
    border-bottom: 1px solid #d0d7de;
    border-radius: 9px 9px 0 0;
    user-select: none;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  }

  .ur-chatbot-mermaid-badge {
    display: flex;
    align-items: center;
    gap: 7px;
    font-size: 11.5px;
    font-weight: 600;
    color: #57606a;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
    text-transform: uppercase;
    letter-spacing: 0.4px;

    svg {
      flex-shrink: 0;
      color: #57606a;
    }
  }

  .ur-chatbot-mermaid-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .ur-chatbot-mermaid-btn {
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    width: 24px !important;
    height: 24px !important;
    padding: 3px !important;
    border-radius: 4px !important;
    color: #57606a !important;
    background: transparent !important;
    border: 1px solid transparent !important;
    cursor: pointer !important;
    transition: all 0.15s ease !important;

    svg {
      pointer-events: none;
      flex-shrink: 0;
    }

    &:hover {
      background: rgba(208, 215, 222, 0.48) !important;
      color: #24292f !important;
      border-color: #d0d7de !important;
    }

    &.is-active {
      background: rgba(84, 174, 255, 0.12) !important;
      color: #0969da !important;
      border-color: #54aeff !important;
    }

    &.copied {
      color: #16a34a !important;
    }
  }

  .ur-chatbot-mermaid-viewport {
    background: #ffffff;
    border-radius: 0 0 9px 9px;
    overflow: hidden;
  }

  .ur-chatbot-mermaid-preview {
    padding: 16px 12px;
    overflow-x: auto;
    overflow-y: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 90px;
    background: radial-gradient(circle at center, #ffffff 0%, #fbfcfd 100%);
    color: initial;

    scrollbar-width: thin;
    scrollbar-color: #cbd5e1 #f1f5f9;

    &::-webkit-scrollbar {
      height: 8px;
    }
    &::-webkit-scrollbar-track {
      background: #f1f5f9;
      border-radius: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: #cbd5e1;
      border-radius: 6px;
      &:hover {
        background: #94a3b8;
      }
    }

    &.is-clickable {
      cursor: zoom-in;
      transition: background 0.2s ease;

      &:hover {
        background: radial-gradient(circle at center, #ffffff 0%, #f1f5f9 100%);
      }
    }
  }

  .ur-chatbot-mermaid-target {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: initial;

    > svg {
      max-width: 100%;
      height: auto !important;
      display: block;
      margin: 0 auto;
      filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.03));

      // Không để quy tắc typography của markdown (.ur-chatbot-markdown) ghi đè lên các nhãn trong SVG
      p {
        margin: 0 !important;
        color: inherit !important;
      }

      foreignObject {
        div, span {
          color: inherit;
        }
      }

      .nodeLabel,
      .edgeLabel,
      .label {
        font-family: inherit;
      }
    }
  }

  .ur-chatbot-mermaid-loading {
    display: flex;
    align-items: center;
    gap: 9px;
    font-size: 12.5px;
    color: #64748b;
    padding: 14px 0;
  }

  .ur-chatbot-mermaid-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid #e2e8f0;
    border-top-color: #0284c7;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    display: inline-block;
  }

  .ur-chatbot-mermaid-error {
    width: 100%;
    margin: 6px 0;
    padding: 12px 16px;
    background: #fff1f2;
    border: 1px solid #fecdd3;
    border-radius: 8px;
    color: #9f1239;
    font-size: 12px;
    text-align: left;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .ur-chatbot-mermaid-error-title {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 8px;
    font-weight: 600;
    color: #be123c;

    svg {
      flex-shrink: 0 !important;
      margin: 0 !important;
      display: inline-block !important;
      width: 15px !important;
      height: 15px !important;
    }

    span {
      flex: 1;
      margin: 0 !important;
      text-align: left !important;
    }
  }

  .ur-chatbot-mermaid-error-desc {
    color: #be123c;
    font-size: 11.5px;
    margin: 0 0 0 23px !important;
    text-align: left !important;
    opacity: 0.9;
  }

  .ur-chatbot-mermaid-code-view {
    border-top: 1px solid #e2e8f0;
    background: #f8fafc;
  }

  /* ---------------------------------------------------------
     13. CODE BLOCK GITHUB LIGHT THEME & STICKY HEADER
     --------------------------------------------------------- */
  .ur-chatbot-code-card,
  .code-card {
    position: relative;
    margin: 12px 0;
    border: 1px solid #d0d7de;
    border-radius: 8px;
    background: #f6f8fa;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  }

  .ur-chatbot-code-header,
  .code-card-header {
    position: sticky;
    top: 0;
    z-index: 20;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 12px;
    background: #f6f8fa;
    border-bottom: 1px solid #d0d7de;
    border-radius: 7px 7px 0 0;
    user-select: none;
  }

  .ur-chatbot-code-header-left,
  .code-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .ur-chatbot-code-lang,
  .code-lang-tag {
    font-size: 11.5px;
    font-weight: 600;
    color: #57606a;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
    text-transform: uppercase;
    letter-spacing: 0.4px;
  }

  .ur-chatbot-copy-btn,
  .copy-code-btn {
    background: transparent;
    border: 1px solid transparent;
    color: #57606a;
    padding: 3px;
    width: 24px;
    height: 24px;
    border-radius: 4px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease;

    &:hover {
      background: #eaeef2;
      color: #24292f;
    }

    &.copied {
      color: #16a34a;
    }
  }

  .ur-chatbot-code-body,
  .code-body-wrapper {
    display: flex;
    position: relative;
    background: #f6f8fa;
    overflow-x: auto;
    border-radius: 0 0 7px 7px;
  }

  .ur-chatbot-code-gutter,
  .code-gutter {
    position: sticky;
    left: 0;
    z-index: 5;
    display: flex;
    flex-direction: column;
    padding: 12px 10px 12px 12px;
    background: #f6f8fa;
    border-right: 1px solid #d0d7de;
    user-select: none;
    text-align: right;
    flex-shrink: 0;
    box-sizing: border-box;
    color: #8c959f;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
    font-size: 12.5px;
    line-height: 1.6;
    white-space: nowrap !important;
    word-break: normal !important;
    overflow-wrap: normal !important;

    span, .gutter-num, .ur-chatbot-gutter-num {
      display: block;
      height: 20px;
      line-height: 20px;
      white-space: nowrap !important;
      word-break: normal !important;
      overflow-wrap: normal !important;
    }
  }

  .ur-chatbot-code-pre,
  .code-pre {
    margin: 0 !important;
    padding: 12px 16px !important;
    background: transparent !important;
    white-space: pre !important;
    flex: 1;
    overflow: visible !important;

    code.hljs {
      padding: 0 !important;
      background: transparent !important;
      font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace !important;
      font-size: 12.5px;
      line-height: 1.6;
    }
  }

  .ur-chatbot-code-body,
  .code-body-wrapper,
  .ur-chatbot-code-pre,
  .code-pre {
    scrollbar-width: thin;
    scrollbar-color: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35)) var(--ucb-sb-track, #f1f5f9);
  }

  .ur-chatbot-code-body::-webkit-scrollbar,
  .code-body-wrapper::-webkit-scrollbar,
  .ur-chatbot-code-pre::-webkit-scrollbar,
  .code-pre::-webkit-scrollbar {
    height: var(--ucb-sb-size, 8px) !important;
    width: var(--ucb-sb-size, 8px) !important;
  }

  .ur-chatbot-code-body::-webkit-scrollbar-track,
  .code-body-wrapper::-webkit-scrollbar-track,
  .ur-chatbot-code-pre::-webkit-scrollbar-track,
  .code-pre::-webkit-scrollbar-track {
    background: var(--ucb-sb-track, #f1f5f9) !important;
    border-radius: var(--ucb-sb-radius, 6px) !important;
  }

  .ur-chatbot-code-body::-webkit-scrollbar-thumb,
  .code-body-wrapper::-webkit-scrollbar-thumb,
  .ur-chatbot-code-pre::-webkit-scrollbar-thumb,
  .code-pre::-webkit-scrollbar-thumb {
    background: var(--ucb-sb-thumb, rgba(2, 132, 199, 0.35)) !important;
    border-radius: var(--ucb-sb-radius, 6px) !important;
    transition: background 0.2s ease;
    &:hover {
      background: var(--ucb-sb-thumb-hover, #0284c7) !important;
    }
  }

  .ur-chatbot-code-body::-webkit-scrollbar-button,
  .code-body-wrapper::-webkit-scrollbar-button,
  .ur-chatbot-code-pre::-webkit-scrollbar-button,
  .code-pre::-webkit-scrollbar-button {
    display: none !important;
    width: 0 !important;
    height: 0 !important;
  }

  /* ---------------------------------------------------------
     8. HÌNH ẢNH MARKDOWN & LIGHTBOX PREVIEW (ISOLATED)
     --------------------------------------------------------- */
  .ur-chatbot-image-figure,
  .image-figure {
    margin: 10px 0;
    display: inline-block;
    max-width: 250px;
    width: 100%;
  }

  .ur-chatbot-image-wrapper,
  .image-wrapper {
    position: relative;
    display: block;
    max-width: 250px;
    width: 100%;
    border-radius: 10px;
    overflow: hidden;
    background: #f1f5f9;
    border: 1px solid rgba(226, 232, 240, 0.9);
    box-shadow: 0 4px 14px rgba(15, 23, 42, 0.08);
    cursor: zoom-in;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 22px rgba(15, 23, 42, 0.12);
      border-color: #cbd5e1;

      .ur-chatbot-image-zoom-overlay {
        opacity: 1;
      }
    }

    .ur-chatbot-image,
    .markdown-image {
      display: block;
      max-width: 100%;
      height: auto;
      border-radius: 9px;
      object-fit: cover;
      transition: opacity 0.2s ease;

      &.error {
        display: none !important;
      }
    }

    .ur-chatbot-image-zoom-overlay {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 32px;
      height: 32px;
      background: rgba(15, 23, 42, 0.68);
      backdrop-filter: blur(4px);
      color: #ffffff;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: none;
    }

    .ur-chatbot-image-error-fallback {
      display: none;
      padding: 14px 16px;
      background: #fef2f2;
      border: 1px dashed #f87171;
      border-radius: 8px;
      color: #991b1b;
      font-size: 12.5px;
      align-items: center;
      gap: 10px;

      .ur-chatbot-image-error-text {
        display: flex;
        flex-direction: column;
        gap: 3px;

        a {
          color: #dc2626;
          font-weight: 600;
          text-decoration: underline;
          &:hover {
            color: #b91c1c;
          }
        }
      }
    }

    &.has-error {
      cursor: default;
      &:hover {
        transform: none;
        box-shadow: none;
      }
      .ur-chatbot-image-zoom-overlay {
        display: none !important;
      }
      .ur-chatbot-image-error-fallback {
        display: flex !important;
      }
    }
  }

  .ur-chatbot-image-caption,
  .image-caption {
    margin-top: 6px;
    font-size: 12px;
    color: #64748b;
    text-align: center;
    font-style: italic;
    line-height: 1.4;
  }

  /* Live Preview ảnh ngay trong Code Block Markdown */
  .ur-chatbot-code-image-preview {
    padding: 10px 14px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
  }

  .ur-chatbot-code-preview-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11.5px;
    font-weight: 600;
    color: #475569;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 8px;

    svg {
      color: #0284c7;
    }
  }

  .ur-chatbot-code-preview-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .ur-chatbot-code-img-item {
    position: relative;
    max-width: 220px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #cbd5e1;
    background: #ffffff;
    cursor: zoom-in;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 14px rgba(0, 0, 0, 0.1);
      border-color: #0ea5e9;
    }

    .ur-chatbot-preview-thumb {
      display: block;
      width: 100%;
      height: 115px;
      object-fit: cover;

      &.error {
        display: none !important;
      }
    }

    .ur-chatbot-code-img-desc {
      display: block;
      padding: 4px 8px;
      font-size: 11px;
      color: #475569;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      background: #f8fafc;
      border-top: 1px solid #f1f5f9;
    }

    &.has-error {
      cursor: default;
      display: none;
    }
  }

  /* Modal Lightbox Xem Ảnh Phóng To */
  .ur-chatbot-image-modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(15, 23, 42, 0.85);
    backdrop-filter: blur(8px);
    z-index: 100000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    pointer-events: auto;
    animation: urChatbotModalFadeIn 0.2s ease-out forwards;
  }

  .ur-chatbot-image-modal-content {
    position: relative;
    max-width: 92vw;
    max-height: 92vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    background: #0f172a;
    border-radius: 12px;
    padding: 12px;
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.55), 0 0 0 1px rgba(255, 255, 255, 0.1);
    animation: urChatbotModalZoomIn 0.25s cubic-bezier(0.34, 1.3, 0.64, 1) forwards;
  }

  .ur-chatbot-image-modal-close {
    position: absolute;
    top: -14px;
    right: -14px;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: #1e293b;
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #f8fafc;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.2s ease;

    &:hover {
      background: #ef4444;
      border-color: #ef4444;
      transform: scale(1.08);
    }
  }

  .ur-chatbot-image-modal-img {
    max-width: calc(92vw - 40px);
    max-height: calc(82vh - 60px);
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  }

  .ur-chatbot-image-modal-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    margin-top: 10px;
    padding: 0 6px;
    gap: 12px;
  }

  .ur-chatbot-image-modal-caption {
    font-size: 13px;
    color: #cbd5e1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 65%;
  }

  .ur-chatbot-image-modal-actions {
    display: flex;
    gap: 8px;
  }

  .ur-chatbot-image-modal-btn {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 500;
    color: #38bdf8;
    background: rgba(56, 189, 248, 0.12);
    border: 1px solid rgba(56, 189, 248, 0.25);
    border-radius: 6px;
    text-decoration: none;
    transition: all 0.2s ease;
    cursor: pointer;

    &:hover {
      background: rgba(56, 189, 248, 0.25);
      border-color: #38bdf8;
      color: #ffffff;
    }
  }

  @keyframes urChatbotModalFadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }

  @keyframes urChatbotModalZoomIn {
    from { transform: scale(0.92); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
  }

  .ur-chatbot-fade-enter-active,
  .ur-chatbot-fade-leave-active {
    transition: opacity 0.2s ease;
  }
  .ur-chatbot-fade-enter,
  .ur-chatbot-fade-leave-to {
    opacity: 0;
  }

  /* Modal Phóng to & Tương tác Biểu đồ Mermaid (Zoom & Pan, Tải PNG) */
  .ur-chatbot-mermaid-modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    // background: rgba(15, 23, 42, 0.88);
    backdrop-filter: blur(5px);
    z-index: 100000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    pointer-events: auto;
    animation: urChatbotModalFadeIn 0.2s ease-out forwards;
  }

  .ur-chatbot-mermaid-modal-content {
    position: relative;
    width: 95vw;
    max-width: 1240px;
    height: 90vh;
    max-height: 860px;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    border-radius: 14px;
    box-shadow: 0 25px 65px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.15);
    overflow: hidden;
    animation: urChatbotModalZoomIn 0.25s cubic-bezier(0.34, 1.3, 0.64, 1) forwards;
  }

  .ur-chatbot-mermaid-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 18px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    flex-shrink: 0;
  }

  .ur-chatbot-mermaid-modal-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;

    svg {
      color: #0284c7;
    }
  }

  .ur-chatbot-mermaid-modal-zoom-badge {
    padding: 2px 8px;
    font-size: 11px;
    font-weight: 600;
    color: #0284c7;
    background: #e0f2fe;
    border-radius: 9999px;
    font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, monospace;
  }

  .ur-chatbot-mermaid-modal-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .ur-chatbot-mermaid-modal-viewport {
    flex: 1 1 auto;
    position: relative;
    width: 100%;
    height: 100%;
    min-height: 240px;
    overflow: hidden;
    background: radial-gradient(circle at center, #ffffff 0%, #f8fafc 100%);
    cursor: grab;
    display: flex;
    align-items: center;
    justify-content: center;
    user-select: none;

    &.is-dragging {
      cursor: grabbing;
    }
  }

  .ur-chatbot-mermaid-modal-canvas {
    width: 100%;
    height: 100%;
    min-height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;
    transform-origin: center center;

    svg {
      width: 100% !important;
      height: 100% !important;
      max-width: 92% !important;
      max-height: 88% !important;
      display: block !important;
      margin: auto !important;
      filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.04));
      pointer-events: auto;
      overflow: visible !important;
    }
  }

  .ur-chatbot-mermaid-modal-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px 18px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
    flex-shrink: 0;
  }

  .ur-chatbot-modal-tip {
    font-size: 12px;
    color: #64748b;
    text-align: center;
  }
}

// Cách ly phần tử tạm thời Mermaid gắn vào body ra khỏi luồng layout nhưng GIỮ NGUYÊN kích thước đo đạc DOM
body > [id^="dmmd_"],
body > [id^="immd_"],
body > [id^="mmd_"] {
  position: fixed !important;
  top: 0 !important;
  left: -9999px !important;
  opacity: 0 !important;
  pointer-events: none !important;
  z-index: -9999 !important;
}
</style>
