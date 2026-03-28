import React from 'react';

export default function DocumentationPage() {
  return (
    <div className="docs-page">
      <div className="card">
        <h2 className="card-title">📖 Documentation</h2>
        <div className="docs-content">
          <section className="doc-section">
            <h3>🏗️ Architecture</h3>
            <p>MTNG is built with <strong>React 18</strong> (frontend) and <strong>Spring Boot 3.4</strong> (backend).
            The application uses a component-based architecture with loose coupling between UI modules.</p>
            <ul>
              <li><strong>Frontend:</strong> React 18 + Vite + React Router 6</li>
              <li><strong>Backend:</strong> Spring Boot 3.4.3, Java 17</li>
              <li><strong>Database:</strong> H2 In-Memory (JPA/Hibernate)</li>
              <li><strong>Security:</strong> Spring Security (Session-based)</li>
              <li><strong>Real-time:</strong> WebSocket (STOMP/SockJS) + WebRTC</li>
            </ul>
          </section>

          <section className="doc-section">
            <h3>🔒 Security</h3>
            <p>Role-based access control with two roles:</p>
            <ul>
              <li><strong>ADMIN</strong> – Full access: start/stop meetings, manage students, view all recordings</li>
              <li><strong>USER</strong> – Limited: join meetings, chat, view own recordings</li>
            </ul>
            <p>Authentication uses session cookies (JSESSIONID). CSRF is disabled for API endpoints.
            All communication is encrypted via TLS (HTTPS).</p>
          </section>

          <section className="doc-section">
            <h3>📡 REST API Endpoints</h3>
            <div className="api-table">
              <table className="data-table">
                <thead><tr><th>Method</th><th>Endpoint</th><th>Auth</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td>POST</td><td><code>/api/auth/login</code></td><td>Public</td><td>Login (JSON body)</td></tr>
                  <tr><td>GET</td><td><code>/api/auth/me</code></td><td>Auth</td><td>Current user info</td></tr>
                  <tr><td>POST</td><td><code>/api/auth/logout</code></td><td>Auth</td><td>Logout</td></tr>
                  <tr><td>GET</td><td><code>/api/students</code></td><td>Auth</td><td>List all students</td></tr>
                  <tr><td>POST</td><td><code>/api/students</code></td><td>Admin</td><td>Create student</td></tr>
                  <tr><td>PUT</td><td><code>/api/students/:id</code></td><td>Admin</td><td>Update student</td></tr>
                  <tr><td>DELETE</td><td><code>/api/students/:id</code></td><td>Admin</td><td>Delete student</td></tr>
                  <tr><td>POST</td><td><code>/api/students/:id/block</code></td><td>Admin</td><td>Toggle block</td></tr>
                  <tr><td>POST</td><td><code>/api/students/:id/mute</code></td><td>Admin</td><td>Toggle mute</td></tr>
                  <tr><td>GET</td><td><code>/api/meeting/active</code></td><td>Auth</td><td>Active meeting info</td></tr>
                  <tr><td>POST</td><td><code>/api/meeting/start</code></td><td>Admin</td><td>Start meeting</td></tr>
                  <tr><td>POST</td><td><code>/api/meeting/stop</code></td><td>Admin</td><td>Stop meeting</td></tr>
                  <tr><td>POST</td><td><code>/api/meeting/join</code></td><td>Auth</td><td>Join active meeting</td></tr>
                  <tr><td>GET</td><td><code>/api/chat/messages</code></td><td>Auth</td><td>Get chat messages</td></tr>
                  <tr><td>POST</td><td><code>/api/chat/send</code></td><td>Auth</td><td>Send message</td></tr>
                  <tr><td>GET</td><td><code>/api/recordings</code></td><td>Auth</td><td>List recordings</td></tr>
                </tbody>
              </table>
            </div>
          </section>

          <section className="doc-section">
            <h3>🧩 Component Architecture</h3>
            <pre className="code-block">{`src/
├── main.jsx           # Entry point
├── App.jsx            # Router + route definitions
├── api.js             # Centralized API client
├── contexts/
│   └── AuthContext.jsx # Auth state management
├── components/
│   ├── Layout.jsx     # App shell (header, nav, footer)
│   ├── Modal.jsx      # Reusable modal
│   ├── Toast.jsx      # Toast notifications
│   └── ProtectedRoute.jsx
└── pages/
    ├── LoginPage.jsx
    ├── DashboardPage.jsx
    ├── StudentsPage.jsx
    ├── CreateStudentPage.jsx
    ├── ChatPage.jsx
    ├── RecordingsPage.jsx
    ├── MeetingRoomPage.jsx  # WebRTC audio
    ├── DocumentationPage.jsx
    └── UserGuidePage.jsx`}</pre>
          </section>
        </div>
      </div>
    </div>
  );
}

