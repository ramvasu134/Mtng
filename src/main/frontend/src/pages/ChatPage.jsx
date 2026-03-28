import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useOutletContext } from 'react-router-dom';
import { chat as chatApi, meeting as meetingApi } from '../api';
import { useAuth } from '../contexts/AuthContext';

export default function ChatPage() {
  const { user } = useAuth();
  const { toast } = useOutletContext();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [activeMeeting, setActiveMeeting] = useState(null);
  const bottomRef = useRef(null);

  const loadMessages = useCallback(async () => {
    try {
      const [msgs, mtg] = await Promise.all([chatApi.messages(), meetingApi.active()]);
      setMessages(msgs);
      setActiveMeeting(mtg);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadMessages(); const id = setInterval(loadMessages, 3000); return () => clearInterval(id); }, [loadMessages]);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const send = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    try {
      await chatApi.send(input.trim());
      setInput('');
      loadMessages();
    } catch (err) { toast.error(err.message); }
  };

  return (
    <div className="chat-page">
      <div className="card chat-card">
        <div className="chat-card-header">
          <h2>💬 Chat {activeMeeting && <span className="live-badge-sm">LIVE</span>}</h2>
          <span className="text-muted">{messages.length} messages</span>
        </div>

        <div className="chat-messages-area">
          {messages.length === 0 ? (
            <div className="chat-empty">💭 No messages yet. Start the conversation!</div>
          ) : messages.map((m, i) => {
            const isOwn = m.sender === user?.username || m.sender === user?.displayName;
            return (
              <div key={m.id || i} className={`chat-msg ${isOwn ? 'own' : ''}`}>
                <div className={`chat-bubble ${isOwn ? 'mine' : 'theirs'}`}>
                  {!isOwn && <div className="bubble-sender">{m.sender}</div>}
                  <div className="bubble-text">{m.content}</div>
                  <div className="bubble-time">{m.timestamp ? new Date(m.timestamp).toLocaleTimeString() : ''}</div>
                </div>
              </div>
            );
          })}
          <div ref={bottomRef} />
        </div>

        <form className="chat-input-bar" onSubmit={send}>
          <input
            className="form-input chat-input"
            placeholder="Type a message…"
            value={input}
            onChange={e => setInput(e.target.value)}
          />
          <button type="submit" className="btn btn-primary chat-send" disabled={!input.trim()}>
            ➤
          </button>
        </form>
      </div>
    </div>
  );
}

