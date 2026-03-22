# 📝 Function/API Documentation

## MTNG – Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Document Version**| 1.0                                       |
| **Date**            | March 22, 2026                            |
| **Base URL**        | `http://{host}:8080`                      |

---

## 1. REST API Reference

### 1.1 Meeting APIs (`/api/meeting`)

---

#### `GET /api/meeting/active`
**Description:** Get the currently active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |
| **Content-Type** | application/json         |

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Mtng Session",
  "roomName": "mtng-a1b2c3d4",
  "active": true,
  "fullRecording": false,
  "inMeetingCount": 3,
  "onlineCount": 5,
  "startTime": "2026-03-22T10:00:00",
  "endTime": null
}
```

**Response (204 No Content):** No active meeting exists.

---

#### `POST /api/meeting/start`
**Description:** Start a new meeting. Automatically stops any existing active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |
| **Content-Type** | application/json         |

**Request Body (optional):**
```json
{
  "title": "Math Class"
}
```

**Response (200 OK):**
```json
{
  "message": "Meeting started successfully",
  "meetingId": 1,
  "title": "Math Class",
  "roomName": "mtng-a1b2c3d4",
  "startTime": "2026-03-22T10:00:00",
  "redirect": "/meeting-room",
  "notifyStudents": true
}
```

**Behavior:**
1. Stops all currently active meetings
2. Creates new meeting with UUID-based Jitsi room name
3. Returns meeting details for client-side redirect

---

#### `POST /api/meeting/stop`
**Description:** Stop the currently active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Response (200 OK):**
```json
{
  "message": "Meeting stopped"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "No active meeting to stop"
}
```

**Behavior:**
1. Sets meeting `active = false`, records `endTime`
2. If `fullRecording` was enabled, saves recordings for all participants
3. Clears in-memory participant tracking

---

#### `POST /api/meeting/toggle-recording`
**Description:** Toggle full-recording mode on the active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Response (200 OK):** Updated Meeting object with toggled `fullRecording` field.

**Response (400 Bad Request):**
```json
{
  "error": "No active meeting"
}
```

---

#### `POST /api/meeting/join`
**Description:** Join the active meeting as the authenticated user.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | POST                       |

**Response (200 OK):**
```json
{
  "message": "Joined meeting",
  "meetingId": 1,
  "roomName": "mtng-a1b2c3d4",
  "username": "HARI34"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "No active meeting to join"
}
```

**Behavior:**
1. Uses `Authentication.getName()` for username
2. Adds user to in-memory participant set
3. Updates `inMeetingCount` in database
4. Marks student as ONLINE

---

#### `POST /api/meeting/leave`
**Description:** Leave the active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | POST                       |

**Response (200 OK):**
```json
{
  "message": "Left meeting"
}
```

---

#### `POST /api/meeting/notify-students`
**Description:** Notify all students about meeting start (placeholder for push notification).

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Request Body:**
```json
{
  "meetingId": 1,
  "title": "Math Class"
}
```

**Response (200 OK):**
```json
{
  "message": "Students notified",
  "meetingId": 1
}
```

---

### 1.2 Student APIs (`/api/students`)

---

#### `GET /api/students`
**Description:** List all students.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Hari",
    "username": "HARI34",
    "password": "$2a$10$...",
    "rawPassword": "pass1234",
    "deviceLock": false,
    "showRecordings": true,
    "blocked": false,
    "muted": false,
    "status": "OFFLINE",
    "createdAt": "2026-03-22T09:00:00",
    "lastSeen": "2026-03-22T09:00:00"
  }
]
```

---

#### `GET /api/students/{id}`
**Description:** Get a single student by ID.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):** Student object.
**Response (404 Not Found):** Student doesn't exist.

---

#### `POST /api/students`
**Description:** Create a new student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Request Body:**
```json
{
  "name": "Ravi",
  "username": "RAVI99",
  "password": "pass1234"
}
```

**Response (200 OK):** Created Student object.

**Response (400 Bad Request):**
```json
{
  "error": "Username already exists: RAVI99"
}
```

**Behavior:**
1. Checks username uniqueness
2. Stores raw password in `rawPassword` field
3. BCrypt-encodes password before saving

---

#### `PUT /api/students/{id}`
**Description:** Update an existing student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | PUT                        |

**Request Body:**
```json
{
  "name": "Ravi Kumar",
  "deviceLock": true,
  "showRecordings": false,
  "password": "newpass123"
}
```

**Response (200 OK):** Updated Student object.

**Behavior:**
- Updates name, deviceLock, showRecordings
- If password provided and non-blank: re-encodes and updates

---

#### `DELETE /api/students/{id}`
**Description:** Delete a student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | DELETE                     |

**Response (204 No Content):** Student deleted.

---

#### `POST /api/students/{id}/block`
**Description:** Toggle block status on a student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Response (200 OK):** Student object with toggled `blocked` field.

---

#### `POST /api/students/{id}/mute`
**Description:** Toggle mute status on a student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Response (200 OK):** Student object with toggled `muted` field.

---

### 1.3 Chat APIs (`/api/chat`)

---

#### `GET /api/chat/messages`
**Description:** Get all chat messages for the active meeting (or all messages if no active meeting).

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "sender": "HARI34",
    "content": "Hello everyone!",
    "sentAt": "2026-03-22T10:05:00",
    "meetingId": 1
  }
]
```

---

#### `POST /api/chat/send`
**Description:** Send a chat message. Sender is automatically determined from the authenticated session.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | POST                       |

**Request Body:**
```json
{
  "content": "Hello everyone!"
}
```
*Also accepts `"message"` key for backward compatibility.*

**Response (200 OK):** Created ChatMessage object.
**Response (400 Bad Request):** Content is blank or missing.

---

#### `DELETE /api/chat/clear`
**Description:** Clear all chat messages for the active meeting.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | DELETE                     |

**Response (200 OK):** Messages cleared.

---

### 1.4 Recording APIs (`/api/recordings`)

---

#### `GET /api/recordings`
**Description:** Get all recordings.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "studentId": 1,
    "studentName": "Hari",
    "durationSeconds": 6,
    "recordingDate": "2026-03-21",
    "recordingTime": "20:41:00",
    "meetingId": 1
  }
]
```

---

#### `GET /api/recordings/student/{id}`
**Description:** Get recordings for a specific student.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):** List of recordings ordered by date desc, time desc.

---

#### `POST /api/recordings`
**Description:** Add a recording manually.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | POST                       |

**Request Body:**
```json
{
  "studentId": 1,
  "duration": 120,
  "meetingId": 1
}
```

**Response (200 OK):** Created Recording object.

---

#### `DELETE /api/recordings/{id}`
**Description:** Delete a single recording.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | DELETE                     |

**Response (204 No Content):** Recording deleted.

---

#### `DELETE /api/recordings/clear`
**Description:** Delete all recordings.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | ADMIN only                 |
| **Method**     | DELETE                     |

**Response (200 OK):** All recordings cleared.

---

### 1.5 Settings APIs (`/api`)

---

#### `POST /api/settings/change-password`
**Description:** Change the password of the currently logged-in teacher.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | POST                       |

**Request Body:**
```json
{
  "newPassword": "newSecure123"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully."
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Password must be at least 4 characters."
}
```

---

#### `GET /api/server-info`
**Description:** Get the application's real LAN URL for sharing.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |

**Response (200 OK):**
```json
{
  "appUrl": "http://192.168.1.42:8080"
}
```

---

#### `GET /api/students/{id}/whatsapp-link`
**Description:** Generate a WhatsApp deep link with the student's credentials.

| Property       | Value                      |
|----------------|----------------------------|
| **Auth**       | Any authenticated user     |
| **Method**     | GET                        |
| **Header**     | `X-App-Url` (optional override) |

**Response (200 OK):**
```json
{
  "whatsappUrl": "https://wa.me/?text=%F0%9F%8E%93...",
  "appUrl": "http://192.168.1.42:8080",
  "message": "🎓 *MTNG – Meeting App*\n\nHi Hari!..."
}
```

**WhatsApp Message Format:**
```
🎓 *MTNG – Meeting App*

Hi {name}!

Your account has been created:
👤 Username: *{username}*
🔑 Password: *{password}*

🔗 App URL: http://192.168.x.x:8080

Please login and join the meeting. 🙏
```

---

## 2. View Controller Functions

### 2.1 DashboardController

| Function                | Route              | Returns Template      | Description                              |
|-------------------------|--------------------|-----------------------|------------------------------------------|
| `dashboard(Model)`     | `GET /`            | `dashboard.html`      | Meeting controls page with active meeting info |
| `students(search, Model)` | `GET /students` | `students.html`       | Students list with optional search filter |
| `chat(Model)`          | `GET /chat`        | `chat.html`           | Chat page with messages                  |
| `createStudentPage(Model)` | `GET /create-student` | `create-student.html` | Student creation form (ADMIN only)   |
| `recordings(search, Model)` | `GET /recordings` | `recordings.html`  | Recordings grouped by student            |
| `documentation(Model)` | `GET /docs`        | `documentation.html`  | System documentation                     |
| `userGuide(Model)`     | `GET /userguide`   | `userguide.html`      | User guide                               |
| `accessDenied(Model)`  | `GET /access-denied` | `access-denied.html` | 403 error page                          |
| `meetingRoom(Model)`   | `GET /meeting-room`| `meeting-room.html`   | Jitsi meeting room with controls         |

**Common Model Attributes (via `addCommonAttributes`):**

| Attribute        | Type              | Description                          |
|------------------|-------------------|--------------------------------------|
| `activeMeeting`  | Meeting           | Current active meeting (if any)      |
| `meetingActive`  | boolean           | Whether a meeting is currently active|
| `totalStudents`  | long              | Total student count                  |
| `onlineStudents` | long              | Online student count                 |
| `inMeetingCount` | int               | Participants in current meeting      |

### 2.2 LoginController

| Function                       | Route              | Description                          |
|--------------------------------|--------------------|------------------------------------- |
| `loginPage(error, logout, Model)` | `GET /login`   | Renders login page with messages     |
| `logoutHandler(Authentication)` | `POST /logout-handler` | Marks student OFFLINE on logout |

---

## 3. Service Layer Functions

### 3.1 MeetingService

| Function                                    | Parameters                        | Returns            | Description                                |
|---------------------------------------------|-----------------------------------|--------------------|--------------------------------------------|
| `getActiveMeeting()`                        | —                                 | `Optional<Meeting>`| Returns first active meeting               |
| `startMeeting(title)`                       | `String title`                    | `Meeting`          | Stops active meetings, creates new one     |
| `stopMeeting()`                             | —                                 | `void`             | Stops active meeting, saves recordings     |
| `addStudentToMeeting(meetingId, username)`   | `Long, String`                   | `void`             | Tracks participant in memory + DB count    |
| `removeStudentFromMeeting(meetingId, username)` | `Long, String`               | `void`             | Removes participant tracking               |
| `toggleFullRecording()`                     | —                                 | `Meeting`          | Toggles recording flag on active meeting   |
| `updateCounters(id, inMeeting, online)`     | `Long, int, int`                 | `Meeting`          | Updates participant counts                 |
| `getAllMeetings()`                           | —                                 | `List<Meeting>`    | Returns all meetings                       |
| `findById(id)`                              | `Long`                            | `Optional<Meeting>`| Find meeting by ID                         |
| `getMeetingParticipants(meetingId)`          | `Long`                            | `Set<String>`      | Returns participant usernames              |

### 3.2 StudentService

| Function                         | Parameters                  | Returns            | Description                              |
|----------------------------------|-----------------------------|--------------------|------------------------------------------|
| `getAllStudents()`               | —                           | `List<Student>`    | All students                             |
| `findById(id)`                   | `Long`                      | `Optional<Student>`| Find by ID                               |
| `search(keyword)`               | `String`                    | `List<Student>`    | Search by name or username               |
| `createStudent(student)`         | `Student`                   | `Student`          | Create with BCrypt password              |
| `updateStudent(id, updated)`     | `Long, Student`             | `Student`          | Update name/settings/password            |
| `toggleBlock(id)`               | `Long`                      | `Student`          | Toggle blocked flag                      |
| `toggleMute(id)`                | `Long`                      | `Student`          | Toggle muted flag                        |
| `deleteStudent(id)`             | `Long`                      | `void`             | Delete student                           |
| `getTotalCount()`               | —                           | `long`             | Total student count                      |
| `getOnlineCount()`              | —                           | `long`             | Online student count                     |
| `markSeen(id)`                  | `Long`                      | `void`             | Update lastSeen timestamp                |
| `markOnline(username)`          | `String`                    | `void`             | Set status=ONLINE                        |
| `markOffline(username)`         | `String`                    | `void`             | Set status=OFFLINE                       |

### 3.3 ChatService

| Function                         | Parameters                  | Returns             | Description                             |
|----------------------------------|-----------------------------|--------------------|------------------------------------------|
| `sendMessage(sender, content, meetingId)` | `String, String, Long` | `ChatMessage`   | Create and save chat message             |
| `getMessages(meetingId)`         | `Long`                      | `List<ChatMessage>` | Messages for meeting (chronological)    |
| `clearMessages(meetingId)`       | `Long`                      | `void`             | Delete all messages for meeting          |
| `getAllMessages()`               | —                           | `List<ChatMessage>` | All messages                            |

### 3.4 RecordingService

| Function                         | Parameters                          | Returns          | Description                         |
|----------------------------------|-------------------------------------|------------------|-------------------------------------|
| `addRecording(studentId, name, duration, meetingId)` | `Long, String, int, Long` | `Recording` | Create recording entry         |
| `getStudentRecordings(studentId)` | `Long`                             | `List<Recording>` | Recordings for student (desc)      |
| `getAllRecordings()`             | —                                   | `List<Recording>` | All recordings                     |
| `deleteRecording(id)`           | `Long`                              | `void`           | Delete single recording             |
| `deleteStudentRecordings(studentId)` | `Long`                          | `void`           | Delete all recordings for student   |
| `clearAll()`                    | —                                   | `void`           | Delete all recordings               |

### 3.5 AuditService

| Function                         | Parameters                          | Returns           | Description                         |
|----------------------------------|-------------------------------------|-------------------|-------------------------------------|
| `logEvent(actor, action, details, ip)` | `String×4`                    | `void`            | Log audit event with IP             |
| `logEvent(actor, action, details)` | `String×3`                        | `void`            | Log audit event (no IP)             |
| `getRecentLogs()`               | —                                   | `List<AuditLog>`  | Last 100 entries                    |
| `getLogsByActor(actor)`         | `String`                            | `List<AuditLog>`  | Logs by actor                       |
| `getLogsByAction(action)`       | `String`                            | `List<AuditLog>`  | Logs by action type                 |
| `getLogsBetween(from, to)`      | `LocalDateTime×2`                   | `List<AuditLog>`  | Logs in date range                  |

### 3.6 MtngUserDetailsService

| Function                         | Parameters                  | Returns           | Description                              |
|----------------------------------|-----------------------------|--------------------|------------------------------------------|
| `loadUserByUsername(username)`   | `String`                    | `UserDetails`     | Checks Teacher then Student tables       |

---

## 4. Configuration Functions

### 4.1 SecurityConfig Beans

| Bean                              | Type                              | Description                            |
|-----------------------------------|-----------------------------------|----------------------------------------|
| `passwordEncoder()`              | `BCryptPasswordEncoder`           | Password hashing for all auth          |
| `authProvider()`                 | `DaoAuthenticationProvider`       | Links UserDetailsService + encoder     |
| `accessDeniedHandler()`         | `AccessDeniedHandler`             | JSON for API, redirect for pages       |
| `authSuccessHandler()`          | `CustomAuthenticationSuccessHandler` | Marks student ONLINE on login       |
| `logoutSuccessHandler()`        | `CustomLogoutSuccessHandler`      | Marks student OFFLINE on logout        |
| `filterChain(http)`             | `SecurityFilterChain`             | Full security configuration            |

### 4.2 DataInitializer

| Function    | Description                                                       |
|-------------|-------------------------------------------------------------------|
| `run(args)` | Seeds 2 teachers (admin, user), 3 students, 4 sample recordings   |

### 4.3 GlobalExceptionHandler

| Function                                        | Description                                |
|-------------------------------------------------|--------------------------------------------|
| `handleAllExceptions(ex, request)` | Routes to JSON (API) or error-page.html (web) |

