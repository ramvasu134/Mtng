import React, { useState, useEffect, useCallback } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { students as api } from '../api';
import Modal from '../components/Modal';

export default function StudentsPage() {
  const { isAdmin } = useAuth();
  const { toast } = useOutletContext();
  const [list, setList] = useState([]);
  const [search, setSearch] = useState('');
  const [editing, setEditing] = useState(null);
  const [editForm, setEditForm] = useState({});

  const load = useCallback(async () => {
    try { setList(await api.list()); } catch (e) { toast.error(e.message); }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  // ★ Auto-refresh student list every 5s for live online status
  useEffect(() => {
    const interval = setInterval(() => {
      api.list().then(data => setList(data)).catch(() => {});
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  const filtered = list.filter(s =>
    s.name?.toLowerCase().includes(search.toLowerCase()) ||
    s.username?.toLowerCase().includes(search.toLowerCase()) ||
    s.email?.toLowerCase().includes(search.toLowerCase())
  );

  const toggleBlock = async (id) => {
    try { await api.block(id); toast.success('Block toggled'); load(); } catch (e) { toast.error(e.message); }
  };
  const toggleMute = async (id) => {
    try { await api.mute(id); toast.success('Mute toggled'); load(); } catch (e) { toast.error(e.message); }
  };
  const deleteStudent = async (id) => {
    if (!confirm('Delete this student?')) return;
    try { await api.delete(id); toast.success('Student deleted'); load(); } catch (e) { toast.error(e.message); }
  };
  const shareWhatsApp = async (id) => {
    try {
      const data = await api.whatsappLink(id);
      window.open(data.whatsappUrl, '_blank');
    } catch (e) { toast.error(e.message); }
  };

  const openEdit = (s) => {
    setEditing(s);
    setEditForm({ name: s.name, email: s.email || '', phone: s.phone || '' });
  };
  const saveEdit = async (e) => {
    e.preventDefault();
    try {
      await api.update(editing.id, editForm);
      toast.success('Student updated!');
      setEditing(null);
      load();
    } catch (err) { toast.error(err.message); }
  };

  return (
    <div className="students-page">
      <div className="page-header">
        <h2>👥 Students ({filtered.length})</h2>
        <input
          type="text"
          className="form-input search-input"
          placeholder="🔍 Search students…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
      </div>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>#</th><th>Name</th><th>Username</th><th>Email</th><th>Status</th>
              {isAdmin && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr><td colSpan={isAdmin ? 6 : 5} className="empty-row">No students found</td></tr>
            ) : filtered.map((s, i) => {
              const isOnline = s.status === 'ONLINE';
              return (
                <tr key={s.id} className={s.blocked ? 'row-blocked' : ''}>
                  <td>{i + 1}</td>
                  <td>
                    <span className="student-name">{s.name}</span>
                    {isOnline && <span className="online-dot" title="Online">🟢</span>}
                  </td>
                  <td><code>{s.username}</code></td>
                  <td>{s.email || '—'}</td>
                  <td>
                    {s.blocked ? <span className="badge badge-red">Blocked</span>
                      : s.muted ? <span className="badge badge-yellow">Muted</span>
                      : isOnline ? <span className="badge badge-green">Online</span>
                      : <span className="badge badge-gray">Offline</span>}
                  </td>
                  {isAdmin && (
                    <td className="action-cell">
                      <button className="btn-icon" title="Edit" onClick={() => openEdit(s)}>✏️</button>
                      <button className="btn-icon" title={s.blocked ? 'Unblock' : 'Block'} onClick={() => toggleBlock(s.id)}>
                        {s.blocked ? '🔓' : '🔒'}
                      </button>
                      <button className="btn-icon" title={s.muted ? 'Unmute' : 'Mute'} onClick={() => toggleMute(s.id)}>
                        {s.muted ? '🔊' : '🔇'}
                      </button>
                      <button className="btn-icon" title="WhatsApp" onClick={() => shareWhatsApp(s.id)}>📱</button>
                      <button className="btn-icon danger" title="Delete" onClick={() => deleteStudent(s.id)}>🗑️</button>
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* ── Edit Modal ──────────────────────────────── */}
      <Modal open={!!editing} onClose={() => setEditing(null)} title="✏️ Edit Student">
        <form onSubmit={saveEdit} className="form-stack">
          <div className="form-group">
            <label>Name</label>
            <input className="form-input" value={editForm.name || ''} onChange={e => setEditForm(f => ({...f, name: e.target.value}))} required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input className="form-input" type="email" value={editForm.email || ''} onChange={e => setEditForm(f => ({...f, email: e.target.value}))} />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input className="form-input" value={editForm.phone || ''} onChange={e => setEditForm(f => ({...f, phone: e.target.value}))} />
          </div>
          <button type="submit" className="btn btn-primary">💾 Save Changes</button>
        </form>
      </Modal>
    </div>
  );
}

