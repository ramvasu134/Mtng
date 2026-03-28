package com.Mtng.Mtng.config;

import com.Mtng.Mtng.service.MtngUserDetailsService;
import com.Mtng.Mtng.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * SecurityConfig – Role-based security for Mtng MVP.
 *
 * <p>Roles:</p>
 * <ul>
 *   <li><b>ADMIN</b> – full access to all pages and APIs</li>
 *   <li><b>USER</b>  – can view dashboard stats, chat, students list (read-only), recordings (read-only)</li>
 * </ul>
 * <p>Seeded credentials: admin/admin123, user/user123</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controllers
public class SecurityConfig {

    @Autowired
    private MtngUserDetailsService userDetailsService;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider prov = new DaoAuthenticationProvider();
        prov.setUserDetailsService(userDetailsService);
        prov.setPasswordEncoder(passwordEncoder());
        return prov;
    }

    /** Expose AuthenticationManager for REST auth endpoint */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String accept = request.getHeader("Accept");
            String uri    = request.getRequestURI();

            // API requests → return 403 JSON (so frontend JS can handle it gracefully)
            if (uri.startsWith("/api/") ||
                    (accept != null && accept.contains("application/json"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    "{\"error\":\"Access Denied\"," +
                    "\"message\":\"You do not have permission to perform this action.\"}");
                return;
            }

            // Web page requests → REDIRECT (not forward) so the browser issues a GET
            response.sendRedirect(request.getContextPath() + "/access-denied");
        };
    }

    @Bean
    public CustomAuthenticationSuccessHandler authSuccessHandler(StudentService studentService) {
        return new CustomAuthenticationSuccessHandler(studentService);
    }

    @Bean
    public CustomLogoutSuccessHandler logoutSuccessHandler(StudentService studentService) {
        return new CustomLogoutSuccessHandler(studentService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAuthenticationSuccessHandler authSuccessHandler,
                                           CustomLogoutSuccessHandler logoutSuccessHandler) throws Exception {
        http
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                // Public resources (including React SPA assets)
                .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico", "/favicon.svg").permitAll()
                .requestMatchers("/index.html", "/assets/**", "/react-app.html").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                // WebSocket signaling endpoint (SockJS + STOMP)
                .requestMatchers("/ws/**", "/ws").permitAll()

                // ADMIN-only pages
                .requestMatchers("/create-student").hasRole("ADMIN")

                // H2 Console – must use PathRequest (plain string won't match the servlet)
                .requestMatchers(PathRequest.toH2Console()).hasRole("ADMIN")

                // ADMIN-only API endpoints (create, update, delete, block, mute)
                .requestMatchers("/api/students/*/block").hasRole("ADMIN")
                .requestMatchers("/api/students/*/mute").hasRole("ADMIN")
                .requestMatchers("/api/meeting/start").hasRole("ADMIN")
                .requestMatchers("/api/meeting/stop").hasRole("ADMIN")
                .requestMatchers("/api/meeting/stop-all").hasRole("ADMIN")
                .requestMatchers("/api/meeting/toggle-recording").hasRole("ADMIN")
                .requestMatchers("/api/recordings/clear").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/recordings/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/chat/clear").hasRole("ADMIN")
                .requestMatchers("/api/settings/change-password").authenticated()

                // Everything else: just be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .successHandler(authSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    // DisabledException → blocked student
                    if (exception instanceof org.springframework.security.authentication.DisabledException) {
                        response.sendRedirect("/login?disabled=true");
                    } else {
                        response.sendRedirect("/login?error=true");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(PathRequest.toH2Console())
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/ws/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler()))
            // Force HTTP → HTTPS redirect
            .requiresChannel(channel -> channel
                .anyRequest().requiresSecure());
        return http.build();
    }
}
