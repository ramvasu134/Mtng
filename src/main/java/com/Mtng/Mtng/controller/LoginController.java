package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * LoginController – serves the login page and handles logout.
 *
 * <p>Spring Security handles POST /login automatically via the filter chain.</p>
 */
@Controller
public class LoginController {

    private final StudentService studentService;

    @Autowired
    public LoginController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** GET /login – render login page */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error  != null) model.addAttribute("errorMsg",  "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out successfully.");

        return "login";
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

