# 🏛️ System Architecture Document

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Architecture**    | Monolithic MVC + REST API Hybrid          |

---

## 1. Architecture Overview

MTNG follows a **Monolithic Layered Architecture** pattern with a hybrid approach combining:
- **Server-Side Rendering (SSR)** via Thymeleaf for full page navigation
- **REST API layer** for dynamic client-side interactions via JavaScript (AJAX/Fetch)

```
┌─────────────────────────────────────────────────────────────┐
│                    MTNG SYSTEM ARCHITECTURE                   │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐   │
│  │                   CLIENT LAYER                         │   │
│  │  ┌───────────┐  ┌──────────┐  ┌───────────────────┐  │   │
│  │  │ Thymeleaf │  │  CSS      │  │  JavaScript        │  │   │
│  │  │ HTML      │  │ (login.css│  │  (mtng.js)         │  │   │
│  │  │ Templates │  │  mtng.css)│  │  - AJAX to REST    │  │   │
│  │  │ (11 pages)│  │          │  │  - DOM manipulation │  │   │
│  │  └───────────┘  └──────────┘  └───────────────────┘  │   │
│  └───────────────────────────────────────────────────────┘   │
│                              │                                │
│                    HTTP / REST API                            │
│                              │                                │
│  ┌───────────────────────────────────────────────────────┐   │
│  │                SPRING BOOT APPLICATION                  │   │
│  │                                                         │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │          SECURITY LAYER (Spring Security 6)      │   │   │
│  │  │                                                   │   │   │
│  │  │  ┌─────────────┐  ┌──────────────────────────┐  │   │   │
│  │  │  │ Auth Filter  │  │  URL Authorization Rules  │  │   │   │
│  │  │  │ Chain        │  │  @PreAuthorize Methods    │  │   │   │
│  │  │  └─────────────┘  └──────────────────────────┘  │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                              │                          │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │          CONTROLLER LAYER                        │   │   │
│  │  │                                                   │   │   │
│  │  │  ┌──────────────┐  ┌───────────────────────┐    │   │   │
│  │  │  │  @Controller  │  │   @RestController      │    │   │   │
│  │  │  │  (Views)      │  │   (REST APIs)          │    │   │   │
│  │  │  │              │  │                         │    │   │   │
│  │  │  │ Dashboard    │  │  Meeting API            │    │   │   │
│  │  │  │ Login        │  │  Student API            │    │   │   │
│  │  │  │              │  │  Chat API               │    │   │   │
│  │  │  │              │  │  Recording API           │    │   │   │
│  │  │  │              │  │  Settings API            │    │   │   │
│  │  │  └──────────────┘  └───────────────────────┘    │   │   │
│  │  │                                                   │   │   │
│  │  │  ┌──────────────────────────────────────────┐    │   │   │
│  │  │  │  @ControllerAdvice (GlobalExceptionHandler) │    │   │   │
│  │  │  └──────────────────────────────────────────┘    │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                              │                          │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │          SERVICE LAYER                           │   │   │
│  │  │                                                   │   │   │
│  │  │  MeetingService     StudentService               │   │   │
│  │  │  ChatService        RecordingService             │   │   │
│  │  │  AuditService       MtngUserDetailsService       │   │   │
│  │  │                                                   │   │   │
│  │  │  • Business logic & validation                   │   │   │
│  │  │  • @Transactional management                     │   │   │
│  │  │  • Cross-service coordination                    │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                              │                          │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │       DATA ACCESS LAYER (Spring Data JPA)        │   │   │
│  │  │                                                   │   │   │
│  │  │  StudentRepository    TeacherRepository          │   │   │
│  │  │  MeetingRepository    ChatMessageRepository      │   │   │
│  │  │  RecordingRepository  AuditLogRepository         │   │   │
│  │  │                                                   │   │   │
│  │  │  • JpaRepository interfaces                      │   │   │
│  │  │  • Derived query methods                         │   │   │
│  │  │  • Hibernate ORM mapping                         │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                              │                          │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │          DATA LAYER                              │   │   │
│  │  │                                                   │   │   │
│  │  │     H2 In-Memory Database (mtngdb)               │   │   │
│  │  │     Tables: 6 (students, teachers, meetings,     │   │   │
│  │  │              chat_messages, recordings,           │   │   │
│  │  │              audit_logs)                          │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                │
│  ┌───────────────────────────────────────────────────────┐   │
│  │              EXTERNAL INTEGRATIONS                     │   │
│  │                                                         │   │
│  │  ┌────────────────┐    ┌────────────────────────┐     │   │
│  │  │  Jitsi Meet     │    │  WhatsApp Deep Links    │     │   │
│  │  │  (meet.jit.si)  │    │  (wa.me/?text=...)      │     │   │
│  │  │                │    │                          │     │   │
│  │  │  • Audio/Video  │    │  • Credential sharing   │     │   │
│  │  │  • WebRTC       │    │  • LAN IP auto-detect   │     │   │
│  │  │  • iframe embed │    │  • URL encoding          │     │   │
│  │  └────────────────┘    └────────────────────────┘     │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Architecture Principles

| Principle                  | Implementation                                                |
|----------------------------|---------------------------------------------------------------|
| **Separation of Concerns** | Controllers → Services → Repositories → Database             |
| **Single Responsibility**  | Each service handles one domain area                         |
| **Dependency Injection**   | Constructor-based injection via @Autowired                   |
| **Open/Closed**            | JpaRepository interfaces extended without modification       |
| **Convention over Config** | Spring Boot auto-configuration, derived query methods        |
| **Fail-Safe Defaults**     | Global exception handler catches all unhandled errors        |

---

## 3. Component Architecture

### 3.1 Spring Boot Configuration Components

```
                    MtngApplication (@SpringBootApplication)
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
           Auto-Config   Component   Data Init
                │         Scan         │
                ▼           │          ▼
        ┌───────────┐      │    DataInitializer
        │ Embedded   │      │    (CommandLineRunner)
        │ Tomcat     │      │         │
        │ Port:8080  │      │    Seed teachers,
        └───────────┘      │    students, recordings
                            │
               ┌────────────┼────────────┐
               ▼            ▼            ▼
         Controllers    Services    Repositories
```

### 3.2 Authentication Architecture

```
                   HTTP Request
                       │
                       ▼
            Spring Security Filter Chain
                       │
              ┌────────┴────────┐
              ▼                 ▼
        Login Request      Authenticated Request
              │                 │
              ▼                 ▼
     UsernamePassword      SecurityContext
     AuthFilter            .getAuthentication()
              │                 │
              ▼                 ▼
     DaoAuthenticationProvider   Controller
              │                    │
              ▼                    ▼
     MtngUserDetailsService    @PreAuthorize
              │                 check role
         ┌────┴────┐
         ▼         ▼
     Teacher    Student
     Table      Table
         │         │
         ▼         ▼
     ROLE_ADMIN  ROLE_USER
     ROLE_USER
```

### 3.3 Request Processing Pipeline

```
Client Request
    │
    ▼
[Embedded Tomcat (Port 8080)]
    │
    ▼
[Spring Security Filter Chain]
    │  ├── CsrfFilter (disabled for /api/**)
    │  ├── UsernamePasswordAuthenticationFilter
    │  ├── AuthorizationFilter (URL rules)
    │  └── ExceptionTranslationFilter
    │
    ▼
[DispatcherServlet]
    │
    ├──► @Controller routes → Thymeleaf → HTML Response
    │
    └──► @RestController routes → JSON Response
             │
             ▼
        [Service Layer (@Transactional)]
             │
             ▼
        [Repository Layer (JPA)]
             │
             ▼
        [Hibernate ORM]
             │
             ▼
        [H2 Database]
```

---

## 4. Networking Architecture

### 4.1 LAN Deployment Model

```
                    ┌──────────────────┐
                    │   MTNG Server     │
                    │   192.168.x.x     │
                    │   Port: 8080      │
                    │                  │
                    │ ┌──────────────┐ │
                    │ │ Spring Boot  │ │
                    │ │ + H2 DB      │ │
                    │ └──────────────┘ │
                    └────────┬─────────┘
                             │
                    ┌────────┴────────┐
                    │   LAN Router     │
                    │  192.168.x.1     │
                    └────────┬─────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
    ┌────┴──────┐     ┌─────┴──────┐      ┌─────┴──────┐
    │  Teacher   │     │  Student 1  │      │  Student N  │
    │  Browser   │     │  Browser    │      │  Browser    │
    │192.168.x.y │     │192.168.x.z  │      │192.168.x.w  │
    └───────────┘     └────────────┘      └────────────┘
```

### 4.2 LAN IP Auto-Detection

```
SettingsApiController.detectLanIp()
    │
    ▼
NetworkInterface.getNetworkInterfaces()
    │
    ├── Skip loopback (127.0.0.1)
    ├── Skip down interfaces
    ├── Skip virtual interfaces (docker, vmnet, vbox)
    │
    ▼
Find first IPv4, non-loopback address
    │
    ▼
Return "192.168.x.x" (or "localhost" fallback)
```

---

## 5. Meeting Room Architecture

### 5.1 Jitsi Integration

```
┌─────────────────────────────────────────────────────────┐
│                   MEETING ROOM PAGE                       │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │  Header: Teacher Name, Timer, Participant Counts     │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │  TAB BAR                                             │ │
│  │  [Meeting Controls] [Chat] [Students] [Create] [Rec] │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                                                       │ │
│  │           Jitsi Meet iframe                           │ │
│  │           Room: mtng-{uuid}                           │ │
│  │           Source: meet.jit.si/mtng-{uuid}             │ │
│  │                                                       │ │
│  │  All participants join SAME room via shared roomName  │ │
│  │                                                       │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │  Footer: [🔊 Volume] [🎤 Mute Toggle] [🔔 Alert]   │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Meeting Room Data Flow

```
Admin starts meeting → POST /api/meeting/start
     │
     ▼
Server creates Meeting (roomName = "mtng-abc12345")
     │
     ▼
Response: { roomName: "mtng-abc12345", redirect: "/meeting-room" }
     │
     ▼
All users navigate to /meeting-room
     │
     ▼
Thymeleaf renders page with roomName in model
     │
     ▼
JavaScript loads Jitsi iframe: meet.jit.si/mtng-abc12345
     │
     ├──► Teacher (Admin) joins room as moderator
     └──► Students join same room as participants
```

---

## 6. Security Architecture

### 6.1 Multi-Layer Security

```
Layer 1: URL-based Authorization (SecurityFilterChain)
    ├── Public:    /login, /css/**, /js/**
    ├── ADMIN:     /create-student, /h2-console
    └── Auth:      Everything else

Layer 2: Method-level Security (@PreAuthorize)
    ├── hasRole('ADMIN'): start, stop, create, delete, block, mute
    └── authenticated(): join, leave, chat, view

Layer 3: Custom Access Denied Handler
    ├── API requests → 403 JSON response
    └── Page requests → Redirect to /access-denied

Layer 4: CSRF Configuration
    ├── Enabled for all web forms
    └── Disabled for /api/** (REST client compatibility)

Layer 5: Password Security
    └── BCryptPasswordEncoder for all passwords
```

### 6.2 Session Management

```
Login → Create HttpSession → Store SecurityContext
                                    │
                     ┌──────────────┴──────────────┐
                     │                              │
              JSESSIONID cookie               SecurityContextHolder
              (sent with every request)       (Authentication object)
                     │                              │
                     ▼                              ▼
              Session Validation             Role-based decisions
                     │
                     ▼
              Logout → Invalidate session
                     → Delete JSESSIONID
                     → Mark student OFFLINE
                     → Redirect to /login
```

---

## 7. Logging & Monitoring Architecture

```
┌──────────────────────────────────────────┐
│           APPLICATION LOGGING             │
│                                          │
│  SLF4J (Facade) → Logback (Implementation)│
│                                          │
│  ┌──────────┐    ┌──────────────────────┐│
│  │ Console   │    │ File: app.log        ││
│  │ Appender  │    │ Max: 10MB            ││
│  │           │    │ History: 5 files     ││
│  │ Pattern:  │    │                      ││
│  │ HH:mm:ss  │    │ Pattern:             ││
│  │ [thread]  │    │ yyyy-MM-dd HH:mm:ss  ││
│  │ LEVEL msg │    │ [thread] LEVEL msg   ││
│  └──────────┘    └──────────────────────┘│
│                                          │
│  Log Levels:                             │
│  ├── ROOT: WARN                          │
│  ├── com.Mtng.Mtng: INFO                 │
│  ├── AuditService: INFO                  │
│  └── Spring Security: WARN               │
│                                          │
│  Audit Trail:                            │
│  ├── [AUDIT] prefix in log messages      │
│  ├── AuditLog table in database          │
│  └── Events: LOGIN, LOGOUT, MEETING_*,   │
│       CHAT_SEND, STUDENT_*               │
└──────────────────────────────────────────┘
```

---

## 8. Error Handling Architecture

```
                 Exception occurs
                       │
                       ▼
           GlobalExceptionHandler
           (@ControllerAdvice)
                       │
              ┌────────┴────────┐
              │                 │
         API Request       Page Request
         (Accept: JSON     (Accept: HTML
          or /api/ URI)     or other)
              │                 │
              ▼                 ▼
         JSON Response     error-page.html
         {                 Template with:
           status: 500,    - errorMessage
           error: "...",   - errorType
           message: "...", - requestUri
           path: "..."    - Cause chain
         }
```

---

## 9. Build & Deployment Architecture

### 9.1 Build Pipeline

```
Source Code (Java 17)
       │
       ▼
Maven Build (mvnw / mvnw.cmd)
       │
       ├── Compile (javac 17)
       ├── Test (JUnit 5 + Spring Test)
       ├── Package (spring-boot-maven-plugin)
       │
       ▼
Mtng-0.0.1-SNAPSHOT.jar
(Fat JAR with embedded Tomcat)
       │
       ▼
java -jar Mtng-0.0.1-SNAPSHOT.jar
       │
       ▼
Application running on http://0.0.0.0:8080
```

### 9.2 Runtime Architecture

```
┌─────────────────────────────────────────┐
│            JVM (Java 17)                 │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │    Embedded Apache Tomcat          │  │
│  │    (Port: 8080)                    │  │
│  │                                    │  │
│  │  ┌──────────────────────────────┐  │  │
│  │  │   Spring Boot Application    │  │  │
│  │  │                              │  │  │
│  │  │  ┌────────────────────────┐  │  │  │
│  │  │  │ DispatcherServlet      │  │  │  │
│  │  │  └────────────────────────┘  │  │  │
│  │  │  ┌────────────────────────┐  │  │  │
│  │  │  │ Spring Security Filters│  │  │  │
│  │  │  └────────────────────────┘  │  │  │
│  │  │  ┌────────────────────────┐  │  │  │
│  │  │  │ H2 Database Engine     │  │  │  │
│  │  │  │ (In-Process, In-Memory)│  │  │  │
│  │  │  └────────────────────────┘  │  │  │
│  │  │  ┌────────────────────────┐  │  │  │
│  │  │  │ HikariCP Connection    │  │  │  │
│  │  │  │ Pool                   │  │  │  │
│  │  │  └────────────────────────┘  │  │  │
│  │  └──────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 10. Scalability Considerations

| Aspect             | Current State              | Future Enhancement                    |
|--------------------|----------------------------|---------------------------------------|
| **Database**       | H2 In-Memory               | PostgreSQL with connection pooling    |
| **Caching**        | None                       | Redis/Ehcache for session & data      |
| **Real-time**      | HTTP polling (AJAX)        | WebSocket for chat & notifications    |
| **File Storage**   | Not implemented            | S3/Azure Blob for recordings          |
| **Load Balancing** | Single instance            | Nginx + multiple instances            |
| **Session**        | Server-side (Tomcat)       | Redis-backed distributed sessions     |
| **Meeting Infra**  | Jitsi Meet (public)        | Self-hosted Jitsi for privacy         |
| **Search**         | JPA LIKE queries           | Elasticsearch for full-text search    |
| **Monitoring**     | Log files                  | Prometheus + Grafana dashboards       |

