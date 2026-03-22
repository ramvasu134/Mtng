# 📋 Software Requirements Specification (SRS) / Business Requirements Specification (BRS)

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Project Name**    | MTNG (Meeting Management Platform)        |
| **Author**          | Development Team                          |
| **Technology Stack**| Java 17, Spring Boot 3.4.3, H2 Database   |

---

## 1. Introduction

### 1.1 Purpose
This document defines the functional and non-functional requirements for **MTNG**, a web-based meeting management platform designed for educational environments. The system enables teachers (administrators) to conduct, manage, and monitor virtual meeting sessions with students.

### 1.2 Scope
MTNG provides:
- Real-time virtual meeting room management (Jitsi-based)
- Student lifecycle management (CRUD, block, mute, device lock)
- In-meeting chat system
- Audio recording management per student/meeting
- Role-based access control (ADMIN / USER)
- Credential sharing via WhatsApp deep links
- Comprehensive audit logging

### 1.3 Definitions & Acronyms

| Term         | Definition                                                    |
|--------------|---------------------------------------------------------------|
| **MTNG**     | Meeting Management Platform (application name)                |
| **Teacher**  | Admin user who creates meetings and manages students          |
| **Student**  | Participant who joins meetings and interacts via chat          |
| **Meeting**  | A live virtual session with audio, chat, and recording         |
| **Jitsi**    | Open-source video/audio conferencing platform integrated       |
| **RBAC**     | Role-Based Access Control                                     |
| **SPA**      | Single Page Application                                       |

### 1.4 References
- Spring Boot 3.4.3 Documentation
- Spring Security 6.x Reference
- Jitsi Meet API Documentation
- H2 Database Engine Reference

---

## 2. Business Requirements (BRS)

### 2.1 Business Objectives
| ID      | Objective                                                                 |
|---------|---------------------------------------------------------------------------|
| BO-001  | Enable teachers to conduct virtual meetings with students over LAN        |
| BO-002  | Provide real-time participant tracking during meetings                    |
| BO-003  | Allow teachers to manage student accounts and control meeting behavior    |
| BO-004  | Record meeting audio per participant for review                           |
| BO-005  | Facilitate credential sharing to students via WhatsApp                    |
| BO-006  | Maintain audit trail of all significant system actions                    |

### 2.2 Stakeholders

| Stakeholder   | Role                                         |
|---------------|----------------------------------------------|
| Teacher/Admin | Creates meetings, manages students, views recordings |
| Student/User  | Joins meetings, participates in chat          |
| System Admin  | Manages deployment and configuration          |

### 2.3 Business Rules

| ID      | Rule                                                                      |
|---------|---------------------------------------------------------------------------|
| BR-001  | Only one meeting can be active at any given time                          |
| BR-002  | Starting a new meeting automatically stops the currently active meeting   |
| BR-003  | Only ADMIN users can start/stop meetings                                  |
| BR-004  | Only ADMIN users can create, edit, delete, block, or mute students        |
| BR-005  | Blocked students cannot participate in meetings                           |
| BR-006  | Muted students have their microphone disabled in meetings                 |
| BR-007  | Student usernames must be unique across the system                        |
| BR-008  | Passwords are stored BCrypt-encoded; raw passwords kept for admin sharing  |
| BR-009  | When a meeting stops with full-recording enabled, recordings are auto-saved|
| BR-010  | Student status changes to ONLINE on login and OFFLINE on logout            |

---

## 3. Functional Requirements

### 3.1 Authentication & Authorization (FR-AUTH)

| ID         | Requirement                                                              | Priority |
|------------|--------------------------------------------------------------------------|----------|
| FR-AUTH-01 | System shall provide a login page with username/password form            | HIGH     |
| FR-AUTH-02 | System shall authenticate against both Teacher and Student tables        | HIGH     |
| FR-AUTH-03 | Teachers shall be assigned ROLE_ADMIN or ROLE_USER roles                 | HIGH     |
| FR-AUTH-04 | Students shall be assigned ROLE_USER role upon authentication            | HIGH     |
| FR-AUTH-05 | System shall redirect to dashboard on successful login                   | HIGH     |
| FR-AUTH-06 | System shall display error message on failed login                       | HIGH     |
| FR-AUTH-07 | System shall mark student as ONLINE on successful login                  | MEDIUM   |
| FR-AUTH-08 | System shall mark student as OFFLINE on logout                           | MEDIUM   |
| FR-AUTH-09 | System shall invalidate session and delete JSESSIONID cookie on logout   | HIGH     |
| FR-AUTH-10 | ADMIN shall be able to change their password via settings API            | MEDIUM   |

### 3.2 Meeting Management (FR-MTG)

| ID         | Requirement                                                              | Priority |
|------------|--------------------------------------------------------------------------|----------|
| FR-MTG-01  | ADMIN shall be able to start a meeting with a custom title               | HIGH     |
| FR-MTG-02  | System shall generate a unique Jitsi room name (mtng-{uuid}) for each meeting | HIGH |
| FR-MTG-03  | System shall automatically stop any existing active meeting when a new one starts | HIGH |
| FR-MTG-04  | ADMIN shall be able to stop the active meeting                           | HIGH     |
| FR-MTG-05  | System shall record start and end timestamps for meetings                | HIGH     |
| FR-MTG-06  | Authenticated users shall be able to join the active meeting             | HIGH     |
| FR-MTG-07  | System shall track in-meeting participant count in real-time             | HIGH     |
| FR-MTG-08  | System shall track online participant count                              | MEDIUM   |
| FR-MTG-09  | Users shall be able to leave a meeting voluntarily                       | HIGH     |
| FR-MTG-10  | ADMIN shall be able to toggle full-recording mode on active meeting      | MEDIUM   |
| FR-MTG-11  | Meeting room page shall display Jitsi iframe with shared room name       | HIGH     |
| FR-MTG-12  | System shall display meeting timer showing elapsed time                  | MEDIUM   |
| FR-MTG-13  | ADMIN shall be able to notify students about meeting start               | LOW      |

### 3.3 Student Management (FR-STU)

| ID         | Requirement                                                              | Priority |
|------------|--------------------------------------------------------------------------|----------|
| FR-STU-01  | ADMIN shall be able to create a student with name, username, password    | HIGH     |
| FR-STU-02  | System shall reject duplicate usernames during student creation          | HIGH     |
| FR-STU-03  | ADMIN shall be able to edit student name, password, and settings         | HIGH     |
| FR-STU-04  | ADMIN shall be able to delete a student                                  | HIGH     |
| FR-STU-05  | ADMIN shall be able to toggle block status on a student                  | HIGH     |
| FR-STU-06  | ADMIN shall be able to toggle mute status on a student                   | HIGH     |
| FR-STU-07  | All authenticated users shall be able to view the students list          | HIGH     |
| FR-STU-08  | Users shall be able to search students by name or username               | MEDIUM   |
| FR-STU-09  | System shall display student online/offline status                       | MEDIUM   |
| FR-STU-10  | System shall store raw password for admin display and WhatsApp sharing   | MEDIUM   |
| FR-STU-11  | Each student shall have configurable device-lock and show-recordings flags | LOW   |

### 3.4 Chat System (FR-CHAT)

| ID          | Requirement                                                             | Priority |
|-------------|-------------------------------------------------------------------------|----------|
| FR-CHAT-01  | Authenticated users shall be able to send chat messages during a meeting | HIGH    |
| FR-CHAT-02  | System shall identify the sender from the authenticated session          | HIGH    |
| FR-CHAT-03  | Chat messages shall be associated with the active meeting                | HIGH    |
| FR-CHAT-04  | All chat messages shall be retrievable in chronological order            | HIGH    |
| FR-CHAT-05  | ADMIN shall be able to clear all chat messages for the active meeting    | MEDIUM  |
| FR-CHAT-06  | Chat shall support both "content" and "message" keys in request body     | LOW     |

### 3.5 Recording Management (FR-REC)

| ID          | Requirement                                                             | Priority |
|-------------|-------------------------------------------------------------------------|----------|
| FR-REC-01   | ADMIN shall be able to manually add a recording for a student           | HIGH     |
| FR-REC-02   | System shall auto-save recordings for all participants when a meeting with full-recording enabled stops | HIGH |
| FR-REC-03   | Recordings shall store student ID, name, duration, date, time, meeting ID | HIGH  |
| FR-REC-04   | All authenticated users shall be able to view recordings                 | MEDIUM  |
| FR-REC-05   | Recordings shall be viewable per student (grouped)                       | MEDIUM  |
| FR-REC-06   | ADMIN shall be able to delete individual recordings                      | MEDIUM  |
| FR-REC-07   | ADMIN shall be able to clear all recordings                              | LOW     |

### 3.6 WhatsApp Integration (FR-WA)

| ID          | Requirement                                                             | Priority |
|-------------|-------------------------------------------------------------------------|----------|
| FR-WA-01    | System shall generate WhatsApp deep links with student credentials      | MEDIUM   |
| FR-WA-02    | WhatsApp message shall include username, password, and app URL          | MEDIUM   |
| FR-WA-03    | System shall auto-detect LAN IP address for the app URL                 | MEDIUM   |
| FR-WA-04    | System shall expose server-info API for URL resolution                  | LOW      |

### 3.7 Audit Logging (FR-AUD)

| ID          | Requirement                                                             | Priority |
|-------------|-------------------------------------------------------------------------|----------|
| FR-AUD-01   | System shall log all significant events (login, logout, meeting, chat)  | MEDIUM   |
| FR-AUD-02   | Each audit entry shall include actor, action, details, IP, timestamp    | MEDIUM   |
| FR-AUD-03   | System shall provide API to query recent audit logs (last 100)          | LOW      |
| FR-AUD-04   | System shall provide API to query logs by actor, action, or date range  | LOW      |

---

## 4. Non-Functional Requirements

### 4.1 Performance (NFR-PERF)

| ID          | Requirement                                                             |
|-------------|-------------------------------------------------------------------------|
| NFR-PERF-01 | API response time shall be < 500ms under normal load                   |
| NFR-PERF-02 | Dashboard page shall load within 2 seconds                             |
| NFR-PERF-03 | System shall support up to 50 concurrent students per meeting          |

### 4.2 Security (NFR-SEC)

| ID          | Requirement                                                             |
|-------------|-------------------------------------------------------------------------|
| NFR-SEC-01  | Passwords shall be encoded with BCrypt before storage                  |
| NFR-SEC-02  | CSRF protection shall be enabled for web forms (disabled for API)      |
| NFR-SEC-03  | API endpoints shall return 403 JSON for unauthorized access            |
| NFR-SEC-04  | H2 Console shall only be accessible to ADMIN role                      |
| NFR-SEC-05  | Session shall be invalidated on logout                                 |
| NFR-SEC-06  | Method-level security (@PreAuthorize) shall protect all write APIs     |

### 4.3 Usability (NFR-USE)

| ID          | Requirement                                                             |
|-------------|-------------------------------------------------------------------------|
| NFR-USE-01  | UI shall be responsive and work on desktop and tablet browsers          |
| NFR-USE-02  | Navigation shall use a tab-based layout for quick context switching     |
| NFR-USE-03  | Error messages shall be human-readable and actionable                   |
| NFR-USE-04  | Application shall provide a user guide page                            |

### 4.4 Reliability (NFR-REL)

| ID          | Requirement                                                             |
|-------------|-------------------------------------------------------------------------|
| NFR-REL-01  | System shall handle exceptions gracefully with error pages and JSON     |
| NFR-REL-02  | Database schema shall auto-recreate on startup (in-memory H2)          |
| NFR-REL-03  | Sample data shall be seeded on first startup                           |

### 4.5 Maintainability (NFR-MNT)

| ID          | Requirement                                                             |
|-------------|-------------------------------------------------------------------------|
| NFR-MNT-01  | Code shall follow Spring Boot layered architecture                     |
| NFR-MNT-02  | All service classes shall use transactional annotations                 |
| NFR-MNT-03  | Comprehensive logging shall be implemented via SLF4J                    |

---

## 5. User Interface Requirements

### 5.1 Pages

| Page             | URL                | Access      | Description                                  |
|------------------|--------------------|-------------|----------------------------------------------|
| Login            | `/login`           | Public      | Username/password authentication form         |
| Dashboard        | `/`                | Authenticated | Meeting controls, stats, active meeting view |
| Students List    | `/students`        | Authenticated | Student directory with search                |
| Create Student   | `/create-student`  | ADMIN only  | New student registration form                 |
| Chat             | `/chat`            | Authenticated | Meeting chat interface                       |
| Recordings       | `/recordings`      | Authenticated | Recording history grouped by student          |
| Meeting Room     | `/meeting-room`    | Authenticated | Jitsi meeting embed with controls            |
| Documentation    | `/docs`            | Authenticated | System documentation                         |
| User Guide       | `/userguide`       | Authenticated | User guide page                              |
| Access Denied    | `/access-denied`   | Authenticated | 403 error page                               |
| Error Page       | (auto)             | Any         | Global exception display                      |

### 5.2 Dashboard Widgets
- Meeting timer (elapsed time since meeting start)
- Participant counters: In-Meeting / Online / Total
- Start/End meeting buttons
- Microphone mute/unmute control
- Speaker volume control

---

## 6. Constraints

| ID     | Constraint                                                                   |
|--------|------------------------------------------------------------------------------|
| C-001  | H2 in-memory database — data is lost on application restart                  |
| C-002  | Single-server deployment — no clustering or horizontal scaling               |
| C-003  | Jitsi integration requires network access to meet.jit.si (or self-hosted)    |
| C-004  | WhatsApp sharing requires all users on the same LAN for app URL to work      |
| C-005  | CSRF disabled for `/api/**` endpoints for REST client compatibility          |

---

## 7. Assumptions

1. The application will be deployed on a single machine within a LAN.
2. Teachers and students access the application from devices on the same network.
3. H2 in-memory database is acceptable for the MVP (production would use PostgreSQL/MySQL).
4. Jitsi Meet public servers (meet.jit.si) are available for audio/video.
5. Browsers support WebRTC for Jitsi integration.

---

## 8. Acceptance Criteria

| ID     | Criteria                                                                     |
|--------|------------------------------------------------------------------------------|
| AC-001 | Admin can log in with seeded credentials (admin/admin123)                    |
| AC-002 | Admin can create a student and student can log in with created credentials   |
| AC-003 | Admin can start a meeting and students see the meeting room                  |
| AC-004 | Chat messages appear for all participants in the meeting                     |
| AC-005 | Recordings are saved when a full-recording meeting is stopped                |
| AC-006 | WhatsApp link opens with correct credentials and app URL                     |
| AC-007 | Blocked students cannot interact in meetings                                 |
| AC-008 | Non-admin users cannot access admin-only APIs (403 returned)                 |

