package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthApiController – REST endpoints for SPA authentication.
 *
 * <ul>
 *   <li>POST /api/auth/login  – authenticate and create session</li>
 *   <li>GET  /api/auth/me     – return current user info</li>
 *   <li>POST /api/auth/logout – invalidate session</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private static final Logger log = LoggerFactory.getLogger(AuthApiController.class);

    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;

    @Autowired
    public AuthApiController(AuthenticationManager authenticationManager,
                              StudentService studentService) {
        this.authenticationManager = authenticationManager;
        this.studentService = studentService;
    }

    /**
     * POST /api/auth/login
     * Accepts JSON { "username": "...", "password": "..." }
     * Returns user info on success, 401 on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Create session and store security context
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);

            // Mark student as ONLINE (same as CustomAuthenticationSuccessHandler)
            try { studentService.markOnline(username); } catch (Exception ignored) {}

            log.info("[AUTH] Login success: {}", username);
            return ResponseEntity.ok(buildUserInfo(auth));

        } catch (DisabledException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Account is blocked. Contact admin."));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid username or password."));
        }
    }

    /**
     * GET /api/auth/me
     * Returns current authenticated user info, or 401 if not logged in.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(buildUserInfo(auth));
    }

    /**
     * POST /api/auth/logout
     * Invalidates session, marks student offline.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            try { studentService.markOffline(auth.getName()); } catch (Exception ignored) {}
            log.info("[AUTH] Logout: {}", auth.getName());
        }
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildUserInfo(Authentication auth) {
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String role = isAdmin ? "ADMIN" : "USER";
        String displayName = username;

        // Try to get student's display name
        if (!isAdmin) {
            displayName = studentService.findByUsername(username)
                    .map(s -> s.getName())
                    .orElse(username);
        }

        return Map.of(
                "authenticated", true,
                "username", username,
                "role", role,
                "displayName", displayName
        );
    }
}

