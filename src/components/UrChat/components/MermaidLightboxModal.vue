<template>
  <transition name="ur-chatbot-fade">
    <div
      v-if="visible"
      class="ur-chatbot-mermaid-modal-backdrop"
      @click.self="close"
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
            <span class="ur-chatbot-mermaid-modal-zoom-badge">{{ Math.round(zoom * 100) }}%</span>
          </div>

          <div class="ur-chatbot-mermaid-modal-actions header-actions">
            <button
              type="button"
              class="ur-chatbot-btn-header-action btn-header-action"
              title="Zoom out (-)"
              @click="zoomDelta(-0.2)"
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
              @click="zoomDelta(0.2)"
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
              @click="resetView"
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
              @click="downloadPng"
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
              @click="downloadSvg"
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
              @click="close"
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
          ref="viewport"
          class="ur-chatbot-mermaid-modal-viewport"
          :class="{ 'is-dragging': isDragging }"
          @wheel.prevent="handleWheel"
          @mousedown="handleMouseDown"
          @touchstart="handleTouchStart"
          @touchmove="handleTouchMove"
          @touchend="handleTouchEnd"
        >
          <div
            ref="canvas"
            class="ur-chatbot-mermaid-modal-canvas"
            :style="canvasStyle"
            v-html="svgHtml"
          ></div>
        </div>

        <!-- Footer của Modal -->
        <div class="ur-chatbot-mermaid-modal-footer">
          <span class="ur-chatbot-modal-tip">💡 <strong>Tip:</strong> Scroll to zoom • Click & drag to pan • Press <strong>Esc</strong> to close</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { downloadSvgElementAsPng, downloadSvgElementAsSvg } from '../services/mermaidEngine';

export default {
  name: 'MermaidLightboxModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    svgHtml: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      zoom: 1,
      panX: 0,
      panY: 0,
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      _touchDist: null,
      _startZoom: 1
    };
  },
  computed: {
    canvasStyle() {
      return {
        transform: `translate3d(${this.panX}px, ${this.panY}px, 0) scale(${this.zoom})`,
        transformOrigin: 'center center',
        transition: this.isDragging ? 'none' : 'transform 0.15s ease-out'
      };
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.resetView();
      }
    }
  },
  methods: {
    close() {
      this.$emit('close');
    },
    zoomDelta(delta) {
      let newZoom = this.zoom + delta;
      newZoom = Math.min(Math.max(0.2, newZoom), 5.0);
      this.zoom = Math.round(newZoom * 100) / 100;
    },
    resetView() {
      this.zoom = 1;
      this.panX = 0;
      this.panY = 0;
      this.isDragging = false;
    },
    handleWheel(e) {
      const delta = e.deltaY < 0 ? 0.15 : -0.15;
      this.zoomDelta(delta);
    },
    handleMouseDown(e) {
      if (e.button !== 0) return;
      this.isDragging = true;
      this.dragStartX = e.clientX - this.panX;
      this.dragStartY = e.clientY - this.panY;

      const onMouseMove = (ev) => {
        if (!this.isDragging) return;
        this.panX = ev.clientX - this.dragStartX;
        this.panY = ev.clientY - this.dragStartY;
      };

      const onMouseUp = () => {
        this.isDragging = false;
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
      };

      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    },
    handleTouchStart(e) {
      if (e.touches.length === 1) {
        this.isDragging = true;
        this.dragStartX = e.touches[0].clientX - this.panX;
        this.dragStartY = e.touches[0].clientY - this.panY;
      } else if (e.touches.length === 2) {
        this._touchDist = Math.hypot(
          e.touches[0].clientX - e.touches[1].clientX,
          e.touches[0].clientY - e.touches[1].clientY
        );
        this._startZoom = this.zoom;
      }
    },
    handleTouchMove(e) {
      if (e.touches.length === 1 && this.isDragging) {
        this.panX = e.touches[0].clientX - this.dragStartX;
        this.panY = e.touches[0].clientY - this.dragStartY;
      } else if (e.touches.length === 2 && this._touchDist) {
        const dist = Math.hypot(
          e.touches[0].clientX - e.touches[1].clientX,
          e.touches[0].clientY - e.touches[1].clientY
        );
        const factor = dist / this._touchDist;
        let newZoom = this._startZoom * factor;
        newZoom = Math.min(Math.max(0.2, newZoom), 5.0);
        this.zoom = Math.round(newZoom * 100) / 100;
      }
    },
    handleTouchEnd() {
      this.isDragging = false;
      this._touchDist = null;
    },
    downloadPng() {
      const container = this.$refs.canvas;
      if (!container) return;
      const svgEl = container.querySelector('svg');
      if (svgEl) {
        downloadSvgElementAsPng(svgEl, 'mermaid-diagram-full.png');
      }
    },
    downloadSvg() {
      const container = this.$refs.canvas;
      if (!container) return;
      const svgEl = container.querySelector('svg');
      if (svgEl) {
        downloadSvgElementAsSvg(svgEl, 'mermaid-diagram-full.svg');
      }
    }
  }
};
</script>
