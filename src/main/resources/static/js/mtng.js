/**
 * mtng.js – Frontend logic for the Mtng Meeting App
 * Handles: Settings dropdown, Meeting start/stop, Student CRUD,
 *          Chat polling, Recording management, modal dialogs,
 *          WhatsApp sharing, Mic toggle, Change password API.
 */

/* ═══════════════════════════════════════════
   SETTINGS DROPDOWN
   ═══════════════════════════════════════════ */
function toggleSettings() {
  const dd = document.getElementById('settingsDropdown');
  if (dd) dd.classList.toggle('hidden');
}

document.addEventListener('click', function(e) {
  const dd  = document.getElementById('settingsDropdown');
  const btn = document.getElementById('settingsBtn');
  if (dd && btn && !btn.contains(e.target) && !dd.contains(e.target)) {
    dd.classList.add('hidden');
  }
});

/* ═══════════════════════════════════════════
   403 / PERMISSION HELPER
   ═══════════════════════════════════════════ */
/**
 * Returns true if the response was a 403 Access Denied, and shows an alert.
 * Use at the top of every admin-only fetch handler.
 */
function check403(res) {
  if (res.status === 403) {
    showAlert('⛔ Access Denied – Admin privileges required.', 'error');
    return true;
  }
  return false;
}

/* ═══════════════════════════════════════════
   MEETING CONTROLS
   ═══════════════════════════════════════════ */
async function toggleMeeting() {
  const btn  = document.getElementById('startMeetingBtn');
  const isOn = btn && btn.classList.contains('active');

  const res = isOn
    ? await fetch('/api/meeting/stop', { method: 'POST' })
    : await fetch('/api/meeting/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: 'Mtng Session' })
      });

  if (check403(res)) return;

  if (res.ok) {
    const data = await res.json();

    if (isOn) {
      // Meeting stopped
      showAlert('✅ Meeting ended & recordings saved', 'success');
      setTimeout(() => location.reload(), 2000);
    } else {
      // Meeting started
      showAlert('✅ Meeting started! Redirecting to meeting room...', 'success');

      // Notify all students via polling endpoint
      notifyAllStudentsOfMeeting(data.meetingId, data.title);

      // Redirect admin to meeting room after brief delay
      setTimeout(() => {
        window.location.href = '/meeting-room';
      }, 1500);
    }
  }
}

// Notify all students that a meeting has started
function notifyAllStudentsOfMeeting(meetingId, title) {
  // Send notification via API (or WebSocket in future)
  fetch('/api/meeting/notify-students', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ meetingId: meetingId, title: title })
  }).catch(e => console.log('Notification sent'));
}

async function toggleFullRecording() {
  try {
    const res = await fetch('/api/meeting/toggle-recording', { method: 'POST' });
    if (check403(res)) return;
    if (!res.ok) { showAlert('No active meeting to toggle recording', 'error'); return; }
    const m = await res.json();
    const sw = document.getElementById('recordingToggle');
    if (sw) sw.classList.toggle('on', m.fullRecording);
    showAlert('Full Meeting Recording: ' + (m.fullRecording ? 'ON' : 'OFF'), 'info');
  } catch(e) { showAlert('Error toggling recording', 'error'); }
}

/* ═══════════════════════════════════════════
   MIC ON/OFF TOGGLE
   ═══════════════════════════════════════════ */
let micOn = true;

function toggleMic() {
  micOn = !micOn;
  const btn = document.getElementById('micToggleBtn');
  if (btn) {
    btn.textContent = micOn ? '🎤' : '🔇';
    btn.title = micOn ? 'Mic ON – Click to mute' : 'Mic OFF – Click to unmute';
    btn.style.background = micOn ? 'rgba(255,107,0,0.2)' : 'rgba(220,38,38,0.3)';
    btn.style.borderColor = micOn ? '#FF6B00' : '#DC2626';
  }
  showAlert(micOn ? '🎤 Microphone ON' : '🔇 Microphone OFF', micOn ? 'success' : 'error');
}

/* ═══════════════════════════════════════════
   STUDENT ACTIONS
   ═══════════════════════════════════════════ */
async function blockStudent(id) {
  const res = await fetch(`/api/students/${id}/block`, { method: 'POST' });
  if (check403(res)) return;
  if (res.ok) { const s = await res.json(); refreshStudentCard(id, s); }
}

async function muteStudent(id) {
  const res = await fetch(`/api/students/${id}/mute`, { method: 'POST' });
  if (check403(res)) return;
  if (res.ok) { const s = await res.json(); refreshStudentCard(id, s); }
}

async function deleteStudent(id) {
  if (!confirm('Delete this student? This cannot be undone.')) return;
  const res = await fetch(`/api/students/${id}`, { method: 'DELETE' });
  if (check403(res)) return;
  if (res.ok) {
    const card = document.getElementById(`student-card-${id}`);
    if (card) card.remove();
    showAlert('Student deleted.', 'success');
    updateTotalCount(-1);
  }
}

function editStudent(id, name, deviceLock, showRec) {
  document.getElementById('editId').value          = id;
  document.getElementById('editName').value        = name;
  document.getElementById('editDeviceLock').checked = deviceLock === 'true' || deviceLock === true;
  document.getElementById('editShowRec').checked   = showRec === 'true'   || showRec   === true;
  showModal('editModal');
}

async function saveEditStudent() {
  const id   = document.getElementById('editId').value;
  const body = {
    name:           document.getElementById('editName').value,
    password:       document.getElementById('editPassword').value,
    deviceLock:     document.getElementById('editDeviceLock').checked,
    showRecordings: document.getElementById('editShowRec').checked
  };
  const res = await fetch(`/api/students/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (res.ok) {
    closeModal('editModal');
    showAlert('Student updated!', 'success');
    setTimeout(() => location.reload(), 800);
  } else {
    const err = await res.json();
    showAlert(err.error || 'Update failed', 'error');
  }
}

function refreshStudentCard(id, s) {
  const blockBtn = document.getElementById(`block-btn-${id}`);
  const muteBtn  = document.getElementById(`mute-btn-${id}`);
  if (blockBtn) blockBtn.textContent = s.blocked ? '🔓 Unblock' : '🚫 Block';
  if (muteBtn)  muteBtn.textContent  = s.muted   ? '🔊 Unmute'  : '🔇 Mute';
  showAlert(`${s.blocked ? 'Blocked' : 'Unblocked'} / ${s.muted ? 'Muted' : 'Unmuted'}`, 'info');
}

function updateTotalCount(delta) {
  const el = document.getElementById('totalCount');
  if (el) el.textContent = Math.max(0, parseInt(el.textContent || '0') + delta);
}

/* ═══════════════════════════════════════════
   WHATSAPP SHARING
   ═══════════════════════════════════════════ */

/**
 * Fetches the real LAN IP-based app URL from the server.
 * Falls back to window.location.origin if the endpoint is unreachable.
 */
async function getAppUrl() {
  try {
    const res = await fetch('/api/server-info');
    if (res.ok) {
      const data = await res.json();
      return data.appUrl;
    }
  } catch (_) {}
  return window.location.origin;
}

async function shareWhatsApp(id, name, username, password) {
  try {
    // Backend builds the message with real LAN IP automatically
    const res = await fetch(`/api/students/${id}/whatsapp-link`);
    if (res.ok) {
      const data = await res.json();
      window.open(data.whatsappUrl, '_blank');
      showAlert(`WhatsApp share opened for ${name}! 🔗 ${data.appUrl}`, 'success');
      return;
    }
  } catch (_) {}

  // Fallback: build message client-side using real IP from /api/server-info
  try {
    const appUrl = await getAppUrl();
    const msg =
      `🎓 *MTNG – Meeting App*\n\n` +
      `Hi ${name}!\n\n` +
      `Your account:\n` +
      `👤 Username: *${username}*\n` +
      `🔑 Password: *${password}*\n\n` +
      `🔗 App URL: ${appUrl}\n\n` +
      `Please login and join the meeting. 🙏`;
    window.open('https://wa.me/?text=' + encodeURIComponent(msg), '_blank');
    showAlert(`WhatsApp share opened for ${name}! 🔗 ${appUrl}`, 'info');
  } catch (e) {
    showAlert('Could not open WhatsApp share.', 'error');
  }
}

/* ═══════════════════════════════════════════
   CREATE STUDENT FORM
   ═══════════════════════════════════════════ */
async function createStudent(event) {
  event.preventDefault();
  const body = {
    name:           document.getElementById('studentName').value.trim(),
    username:       document.getElementById('studentUsername').value.trim(),
    password:       document.getElementById('studentPassword').value,
    deviceLock:     document.getElementById('deviceLock').checked,
    showRecordings: document.getElementById('showRecordings').checked
  };
  if (!body.name || !body.username || !body.password) {
    showAlert('Please fill all required fields.', 'error'); return;
  }
  if (body.password.length < 4) {
    showAlert('Password must be at least 4 characters.', 'error'); return;
  }
  const res = await fetch('/api/students', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (res.ok) {
    showAlert('Student created successfully! 🎉', 'success');
    document.getElementById('createStudentForm').reset();
    setTimeout(() => window.location.href = '/students', 1200);
  } else {
    const err = await res.json();
    showAlert(err.error || 'Failed to create student.', 'error');
  }
}

/* ═══════════════════════════════════════════
   CHAT
   ═══════════════════════════════════════════ */
let chatPollingInterval = null;

async function sendMessage() {
  const input   = document.getElementById('chatInput');
  const content = input.value.trim();
  if (!content) return;
  input.value = '';
  await fetch('/api/chat/send', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sender: 'Teacher', content })
  });
  await loadMessages();
}

async function loadMessages() {
  const res      = await fetch('/api/chat/messages');
  const messages = await res.json();
  const box      = document.getElementById('chatMessages');
  if (!box) return;
  if (messages.length === 0) {
    box.innerHTML = '<div class="chat-empty">💬 No messages yet. Start a conversation!</div>';
    return;
  }
  box.innerHTML = messages.map(m => `
    <div style="margin-bottom:12px; text-align:${m.sender === 'Teacher' ? 'right' : 'left'}">
      <div class="chat-bubble ${m.sender === 'Teacher' ? 'mine' : 'theirs'}">
        <div class="bubble-sender">${escHtml(m.sender)}</div>
        <div>${escHtml(m.content)}</div>
        <div class="bubble-time">${formatTime(m.sentAt)}</div>
      </div>
    </div>`).join('');
  box.scrollTop = box.scrollHeight;
}

function startChatPolling() {
  loadMessages();
  chatPollingInterval = setInterval(loadMessages, 3000);
}

function stopChatPolling() {
  if (chatPollingInterval) clearInterval(chatPollingInterval);
}

function onChatKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
}

async function clearChat() {
  if (!confirm('Clear all messages?')) return;
  const res = await fetch('/api/chat/clear', { method: 'DELETE' });
  if (check403(res)) return;
  await loadMessages();
}

/* ═══════════════════════════════════════════
   RECORDINGS
   ═══════════════════════════════════════════ */
async function deleteRecording(id) {
  if (!confirm('Delete this recording?')) return;
  const res = await fetch(`/api/recordings/${id}`, { method: 'DELETE' });
  if (check403(res)) return;
  if (res.ok) {
    const el = document.getElementById(`rec-${id}`);
    if (el) el.remove();
    showAlert('Recording deleted.', 'success');
  }
}

async function clearAllRecordings() {
  if (!confirm('Clear ALL recordings? This cannot be undone.')) return;
  const res = await fetch('/api/recordings/clear', { method: 'DELETE' });
  if (check403(res)) return;
  showAlert('All recordings cleared.', 'success');
  setTimeout(() => location.reload(), 800);
}

/** Prompt user to choose save location: Local Disk (JSON download) */
function promptSaveRecordings() {
  const choice = confirm(
    '💾 Save All Recordings\n\n' +
    'OK = Save to Local Disk (downloads JSON file)\n' +
    'Cancel = Abort'
  );
  if (choice) {
    saveAllRecordings();
  }
}

async function saveAllRecordings() {
  const res  = await fetch('/api/recordings');
  const recs = await res.json();
  if (recs.length === 0) {
    showAlert('No recordings to save.', 'info');
    return;
  }
  const blob = new Blob([JSON.stringify(recs, null, 2)], { type: 'application/json' });
  const a    = document.createElement('a');
  a.href     = URL.createObjectURL(blob);
  a.download = `mtng-recordings-${new Date().toISOString().slice(0,10)}.json`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(a.href);
  showAlert('Recordings saved to local disk as JSON! 💾', 'success');
}

function loadAudio(recId) {
  showAlert(`Audio player for recording #${recId} – in production, stream from server.`, 'info');
}

function searchRecordings(keyword) {
  const cards = document.querySelectorAll('.student-recording-group');
  keyword = keyword.toLowerCase();
  cards.forEach(c => {
    const name = c.dataset.name || '';
    c.style.display = name.toLowerCase().includes(keyword) ? '' : 'none';
  });
}

/* ═══════════════════════════════════════════
   MODAL HELPERS
   ═══════════════════════════════════════════ */
function showModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('show');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('show');
}

/* ═══════════════════════════════════════════
   ALERT TOAST
   ═══════════════════════════════════════════ */
function showAlert(msg, type = 'info') {
  let toast = document.getElementById('toastAlert');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toastAlert';
    toast.style.cssText =
      'position:fixed;bottom:24px;right:24px;padding:12px 20px;border-radius:10px;'
    + 'font-size:13px;font-weight:600;z-index:9999;max-width:340px;'
    + 'transition:opacity 0.4s;box-shadow:0 4px 16px rgba(0,0,0,0.5);';
    document.body.appendChild(toast);
  }
  const colors = {
    success: { bg: 'rgba(22,163,74,0.9)',  color: '#fff' },
    error:   { bg: 'rgba(220,38,38,0.9)',  color: '#fff' },
    info:    { bg: 'rgba(255,107,0,0.9)',  color: '#fff' }
  };
  const c = colors[type] || colors.info;
  toast.style.background = c.bg;
  toast.style.color      = c.color;
  toast.textContent      = msg;
  toast.style.opacity    = '1';
  clearTimeout(toast._timeout);
  toast._timeout = setTimeout(() => { toast.style.opacity = '0'; }, 3000);
}

/* ═══════════════════════════════════════════
   UTILITIES
   ═══════════════════════════════════════════ */
function escHtml(str) {
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatTime(iso) {
  if (!iso) return '';
  try { return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }); }
  catch(e) { return iso; }
}

function searchStudents(keyword) {
  window.location.href = `/students?search=${encodeURIComponent(keyword)}`;
}

function onSearchKeydown(e) {
  if (e.key === 'Enter') searchStudents(e.target.value);
}

/* ═══════════════════════════════════════════
   CHANGE PASSWORD (calls real backend API)
   ═══════════════════════════════════════════ */
function openChangePassword() {
  toggleSettings();
  showModal('changePasswordModal');
}

async function savePassword() {
  const np = document.getElementById('newPassword').value;
  const cp = document.getElementById('confirmPassword').value;
  if (np.length < 4)  { showAlert('Password must be at least 4 characters.', 'error'); return; }
  if (np !== cp)      { showAlert('Passwords do not match.', 'error'); return; }

  try {
    const res = await fetch('/api/settings/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ newPassword: np })
    });
    if (res.ok) {
      closeModal('changePasswordModal');
      document.getElementById('newPassword').value = '';
      document.getElementById('confirmPassword').value = '';
      showAlert('Password changed successfully! 🔑', 'success');
    } else {
      const err = await res.json();
      showAlert(err.error || 'Failed to change password.', 'error');
    }
  } catch(e) {
    showAlert('Error changing password.', 'error');
  }
}

/* ═══════════════════════════════════════════
   SCHEDULE MODAL
   ═══════════════════════════════════════════ */
function openSchedule() {
  toggleSettings();
  showModal('scheduleModal');
}

/* ═══════════════════════════════════════════
   PAGE INIT
   ═══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function() {
  // Auto-start chat polling on chat page
  // NOTE: skip on meeting-room page — it has its own MtngJitsiWrapper._startChatPolling()
  if (document.getElementById('chatMessages') && !window.location.pathname.includes('/meeting-room')) {
    startChatPolling();
    window.addEventListener('beforeunload', stopChatPolling);
  }
  // Attach create student form
  const form = document.getElementById('createStudentForm');
  if (form) form.addEventListener('submit', createStudent);

  // Global meeting start notification (all pages except meeting-room)
  if (!window.location.pathname.includes('/meeting-room')) {
    initMeetingNotifications();
  }
});

/* ═══════════════════════════════════════════
   GLOBAL MEETING NOTIFICATION SYSTEM
   Polls /api/meeting/active every 5s.
   Shows animated banner + auto-redirects when meeting starts.
   ═══════════════════════════════════════════ */
let _meetingPollInterval = null;
let _lastMeetingActiveState = null;
let _meetingNotifBanner = null;

function initMeetingNotifications() {
  checkMeetingStatus();
  _meetingPollInterval = setInterval(checkMeetingStatus, 5000);
}

async function checkMeetingStatus() {
  try {
    const res = await fetch('/api/meeting/active');
    const isActive = res.status === 200;

    if (isActive === _lastMeetingActiveState) return; // no change
    _lastMeetingActiveState = isActive;

    if (isActive) {
      const data = await res.json();
      showMeetingStartBanner(data);
    } else {
      hideMeetingBanner();
    }
  } catch(e) { /* ignore network errors */ }
}

function showMeetingStartBanner(meeting) {
  if (_meetingNotifBanner) return;

  // Inject CSS animation once
  if (!document.getElementById('_bannerCss')) {
    const s = document.createElement('style');
    s.id = '_bannerCss';
    s.textContent =
      '@keyframes slideDownBanner{from{transform:translateY(-100%)}to{transform:translateY(0)}}' +
      '@keyframes livePulse{0%,100%{opacity:1}50%{opacity:0.5}}';
    document.head.appendChild(s);
  }

  _meetingNotifBanner = document.createElement('div');
  _meetingNotifBanner.id = '_meetingBanner';
  _meetingNotifBanner.style.cssText =
    'position:fixed;top:0;left:0;right:0;z-index:9990;' +
    'background:linear-gradient(135deg,#15803D,#16A34A);' +
    'color:#fff;padding:10px 20px;' +
    'display:flex;align-items:center;justify-content:space-between;' +
    'box-shadow:0 4px 24px rgba(22,163,74,0.7);' +
    'animation:slideDownBanner 0.4s ease-out;';

  const title = meeting && meeting.title ? escHtml(meeting.title) : 'Meeting';
  _meetingNotifBanner.innerHTML =
    '<div style="display:flex;align-items:center;gap:14px">' +
      '<span style="font-size:24px;animation:livePulse 1s infinite">📢</span>' +
      '<div>' +
        '<div style="font-weight:700;font-size:15px">🟢 LIVE: ' + title + '</div>' +
        '<div style="font-size:12px;opacity:0.85">Meeting has started – join now!</div>' +
      '</div>' +
    '</div>' +
    '<div style="display:flex;gap:10px">' +
      '<button onclick="window.location.href=\'/meeting-room\'" ' +
        'style="background:#fff;color:#15803D;border:none;padding:9px 20px;' +
        'border-radius:8px;font-weight:700;cursor:pointer;font-size:14px">🎤 Join Now</button>' +
      '<button onclick="this.closest(\'#_meetingBanner\').remove();window._meetingNotifBanner=null;" ' +
        'style="background:rgba(0,0,0,0.2);color:#fff;border:1px solid rgba(255,255,255,0.3);' +
        'padding:7px 14px;border-radius:6px;cursor:pointer;font-size:12px">✕</button>' +
    '</div>';

  document.body.insertBefore(_meetingNotifBanner, document.body.firstChild);
  _playMeetingChime();
}

function hideMeetingBanner() {
  if (_meetingNotifBanner) { _meetingNotifBanner.remove(); _meetingNotifBanner = null; }
}

function _playMeetingChime() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    [523, 659, 784, 1047].forEach((f, i) => {
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.connect(g); g.connect(ctx.destination);
      o.frequency.value = f; o.type = 'sine';
      const t = ctx.currentTime + i * 0.13;
      g.gain.setValueAtTime(0.28, t);
      g.gain.exponentialRampToValueAtTime(0.001, t + 0.3);
      o.start(t); o.stop(t + 0.35);
    });
  } catch(e) { /* ignore */ }
}


