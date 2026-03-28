package com.Mtng.Mtng.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * GlobalExceptionHandler – catches all unhandled exceptions from controllers
 * and returns a readable error page or JSON, logging the full stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle missing static resources (e.g. /favicon.ico) silently.
     * Logs at DEBUG only – no scary ERROR stack trace in the log.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex,
                                                 jakarta.servlet.http.HttpServletRequest request) {
        log.debug("Static resource not found: {}", request.getRequestURI());
        return ResponseEntity.notFound().build();
    }

    /** Catch-all for view-rendering errors – returns simple HTML with the message */
    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(Exception ex,
                                      jakarta.servlet.http.HttpServletRequest request) {
        // Also check the original Servlet exception stored in request attributes
        Object origException = request.getAttribute("jakarta.servlet.error.exception");
        Exception rootCause = (origException instanceof Exception) ? (Exception) origException : ex;

        log.error("Exception on [{}]: {} → root: {}",
                  request.getRequestURI(), ex.getMessage(),
                  rootCause.getMessage(), rootCause);

        String uri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        if (uri.startsWith("/api/") || (acceptHeader != null && acceptHeader.contains("application/json"))) {
            java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("status", 500);
            body.put("error", "Internal Server Error");
            body.put("message", rootCause.getMessage());
            body.put("path", uri);
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Build a useful cause chain for display
        StringBuilder msgChain = new StringBuilder();
        Throwable t = rootCause;
        while (t != null) {
            msgChain.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
            t = t.getCause();
        }

        ModelAndView mav = new ModelAndView("error-page");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("errorMessage", msgChain.toString());
        mav.addObject("errorType", rootCause.getClass().getSimpleName());
        mav.addObject("requestUri", uri);
        return mav;
    }
}

