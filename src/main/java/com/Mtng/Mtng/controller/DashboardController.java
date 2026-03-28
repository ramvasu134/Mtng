package com.Mtng.Mtng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * DashboardController – forwards all UI routes to the React SPA (index.html).
 *
 * <p>The React SPA uses BrowserRouter and handles all client-side routing.
 * This controller ensures that direct URL navigation and page refresh
 * serve the SPA entry point so React Router can take over.</p>
 */
@Controller
public class DashboardController {

    /**
     * Forward all known UI routes to the React SPA.
     * The React SPA's index.html is in /static/index.html.
     */
    @GetMapping({
        "/",
        "/login",
        "/students",
        "/chat",
        "/recordings",
        "/create-student",
        "/docs",
        "/userguide",
        "/access-denied",
        "/meeting-room",
        "/meeting-room/{roomName}"
    })
    public String forwardToReactSpa() {
        return "forward:/index.html";
    }
}
