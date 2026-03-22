# 🏗️ High-Level Design (HLD) Document

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Project Name**    | MTNG (Meeting Management Platform)        |

---

## 1. System Overview

MTNG is a **monolithic web application** built on the **Spring Boot 3.4.3** framework. It serves as a meeting management platform for educational environments, allowing teachers (admins) to create and manage virtual meetings, manage students, and facilitate real-time chat and recording features.

### 1.1 Architecture Style
- **Monolithic MVC + REST API** hybrid architecture
- **Server-Side Rendering (SSR)** using Thymeleaf templates for web pages
- **RESTful APIs** for client-side JavaScript interactions (AJAX)
- **Layered Architecture**: Controller → Service → Repository → Database

### 1.2 Technology Stack

| Layer          | Technology                          | Version    |
|----------------|-------------------------------------|------------|
| Language       | Java                                | 17 (LTS)   |
| Framework      | Spring Boot                         | 3.4.3      |
| Web Layer      | Spring MVC + Thymeleaf              | 6.x        |
| Security       | Spring Security                     | 6.x        |
| ORM            | Spring Data JPA / Hibernate         | 6.x        |
| Database       | H2 (In-Memory)                      | Runtime    |
| Build Tool     | Apache Maven                        | 3.x        |
| Template Engine| Thymeleaf + Security Extras         | 3.x        |
| Conferencing   | Jitsi Meet (External)               | Latest     |
| Boilerplate    | Project Lombok                      | Latest     |
| Testing        | JUnit 5 + Spring Security Test      | 5.x        |

---

## 2. System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                           CLIENT TIER                                │
│                                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│  │   Browser    │  │   Browser    │  │   Browser    │                │
│  │  (Teacher)   │  │  (Student)   │  │  (Student)   │                │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                │
│         │ HTTP/HTTPS       │                  │                       │
└─────────┼──────────────────┼──────────────────┼──────────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        APPLICATION TIER                               │
│                     (Spring Boot 3.4.3)                               │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    PRESENTATION LAYER                           │  │
│  │                                                                │  │
│  │  ┌──────────────────┐  ┌──────────────────────────────────┐   │  │
│  │  │  View Controllers │  │       REST API Controllers        │   │  │
│  │  │  (Thymeleaf SSR)  │  │    (JSON Request/Response)        │   │  │
│  │  │                   │  │                                   │   │  │
│  │  │  - Dashboard      │  │  - MeetingApiController           │   │  │
│  │  │  - Login          │  │  - StudentApiController           │   │  │
│  │  │                   │  │  - ChatApiController               │   │  │
│  │  │                   │  │  - RecordingApiController          │   │  │
│  │  │                   │  │  - SettingsApiController           │   │  │
│  │  └──────────────────┘  └──────────────────────────────────────┘   │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    SECURITY LAYER                               │  │
│  │                                                                │  │
│  │  SecurityConfig ──► DaoAuthenticationProvider                  │  │
│  │       │                     │                                  │  │
│  │       ├── URL-based rules   ├── MtngUserDetailsService         │  │
│  │       ├── CSRF config       │      ├── Teacher table lookup    │  │
│  │       ├── AccessDenied      │      └── Student table lookup    │  │
│  │       ├── SuccessHandler    │                                  │  │
│  │       └── LogoutHandler     └── BCryptPasswordEncoder          │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    BUSINESS LOGIC LAYER                         │  │
│  │                                                                │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌─────────────────┐ │  │
│  │  │ MeetingService  │  │ StudentService  │  │   ChatService    │ │  │
│  │  │                │  │                │  │                 │ │  │
│  │  │ - Start/Stop   │  │ - CRUD         │  │ - Send message  │ │  │
│  │  │ - Join/Leave   │  │ - Block/Mute   │  │ - Get messages  │ │  │
│  │  │ - Recording    │  │ - Online/Off   │  │ - Clear chat    │ │  │
│  │  │ - Participants │  │ - Search       │  │                 │ │  │
│  │  └────────────────┘  └────────────────┘  └─────────────────┘ │  │
│  │                                                                │  │
│  │  ┌─────────────────┐  ┌─────────────────┐                    │  │
│  │  │RecordingService  │  │  AuditService    │                    │  │
│  │  │                 │  │                 │                    │  │
│  │  │ - Add recording │  │ - Log events    │                    │  │
│  │  │ - Get/Delete    │  │ - Query logs    │                    │  │
│  │  │ - Clear all     │  │ - Date range    │                    │  │
│  │  └─────────────────┘  └─────────────────┘                    │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                  DATA ACCESS LAYER (Spring Data JPA)            │  │
│  │                                                                │  │
│  │  ┌──────────────────┐  ┌──────────────────┐                   │  │
│  │  │StudentRepository  │  │TeacherRepository  │                   │  │
│  │  │MeetingRepository  │  │ChatMessageRepo    │                   │  │
│  │  │RecordingRepository│  │AuditLogRepository  │                   │  │
│  │  └──────────────────┘  └──────────────────┘                   │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                  CROSS-CUTTING CONCERNS                         │  │
│  │                                                                │  │
│  │  - GlobalExceptionHandler (@ControllerAdvice)                  │  │
│  │  - DataInitializer (CommandLineRunner – seed data)             │  │
│  │  - SLF4J Logging (file + console)                              │  │
│  │  - Transaction Management (@Transactional)                     │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         DATA TIER                                    │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │              H2 In-Memory Database (mtngdb)                    │  │
│  │                                                                │  │
│  │   Tables: students, teachers, meetings,                        │  │
│  │           chat_messages, recordings, audit_logs                │  │
│  │                                                                │  │
│  │   Console: http://localhost:8080/h2-console (ADMIN only)       │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     EXTERNAL SERVICES                                │
│                                                                      │
│  ┌────────────────────┐  ┌─────────────────────────┐                │
│  │  Jitsi Meet Server  │  │  WhatsApp Deep Link API  │                │
│  │  (meet.jit.si)      │  │  (wa.me)                 │                │
│  └────────────────────┘  └─────────────────────────┘                │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. Module Decomposition

### 3.1 Module Overview

```
com.Mtng.Mtng
├── MtngApplication.java              # Spring Boot entry point
├── config/                            # Configuration module
│   ├── SecurityConfig.java            # Spring Security filter chain
│   ├── CustomAuthenticationSuccessHandler.java
│   ├── CustomLogoutSuccessHandler.java
│   └── DataInitializer.java           # Startup data seeding
├── controller/                        # Presentation module
│   ├── DashboardController.java       # Thymeleaf view routes
│   ├── LoginController.java           # Login/logout views
│   ├── MeetingApiController.java      # Meeting REST API
│   ├── StudentApiController.java      # Student REST API
│   ├── ChatApiController.java         # Chat REST API
│   ├── RecordingApiController.java    # Recording REST API
│   ├── SettingsApiController.java     # Settings + WhatsApp REST API
│   └── GlobalExceptionHandler.java    # Global error handling
├── service/                           # Business logic module
│   ├── MeetingService.java
│   ├── StudentService.java
│   ├── ChatService.java
│   ├── RecordingService.java
│   ├── AuditService.java
│   └── MtngUserDetailsService.java    # Spring Security UserDetailsService
├── repository/                        # Data access module
│   ├── StudentRepository.java
│   ├── TeacherRepository.java
│   ├── MeetingRepository.java
│   ├── ChatMessageRepository.java
│   ├── RecordingRepository.java
│   └── AuditLogRepository.java
└── model/                             # Domain model module
    ├── Student.java
    ├── Teacher.java
    ├── Meeting.java
    ├── ChatMessage.java
    ├── Recording.java
    └── AuditLog.java
```

### 3.2 Module Dependencies

```
Controller Layer ──depends-on──► Service Layer ──depends-on──► Repository Layer
       │                               │                             │
       │                               │                             ▼
       │                               │                      JPA / Hibernate
       │                               │                             │
       ▼                               ▼                             ▼
  Spring Security               Domain Models                  H2 Database
```

---

## 4. Security Architecture

### 4.1 Authentication Flow

```
User ──► GET /login ──► Login Page (Thymeleaf)
  │
  └──► POST /login ──► Spring Security Filter Chain
                            │
                            ▼
                   DaoAuthenticationProvider
                            │
                            ▼
                   MtngUserDetailsService
                       │           │
                       ▼           ▼
                  Teacher DB   Student DB
                       │           │
                       ▼           ▼
                  ROLE_ADMIN    ROLE_USER
                  or ROLE_USER
                       │
                       ▼
              CustomAuthenticationSuccessHandler
                       │
                       ├── Mark student ONLINE (if applicable)
                       └── Redirect to / (dashboard)
```

### 4.2 Authorization Matrix

| Resource                          | ADMIN | USER  | Anonymous |
|-----------------------------------|-------|-------|-----------|
| `/login`                          | ✅    | ✅    | ✅        |
| `/css/**`, `/js/**`               | ✅    | ✅    | ✅        |
| `/` (Dashboard)                   | ✅    | ✅    | ❌        |
| `/students`                       | ✅    | ✅    | ❌        |
| `/chat`                           | ✅    | ✅    | ❌        |
| `/recordings`                     | ✅    | ✅    | ❌        |
| `/meeting-room`                   | ✅    | ✅    | ❌        |
| `/create-student`                 | ✅    | ❌    | ❌        |
| `/h2-console`                     | ✅    | ❌    | ❌        |
| `POST /api/meeting/start`         | ✅    | ❌    | ❌        |
| `POST /api/meeting/stop`          | ✅    | ❌    | ❌        |
| `POST /api/meeting/join`          | ✅    | ✅    | ❌        |
| `POST /api/students` (create)     | ✅    | ❌    | ❌        |
| `GET /api/students` (list)        | ✅    | ✅    | ❌        |
| `DELETE /api/students/{id}`       | ✅    | ❌    | ❌        |
| `POST /api/students/{id}/block`   | ✅    | ❌    | ❌        |
| `POST /api/students/{id}/mute`    | ✅    | ❌    | ❌        |
| `POST /api/chat/send`             | ✅    | ✅    | ❌        |
| `DELETE /api/chat/clear`          | ✅    | ❌    | ❌        |
| `POST /api/recordings`            | ✅    | ❌    | ❌        |
| `DELETE /api/recordings/{id}`     | ✅    | ❌    | ❌        |
| `POST /api/settings/change-password` | ✅ | ✅   | ❌        |

---

## 5. Data Flow Diagrams

### 5.1 Meeting Lifecycle

```
ADMIN clicks "Start Meeting"
        │
        ▼
POST /api/meeting/start  ──►  MeetingApiController
        │                            │
        │                            ▼
        │                     MeetingService.startMeeting()
        │                            │
        │                  ┌─────────┴─────────┐
        │                  │                   │
        │            Stop existing        Create new Meeting
        │            active meeting       (roomName = mtng-{uuid})
        │                  │                   │
        │                  └─────────┬─────────┘
        │                            │
        │                            ▼
        │                     MeetingRepository.save()
        │                            │
        ▼                            ▼
  Response: {meetingId, roomName, redirect: "/meeting-room"}
        │
        ▼
  Students navigate to /meeting-room
        │
        ▼
  POST /api/meeting/join  ──►  addStudentToMeeting()
        │
        ▼
  Jitsi iframe loads shared room
        │
        ▼
  ADMIN clicks "End Meeting"
        │
        ▼
  POST /api/meeting/stop  ──►  stopMeeting()
        │
        ├── Set meeting.active = false
        ├── Record endTime
        ├── If fullRecording → save recordings for all participants
        └── Clear participant tracking
```

### 5.2 Student Registration & Sharing

```
ADMIN fills "Create Student" form
        │
        ▼
POST /api/students  ──►  StudentApiController.create()
        │                        │
        │                        ▼
        │               StudentService.createStudent()
        │                        │
        │               ┌────────┴────────┐
        │               │                 │
        │         Check uniqueness   BCrypt encode password
        │               │                 │
        │               └────────┬────────┘
        │                        │
        │                        ▼
        │               StudentRepository.save()
        │                        │
        ▼                        ▼
  Student created successfully
        │
        ▼
  ADMIN clicks "WhatsApp" icon
        │
        ▼
  GET /api/students/{id}/whatsapp-link
        │
        ▼
  SettingsApiController  ──►  Detect LAN IP  ──►  Build message
        │
        ▼
  Open https://wa.me/?text={encoded_message}
```

---

## 6. Deployment Architecture

```
┌─────────────────────────────────────────────┐
│              Deployment Server               │
│             (Windows / Linux)                │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │         JVM (Java 17)                  │  │
│  │                                        │  │
│  │  ┌──────────────────────────────────┐  │  │
│  │  │  Mtng-0.0.1-SNAPSHOT.jar         │  │  │
│  │  │  (Embedded Tomcat on port 8080)  │  │  │
│  │  │                                  │  │  │
│  │  │  Spring Boot Application         │  │  │
│  │  │  + H2 In-Memory Database         │  │  │
│  │  └──────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  Port: 8080 (HTTP)                           │
│  Log:  app.log (10MB max, 5 file rotation)   │
└─────────────────────────────────────────────┘
         │
         │  LAN Network (192.168.x.x)
         │
    ┌────┴────┬─────────┬─────────┐
    ▼         ▼         ▼         ▼
 Teacher   Student1  Student2  StudentN
 Browser   Browser   Browser   Browser
```

---

## 7. Integration Points

| Integration       | Type        | Protocol | Description                           |
|-------------------|-------------|----------|---------------------------------------|
| Jitsi Meet        | External    | HTTPS    | Video/audio conferencing via iframe   |
| WhatsApp          | External    | HTTPS    | Deep link for credential sharing      |
| H2 Console        | Internal    | HTTP     | Database admin UI at `/h2-console`    |
| Browser JS        | Internal    | HTTP     | AJAX calls to REST APIs               |

---

## 8. Error Handling Strategy

| Scenario                  | Handler                      | Response                       |
|---------------------------|------------------------------|--------------------------------|
| API 500 error             | GlobalExceptionHandler       | JSON: {status, error, message} |
| Page 500 error            | GlobalExceptionHandler       | error-page.html template       |
| 403 Forbidden (API)       | AccessDeniedHandler          | JSON: {error, message}         |
| 403 Forbidden (Page)      | AccessDeniedHandler          | Redirect to `/access-denied`   |
| Invalid login             | Spring Security              | Redirect to `/login?error`     |
| Entity not found          | Service layer exceptions     | 400 Bad Request with message   |
| Duplicate username        | StudentService               | 400 Bad Request with message   |

---

## 9. Logging Strategy

| Logger Level | Target                          | Output                     |
|-------------|---------------------------------|----------------------------|
| `WARN`      | Root (all frameworks)            | Console + app.log          |
| `INFO`      | `com.Mtng.Mtng`                  | Console + app.log          |
| `INFO`      | `com.Mtng.Mtng.service.AuditService` | Console + app.log    |
| `WARN`      | Spring Security                  | Console + app.log          |
| `ERROR`     | Security initializer             | Console + app.log          |

**Log file configuration:**
- File: `app.log`
- Max size: 10MB per file
- Max history: 5 rotated files
- Console pattern: `HH:mm:ss.SSS [thread] LEVEL logger - message`
- File pattern: `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

