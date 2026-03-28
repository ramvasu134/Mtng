import React, { useState, useRef, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ToastContainer, useToast } from './Toast';
import Modal from './Modal';
import { settings as settingsApi } from '../api';

export default function Layout() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [pwdModal, setPwdModal] = useState(false);
  const [newPwd, setNewPwd] = useState('');
  const dropRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) setSettingsOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    try {
      await settingsApi.changePassword(newPwd);
      toast.success('Password changed successfully!');
      setPwdModal(false);
      setNewPwd('');
    } catch (err) {
      toast.error(err.message || 'Failed to change password');
    }
  };

  const tabs = [
    { to: '/',           label: '🎥 Meeting',   end: true },
    { to: '/students',   label: '👥 Students' },
    ...(isAdmin ? [{ to: '/create-student', label: '➕ Add Student' }] : []),
    { to: '/chat',       label: '💬 Chat' },
    { to: '/recordings', label: '🎙️ Recordings' },
    { to: '/docs',       label: '📖 Docs' },
    { to: '/userguide',  label: '📘 Guide' },
  ];

  return (
    <div className="app-shell">
      {/* ── Header ──────────────────────────────────── */}
      <header className="app-header">
        <div className="header-brand">
          <span className="brand-logo">📹</span>
          <span className="brand-name">MTNG</span>
          <span className="brand-tag">Meeting Platform</span>
        </div>
        <div className="header-right">
          <div className="user-badge" ref={dropRef}>
            <button className="user-btn" onClick={() => setSettingsOpen(v => !v)}>
              <span className="user-avatar">{(user?.displayName || user?.username || '?')[0].toUpperCase()}</span>
              <span className="user-name">{user?.displayName || user?.username}</span>
              <span className="user-role-badge">{user?.role}</span>
              <span className="dropdown-arrow">▾</span>
            </button>
            {settingsOpen && (
              <div className="dropdown-menu">
                {isAdmin && (
                  <button onClick={() => { setPwdModal(true); setSettingsOpen(false); }}>
                    🔑 Change Password
                  </button>
                )}
                <button onClick={handleLogout}>🚪 Logout</button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* ── Navigation Tabs ─────────────────────────── */}
      <nav className="nav-tabs">
        {tabs.map(t => (
          <NavLink
            key={t.to}
            to={t.to}
            end={t.end}
            className={({ isActive }) => `nav-tab${isActive ? ' active' : ''}`}
          >
            {t.label}
          </NavLink>
        ))}
      </nav>

      {/* ── Page Content ────────────────────────────── */}
      <main className="page-content">
        <Outlet context={{ toast }} />
      </main>

      {/* ── Footer ──────────────────────────────────── */}
      <footer className="app-footer">
        <span>© 2026 MTNG – Meeting Platform</span>
        <span>Built with React + Spring Boot</span>
      </footer>

      {/* ── Change Password Modal ───────────────────── */}
      <Modal open={pwdModal} onClose={() => setPwdModal(false)} title="🔑 Change Password" width={400}>
        <form onSubmit={handleChangePassword} className="form-stack">
          <input
            type="password"
            placeholder="New password (min 4 chars)"
            value={newPwd}
            onChange={e => setNewPwd(e.target.value)}
            minLength={4}
            required
            className="form-input"
          />
          <button type="submit" className="btn btn-primary">Update Password</button>
        </form>
      </Modal>

      <ToastContainer toasts={toast.toasts} />
    </div>
  );
}

