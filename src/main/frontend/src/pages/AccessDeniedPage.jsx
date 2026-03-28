import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function AccessDeniedPage() {
  const navigate = useNavigate();
  return (
    <div className="meeting-screen-center">
      <div className="prejoin-card">
        <span className="big-icon">🚫</span>
        <h2>Access Denied</h2>
        <p className="text-muted">You don't have permission to access this page.</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>← Back to Dashboard</button>
      </div>
    </div>
  );
}

