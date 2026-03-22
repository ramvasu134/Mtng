# 🔧 Low-Level Design (LLD) Document

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Project Name**    | MTNG (Meeting Management Platform)        |

---

## 1. Package Structure

```
com.Mtng.Mtng/
│
├── MtngApplication.java                    # @SpringBootApplication entry point
│
├── config/
│   ├── SecurityConfig.java                  # @Configuration @EnableWebSecurity
│   ├── CustomAuthenticationSuccessHandler.java  # AuthenticationSuccessHandler
│   ├── CustomLogoutSuccessHandler.java      # LogoutSuccessHandler
│   └── DataInitializer.java                 # CommandLineRunner (seed data)
│
├── controller/
│   ├── DashboardController.java             # @Controller (Thymeleaf views)
│   ├── LoginController.java                 # @Controller (login/logout)
│   ├── MeetingApiController.java            # @RestController /api/meeting
│   ├── StudentApiController.java            # @RestController /api/students
│   ├── ChatApiController.java               # @RestController /api/chat
│   ├── RecordingApiController.java          # @RestController /api/recordings
│   ├── SettingsApiController.java           # @RestController /api/settings
│   └── GlobalExceptionHandler.java          # @ControllerAdvice
│
├── service/
│   ├── MeetingService.java                  # @Service @Transactional
│   ├── StudentService.java                  # @Service @Transactional
│   ├── ChatService.java                     # @Service @Transactional
│   ├── RecordingService.java                # @Service @Transactional
│   ├── AuditService.java                    # @Service @Transactional
│   └── MtngUserDetailsService.java          # @Service implements UserDetailsService
│
├── repository/
│   ├── StudentRepository.java               # JpaRepository<Student, Long>
│   ├── TeacherRepository.java               # JpaRepository<Teacher, Long>
│   ├── MeetingRepository.java               # JpaRepository<Meeting, Long>
│   ├── ChatMessageRepository.java           # JpaRepository<ChatMessage, Long>
│   ├── RecordingRepository.java             # JpaRepository<Recording, Long>
│   └── AuditLogRepository.java              # JpaRepository<AuditLog, Long>
│
└── model/
    ├── Student.java                          # @Entity students
    ├── Teacher.java                          # @Entity teachers
    ├── Meeting.java                          # @Entity meetings
    ├── ChatMessage.java                      # @Entity chat_messages
    ├── Recording.java                        # @Entity recordings
    └── AuditLog.java                         # @Entity audit_logs
```

---

## 2. Entity Class Designs

### 2.1 Student Entity

```java
@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;           // BCrypt-encoded

    @Column(name = "raw_password")
    private String rawPassword;        // Plain-text for admin display

    private boolean deviceLock = false;
    private boolean showRecordings = true;
    private boolean blocked = false;
    private boolean muted = false;
    private String status = "OFFLINE"; // ONLINE | OFFLINE
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;

    @PrePersist → sets createdAt and lastSeen to now()
}
```

**Field Constraints:**

| Field          | Type          | Nullable | Unique | Default    | Notes                    |
|----------------|---------------|----------|--------|------------|--------------------------|
| id             | Long          | No       | Yes    | Auto       | Primary Key, IDENTITY    |
| name           | String        | No       | No     | —          | Student display name     |
| username       | String        | No       | Yes    | —          | Login identifier         |
| password       | String        | No       | No     | —          | BCrypt hash              |
| rawPassword    | String        | Yes      | No     | null       | Plain text for sharing   |
| deviceLock     | boolean       | No       | No     | false      | Device lock toggle       |
| showRecordings | boolean       | No       | No     | true       | Show recordings toggle   |
| blocked        | boolean       | No       | No     | false      | Block from meetings      |
| muted          | boolean       | No       | No     | false      | Mute in meetings         |
| status         | String        | Yes      | No     | "OFFLINE"  | ONLINE/OFFLINE           |
| createdAt      | LocalDateTime | Yes      | No     | now()      | @PrePersist              |
| lastSeen       | LocalDateTime | Yes      | No     | now()      | Updated on login/logout  |

---

### 2.2 Teacher Entity

```java
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;           // BCrypt-encoded

    @Column(nullable = false)
    private String role = "ROLE_ADMIN";

    @Column(nullable = false)
    private String displayName;
}
```

| Field       | Type   | Nullable | Unique | Default       |
|-------------|--------|----------|--------|---------------|
| id          | Long   | No       | Yes    | Auto          |
| username    | String | No       | Yes    | —             |
| password    | String | No       | No     | —             |
| role        | String | No       | No     | "ROLE_ADMIN"  |
| displayName | String | No       | No     | —             |

---

### 2.3 Meeting Entity

```java
@Entity
@Table(name = "meetings")
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String roomName;           // Jitsi room: "mtng-{uuid8}"

    private boolean active = false;
    private boolean fullRecording = false;
    private int inMeetingCount = 0;
    private int onlineCount = 0;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @PrePersist → sets default title if null
}
```

| Field          | Type          | Nullable | Unique | Default        |
|----------------|---------------|----------|--------|----------------|
| id             | Long          | No       | Yes    | Auto           |
| title          | String        | No       | No     | "New Meeting"  |
| roomName       | String        | Yes      | Yes    | —              |
| active         | boolean       | No       | No     | false          |
| fullRecording  | boolean       | No       | No     | false          |
| inMeetingCount | int           | No       | No     | 0              |
| onlineCount    | int           | No       | No     | 0              |
| startTime      | LocalDateTime | Yes      | No     | —              |
| endTime        | LocalDateTime | Yes      | No     | —              |

---

### 2.4 ChatMessage Entity

```java
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, length = 1000)
    private String content;

    private LocalDateTime sentAt;
    private Long meetingId;

    @PrePersist → sets sentAt to now()
}
```

| Field     | Type          | Nullable | Length | Default |
|-----------|---------------|----------|--------|---------|
| id        | Long          | No       | —      | Auto    |
| sender    | String        | No       | 255    | —       |
| content   | String        | No       | 1000   | —       |
| sentAt    | LocalDateTime | Yes      | —      | now()   |
| meetingId | Long          | Yes      | —      | —       |

---

### 2.5 Recording Entity

```java
@Entity
@Table(name = "recordings")
public class Recording {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private int durationSeconds;
    private LocalDate recordingDate;
    private LocalTime recordingTime;
    private Long meetingId;

    @PrePersist → sets date and time to now() if null
}
```

| Field           | Type      | Nullable | Default |
|-----------------|-----------|----------|---------|
| id              | Long      | No       | Auto    |
| studentId       | Long      | Yes      | —       |
| studentName     | String    | Yes      | —       |
| durationSeconds | int       | No       | 0       |
| recordingDate   | LocalDate | Yes      | today() |
| recordingTime   | LocalTime | Yes      | now()   |
| meetingId       | Long      | Yes      | —       |

---

### 2.6 AuditLog Entity

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(length = 500)
    private String details;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist → sets timestamp to now() if null
}
```

| Field     | Type          | Nullable | Length | Default |
|-----------|---------------|----------|--------|---------|
| id        | Long          | No       | —      | Auto    |
| actor     | String        | No       | 255    | —       |
| action    | String        | No       | 255    | —       |
| details   | String        | Yes      | 500    | —       |
| ipAddress | String        | Yes      | 255    | —       |
| timestamp | LocalDateTime | No       | —      | now()   |

---

## 3. Repository Interface Designs

### 3.1 StudentRepository
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsername(String username);
    List<Student> findByStatus(String status);
    List<Student> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String name, String username);
    boolean existsByUsername(String username);
}
```

### 3.2 TeacherRepository
```java
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUsername(String username);
}
```

### 3.3 MeetingRepository
```java
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByActive(boolean active);
}
```

### 3.4 ChatMessageRepository
```java
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByMeetingIdOrderBySentAtAsc(Long meetingId);
    void deleteByMeetingId(Long meetingId);
}
```

### 3.5 RecordingRepository
```java
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(Long studentId);
    List<Recording> findByMeetingId(Long meetingId);
    void deleteByStudentId(Long studentId);
}
```

### 3.6 AuditLogRepository
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorOrderByTimestampDesc(String actor);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);
    List<AuditLog> findTop100ByOrderByTimestampDesc();
}
```

---

## 4. Service Class Designs

### 4.1 MeetingService

```
@Service @Transactional
class MeetingService {

    // Dependencies
    - MeetingRepository meetingRepository
    - RecordingRepository recordingRepository
    - Map<Long, Set<String>> meetingParticipants  (in-memory tracking)

    // Methods
    + getActiveMeeting() : Optional<Meeting>
        → findByActive(true), return first or empty

    + startMeeting(title: String) : Meeting
        → 1. Stop all active meetings
        → 2. Create new Meeting with UUID-based roomName
        → 3. Initialize participant set
        → 4. Save and return

    + stopMeeting() : void
        → 1. Find active meeting
        → 2. Set active=false, endTime=now
        → 3. If fullRecording, save Recording for each participant
        → 4. Clean up participants map

    + addStudentToMeeting(meetingId: Long, username: String) : void
        → Add to participants map, update DB count

    + removeStudentFromMeeting(meetingId: Long, username: String) : void
        → Remove from participants map, update DB count

    + toggleFullRecording() : Meeting
        → Toggle fullRecording flag on active meeting

    + updateCounters(id: Long, inMeeting: int, online: int) : Meeting

    + getAllMeetings() : List<Meeting>

    + findById(id: Long) : Optional<Meeting>

    - updateInMeetingCount(meetingId: Long) : void [private]

    - calculateDuration(start, end) : int [private]
        → ChronoUnit.SECONDS.between(start, end)
}
```

### 4.2 StudentService

```
@Service @Transactional
class StudentService {

    // Dependencies
    - StudentRepository studentRepository
    - PasswordEncoder passwordEncoder

    // Methods
    + getAllStudents() : List<Student>

    + findById(id: Long) : Optional<Student>

    + search(keyword: String) : List<Student>
        → findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase

    + createStudent(student: Student) : Student
        → 1. Check username uniqueness (existsByUsername)
        → 2. Store rawPassword
        → 3. BCrypt encode password
        → 4. Save

    + updateStudent(id: Long, updated: Student) : Student
        → 1. Find existing
        → 2. Update name, deviceLock, showRecordings
        → 3. If password provided, re-encode
        → 4. Save

    + toggleBlock(id: Long) : Student
        → Flip blocked flag

    + toggleMute(id: Long) : Student
        → Flip muted flag

    + deleteStudent(id: Long) : void

    + getTotalCount() : long → count()

    + getOnlineCount() : long → findByStatus("ONLINE").size()

    + markSeen(id: Long) : void → update lastSeen

    + markOnline(username: String) : void
        → Set status="ONLINE", lastSeen=now

    + markOffline(username: String) : void
        → Set status="OFFLINE", lastSeen=now
}
```

### 4.3 ChatService

```
@Service @Transactional
class ChatService {

    // Dependencies
    - ChatMessageRepository chatRepo

    // Methods
    + sendMessage(sender, content, meetingId) : ChatMessage
        → Create and save ChatMessage

    + getMessages(meetingId: Long) : List<ChatMessage>
        → findByMeetingIdOrderBySentAtAsc

    + clearMessages(meetingId: Long) : void
        → deleteByMeetingId

    + getAllMessages() : List<ChatMessage>
        → findAll
}
```

### 4.4 RecordingService

```
@Service @Transactional
class RecordingService {

    // Dependencies
    - RecordingRepository recordingRepo

    // Methods
    + addRecording(studentId, studentName, durationSeconds, meetingId) : Recording

    + getStudentRecordings(studentId) : List<Recording>
        → Ordered by date desc, time desc

    + getAllRecordings() : List<Recording>

    + deleteRecording(id: Long) : void

    + deleteStudentRecordings(studentId: Long) : void

    + clearAll() : void
}
```

### 4.5 AuditService

```
@Service @Transactional
class AuditService {

    // Dependencies
    - AuditLogRepository auditRepo

    // Methods
    + logEvent(actor, action, details, ip) : void
        → Create and save AuditLog entry

    + logEvent(actor, action, details) : void
        → Overload without IP

    + getRecentLogs() : List<AuditLog>
        → Top 100 by timestamp desc

    + getLogsByActor(actor) : List<AuditLog>

    + getLogsByAction(action) : List<AuditLog>

    + getLogsBetween(from, to) : List<AuditLog>
}
```

### 4.6 MtngUserDetailsService

```
@Service implements UserDetailsService
class MtngUserDetailsService {

    // Dependencies
    - TeacherRepository teacherRepo
    - StudentRepository studentRepo

    // Methods
    + loadUserByUsername(username) : UserDetails
        → 1. Check Teacher table first
        →    If found: return User with teacher's role
        → 2. Check Student table
        →    If found: return User with ROLE_USER
        → 3. Throw UsernameNotFoundException
}
```

---

## 5. Controller Designs

### 5.1 REST API Endpoint Specifications

#### 5.1.1 MeetingApiController (`/api/meeting`)

| Method | Endpoint                    | Auth      | Request Body                     | Response                                                         |
|--------|-----------------------------|-----------|----------------------------------|------------------------------------------------------------------|
| GET    | `/api/meeting/active`       | Any auth  | —                                | `200 Meeting` or `204 No Content`                                |
| POST   | `/api/meeting/start`        | ADMIN     | `{"title": "..."}` (optional)    | `200 {message, meetingId, title, roomName, startTime, redirect}` |
| POST   | `/api/meeting/stop`         | ADMIN     | —                                | `200 {message}` or `400 {error}`                                 |
| POST   | `/api/meeting/toggle-recording` | ADMIN | —                                | `200 Meeting` or `400 {error}`                                   |
| POST   | `/api/meeting/join`         | Any auth  | — (uses Authentication)          | `200 {message, meetingId, roomName, username}` or `400 {error}`  |
| POST   | `/api/meeting/leave`        | Any auth  | — (uses Authentication)          | `200 {message}` or `400 {error}`                                 |
| POST   | `/api/meeting/notify-students` | ADMIN  | `{meetingId, title}`             | `200 {message, meetingId}`                                       |

#### 5.1.2 StudentApiController (`/api/students`)

| Method | Endpoint                    | Auth      | Request Body        | Response                        |
|--------|-----------------------------|-----------|---------------------|---------------------------------|
| GET    | `/api/students`             | Any auth  | —                   | `200 List<Student>`             |
| GET    | `/api/students/{id}`        | Any auth  | —                   | `200 Student` or `404`          |
| POST   | `/api/students`             | ADMIN     | `Student JSON`      | `200 Student` or `400 {error}`  |
| PUT    | `/api/students/{id}`        | ADMIN     | `Student JSON`      | `200 Student` or `400 {error}`  |
| DELETE | `/api/students/{id}`        | ADMIN     | —                   | `204 No Content`                |
| POST   | `/api/students/{id}/block`  | ADMIN     | —                   | `200 Student`                   |
| POST   | `/api/students/{id}/mute`   | ADMIN     | —                   | `200 Student`                   |

#### 5.1.3 ChatApiController (`/api/chat`)

| Method | Endpoint              | Auth      | Request Body                      | Response                   |
|--------|-----------------------|-----------|-----------------------------------|----------------------------|
| GET    | `/api/chat/messages`  | Any auth  | —                                 | `200 List<ChatMessage>`    |
| POST   | `/api/chat/send`      | Any auth  | `{"content": "..."}` or `{"message": "..."}` | `200 ChatMessage` or `400` |
| DELETE | `/api/chat/clear`     | ADMIN     | —                                 | `200`                      |

#### 5.1.4 RecordingApiController (`/api/recordings`)

| Method | Endpoint                       | Auth      | Request Body                          | Response                 |
|--------|--------------------------------|-----------|---------------------------------------|--------------------------|
| GET    | `/api/recordings`              | Any auth  | —                                     | `200 List<Recording>`    |
| GET    | `/api/recordings/student/{id}` | Any auth  | —                                     | `200 List<Recording>`    |
| POST   | `/api/recordings`              | ADMIN     | `{studentId, duration, meetingId}`    | `200 Recording`          |
| DELETE | `/api/recordings/{id}`         | ADMIN     | —                                     | `204 No Content`         |
| DELETE | `/api/recordings/clear`        | ADMIN     | —                                     | `200`                    |

#### 5.1.5 SettingsApiController (`/api`)

| Method | Endpoint                            | Auth        | Request Body              | Response                                |
|--------|-------------------------------------|-------------|---------------------------|-----------------------------------------|
| POST   | `/api/settings/change-password`     | Authenticated | `{"newPassword": "..."}`| `200 {message}` or `400 {error}`        |
| GET    | `/api/server-info`                  | Any auth    | —                         | `200 {appUrl}`                          |
| GET    | `/api/students/{id}/whatsapp-link`  | Any auth    | —                         | `200 {whatsappUrl, appUrl, message}`    |

### 5.2 View Controller Routes (DashboardController)

| Method | URL              | View Template        | Model Attributes                                          |
|--------|------------------|----------------------|-----------------------------------------------------------|
| GET    | `/`              | `dashboard.html`     | activeMeeting, meetingActive, totalStudents, onlineStudents, inMeetingCount, meetings |
| GET    | `/students`      | `students.html`      | students, searchKeyword + common attrs                    |
| GET    | `/chat`          | `chat.html`          | messages + common attrs                                   |
| GET    | `/create-student`| `create-student.html`| common attrs                                              |
| GET    | `/recordings`    | `recordings.html`    | students, recsByStudent, allRecordings + common attrs     |
| GET    | `/docs`          | `documentation.html` | —                                                         |
| GET    | `/userguide`     | `userguide.html`     | —                                                         |
| GET    | `/access-denied` | `access-denied.html` | common attrs                                              |
| GET    | `/meeting-room`  | `meeting-room.html`  | meeting, hasActiveMeeting, roomName, currentUser + common |

---

## 6. Configuration Class Designs

### 6.1 SecurityConfig

```
@Configuration @EnableWebSecurity @EnableMethodSecurity
class SecurityConfig {

    Beans:
    - PasswordEncoder → BCryptPasswordEncoder
    - DaoAuthenticationProvider → MtngUserDetailsService + BCrypt
    - AccessDeniedHandler → JSON for API, redirect for pages
    - CustomAuthenticationSuccessHandler
    - CustomLogoutSuccessHandler
    - SecurityFilterChain → URL rules, form login, logout, CSRF, headers
}
```

**Security Filter Chain Configuration:**
1. **Public**: `/login`, `/css/**`, `/js/**`
2. **ADMIN only**: `/create-student`, `/h2-console/**`
3. **ADMIN only APIs**: `/api/meeting/start|stop`, `/api/students/*/block|mute`, `/api/recordings/clear`
4. **Authenticated**: Everything else
5. **Form login**: Page `/login`, success handler → `/`, failure → `/login?error`
6. **Logout**: URL `/logout`, invalidate session, delete cookies
7. **CSRF**: Disabled for `/api/**` and H2 console
8. **Headers**: Frame options same-origin (for H2 console iframe)

### 6.2 DataInitializer

```
@Component implements CommandLineRunner
class DataInitializer {

    run():
    1. If no teachers exist:
       - Create admin (admin/admin123, ROLE_ADMIN)
       - Create user (user/user123, ROLE_USER)
    2. If no students exist:
       - Create Hari (HARI34/pass1234)
       - Create Priya (PRIYA01/pass1234)
       - Create Ram (RAM22/pass1234)
       - Create 4 sample recordings for Hari
}
```

---

## 7. Exception Handling Design

### 7.1 GlobalExceptionHandler

```
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    handleAllExceptions(ex, request):

    IF request is API (URI starts with /api/ OR Accept contains application/json):
        → Return ResponseEntity<Map> with:
          {status: 500, error: "Internal Server Error", message, path}

    ELSE (web page request):
        → Build cause chain string
        → Return ModelAndView("error-page") with:
          errorMessage, errorType, requestUri
}
```

### 7.2 Service-Level Exceptions

| Exception              | Thrown By                | Trigger                         | HTTP Code |
|------------------------|-------------------------|---------------------------------|-----------|
| IllegalArgumentException | StudentService.createStudent | Duplicate username            | 400       |
| IllegalArgumentException | StudentService.updateStudent | Student not found             | 400       |
| IllegalArgumentException | StudentService.toggleBlock   | Student not found             | 400       |
| IllegalStateException  | MeetingService.toggleFullRecording | No active meeting         | 400       |
| UsernameNotFoundException | MtngUserDetailsService  | User not in either table       | 401       |

---

## 8. Sequence Diagrams

### 8.1 Login Sequence

```
Browser          LoginController     SecurityFilter     UserDetailsService    StudentService
   │                  │                    │                    │                    │
   │──GET /login─────►│                    │                    │                    │
   │◄─login.html──────│                    │                    │                    │
   │                   │                    │                    │                    │
   │──POST /login─────────────────────────►│                    │                    │
   │                   │                    │──loadUserByUsername►│                    │
   │                   │                    │                    │──check Teacher DB──►│
   │                   │                    │                    │◄──result────────────│
   │                   │                    │                    │──check Student DB──►│
   │                   │                    │                    │◄──result────────────│
   │                   │                    │◄──UserDetails──────│                    │
   │                   │                    │                    │                    │
   │                   │                    │──SuccessHandler────────────────────────►│
   │                   │                    │                    │       markOnline()  │
   │◄──302 Redirect /──│                    │                    │                    │
```

### 8.2 Start Meeting Sequence

```
Browser          MeetingApiController      MeetingService         MeetingRepository
   │                    │                       │                       │
   │──POST /api/meeting/start──►│               │                       │
   │  {title: "Class"}  │                       │                       │
   │                    │──startMeeting("Class")►│                       │
   │                    │                       │──findByActive(true)───►│
   │                    │                       │◄──active meetings──────│
   │                    │                       │──stop each active──────►│
   │                    │                       │                       │
   │                    │                       │──create new Meeting────►│
   │                    │                       │  (roomName=mtng-xxx)   │
   │                    │                       │◄──saved Meeting────────│
   │                    │◄──Meeting──────────────│                       │
   │◄──200 {meetingId, roomName, redirect}──────│                       │
```

### 8.3 Chat Message Sequence

```
Browser          ChatApiController       ChatService        ChatMessageRepository
   │                   │                     │                      │
   │──POST /api/chat/send──►│                │                      │
   │  {content: "Hello"}    │                │                      │
   │                   │──get auth.getName()  │                      │
   │                   │──getActiveMeeting()  │                      │
   │                   │──sendMessage()──────►│                      │
   │                   │                     │──save(ChatMessage)───►│
   │                   │                     │◄──saved───────────────│
   │                   │◄──ChatMessage────────│                      │
   │◄──200 ChatMessage──│                     │                      │
```

