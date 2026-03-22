package com.Mtng.Mtng.config;

import com.Mtng.Mtng.service.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * CustomAuthenticationSuccessHandler – marks student as ONLINE on successful login.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final StudentService studentService;

    public CustomAuthenticationSuccessHandler(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        // Try to mark as online if it's a student
        try {
            studentService.markOnline(username);
        } catch (Exception e) {
            // Not a student, skip
        }
        // Redirect to home
        response.sendRedirect("/");
    }
}

