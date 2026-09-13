import MarkdownIt from 'markdown-it';
import markdownItTaskLists from 'markdown-it-task-lists';
import hljs from 'highlight.js';
import katex from 'katex';
import DOMPurify from 'dompurify';

import 'highlight.js/styles/github.css';
import 'katex/dist/katex.min.css';

// Khởi tạo MarkdownIt độc lập
export const md = new MarkdownIt({
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

// Custom link render: luôn mở tab mới
const defaultLinkRender = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options);
};

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrPush(['target', '_blank']);
  tokens[idx].attrPush(['rel', 'noopener noreferrer']);
  return defaultLinkRender(tokens, idx, options, env, self);
};

// Custom table render: bọc table trong container cuộn ngang (overflow-x)
md.renderer.rules.table_open = function () {
  return '<div class="ur-chatbot-table-container table-container"><table class="ur-chatbot-table markdown-table">';
};
md.renderer.rules.table_close = function () {
  return '</table></div>';
};

// Custom render cho thẻ hình ảnh Markdown
md.renderer.rules.image = function (tokens, idx) {
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

// Custom render cho khối mã nguồn (fenced code block & mermaid card)
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
  const safeCode = encodeURIComponent(code);

  const cleanCode = code.replace(/\n$/, '');
  const lines = cleanCode.split('\n');
  const lineCount = lines.length;
  const digits = String(lineCount).length;
  const gutterWidth = Math.max(34, digits * 9 + 22);

  let gutterHtml = '';
  if (lineCount > 0) {
    const gutterItems = Array.from({ length: lineCount }, (_, i) => `<span class="ur-chatbot-gutter-num gutter-num">${i + 1}</span>`).join('');
    gutterHtml = `<div class="ur-chatbot-code-gutter code-gutter" style="min-width: ${gutterWidth}px; width: ${gutterWidth}px;" aria-hidden="true">${gutterItems}</div>`;
  }

  // Khối Mermaid Diagram
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

  // Khối mã nguồn Code Block tiêu chuẩn
  return `<div class="ur-chatbot-code-card code-card">
    <div class="ur-chatbot-code-header code-header">
      <span class="ur-chatbot-code-lang code-lang">${displayLang}</span>
      <button type="button" class="ur-chatbot-btn-copy-code btn-copy-code" data-code="${safeCode}" onclick="window.__copyCodeBlock(this)" title="Copy code" aria-label="Copy code">
        <span class="icon-copy">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
          </svg>
        </span>
        <span class="icon-copied" style="display:none;">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </span>
      </button>
    </div>
    <div class="ur-chatbot-code-body code-body has-gutter">
      ${gutterHtml}
      <pre class="ur-chatbot-code-pre code-pre"><code class="hljs ${detectedLang}">${highlighted}</code></pre>
    </div>
  </div>`;
};

// Global Copy Code Handler
export function setupCopyCodeGlobal() {
  if (typeof window !== 'undefined') {
    window.__copyCodeBlock = function (btn) {
      const raw = btn.getAttribute('data-code');
      if (!raw) return;
      const text = decodeURIComponent(raw);
      navigator.clipboard.writeText(text).then(() => {
        const copyIcon = btn.querySelector('.icon-copy');
        const copiedIcon = btn.querySelector('.icon-copied');
        if (copyIcon && copiedIcon) {
          copyIcon.style.display = 'none';
          copiedIcon.style.display = 'inline-flex';
          btn.classList.add('copied');
          btn.title = 'Copied!';
          setTimeout(() => {
            copyIcon.style.display = 'inline-flex';
            copiedIcon.style.display = 'none';
            btn.classList.remove('copied');
            btn.title = 'Copy code';
          }, 2000);
        }
      }).catch(err => {
        console.error('Không thể sao chép:', err);
      });
    };
  }
}
setupCopyCodeGlobal();

// KaTeX Helpers
export function katexRenderSilent(formula, opts) {
  const originalWarn = console.warn;
  console.warn = function() {};
  let result = '';
  try {
    result = katex.renderToString(formula, opts);
  } catch (e) {
    // ignore
  } finally {
    console.warn = originalWarn;
  }
  return result;
}

export function looksLikeMath(formula) {
  if (!formula || formula.trim().length === 0) return false;
  const f = formula.trim();
  if (/[+\-*/=^_{}]/.test(f)) return true;
  if (/\\[a-zA-Z]/.test(f)) return true;
  if (/\d+\/\d+/.test(f)) return true;
  if (/[<>]/.test(f)) return true;
  for (let i = 0; i < f.length; i++) {
    const code = f.charCodeAt(i);
    if ((code >= 0x0391 && code <= 0x03C9) || (code >= 0x2200 && code <= 0x22FF)) return true;
  }
  return false;
}

export function renderMathFormulas(content) {
  if (!content) return '';

  const codeBlocks = [];
  let textWithoutCode = content.replace(/(```[\s\S]*?```|`[^`\n]+?`)/g, (match) => {
    const placeholder = `%%UR_MATH_CODE_${codeBlocks.length}%%`;
    codeBlocks.push(match);
    return placeholder;
  });

  // Block LaTeX \[ ... \]
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

  // Block $$ ... $$
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

  // Inline LaTeX \( ... \)
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

  // Inline $ ... $
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

  for (let i = 0; i < codeBlocks.length; i++) {
    textWithoutCode = textWithoutCode.replace(`%%UR_MATH_CODE_${i}%%`, () => codeBlocks[i]);
  }

  return textWithoutCode;
}

export function fixStreamingMarkdown(text, isStreaming) {
  if (!isStreaming || !text) return text;
  let result = text;

  const codeBlockCount = (result.match(/```/g) || []).length;
  if (codeBlockCount % 2 !== 0) {
    result += '\n```';
  }

  const codeHolders = [];
  let textWithoutCode = result.replace(/(```[\s\S]*?```)/g, (match) => {
    const placeholder = `%%UR_STREAM_CODE_${codeHolders.length}%%`;
    codeHolders.push(match);
    return placeholder;
  });

  const inlineBackticks = (textWithoutCode.match(/(?<!`)`(?!`)/g) || []).length;
  if (inlineBackticks % 2 !== 0) textWithoutCode += '`';

  const boldMarkers = (textWithoutCode.match(/\*\*/g) || []).length;
  if (boldMarkers % 2 !== 0) textWithoutCode += '**';

  const mathDisplayMarkers = (textWithoutCode.match(/\$\$/g) || []).length;
  if (mathDisplayMarkers % 2 !== 0) textWithoutCode += '$$';

  const lines = textWithoutCode.split('\n');
  const lastLine = lines[lines.length - 1];
  if (/^\s*\|.*[^|]$/.test(lastLine)) {
    lines[lines.length - 1] = lastLine + ' |';
    textWithoutCode = lines.join('\n');
  }

  if (/<[a-zA-Z][^>]*$/.test(textWithoutCode) || /<\/?[a-zA-Z][^>]*$/.test(textWithoutCode)) {
    textWithoutCode += '>';
  }

  for (let i = 0; i < codeHolders.length; i++) {
    textWithoutCode = textWithoutCode.replace(`%%UR_STREAM_CODE_${i}%%`, () => codeHolders[i]);
  }

  return textWithoutCode;
}

export function processHtmlMarkdownContainers(content) {
  if (!content) return '';

  const codeBlocks = [];
  let text = content.replace(/(```[\s\S]*?```|`[^`\n]+?`)/g, (match) => {
    const placeholder = `%%UR_HTML_CODE_${codeBlocks.length}%%`;
    codeBlocks.push(match);
    return placeholder;
  });

  const voidTags = new Set(['img', 'br', 'hr', 'input', 'source', 'wbr', 'col', 'meta', 'link']);
  const containerTags = new Set([
    'div', 'center', 'p', 'section', 'blockquote', 'aside', 'details', 'summary',
    'table', 'thead', 'tbody', 'tr', 'th', 'td', 'ul', 'ol', 'li',
    'span', 'font', 'b', 'strong', 'i', 'em', 'mark', 'kbd', 'del', 's'
  ]);

  const tagStack = [];
  const tagRegex = /<\/?([a-zA-Z0-9_-]+)(?:\s+[^>]*)?>/g;
  let match;
  while ((match = tagRegex.exec(text)) !== null) {
    const fullTag = match[0];
    const tagName = match[1].toLowerCase();
    if (voidTags.has(tagName) || !containerTags.has(tagName)) continue;

    const isClosing = fullTag.startsWith('</');
    const isSelfClosing = fullTag.endsWith('/>');
    if (isSelfClosing) continue;

    if (!isClosing) {
      tagStack.push(tagName);
    } else {
      const lastIdx = tagStack.lastIndexOf(tagName);
      if (lastIdx !== -1) {
        tagStack.splice(lastIdx, 1);
      }
    }
  }

  while (tagStack.length > 0) {
    const unclosedTag = tagStack.pop();
    text += `\n</${unclosedTag}>`;
  }

  const blockTags = 'div|center|p|blockquote|section|aside|details|table|thead|tbody|tr|th|td|ul|ol|li|h[1-6]|header|footer|nav';
  const closeBlockTagRegex = new RegExp(`(<\\/(?:${blockTags})>[^\\S\\r\\n]*)\\r?\\n(?!\\s*\\r?\\n)`, 'gi');
  text = text.replace(closeBlockTagRegex, '$1\n\n');

  text = text.replace(/<p(\s+[^>]*(?:align|style)[^>]*)>([\s\S]*?)<\/p>/gi, (match, attrs, innerContent) => {
    if (/(?:^|\n)\s*(?:#{1,6}\s+|>\s+|[-*+]\s+|\d+\.\s+|!\[|\|)/.test(innerContent)) {
      return `<div${attrs}>\n\n${innerContent.trim()}\n\n</div>`;
    }
    return match;
  });

  const innerContainerTags = 'center|div|blockquote|section|aside|details';
  const innerContainerRegex = new RegExp(`<(${innerContainerTags})((?:\\s+[^>]*)?)>([\\s\\S]*?)<\\/\\1>`, 'gi');

  let prevText = '';
  let loopCount = 0;
  while (prevText !== text && loopCount < 3) {
    prevText = text;
    loopCount++;
    text = text.replace(innerContainerRegex, (match, tag, attrs, innerContent) => {
      if (!innerContent || !innerContent.trim()) return match;
      const trimmedInner = innerContent.trim();
      return `<${tag}${attrs}>\n\n${trimmedInner}\n\n</${tag}>`;
    });
  }

  text = text.replace(closeBlockTagRegex, '$1\n\n');

  for (let i = 0; i < codeBlocks.length; i++) {
    text = text.replace(`%%UR_HTML_CODE_${i}%%`, () => codeBlocks[i]);
  }

  return text;
}

export function parseAndSanitizeMarkdown(markdownText, isStreaming = false) {
  if (!markdownText) return '';
  try {
    const normalized = fixStreamingMarkdown(markdownText, isStreaming);
    const withHtmlMd = processHtmlMarkdownContainers(normalized);
    const withMath = renderMathFormulas(withHtmlMd);
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
        'strong', 'b', 'em', 'i', 'center', 'font', 'aside', 'section', 'bdo', 'bdi', 'ruby', 'rt', 'rp'
      ],
      ADD_ATTR: [
        'target', 'rel', 'data-code', 'data-src', 'data-alt', 'data-mermaid-code', 'data-rendered',
        'viewBox', 'fill', 'stroke', 'stroke-width',
        'stroke-linecap', 'stroke-linejoin', 'displayMode', 'aria-hidden', 'title', 'style', 'class',
        'src', 'alt', 'loading', 'onerror', 'cx', 'cy', 'r', 'x1', 'y1', 'x2', 'y2', 'points',
        'type', 'checked', 'disabled', 'id', 'for', 'open', 'align', 'colspan', 'rowspan',
        'encoding', 'xmlns', 'display',
        'width', 'height', 'color', 'size', 'border', 'cellpadding', 'cellspacing'
      ]
    });
  } catch (err) {
    console.error('[UrChatbot Markdown] Lỗi render:', err);
    return markdownText;
  }
}
