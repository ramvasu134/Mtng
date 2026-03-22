# ✨ Features Incorporated Document

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Project Name**    | MTNG (Meeting Management Platform)        |
| **Version**         | 0.0.1-SNAPSHOT (MVP)                      |

---

## 1. Feature Summary Matrix

| #  | Feature Category          | Feature                                    | Status    | Role Required |
|----|---------------------------|--------------------------------------------|-----------|---------------|
| 1  | Authentication            | Form-based login with Spring Security      | ✅ Done   | Public        |
| 2  | Authentication            | Dual authentication (Teacher + Student)    | ✅ Done   | Public        |
| 3  | Authentication            | Role-based access (ADMIN / USER)           | ✅ Done   | All           |
| 4  | Authentication            | Custom login success handler               | ✅ Done   | All           |
| 5  | Authentication            | Custom logout handler with status update   | ✅ Done   | All           |
| 6  | Authentication            | Password change for teachers               | ✅ Done   | Authenticated |
| 7  | Authentication            | BCrypt password encoding                   | ✅ Done   | System        |
| 8  | Meeting Management        | Start meeting with custom title            | ✅ Done   | ADMIN         |
| 9  | Meeting Management        | Auto-stop previous meeting on new start    | ✅ Done   | ADMIN         |
| 10 | Meeting Management        | Stop active meeting                        | ✅ Done   | ADMIN         |
| 11 | Meeting Management        | Jitsi room name generation (UUID-based)    | ✅ Done   | System        |
| 12 | Meeting Management        | Join active meeting                        | ✅ Done   | Authenticated |
| 13 | Meeting Management        | Leave active meeting                       | ✅ Done   | Authenticated |
| 14 | Meeting Management        | Real-time participant count tracking       | ✅ Done   | System        |
| 15 | Meeting Management        | Online student count                       | ✅ Done   | System        |
| 16 | Meeting Management        | Meeting timer display                      | ✅ Done   | UI            |
| 17 | Meeting Management        | Student notification (placeholder)         | ✅ Done   | ADMIN         |
| 18 | Meeting Room              | Jitsi Meet iframe embedding                | ✅ Done   | Authenticated |
| 19 | Meeting Room              | Shared room for all participants           | ✅ Done   | System        |
| 20 | Meeting Room              | Microphone mute/unmute controls            | ✅ Done   | UI            |
| 21 | Meeting Room              | Speaker volume control                     | ✅ Done   | UI            |
| 22 | Student Management        | Create student (name, username, password)  | ✅ Done   | ADMIN         |
| 23 | Student Management        | Edit student details                       | ✅ Done   | ADMIN         |
| 24 | Student Management        | Delete student                             | ✅ Done   | ADMIN         |
| 25 | Student Management        | Block/unblock student                      | ✅ Done   | ADMIN         |
| 26 | Student Management        | Mute/unmute student                        | ✅ Done   | ADMIN         |
| 27 | Student Management        | Search students by name/username           | ✅ Done   | Authenticated |
| 28 | Student Management        | Online/offline status tracking             | ✅ Done   | System        |
| 29 | Student Management        | Device lock toggle                         | ✅ Done   | ADMIN         |
| 30 | Student Management        | Show recordings toggle                     | ✅ Done   | ADMIN         |
| 31 | Student Management        | Duplicate username prevention              | ✅ Done   | System        |
| 32 | Chat System               | Send chat messages                         | ✅ Done   | Authenticated |
| 33 | Chat System               | Auto-identify sender from session          | ✅ Done   | System        |
| 34 | Chat System               | Messages linked to active meeting          | ✅ Done   | System        |
| 35 | Chat System               | Chronological message retrieval            | ✅ Done   | System        |
| 36 | Chat System               | Clear chat messages                        | ✅ Done   | ADMIN         |
| 37 | Recording Management      | Toggle full-recording mode                 | ✅ Done   | ADMIN         |
| 38 | Recording Management      | Auto-save recordings on meeting stop       | ✅ Done   | System        |
| 39 | Recording Management      | Manual recording creation                  | ✅ Done   | ADMIN         |
| 40 | Recording Management      | View recordings by student                 | ✅ Done   | Authenticated |
| 41 | Recording Management      | Delete individual recordings               | ✅ Done   | ADMIN         |
| 42 | Recording Management      | Clear all recordings                       | ✅ Done   | ADMIN         |
| 43 | WhatsApp Integration      | Generate WhatsApp deep links               | ✅ Done   | Authenticated |
| 44 | WhatsApp Integration      | Auto-detect LAN IP for URLs                | ✅ Done   | System        |
| 45 | WhatsApp Integration      | Include credentials in WhatsApp message    | ✅ Done   | System        |
| 46 | Server Discovery          | Server info API (real LAN URL)             | ✅ Done   | Authenticated |
| 47 | Audit & Logging           | SLF4J logging (console + file)             | ✅ Done   | System        |
| 48 | Audit & Logging           | Audit log entity with actor/action/IP      | ✅ Done   | System        |
| 49 | Audit & Logging           | Query logs by actor/action/date range      | ✅ Done   | System        |
| 50 | Audit & Logging           | Log file rotation (10MB, 5 files)          | ✅ Done   | System        |
| 51 | Error Handling            | Global exception handler                   | ✅ Done   | System        |
| 52 | Error Handling            | JSON error for API requests                | ✅ Done   | System        |
| 53 | Error Handling            | HTML error page for web requests           | ✅ Done   | System        |
| 54 | Error Handling            | Custom access denied handling              | ✅ Done   | System        |
| 55 | UI/UX                     | Tab-based navigation                       | ✅ Done   | UI            |
| 56 | UI/UX                     | Dark theme interface                       | ✅ Done   | UI            |
| 57 | UI/UX                     | Responsive design                          | ✅ Done   | UI            |
| 58 | UI/UX                     | Login page with custom styling             | ✅ Done   | UI            |
| 59 | UI/UX                     | Documentation page                         | ✅ Done   | Authenticated |
| 60 | UI/UX                     | User guide page                            | ✅ Done   | Authenticated |
| 61 | Database                  | H2 in-memory database                      | ✅ Done   | System        |
| 62 | Database                  | H2 Console (admin only)                    | ✅ Done   | ADMIN         |
| 63 | Database                  | Auto schema creation (create-drop)         | ✅ Done   | System        |
| 64 | Database                  | Sample data seeding on startup             | ✅ Done   | System        |
| 65 | Security                  | URL-based authorization                    | ✅ Done   | System        |
| 66 | Security                  | Method-level security (@PreAuthorize)      | ✅ Done   | System        |
| 67 | Security                  | CSRF protection (forms) / disabled (API)   | ✅ Done   | System        |
| 68 | Security                  | Session management & invalidation          | ✅ Done   | System        |

---

## 2. Detailed Feature Descriptions

### 2.1 🔐 Authentication & Authorization System

**Description:** Comprehensive role-based security system supporting dual user types.

**Key Capabilities:**
- **Dual Authentication Source:** Teachers authenticate from `teachers` table, students from `students` table
- **Role Hierarchy:** ADMIN (full access) → USER (read + participate)
- **Custom Handlers:** Login marks student ONLINE, logout marks OFFLINE
- **Password Security:** All passwords stored with BCrypt hashing
- **Session Security:** JSESSIONID cookie, session invalidation on logout

**Seeded Accounts:**
| Account | Username | Password   | Role       |
|---------|----------|------------|------------|
| Admin   | admin    | admin123   | ROLE_ADMIN |
| User    | user     | user123    | ROLE_USER  |

---

### 2.2 📹 Meeting Management System (Zoom-like)

**Description:** Full meeting lifecycle management with Jitsi Meet integration.

**Key Capabilities:**
- **One-click meeting start** with auto-generated Jitsi room name
- **Automatic cleanup** – starting a new meeting stops any active meeting
- **Shared room architecture** – all participants join the same Jitsi room via `roomName`
- **Real-time counters** – in-meeting count, online count, total students
- **Meeting timer** – elapsed time display since meeting start
- **Full recording mode** – auto-saves recordings for all participants when meeting ends

**Meeting Room UI Components:**
- Header: Teacher name, timer, participant stats, End Meeting button
- Tab bar: Meeting Controls, Chat, Students List, Create Student, Recordings
- Main area: Jitsi iframe with shared room
- Footer: Volume control, Microphone toggle, Alert bell

---

### 2.3 👨‍🎓 Student Management System

**Description:** Complete student CRUD with moderation controls.

**Key Capabilities:**
- **Create:** Register students with name, username, password
- **Read:** View all students with search, see online/offline status
- **Update:** Edit name, password, device settings
- **Delete:** Remove student accounts
- **Block:** Prevent students from participating in meetings
- **Mute:** Disable student microphone in meetings
- **Status Tracking:** Real-time ONLINE/OFFLINE status based on login/logout
- **Unique Enforcement:** Username uniqueness validation
- **Raw Password Storage:** Admin can view/share student passwords

---

### 2.4 💬 Real-Time Chat System

**Description:** In-meeting chat system for communication during sessions.

**Key Capabilities:**
- **Auto-sender identification** from authenticated session
- **Meeting-scoped messages** linked to active meeting ID
- **Chronological ordering** for message display
- **Chat clearing** for meeting cleanup
- **Backward-compatible API** accepting both `content` and `message` keys

---

### 2.5 🎙️ Recording Management

**Description:** Audio recording tracking per student per meeting.

**Key Capabilities:**
- **Auto-recording** when full-recording enabled and meeting stops
- **Manual recording creation** by admin with student ID and duration
- **Grouped view** recordings displayed per student
- **Duration tracking** in seconds, calculated from meeting timestamps
- **Bulk operations** clear all recordings

---

### 2.6 📲 WhatsApp Credential Sharing

**Description:** Share student login credentials via WhatsApp deep links.

**Key Capabilities:**
- **Auto-LAN IP detection** iterates network interfaces to find real IP
- **Smart IP resolution** skips loopback, virtual, and Docker interfaces
- **Formatted message** includes emoji, credentials, and app URL
- **Deep link generation** opens WhatsApp with pre-filled message
- **Custom URL override** via `X-App-Url` header

**Example WhatsApp Message:**
```
🎓 *MTNG – Meeting App*

Hi Hari!

Your account has been created:
👤 Username: *HARI34*
🔑 Password: *pass1234*

🔗 App URL: http://192.168.1.42:8080

Please login and join the meeting. 🙏
```

---

### 2.7 📊 Audit & Logging System

**Description:** Comprehensive event tracking for accountability.

**Key Capabilities:**
- **Dual logging:** SLF4J log files + AuditLog database table
- **Event tracking:** LOGIN, LOGOUT, MEETING_START, MEETING_STOP, CHAT, JOIN, LEAVE
- **Query APIs:** By actor, action type, date range
- **IP tracking:** Client IP address recorded with events
- **Log rotation:** 10MB max file size, 5 rotated history files

---

### 2.8 🛡️ Error Handling & Resilience

**Description:** Multi-layer error handling for graceful failure recovery.

**Key Capabilities:**
- **Global exception handler** catches all unhandled exceptions
- **Smart response format:** JSON for API requests, HTML for page requests
- **Cause chain display** on error pages for debugging
- **Custom 403 handling:** JSON for API, redirect for pages
- **Validation errors:** 400 responses with descriptive messages

---

### 2.9 🎨 User Interface

**Description:** Modern dark-themed web interface with tab-based navigation.

**Pages (11 total):**

| Page              | Description                                     |
|-------------------|-------------------------------------------------|
| Login             | Authentication form with custom styling          |
| Dashboard         | Meeting controls with stats and timer            |
| Students List     | Student directory with search and actions        |
| Create Student    | Student registration form (ADMIN only)           |
| Chat              | Meeting chat interface                           |
| Recordings        | Recording history grouped by student             |
| Meeting Room      | Jitsi iframe with audio controls                 |
| Documentation     | System documentation                             |
| User Guide        | User instructions                                |
| Access Denied     | 403 error page                                   |
| Error Page        | 500 error display with details                   |

**UI Features:**
- Dark theme color scheme
- Tab-based navigation for quick context switching
- Real-time participant counters in header
- Meeting timer with elapsed time
- Microphone mute/unmute toggle
- Speaker volume control

---

### 2.10 🗃️ Database & Data Management

**Description:** H2 in-memory database with automatic schema management.

**Key Capabilities:**
- **6 tables:** students, teachers, meetings, chat_messages, recordings, audit_logs
- **Auto-schema:** `create-drop` DDL strategy recreates schema on every startup
- **Data seeding:** Sample data (2 teachers, 3 students, 4 recordings) on first run
- **H2 Console:** Web-based database admin at `/h2-console` (ADMIN only)
- **Transaction management:** `@Transactional` on all service methods

---

## 3. Technology Features

| Category          | Technology / Feature                                          |
|-------------------|---------------------------------------------------------------|
| **Language**      | Java 17 (LTS) with modern language features                  |
| **Framework**     | Spring Boot 3.4.3 with auto-configuration                    |
| **Web**           | Spring MVC + Thymeleaf SSR + REST API hybrid                 |
| **Security**      | Spring Security 6 with BCrypt, RBAC, CSRF, method security   |
| **ORM**           | Spring Data JPA + Hibernate 6 with derived query methods     |
| **Database**      | H2 In-Memory with web console                                |
| **Templating**    | Thymeleaf 3 with Spring Security dialect (`sec:authorize`)   |
| **Conferencing**  | Jitsi Meet (external) via iframe embedding                   |
| **Sharing**       | WhatsApp deep links with LAN IP auto-detection               |
| **Logging**       | SLF4J + Logback with file rotation                           |
| **Build**         | Apache Maven with Spring Boot plugin                         |
| **Testing**       | JUnit 5 + Spring Boot Test + Spring Security Test            |
| **Boilerplate**   | Project Lombok (excluded from final JAR)                     |

---

## 4. Security Features Summary

| Feature                          | Implementation                                      |
|----------------------------------|-----------------------------------------------------|
| Password Hashing                 | BCrypt with PasswordEncoder bean                    |
| Role-Based Access Control        | ADMIN / USER roles with URL + method security       |
| Session Management               | JSESSIONID cookie, invalidation on logout           |
| CSRF Protection                  | Enabled for forms, disabled for REST APIs           |
| Access Denied Handling           | Custom handler: JSON for API, redirect for web      |
| Input Validation                 | Service-layer validation (uniqueness, required fields)|
| Frame Options                    | Same-origin (for H2 console iframe)                |
| Error Information Hiding         | Stack traces only available via URL parameter       |

---

## 5. Deployment Features

| Feature               | Description                                          |
|-----------------------|------------------------------------------------------|
| Fat JAR               | Self-contained JAR with embedded Tomcat              |
| Maven Wrapper         | `mvnw` / `mvnw.cmd` for build without Maven install |
| Startup Scripts       | `run-app.bat`, `start-mtng.bat`, `run.ps1`          |
| Health Check Scripts  | `check-app.bat`, `verify-app.bat`                   |
| Debug Logging         | `run-and-log.bat` with log capture                   |
| Port Configuration    | Configurable via `server.port` (default: 8080)       |

