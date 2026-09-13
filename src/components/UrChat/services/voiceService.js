/**
 * Voice Service: Text-to-Speech (TTS) & Speech-to-Text (STT)
 */

export function detectLanguage(text) {
  if (!text || text.length < 8) return 'vi-VN';

  const sample = text.slice(0, 500);

  const hasKorean = /[\uAC00-\uD7AF\u1100-\u11FF]/.test(sample);
  const hasJapanese = /[\u3040-\u30FF\u31F0-\u31FF]/.test(sample);
  const hasCJK = /[\u4E00-\u9FFF\u3400-\u4DBF]/.test(sample);

  if (hasKorean) return 'ko-KR';
  if (hasJapanese) return 'ja-JP';
  if (hasCJK) return 'zh-CN';

  const viDiacritics = (sample.match(/[àáảãạăắặẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐÀÁẢÃẠĂẮẶẲẴẶÂẤẦẨẪẬÈÉẺẼẸÊẾỀỂỄỆÌÍỈĨỊÒÓỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÙÚỦŨỤƯỨỪỬỮỰỲÝỶỸỴ]/g) || []).length;
  const viRatio = viDiacritics / sample.replace(/\s/g, '').length;

  if (viRatio > 0.05) return 'vi-VN';

  return 'en-US';
}

export function cleanTextForSpeech(raw) {
  if (!raw) return '';
  return raw
    .replace(/```[\s\S]*?```/g, '') // gỡ code blocks
    .replace(/`[^`]+`/g, '')        // gỡ inline code
    .replace(/!\[.*?\]\(.*?\)/g, '')// gỡ ảnh
    .replace(/\[([^\]]+)\]\(.*?\)/g, '$1') // giữ text của link
    .replace(/[#*_~`>|-]/g, '')     // gỡ markdown markers
    .replace(/\n+/g, ' ')
    .trim();
}

export function stopSpeaking() {
  if (typeof window !== 'undefined' && window.speechSynthesis) {
    window.speechSynthesis.cancel();
  }
}

export function speakText(text, lang, callbacks = {}) {
  if (typeof window === 'undefined' || !window.speechSynthesis) return;

  stopSpeaking();

  const cleanText = cleanTextForSpeech(text);
  if (!cleanText) return;

  const targetLang = lang || detectLanguage(cleanText);
  const utterance = new SpeechSynthesisUtterance(cleanText);

  const trySpeak = () => {
    const voices = window.speechSynthesis.getVoices();
    const langCode = targetLang.split('-')[0];

    const sameLangVoices = voices.filter(v =>
      v.lang === targetLang || v.lang.startsWith(langCode + '-')
    );

    const isNatural = v => /natural|neural|online/i.test(v.name);
    const isMicrosoft = v => /microsoft/i.test(v.name);
    const isGoogle = v => /google/i.test(v.name);

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
      utterance.rate = isNatural(chosenVoice) ? 1.0 : 0.92;
    } else {
      utterance.lang = targetLang;
      utterance.rate = 0.92;
    }

    utterance.pitch = 1;
    utterance.volume = 1;

    utterance.onstart = () => {
      if (callbacks.onStart) callbacks.onStart();
    };
    utterance.onend = () => {
      if (callbacks.onEnd) callbacks.onEnd();
    };
    utterance.onerror = (e) => {
      if (callbacks.onError) callbacks.onError(e);
    };

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
}

export function createSpeechRecognizer(callbacks = {}) {
  if (typeof window === 'undefined') return null;
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) return null;

  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = true;
  recognition.lang = 'vi-VN';

  recognition.onresult = (event) => {
    let transcript = '';
    for (let i = event.resultIndex; i < event.results.length; i++) {
      transcript += event.results[i][0].transcript;
    }
    if (callbacks.onResult) callbacks.onResult(transcript);
  };

  recognition.onend = () => {
    if (callbacks.onEnd) callbacks.onEnd();
  };

  recognition.onerror = (err) => {
    if (callbacks.onError) callbacks.onError(err);
  };

  return recognition;
}
