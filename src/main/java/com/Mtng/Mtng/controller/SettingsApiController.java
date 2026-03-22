package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.repository.TeacherRepository;
import com.Mtng.Mtng.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/**
 * SettingsApiController – REST API for settings actions.
 *
 * <ul>
 *   <li>POST /api/settings/change-password – change logged-in teacher password</li>
 *   <li>GET  /api/server-info              – returns the real LAN IP + port URL</li>
 *   <li>GET  /api/students/{id}/whatsapp-link – generate WhatsApp share link with real IP</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class SettingsApiController {

    private static final Logger log = LoggerFactory.getLogger(SettingsApiController.class);

    private final TeacherRepository teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;

    @Autowired
    public SettingsApiController(TeacherRepository teacherRepo,
                                  PasswordEncoder passwordEncoder,
                                  StudentService studentService) {
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
    }

    // ── Change Password ───────────────────────────────────────────────────────

    /**
     * POST /api/settings/change-password
     * Body: { "newPassword": "..." }
     * Changes the password of the currently logged-in teacher.
     */
    @PostMapping("/settings/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            Authentication auth) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 4 characters."));
        }
        String username = auth.getName();
        return teacherRepo.findByUsername(username)
                .map(teacher -> {
                    teacher.setPassword(passwordEncoder.encode(newPassword));
                    teacherRepo.save(teacher);
                    return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(Map.of("error", "Teacher not found.")));
    }

    // ── Server Info ───────────────────────────────────────────────────────────

    /**
     * GET /api/server-info
     * Returns the application's real URL using the machine's LAN IP address so
     * that links shared via WhatsApp (or any other channel) work on the network.
     * <p>Example response: {@code {"appUrl":"http://192.168.1.42:8080"}}</p>
     */
    @GetMapping("/server-info")
    public ResponseEntity<Map<String, String>> serverInfo(HttpServletRequest request) {
        String appUrl = resolveAppUrl(request);
        log.info("Server info requested – resolved app URL: {}", appUrl);
        return ResponseEntity.ok(Map.of("appUrl", appUrl));
    }

    // ── WhatsApp Link ─────────────────────────────────────────────────────────

    /**
     * GET /api/students/{id}/whatsapp-link
     * Builds a WhatsApp deep link containing the student's credentials and the
     * real LAN IP-based app URL (so recipients on the same network can connect).
     */
    @GetMapping("/students/{id}/whatsapp-link")
    public ResponseEntity<?> getWhatsAppLink(@PathVariable Long id,
                                              HttpServletRequest request,
                                              @RequestHeader(value = "X-App-Url", required = false) String headerUrl) {
        return studentService.findById(id)
                .map(student -> {
                    // Auto-detect real IP; only fall back to header URL if it's
                    // already a non-localhost address (e.g. a public/custom domain).
                    String appUrl = resolveAppUrl(request);
                    if (headerUrl != null && !headerUrl.isBlank()
                            && !headerUrl.contains("localhost")
                            && !headerUrl.contains("127.0.0.1")) {
                        appUrl = headerUrl;
                    }
                    String message    = buildWhatsAppMessage(student, appUrl);
                    String encoded    = URLEncoder.encode(message, StandardCharsets.UTF_8);
                    String whatsappUrl = "https://wa.me/?text=" + encoded;
                    return ResponseEntity.ok(Map.of(
                            "whatsappUrl", whatsappUrl,
                            "appUrl",      appUrl,
                            "message",     message
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the base app URL using the machine's real LAN IP address.
     * Falls back to the request's server name if IP detection fails.
     */
    private String resolveAppUrl(HttpServletRequest request) {
        String scheme = request.getScheme();           // "http" or "https"
        int    port   = request.getServerPort();

        String ip = detectLanIp();

        // If detection failed (returns "localhost"), fall back to request host
        if ("localhost".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = request.getServerName();
        }

        // Omit default port (80 for http, 443 for https)
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                           || ("https".equals(scheme) && port == 443);
        String portSuffix = defaultPort ? "" : ":" + port;

        return scheme + "://" + ip + portSuffix;
    }

    /**
     * Iterates the machine's network interfaces and returns the first
     * non-loopback, non-virtual IPv4 address (i.e., the real LAN IP such as
     * 192.168.x.x or 10.x.x.x).
     *
     * @return LAN IP string, or "localhost" if none found.
     */
    private String detectLanIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return "localhost";

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Skip loopback, down, and virtual interfaces
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                // Skip common virtual adapter name prefixes
                String name = iface.getName().toLowerCase();
                if (name.startsWith("docker") || name.startsWith("vmnet")
                        || name.startsWith("vbox") || name.startsWith("tun")
                        || name.startsWith("tap")) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Only IPv4, non-loopback
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        log.debug("Detected LAN IP {} on interface {}", addr.getHostAddress(), iface.getName());
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("Could not enumerate network interfaces: {}", e.getMessage());
        }
        return "localhost";
    }

    private String buildWhatsAppMessage(Student student, String appUrl) {
        // Use rawPassword (plain text) so recipients can actually log in
        String displayPassword = student.getRawPassword() != null
                ? student.getRawPassword()
                : "(see your credentials)";
        return "🎓 *MTNG – Meeting App*\n\n"
                + "Hi " + student.getName() + "!\n\n"
                + "Your account has been created:\n"
                + "👤 Username: *" + student.getUsername() + "*\n"
                + "🔑 Password: *" + displayPassword + "*\n\n"
                + "🔗 App URL: " + appUrl + "\n\n"
                + "Please login and join the meeting. 🙏";
    }
}

