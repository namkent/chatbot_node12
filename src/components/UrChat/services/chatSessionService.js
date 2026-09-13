/**
 * Chat Session Service: Quản lý lịch sử đa phiên hội thoại (Multi-session Chats)
 * Hỗ trợ lưu trữ bền vững vào H2 Database và cache tức thì tại localStorage.
 * Người dùng mặc định: nam.dovan (Do Van Nam)
 */

export const DEFAULT_USER_ID = 'nam.dovan';
export const DEFAULT_USER_NAME = 'Do Van Nam';

const STORAGE_KEY_SESSIONS = 'urchat_sessions_v2';
const STORAGE_KEY_ACTIVE_ID = 'urchat_active_session_id_v2';

export function generateSessionId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().replace(/-/g, '').substring(0, 12);
  }
  // Fallback: 12 hex characters
  const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  return (s4() + s4() + s4());
}

export function getAllSessions() {
  if (typeof window === 'undefined') return [];
  try {
    const raw = localStorage.getItem(STORAGE_KEY_SESSIONS);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    console.warn('[ChatSessionService] Lỗi nạp sessions từ cache:', e);
    return [];
  }
}

export function saveAllSessions(sessions) {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(STORAGE_KEY_SESSIONS, JSON.stringify(sessions));
  } catch (e) {
    console.warn('[ChatSessionService] Lỗi lưu sessions vào cache:', e);
  }
}

export function getActiveSessionId() {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(STORAGE_KEY_ACTIVE_ID) || null;
}

export function setActiveSessionId(id) {
  if (typeof window === 'undefined') return;
  if (id) {
    localStorage.setItem(STORAGE_KEY_ACTIVE_ID, id);
  } else {
    localStorage.removeItem(STORAGE_KEY_ACTIVE_ID);
  }
}

/**
 * Lấy danh sách sessions từ H2 DB backend (POST/GET /api/sessions)
 */
export async function fetchRemoteSessions(userId = DEFAULT_USER_ID) {
  try {
    const res = await fetch(`/api/sessions?userId=${encodeURIComponent(userId)}`, {
      headers: { 'X-User-Id': userId }
    });
    if (res.ok) {
      const remoteList = await res.json();
      if (Array.isArray(remoteList)) {
        // Hợp nhất với local cache
        const localList = getAllSessions();
        const mergedMap = new Map();
        remoteList.forEach((s) => mergedMap.set(s.id, s));
        localList.forEach((s) => {
          if (!mergedMap.has(s.id)) {
            mergedMap.set(s.id, s);
            syncSessionToRemote(s, userId);
          }
        });
        const mergedList = Array.from(mergedMap.values()).sort(
          (a, b) => (b.updatedAt || b.createdAt || 0) - (a.updatedAt || a.createdAt || 0)
        );
        saveAllSessions(mergedList);
        return mergedList;
      }
    }
  } catch (err) {
    console.warn('[ChatSessionService] Không thể kết nối API H2 sessions:', err.message);
  }
  return getAllSessions();
}

/**
 * Lấy chi tiết phiên chat gồm tin nhắn từ H2 DB
 */
export async function fetchRemoteSessionDetail(sessionId, userId = DEFAULT_USER_ID) {
  if (!sessionId) return null;
  try {
    const res = await fetch(`/api/sessions/${encodeURIComponent(sessionId)}?userId=${encodeURIComponent(userId)}`, {
      headers: { 'X-User-Id': userId }
    });
    if (res.ok) {
      return await res.json();
    }
  } catch (err) {
    console.warn(`[ChatSessionService] Lỗi nạp chi tiết session ${sessionId}:`, err.message);
  }
  return null;
}

/**
 * Đồng bộ phiên chat và nội dung tin nhắn lên H2 Database
 */
export async function syncSessionToRemote(session, userId = DEFAULT_USER_ID, userName = DEFAULT_USER_NAME) {
  if (!session || !session.id) return;
  try {
    const payload = {
      id: session.id,
      userId: userId,
      userName: userName,
      title: session.title || 'Cuộc trò chuyện mới',
      emoji: session.emoji || '',
      isCustomRenamed: !!session.isCustomRenamed,
      model: session.model || '',
      createdAt: session.createdAt || Date.now(),
      updatedAt: session.updatedAt || Date.now(),
      messageList: session.messageList || [],
      apiMessagesHistory: session.apiMessagesHistory || []
    };

    fetch('/api/sessions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId
      },
      body: JSON.stringify(payload)
    }).catch((e) => console.warn('[ChatSessionService] Lỗi ngầm sync lên H2 DB:', e));
  } catch (e) {}
}

export function createNewSession(title = 'Cuộc trò chuyện mới', initialMessages = [], systemPrompt = '', userId = DEFAULT_USER_ID, userName = DEFAULT_USER_NAME) {
  const msgs = Array.isArray(initialMessages) ? initialMessages : [];
  const apiHistory = [];
  if (typeof systemPrompt === 'string' && systemPrompt.trim()) {
    apiHistory.push({ role: 'system', content: systemPrompt.trim() });
  }

  const newSession = {
    id: generateSessionId(),
    userId: userId,
    userName: userName,
    title: title,
    emoji: '💬',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    messageList: msgs,
    apiMessagesHistory: apiHistory,
    isCustomRenamed: false
  };

  const sessions = getAllSessions();
  sessions.unshift(newSession);
  saveAllSessions(sessions);
  setActiveSessionId(newSession.id);

  // Sync ngầm lên H2 DB
  syncSessionToRemote(newSession, userId, userName);
  return newSession;
}

export function getSessionById(id) {
  const sessions = getAllSessions();
  return sessions.find((s) => s.id === id) || null;
}

export function updateSession(id, updates, userId = DEFAULT_USER_ID) {
  const sessions = getAllSessions();
  const index = sessions.findIndex((s) => s.id === id);
  if (index !== -1) {
    sessions[index] = {
      ...sessions[index],
      ...updates,
      updatedAt: Date.now()
    };
    saveAllSessions(sessions);
    syncSessionToRemote(sessions[index], userId);
    return sessions[index];
  }
  return null;
}

export function renameSession(id, newTitle, userId = DEFAULT_USER_ID) {
  const updated = updateSession(id, { title: newTitle.trim() || 'Cuộc trò chuyện', isCustomRenamed: true }, userId);
  fetch(`/api/sessions/${encodeURIComponent(id)}/title`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId
    },
    body: JSON.stringify({ title: newTitle.trim() || 'Cuộc trò chuyện' })
  }).catch(() => {});
  return updated;
}

/**
 * Gọi backend API sinh tiêu đề kèm emoji cho cuộc trò chuyện
 */
export async function fetchSessionTitle(contentOrMessages, model = '') {
  try {
    const payload = {};
    if (typeof contentOrMessages === 'string') {
      payload.content = contentOrMessages;
    } else if (Array.isArray(contentOrMessages)) {
      payload.messages = contentOrMessages;
    }
    if (model) payload.model = model;

    const res = await fetch('/api/chat/title', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      const data = await res.json();
      if (data && data.title) {
        return data.title.trim();
      }
    }
  } catch (e) {
    console.warn('[chatSessionService] fetchSessionTitle error:', e);
  }
  return null;
}

export function deleteSession(id, userId = DEFAULT_USER_ID) {
  let sessions = getAllSessions();
  sessions = sessions.filter((s) => s.id !== id);
  saveAllSessions(sessions);

  if (getActiveSessionId() === id) {
    const nextSession = sessions[0] || null;
    setActiveSessionId(nextSession ? nextSession.id : null);
  }

  fetch(`/api/sessions/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    headers: { 'X-User-Id': userId }
  }).catch(() => {});

  return sessions;
}

export function clearAllSessions() {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(STORAGE_KEY_SESSIONS);
  localStorage.removeItem(STORAGE_KEY_ACTIVE_ID);
}

/**
 * Phân nhóm các phiên hội thoại theo mốc thời gian: Hôm nay, 7 ngày qua, 30 ngày qua, Cũ hơn
 */
export function groupSessionsByDate(sessions) {
  const now = Date.now();
  const oneDay = 24 * 60 * 60 * 1000;

  const groups = {
    today: [],
    past7Days: [],
    past30Days: [],
    older: []
  };

  sessions.forEach((sess) => {
    const diffDays = (now - (sess.updatedAt || sess.createdAt || now)) / oneDay;
    if (diffDays < 1) {
      groups.today.push(sess);
    } else if (diffDays < 7) {
      groups.past7Days.push(sess);
    } else if (diffDays < 30) {
      groups.past30Days.push(sess);
    } else {
      groups.older.push(sess);
    }
  });

  return groups;
}
