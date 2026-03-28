package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * LoginController – handles logout-handler POST.
 *
 * <p>The React SPA handles the login page rendering.
 * Spring Security handles POST /login automatically via the filter chain.
 * The GET /login route is forwarded to the React SPA by DashboardController.</p>
 */
@Controller
public class LoginController {

    private final StudentService studentService;

    @Autowired
    public LoginController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** POST /logout-handler – mark student as OFFLINE when logging out */
    @PostMapping("/logout-handler")
    public String logoutHandler(Authentication auth) {
        if (auth != null) {
            String username = auth.getName();
            studentService.markOffline(username);
        }
        return "redirect:/login?logout=true";
    }
}
