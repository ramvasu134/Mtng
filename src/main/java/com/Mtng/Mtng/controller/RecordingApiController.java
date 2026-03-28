package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.service.RecordingService;
import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * RecordingApiController – REST API for recordings management.
 * Read: all authenticated. Write/Delete: ADMIN only.
 * save-session: any authenticated user (host or participant).
 */
@RestController
@RequestMapping("/api/recordings")
public class RecordingApiController {

    private final RecordingService recordingService;
    private final StudentService   studentService;

    @Autowired
    public RecordingApiController(RecordingService recordingService, StudentService studentService) {
        this.recordingService = recordingService;
        this.studentService   = studentService;
    }

    /**
     * GET /api/recordings – role-filtered recordings.
     * ADMIN sees all recordings.  USER sees only their own recordings.
     */
    @GetMapping
    public List<Recording> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return recordingService.getAllRecordings();
        }
        // Normal user – return only their recordings (matched by display name or username)
        String username = auth.getName();
        // Try matching by student display name first, fall back to username
        List<Recording> byName = recordingService.getRecordingsByParticipantName(username);
        if (byName.isEmpty()) {
            // Also try matching via the studentService (display name → username)
            return studentService.findByUsername(username)
                    .map(s -> recordingService.getRecordingsByParticipantName(s.getName()))
                    .orElse(byName);
        }
        return byName;
    }

    /** GET /api/recordings/student/{id} (anyone authenticated) */
    @GetMapping("/student/{id}")
    public List<Recording> byStudent(@PathVariable Long id) {
        return recordingService.getStudentRecordings(id);
    }

    /** POST /api/recordings – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Recording> add(@RequestBody Map<String, Object> body) {
        Long   studentId = Long.parseLong(body.get("studentId").toString());
        String name      = studentService.findById(studentId)
                .map(s -> s.getName()).orElse("Unknown");
        int    duration  = Integer.parseInt(body.getOrDefault("duration", "6").toString());
        Long   meetingId = body.containsKey("meetingId")
                ? Long.parseLong(body.get("meetingId").toString()) : 0L;
        return ResponseEntity.ok(recordingService.addRecording(studentId, name, duration, meetingId));
    }

    /**
     * POST /api/recordings/save-session
     * Saves recording session metadata after a MediaRecorder session ends.
     * Available to any authenticated user (host or participant).
     * The actual audio file is saved to the client's local disk separately.
     */
    @PostMapping("/save-session")
    public ResponseEntity<Recording> saveSession(@RequestBody Map<String, Object> body) {
        Long   meetingId      = body.containsKey("meetingId")
                ? Long.parseLong(body.get("meetingId").toString()) : null;
        String participantName = body.getOrDefault("participantName", "Unknown").toString();
        Long   studentId       = body.containsKey("studentId")
                ? Long.parseLong(body.get("studentId").toString()) : null;
        int    durationSeconds = Integer.parseInt(body.getOrDefault("durationSeconds", "0").toString());
        String fileName        = body.getOrDefault("fileName", "recording.webm").toString();
        Long   fileSize        = body.containsKey("fileSize")
                ? Long.parseLong(body.get("fileSize").toString()) : 0L;
        String participantType = body.getOrDefault("participantType", "PARTICIPANT").toString();
        Recording saved = recordingService.saveSession(
                meetingId, participantName, studentId,
                durationSeconds, fileName, fileSize, participantType);
        return ResponseEntity.ok(saved);
    }

    /**
     * POST /api/recordings/{id}/upload-audio
     * Uploads the actual audio file (multipart) and stores it in the DB BLOB column.
     * Any authenticated user can upload their own recording.
     */
    @PostMapping(value = "/{id}/upload-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAudio(@PathVariable Long id,
                                          @RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No audio file provided"));
            }
            String mime = audioFile.getContentType();
            if (mime == null || mime.isBlank()) mime = "audio/webm";
            recordingService.storeAudio(id, audioFile.getBytes(), mime);
            return ResponseEntity.ok(Map.of("message", "Audio stored", "id", id, "size", audioFile.getSize()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/recordings/{id}/audio
     * Streams the audio data back to the browser for playback or download.
     * Supports HTTP Range requests for seeking in the &lt;audio&gt; player.
     */
    @GetMapping("/{id}/audio")
    public ResponseEntity<byte[]> streamAudio(@PathVariable Long id,
                                               HttpServletRequest request) {
        return recordingService.findById(id)
                .filter(Recording::hasAudio)
                .map(r -> {
                    byte[] data = r.getAudioData();
                    long total  = data.length;
                    String mime = r.getAudioMimeType() != null ? r.getAudioMimeType() : "audio/webm";
                    String fn   = r.getFileName()      != null ? r.getFileName()      : "recording.webm";

                    String rangeHeader = request.getHeader(HttpHeaders.RANGE);
                    if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                        // Parse Range: bytes=start-end
                        String rangeSpec = rangeHeader.substring(6);
                        String[] parts   = rangeSpec.split("-", 2);
                        long start = Long.parseLong(parts[0]);
                        long end   = (parts.length > 1 && !parts[1].isEmpty())
                                ? Long.parseLong(parts[1])
                                : total - 1;
                        if (start >= total) {
                            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                                    .header("Content-Range", "bytes */" + total)
                                    .<byte[]>build();
                        }
                        if (end >= total) end = total - 1;
                        long contentLength = end - start + 1;
                        byte[] slice = Arrays.copyOfRange(data, (int) start, (int) (end + 1));

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(mime));
                        headers.set("Content-Disposition", "inline; filename=\"" + fn + "\"");
                        headers.set("Accept-Ranges", "bytes");
                        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + total);
                        headers.setContentLength(contentLength);
                        headers.setCacheControl("no-cache");
                        return new ResponseEntity<>(slice, headers, HttpStatus.PARTIAL_CONTENT);
                    }

                    // Full response (no Range header)
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(mime));
                    headers.set("Content-Disposition", "inline; filename=\"" + fn + "\"");
                    headers.set("Accept-Ranges", "bytes");
                    headers.setContentLength(total);
                    headers.setCacheControl("no-cache");
                    return new ResponseEntity<>(data, headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/recordings/{id} – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordingService.deleteRecording(id);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/recordings/clear – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAll() {
        recordingService.clearAll();
        return ResponseEntity.ok().build();
    }
}
