package com.Mtng.Mtng.config;

import com.Mtng.Mtng.service.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * CustomLogoutSuccessHandler – marks student as OFFLINE on logout.
 */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final StudentService studentService;

    public CustomLogoutSuccessHandler(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            String username = authentication.getName();
            // Try to mark as offline if it's a student
            try {
                studentService.markOffline(username);
            } catch (Exception e) {
                // Not a student, skip
            }
        }
        // Redirect to login
        response.sendRedirect("/login?logout=true");
    }
}

