import React, { useState, useEffect, useCallback } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { recordings as api } from '../api';

export default function RecordingsPage() {
  const { isAdmin } = useAuth();
  const { toast } = useOutletContext();
  const [recs, setRecs] = useState([]);
  const [search, setSearch] = useState('');
  const [playingId, setPlayingId] = useState(null);

  const load = useCallback(async () => {
    try { setRecs(await api.list()); } catch (e) { toast.error(e.message); }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  // Group recordings by participant name
  const grouped = recs.reduce((acc, r) => {
    const key = r.studentName || 'Unknown';
    (acc[key] = acc[key] || []).push(r);
    return acc;
  }, {});

  const filtered = Object.entries(grouped).filter(([name]) =>
    name.toLowerCase().includes(search.toLowerCase())
  );

  const deleteRec = async (id) => {
    if (!confirm('Delete this recording?')) return;
    try { await api.delete(id); toast.success('Deleted'); load(); } catch (e) { toast.error(e.message); }
  };
  const clearAll = async () => {
    if (!confirm('Delete ALL recordings?')) return;
    try { await api.clear(); toast.success('All recordings cleared'); load(); } catch (e) { toast.error(e.message); }
  };

  const formatDuration = (s) => {
    if (!s) return '—';
    const m = Math.floor(s / 60), sec = s % 60;
    return `${m}:${String(sec).padStart(2, '0')}`;
  };

  return (
    <div className="recordings-page">
      <div className="page-header">
        <h2>🎙️ Recordings ({recs.length})</h2>
        <div className="header-actions">
          <input className="form-input search-input" placeholder="🔍 Filter…" value={search} onChange={e => setSearch(e.target.value)} />
          {isAdmin && recs.length > 0 && (
            <button className="btn btn-danger btn-sm" onClick={clearAll}>🗑️ Clear All</button>
          )}
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="card empty-state">
          <span className="empty-icon">🎙️</span>
          <p>No recordings found</p>
        </div>
      ) : filtered.map(([name, recordings]) => (
        <div key={name} className="card recording-group">
          <h3 className="rec-group-title">👤 {name} <span className="badge badge-gray">{recordings.length}</span></h3>
          <div className="rec-list">
            {recordings.map(r => (
              <div key={r.id} className="rec-item">
                <div className="rec-info">
                  <span className="rec-title">🎵 Recording #{r.id}</span>
                  <span className="rec-meta">
                    {formatDuration(r.durationSeconds)} · {r.recordingDate || ''} {r.recordingTime ? r.recordingTime.substring(0,5) : ''}
                    {r.fileSizeBytes ? ` · ${(r.fileSizeBytes / 1024).toFixed(0)} KB` : ''}
                    {r.participantType && <> · <em>{r.participantType}</em></>}
                  </span>
                </div>
                <div className="rec-controls">
                  {r.hasAudio ? (
                    <>
                      <button className="btn-icon" title="Play" onClick={() => setPlayingId(playingId === r.id ? null : r.id)}>
                        {playingId === r.id ? '⏸️' : '▶️'}
                      </button>
                      <a href={api.audio(r.id)} download className="btn-icon" title="Download">⬇️</a>
                    </>
                  ) : (
                    <span className="text-muted" style={{fontSize: 11}}>No audio</span>
                  )}
                  {isAdmin && <button className="btn-icon danger" title="Delete" onClick={() => deleteRec(r.id)}>🗑️</button>}
                </div>
                {playingId === r.id && r.hasAudio && (
                  <audio controls autoPlay className="rec-audio" src={api.audio(r.id)} onEnded={() => setPlayingId(null)} />
                )}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

