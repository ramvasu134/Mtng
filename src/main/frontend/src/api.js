/**
 * api.js – Centralized API client for MTNG React SPA.
 * All REST calls go through these functions.
 * Session cookies handled automatically (same-origin).
 * Mobile (Capacitor) builds use the configured server URL.
 */
import { getBaseUrl } from './mobile-config.js';

const BASE = getBaseUrl();  // '' for web (same-origin), full URL for mobile

async function request(url, options = {}) {
  const res = await fetch(BASE + url, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (res.status === 401) throw new AuthError('Not authenticated');
  if (res.status === 403) throw new ForbiddenError('Access denied');
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new ApiError(body.error || res.statusText, res.status);
  }
  if (res.status === 204) return null;
  return res.json();
}

export class AuthError extends Error { constructor(m) { super(m); this.name = 'AuthError'; } }
export class ForbiddenError extends Error { constructor(m) { super(m); this.name = 'ForbiddenError'; } }
export class ApiError extends Error {
  constructor(m, status) { super(m); this.name = 'ApiError'; this.status = status; }
}

// ── Auth ────────────────────────────────────────────────────────────────────
export const auth = {
  login: (username, password) =>
    request('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  me: () => request('/api/auth/me'),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
};

// ── Students ────────────────────────────────────────────────────────────────
export const students = {
  list: ()           => request('/api/students'),
  get: (id)          => request(`/api/students/${id}`),
  create: (data)     => request('/api/students', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/api/students/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id)       => request(`/api/students/${id}`, { method: 'DELETE' }),
  block: (id)        => request(`/api/students/${id}/block`, { method: 'POST' }),
  mute: (id)         => request(`/api/students/${id}/mute`, { method: 'POST' }),
  onlineCount: ()    => request('/api/students/online'),
  onlineList: ()     => request('/api/students/online-list'),
  whatsappLink: (id) => request(`/api/students/${id}/whatsapp-link`),
  heartbeat: ()      => request('/api/students/heartbeat', { method: 'POST' }),
};

// ── Meeting (Multi-Room) ────────────────────────────────────────────────────
export const meeting = {
  /** Get first active meeting (backward compat) */
  active: () =>
    fetch(BASE + '/api/meeting/active', { credentials: 'include' })
      .then(r => r.status === 204 ? null : r.json()),
  /** Get ALL active rooms */
  rooms: () => request('/api/meeting/rooms'),
  /** Get specific room by roomName */
  getRoom: (roomName) => request(`/api/meeting/room/${roomName}`),
  /** Start a new room (admin) with optional participants list */
  start: (title, participants) =>
    request('/api/meeting/start', {
      method: 'POST',
      body: JSON.stringify({ title, participants }),
    }),
  /** Stop a specific room by roomName, or first active if null */
  stop: (roomName) =>
    request('/api/meeting/stop', {
      method: 'POST',
      body: JSON.stringify({ roomName }),
    }),
  /** Stop ALL active rooms */
  stopAll: () =>
    request('/api/meeting/stop-all', { method: 'POST' }),
  /** Join a specific room */
  join: (roomName) =>
    request('/api/meeting/join', {
      method: 'POST',
      body: JSON.stringify({ roomName }),
    }),
  /** Leave a specific room */
  leave: (roomName) =>
    request('/api/meeting/leave', {
      method: 'POST',
      body: JSON.stringify({ roomName }),
    }),
  toggleRecording: (meetingId) =>
    request('/api/meeting/toggle-recording', {
      method: 'POST',
      body: JSON.stringify({ meetingId }),
    }),
};

// ── Chat ────────────────────────────────────────────────────────────────────
export const chat = {
  messages: ()       => request('/api/chat/messages'),
  send: (content)    => request('/api/chat/send', { method: 'POST', body: JSON.stringify({ content }) }),
  clear: ()          => request('/api/chat/clear', { method: 'DELETE' }),
};

// ── Recordings ──────────────────────────────────────────────────────────────
export const recordings = {
  list: ()           => request('/api/recordings'),
  audio: (id)        => `${BASE}/api/recordings/${id}/audio`,
  delete: (id)       => request(`/api/recordings/${id}`, { method: 'DELETE' }),
  clear: ()          => request('/api/recordings/clear', { method: 'DELETE' }),
  saveSession: (data) =>
    request('/api/recordings/save-session', { method: 'POST', body: JSON.stringify(data) }),
  upload: (id, blob) => {
    const fd = new FormData();
    fd.append('audio', blob, 'recording.webm');
    return fetch(`${BASE}/api/recordings/${id}/upload-audio`, {
      method: 'POST', body: fd, credentials: 'include',
    }).then(r => r.json());
  },
};

// ── Settings ────────────────────────────────────────────────────────────────
export const settings = {
  changePassword: (newPassword) =>
    request('/api/settings/change-password', { method: 'POST', body: JSON.stringify({ newPassword }) }),
  serverInfo: () => request('/api/server-info'),
};
