import React, { useState, useCallback } from 'react';

let toastId = 0;

/** Lightweight toast notification system */
export function useToast() {
  const [toasts, setToasts] = useState([]);

  const show = useCallback((message, type = 'info', duration = 3500) => {
    const id = ++toastId;
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), duration);
  }, []);

  const success = useCallback((m) => show(m, 'success'), [show]);
  const error   = useCallback((m) => show(m, 'error', 5000), [show]);
  const info    = useCallback((m) => show(m, 'info'), [show]);
  const warning = useCallback((m) => show(m, 'warning', 4000), [show]);

  return { toasts, show, success, error, info, warning };
}

export function ToastContainer({ toasts }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-container">
      {toasts.map(t => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          <span className="toast-icon">
            {t.type === 'success' ? '✅' : t.type === 'error' ? '❌' : t.type === 'warning' ? '⚠️' : 'ℹ️'}
          </span>
          {t.message}
        </div>
      ))}
    </div>
  );
}

