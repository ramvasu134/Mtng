import React from 'react';

export default function UserGuidePage() {
  return (
    <div className="guide-page">
      <div className="card">
        <h2 className="card-title">📘 User Guide</h2>
        <div className="guide-content">

          <section className="guide-section">
            <h3>🚀 Getting Started</h3>
            <ol>
              <li>Open the app URL in your browser (Chrome/Edge recommended).</li>
              <li>Login with your credentials (provided by your teacher).</li>
              <li>You'll see the <strong>Dashboard</strong> with meeting controls and stats.</li>
            </ol>
          </section>

          <section className="guide-section">
            <h3>🎥 Joining a Meeting</h3>
            <ol>
              <li>When a meeting is active, you'll see a <span className="badge badge-green">LIVE</span> indicator.</li>
              <li>Click <strong>"Join Meeting"</strong> to enter the pre-join screen.</li>
              <li>Allow microphone access when prompted by your browser.</li>
              <li>Click <strong>"Join Meeting"</strong> again to enter the room.</li>
            </ol>
          </section>

          <section className="guide-section">
            <h3>🎙️ Meeting Controls</h3>
            <div className="controls-guide">
              <div className="guide-item"><span className="guide-icon">🎤</span><span>Toggle your microphone on/off</span></div>
              <div className="guide-item"><span className="guide-icon">⏺️</span><span>Start/stop recording (saves locally)</span></div>
              <div className="guide-item"><span className="guide-icon">💬</span><span>Toggle chat sidebar</span></div>
              <div className="guide-item"><span className="guide-icon">📞</span><span>Leave the meeting</span></div>
            </div>
          </section>

          <section className="guide-section">
            <h3>💬 Chat</h3>
            <p>Use the <strong>Chat</strong> tab to send messages during or outside meetings.
            Messages are persisted and visible to all participants.</p>
          </section>

          <section className="guide-section">
            <h3>🎙️ Recordings</h3>
            <p>Recordings made during meetings are saved. Admin can see all recordings;
            students see only their own. Click ▶️ to play or ⬇️ to download.</p>
          </section>

          <section className="guide-section admin-section">
            <h3>👨‍💼 Admin Features</h3>
            <ul>
              <li><strong>Start/Stop Meeting</strong> – Only admins can control meetings.</li>
              <li><strong>Create Students</strong> – Add new student accounts.</li>
              <li><strong>Block/Mute</strong> – Block login or mute mic for students.</li>
              <li><strong>WhatsApp Share</strong> – Share login credentials via WhatsApp.</li>
              <li><strong>Delete Recordings</strong> – Remove recordings.</li>
              <li><strong>Change Password</strong> – Update admin password from Settings.</li>
            </ul>
          </section>

          <section className="guide-section">
            <h3>🌐 Browser Support</h3>
            <div className="browser-grid">
              <div className="browser-card">🌐 Chrome 90+</div>
              <div className="browser-card">🔵 Edge 90+</div>
              <div className="browser-card">🦊 Firefox 85+</div>
              <div className="browser-card">🧭 Safari 15+</div>
            </div>
          </section>

          <section className="guide-section">
            <h3>❓ Troubleshooting</h3>
            <dl className="faq-list">
              <dt>Can't hear other participants?</dt>
              <dd>Check your speaker/headphone volume. Ensure the browser isn't muting the tab.</dd>
              <dt>Microphone not working?</dt>
              <dd>Click the lock icon in the address bar → allow microphone access → reload.</dd>
              <dt>Meeting shows "No active meeting"?</dt>
              <dd>Wait for your teacher to start a meeting, or click Refresh.</dd>
            </dl>
          </section>
        </div>
      </div>
    </div>
  );
}

