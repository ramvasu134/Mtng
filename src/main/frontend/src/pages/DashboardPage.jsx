import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { meeting as meetingApi, students as studentsApi } from '../api';
import Modal from '../components/Modal';

export default function DashboardPage() {
  const { isAdmin, user } = useAuth();
  const { toast } = useOutletContext();
  const navigate = useNavigate();

  const [activeRooms, setActiveRooms] = useState([]);
  const [stats, setStats] = useState({ online: 0, total: 0 });
  const [busy, setBusy] = useState(false);

  // Create Room Modal state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [roomTitle, setRoomTitle] = useState('');
  const [onlineStudents, setOnlineStudents] = useState([]);
  const [allStudents, setAllStudents] = useState([]);
  const [selectedParticipants, setSelectedParticipants] = useState([]);
  const [loadingStudents, setLoadingStudents] = useState(false);

  // ── Refresh active rooms & stats ──────────────────────────────────────────
  const refresh = useCallback(async () => {
    try {
      const [rooms, s] = await Promise.all([
        meetingApi.rooms().catch(() => []),
        studentsApi.onlineCount(),
      ]);
      setActiveRooms(rooms || []);
      setStats(s);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { refresh(); const id = setInterval(refresh, 5000); return () => clearInterval(id); }, [refresh]);

  // ── WebSocket listener for room invitations (students) ────────────────────
  useEffect(() => {
    if (isAdmin) return; // Admin doesn't need invitation notifications
    const protocol = window.location.protocol === 'https:' ? 'https' : 'http';
    const wsUrl = `${protocol}://${window.location.host}/ws`;
    let stomp = null;
    try {
      const socket = new window.SockJS(wsUrl);
      stomp = window.Stomp.over(socket);
      stomp.debug = null;
      stomp.connect({}, () => {
        stomp.subscribe('/topic/room-invitations', (msg) => {
          const data = JSON.parse(msg.body);
          if (data.type === 'room-invitation' && data.participants?.includes(user?.username)) {
            toast.success(`📞 You're invited to "${data.title}" by ${data.createdBy}!`);
            refresh();
          } else if (data.type === 'all-rooms-ended') {
            refresh();
          }
        });
      });
    } catch { /* ignore */ }
    return () => { try { stomp?.disconnect(); } catch {} };
  }, [isAdmin, user, toast, refresh]);

  // ── Open Create Room Modal ────────────────────────────────────────────────
  const openCreateModal = async () => {
    setShowCreateModal(true);
    setLoadingStudents(true);
    setRoomTitle('');
    setSelectedParticipants([]);
    try {
      const [online, all] = await Promise.all([
        studentsApi.onlineList().catch(() => []),
        studentsApi.list(),
      ]);
      setOnlineStudents(online || []);
      setAllStudents(all || []);
    } catch { /* ignore */ }
    finally { setLoadingStudents(false); }
  };

  // ── Toggle participant selection ──────────────────────────────────────────
  const toggleParticipant = (username) => {
    setSelectedParticipants(prev =>
      prev.includes(username)
        ? prev.filter(u => u !== username)
        : [...prev, username]
    );
  };

  const toggleSelectAll = () => {
    const allUsernames = allStudents.map(s => s.username);
    if (selectedParticipants.length === allStudents.length) {
      setSelectedParticipants([]);
    } else {
      setSelectedParticipants(allUsernames);
    }
  };

  // ── Create Room ───────────────────────────────────────────────────────────
  const createRoom = async () => {
    if (!roomTitle.trim()) { toast.error('Please enter a room name'); return; }
    setBusy(true);
    try {
      const res = await meetingApi.start(
        roomTitle.trim(),
        selectedParticipants.length > 0 ? selectedParticipants : null,
      );
      toast.success(`Room "${roomTitle}" created!`);
      setShowCreateModal(false);
      setRoomTitle('');
      setSelectedParticipants([]);
      refresh();
      // Auto-navigate admin to the new room
      navigate(`/meeting-room/${res.roomName}`);
    } catch (e) {
      toast.error(e.message || 'Failed to create room');
    } finally { setBusy(false); }
  };

  // ── Stop a specific room ──────────────────────────────────────────────────
  const stopRoom = async (roomName) => {
    setBusy(true);
    try {
      await meetingApi.stop(roomName);
      toast.success('Room ended.');
      refresh();
    } catch (e) {
      toast.error(e.message);
    } finally { setBusy(false); }
  };

  const stopAllRooms = async () => {
    setBusy(true);
    try {
      await meetingApi.stopAll();
      toast.success('All rooms ended.');
      refresh();
    } catch (e) { toast.error(e.message); }
    finally { setBusy(false); }
  };

  // ── Join a room ───────────────────────────────────────────────────────────
  const joinRoom = (roomName) => navigate(`/meeting-room/${roomName}`);

  /**
   * Helper: normalise invitedParticipants to an array regardless of whether
   * the server returned a JSON array (new normalised API) or a comma-string (legacy).
   */
  const toParticipantsArray = (invited) => {
    if (!invited) return [];
    if (Array.isArray(invited)) return invited;
    return invited.split(',').filter(Boolean);
  };

  // ── Check if user is invited to a room ────────────────────────────────────
  const isInvited = (room) => {
    if (isAdmin) return true;
    const arr = toParticipantsArray(room.invitedParticipants);
    if (arr.length === 0) return true; // no filter = open room
    return arr.includes(user?.username);
  };

  return (
    <div className="dashboard">
      {/* ── Stats Cards ──────────────────────────────── */}
      <div className="stats-row">
        <StatCard icon="👥" label="Total Students" value={stats.total} color="#667eea" />
        <StatCard icon="🟢" label="Online Now" value={stats.online} color="#22C55E" />
        <StatCard icon="🏠" label="Active Rooms" value={activeRooms.length} color={activeRooms.length > 0 ? '#EF4444' : '#888'} />
        <StatCard icon="🎤" label="In Meeting"
          value={activeRooms.reduce((sum, r) => sum + (r.inMeetingCount || 0), 0)} color="#FF6B00" />
      </div>

      {/* ── Room Controls (Admin) ────────────────────── */}
      <div className="card">
        <div className="card-title-row">
          <h2 className="card-title">🏠 Meeting Rooms</h2>
          {isAdmin && (
            <div className="btn-row">
              <button className="btn btn-primary" onClick={openCreateModal}>
                ➕ Create Room
              </button>
              {activeRooms.length > 1 && (
                <button className="btn btn-danger btn-sm" onClick={stopAllRooms} disabled={busy}>
                  ⏹️ End All Rooms
                </button>
              )}
            </div>
          )}
        </div>

        {activeRooms.length === 0 ? (
          <div className="no-meeting">
            <p className="empty-icon-lg">📭</p>
            <p className="text-muted">
              {isAdmin ? 'No active rooms. Create one to get started!' : 'No active rooms right now. Your teacher will create one soon!'}
            </p>
            <button className="btn btn-outline" onClick={refresh}>🔄 Refresh</button>
          </div>
        ) : (
          <div className="rooms-grid">
            {activeRooms.filter(isInvited).map(room => {
              const invitedArr = toParticipantsArray(room.invitedParticipants);
              return (
              <div key={room.id} className="room-card">
                <div className="room-card-header">
                  <span className="live-badge">🔴 LIVE</span>
                  <span className="room-card-title">{room.title}</span>
                </div>
                <div className="room-card-info">
                  <span>🔑 <code>{room.roomName}</code></span>
                  <span>👥 {room.inMeetingCount || 0} in room</span>
                  {room.createdBy && <span>👤 by {room.createdBy}</span>}
                  {invitedArr.length > 0 && (
                    <span className="room-participants-tag">
                      🎯 {invitedArr.length} invited
                    </span>
                  )}
                  <span className="text-muted" style={{fontSize: 11}}>
                    Started: {new Date(room.startTime).toLocaleTimeString()}
                  </span>
                </div>
                {invitedArr.length > 0 && (
                  <div className="room-invited-list">
                    {invitedArr.map(u => (
                      <span key={u} className="invited-chip">{u}</span>
                    ))}
                  </div>
                )}
                <div className="room-card-actions">
                  <button className="btn btn-success btn-sm" onClick={() => joinRoom(room.roomName)}>
                    🎙️ Join
                  </button>
                  {isAdmin && (
                    <button className="btn btn-danger btn-sm" onClick={() => stopRoom(room.roomName)} disabled={busy}>
                      ⏹️ End
                    </button>
                  )}
                </div>
              </div>
              );
            })}
          </div>
        )}
      </div>

      {/* ── Quick Info ────────────────────────────────── */}
      <div className="card">
        <h2 className="card-title">📊 Quick Information</h2>
        <div className="info-grid">
          <div className="info-item"><span>👤 Logged in as</span><strong>{user?.displayName || user?.username}</strong></div>
          <div className="info-item"><span>🔑 Role</span><strong>{user?.role}</strong></div>
          <div className="info-item"><span>🌐 Session</span><strong>Active</strong></div>
        </div>
      </div>

      {/* ═══ CREATE ROOM MODAL ═══════════════════════════════════════════════ */}
      <Modal open={showCreateModal} onClose={() => setShowCreateModal(false)}
             title="➕ Create Meeting Room" width={560}>
        <div className="create-room-modal">
          {/* Room Name */}
          <div className="form-group">
            <label>Room Name *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Physics Class, Group A Discussion"
              value={roomTitle}
              onChange={e => setRoomTitle(e.target.value)}
              autoFocus
            />
          </div>

          {/* Participant Selection */}
          <div className="form-group" style={{marginTop: 16}}>
            <div className="participants-header">
              <label>Select Participants (optional)</label>
              <button className="btn btn-sm btn-outline" onClick={toggleSelectAll}>
                {selectedParticipants.length === allStudents.length ? '☐ Deselect All' : '☑ Select All'}
              </button>
            </div>
            <p className="text-muted" style={{fontSize: 12, margin: '4px 0 8px'}}>
              Leave empty to allow all users. Select specific students for a private room.
            </p>

            {loadingStudents ? (
              <div className="text-muted" style={{textAlign:'center', padding: 20}}>Loading students…</div>
            ) : (
              <div className="participant-selector">
                {allStudents.length === 0 ? (
                  <div className="text-muted" style={{textAlign:'center', padding: 16}}>No students found</div>
                ) : (
                  allStudents.map(s => {
                    const isOnline = s.status === 'ONLINE';
                    const isSelected = selectedParticipants.includes(s.username);
                    return (
                      <div
                        key={s.id}
                        className={`participant-select-item ${isSelected ? 'selected' : ''} ${isOnline ? 'online' : 'offline'}`}
                        onClick={() => toggleParticipant(s.username)}
                      >
                        <div className="psi-checkbox">{isSelected ? '☑' : '☐'}</div>
                        <div className="psi-avatar">{(s.name || s.username)[0].toUpperCase()}</div>
                        <div className="psi-info">
                          <div className="psi-name">{s.name || s.username}</div>
                          <div className="psi-username">@{s.username}</div>
                        </div>
                        <div className={`psi-status ${isOnline ? 'on' : 'off'}`}>
                          {isOnline ? '🟢 Online' : '⚪ Offline'}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}
            {selectedParticipants.length > 0 && (
              <div className="selected-count">
                ✅ {selectedParticipants.length} participant{selectedParticipants.length > 1 ? 's' : ''} selected
              </div>
            )}
          </div>

          {/* Create Button */}
          <div className="btn-row" style={{marginTop: 20, justifyContent: 'flex-end'}}>
            <button className="btn btn-outline" onClick={() => setShowCreateModal(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={createRoom} disabled={busy || !roomTitle.trim()}>
              {busy ? '⏳ Creating…' : '🚀 Create & Join Room'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

function StatCard({ icon, label, value, color }) {
  return (
    <div className="stat-card" style={{ borderTopColor: color }}>
      <span className="stat-icon">{icon}</span>
      <span className="stat-value" style={{ color }}>{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}
