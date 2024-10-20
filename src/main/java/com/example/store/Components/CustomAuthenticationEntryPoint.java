package com.example.store.Components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // Get the requested URL
        String requestURI = request.getRequestURI();

        // If the user tries to access a "/users/**" path, redirect them to the home page
        if (requestURI.startsWith("/users/")) {
            response.sendRedirect(request.getContextPath() + "/home/");
        } else {
            // For other access denied cases, you can handle it differently or send to a default error page
            response.sendRedirect(request.getContextPath() + "/auth/login");
        }




        //response.sendRedirect("/auth/login");
    }
}
