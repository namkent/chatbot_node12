import mermaid from '@/vendor/mermaid.js';

let mermaidInitialized = false;

export function initMermaid() {
  if (mermaidInitialized || typeof window === 'undefined') return;
  mermaid.initialize({
    startOnLoad: false,
    theme: 'neutral',
    securityLevel: 'loose',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, sans-serif'
  });
  mermaidInitialized = true;
}
initMermaid();

export function sanitizeMermaidNode(token) {
  token = token.trim();
  if (!token) return token;

  let classSuffix = '';
  const classMatch = token.match(/:::([a-zA-Z0-9_-]+)$/);
  if (classMatch) {
    classSuffix = ':::' + classMatch[1];
    token = token.slice(0, -classSuffix.length).trim();
  }

  const delimPairs = [
    { open: '([', close: '])' },
    { open: '[[', close: ']]' },
    { open: '[(', close: ')]' },
    { open: '((', close: '))' },
    { open: '{{', close: '}}' },
    { open: '[/', close: '/]' },
    { open: '[\\', close: '\\]' },
    { open: '>', close: ']' },
    { open: '[', close: ']' },
    { open: '(', close: ')' },
    { open: '{', close: '}' }
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

  if (/^(end|subgraph|class|click|style|graph|flowchart|direction|default)$/i.test(token)) {
    return 'node_' + token + classSuffix;
  }

  return token + classSuffix;
}

export function repairFlowchartCode(code) {
  const lines = code.split('\n');
  const arrowRegex = /(\s*(?:-->|---|==>|===|-\.->|-\.-|<-->|<==>|o--o|x--x)(?:\|[^|\n]*\|)?\s*)/g;

  const repairedLines = lines.map(line => {
    const trimmed = line.trim();
    if (!trimmed) return line;

    if (/^%%/.test(trimmed)) return line;

    if (/^\s*(?:flowchart|graph|direction|style|classDef|linkStyle|click|accTitle|accDescr)\b/i.test(trimmed)) {
      return line;
    }

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

    const indentMatch = line.match(/^(\s*)/);
    const indent = indentMatch ? indentMatch[1] : '';

    const parts = trimmed.split(arrowRegex);
    if (parts.length <= 1) {
      return indent + sanitizeMermaidNode(trimmed);
    }

    let repairedLine = '';
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      if (arrowRegex.test(part)) {
        const safeEdge = part.replace(/(\|)([^|\n]+)(\|)/g, (m, p1, edgeText) => {
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
        if (part.includes('&')) {
          const subNodes = part.split(/\s*&\s*/).map(n => sanitizeMermaidNode(n));
          repairedLine += subNodes.join(' & ');
        } else {
          repairedLine += sanitizeMermaidNode(part);
        }
      }
    }

    return indent + repairedLine;
  });

  return repairedLines.join('\n');
}

export function repairClassDiagramCode(code) {
  return code.replace(/<([a-zA-Z0-9_,\s]+)>/g, '~$1~');
}

export function repairPieCode(code) {
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
}

export function repairErDiagramCode(code) {
  return code.replace(/([a-zA-Z0-9_]+)\((\d+)\)/g, '$1_$2');
}

export function repairSequenceDiagramCode(code) {
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
}

export function sanitizeMermaidCode(raw) {
  if (!raw) return '';
  let code = raw.trim();

  code = code.replace(/^```(?:mermaid)?\s*\n?/i, '').replace(/\n?```\s*$/i, '').trim();

  if (/^\s*xychart(-beta)?\b/i.test(code)) {
    code = code.replace(/(x-axis\s*\[)([\s\S]*?)(\])/gi, (m, p1, inner, p3) => {
      return p1 + inner.replace(/'/g, '"') + p3;
    });

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

    const yAxisRangeMatch = code.match(/(y-axis\s+(?:"[^"]*"|'[^']*')?\s*)([+-]?\d+(?:\.\d+)?)\s*-->\s*([+-]?\d+(?:\.\d+)?)/i);
    if (yAxisRangeMatch) {
      const declaredMax = parseFloat(yAxisRangeMatch[3]);
      if (!isNaN(declaredMax) && maxDataVal > declaredMax) {
        code = code.replace(yAxisRangeMatch[0], yAxisRangeMatch[1].trim());
      }
    }

    return code;
  }

  if (/^\s*(?:flowchart|graph)\b/i.test(code)) {
    return repairFlowchartCode(code);
  }
  if (/^\s*classDiagram\b/i.test(code)) {
    return repairClassDiagramCode(code);
  }
  if (/^\s*pie\b/i.test(code)) {
    return repairPieCode(code);
  }
  if (/^\s*erDiagram\b/i.test(code)) {
    return repairErDiagramCode(code);
  }
  if (/^\s*sequenceDiagram\b/i.test(code)) {
    return repairSequenceDiagramCode(code);
  }

  return code;
}

export function aggressiveFallbackRepairMermaid(code) {
  if (!code) return '';
  let c = code;

  c = c.replace(/[\u200B-\u200D\uFEFF]/g, '');
  c = c.replace(/;\s*$/gm, '');

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

  if (/^\s*sequenceDiagram\b/i.test(c)) {
    c = repairSequenceDiagramCode(c);
  }

  return c;
}

export function prepareSvgForExport(svgEl) {
  if (!svgEl) return null;
  const clonedSvg = svgEl.cloneNode(true);

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

  const bgRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
  bgRect.setAttribute('x', '0');
  bgRect.setAttribute('y', '0');
  bgRect.setAttribute('width', '100%');
  bgRect.setAttribute('height', '100%');
  bgRect.setAttribute('fill', '#ffffff');
  clonedSvg.insertBefore(bgRect, clonedSvg.firstChild);

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
}

export function triggerFileDownload(href, filename) {
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
}

export function downloadSvgElementAsSvg(svgEl, filename) {
  if (!svgEl) return;
  try {
    const defaultName = 'mermaid-diagram-' + new Date().toISOString().slice(0, 10) + '.svg';
    const finalName = filename ? filename.replace(/\.png$/i, '.svg') : defaultName;

    const prep = prepareSvgForExport(svgEl);
    if (!prep) return;
    const serializer = new XMLSerializer();
    const svgString = serializer.serializeToString(prep.clonedSvg);

    const blob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    triggerFileDownload(url, finalName);
    setTimeout(() => URL.revokeObjectURL(url), 2000);
  } catch (e) {
    console.error('[Mermaid SVG Download Error]:', e);
  }
}

export function downloadSvgElementAsPng(svgEl, filename) {
  if (!svgEl) return;
  try {
    const defaultName = 'mermaid-diagram-' + new Date().toISOString().slice(0, 10) + '.png';
    const finalName = filename || defaultName;

    const prep = prepareSvgForExport(svgEl);
    if (!prep) return;
    const serializer = new XMLSerializer();
    const svgString = serializer.serializeToString(prep.clonedSvg);

    const blob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const img = new Image();

    img.onload = () => {
      const scale = 2; // retina 2x
      const canvas = document.createElement('canvas');
      canvas.width = Math.round(prep.width * scale);
      canvas.height = Math.round(prep.height * scale);
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

      URL.revokeObjectURL(url);

      canvas.toBlob(pngBlob => {
        if (!pngBlob) return;
        const pngUrl = URL.createObjectURL(pngBlob);
        triggerFileDownload(pngUrl, finalName);
        setTimeout(() => URL.revokeObjectURL(pngUrl), 2000);
      }, 'image/png');
    };

    img.onerror = () => {
      URL.revokeObjectURL(url);
    };

    img.src = url;
  } catch (e) {
    console.error('[Mermaid PNG Download Error]:', e);
  }
}

let _mermaidRendering = false;

export async function renderMermaidDiagrams(rootEl, isStreaming = false) {
  if (typeof window === 'undefined') return;
  if (isStreaming) return;
  if (_mermaidRendering) return;
  _mermaidRendering = true;

  try {
    const targets = rootEl ? rootEl.querySelectorAll('.ur-chatbot-mermaid-target:not([data-rendered="true"])') : [];
    if (!targets || targets.length === 0) return;

    for (const target of targets) {
      const rawCode = target.getAttribute('data-mermaid-code');
      if (!rawCode) continue;
      let code = decodeURIComponent(rawCode).trim();
      if (!code) continue;
      code = sanitizeMermaidCode(code);

      const id = 'mmd_' + Math.random().toString(36).replace(/[^a-z0-9]/g, '').slice(0, 8);

      const existingTemp = document.getElementById(id) || document.getElementById('d' + id);
      if (existingTemp) existingTemp.remove();

      let renderSuccess = false;
      let svgCode = '';
      let bindFunctions = null;

      try {
        const renderFn = (typeof mermaid.renderAsync === 'function')
          ? mermaid.renderAsync.bind(mermaid)
          : mermaid.render.bind(mermaid);
        const res = await renderFn(id, code);
        svgCode = typeof res === 'string' ? res : (res && res.svg ? res.svg : '');
        bindFunctions = res && res.bindFunctions;
        renderSuccess = true;
      } catch (firstErr) {
        const errEl = document.getElementById(id) || document.getElementById('d' + id);
        if (errEl) errEl.remove();

        try {
          const fallbackCode = aggressiveFallbackRepairMermaid(code, firstErr.message);
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
          } catch (e) {}
        }
      } else if (!renderSuccess && !isStreaming && target.isConnected) {
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
    _mermaidRendering = false;
  }
}
