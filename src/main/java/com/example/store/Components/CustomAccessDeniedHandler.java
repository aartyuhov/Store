package com.example.store.Components;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Get the requested URL
        String requestURI = request.getRequestURI();

        // If the user tries to access a "/users/**" path, redirect them to the home page
        if (requestURI.startsWith("/users")) {
            response.sendRedirect(request.getContextPath() + "/home/");
        } else {
            // For other access denied cases, you can handle it differently or send to a default error page
            response.sendRedirect(request.getContextPath() + "/auth/login");
        }
    }
}
