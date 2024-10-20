package com.example.store.Controllers;

import com.example.store.Auth.AuthenticationRequest;
import com.example.store.Auth.AuthenticationResponse;
import com.example.store.Auth.RegisterRequest;
import com.example.store.Config.JwtService;
import com.example.store.Models.User;
import com.example.store.Repo.UserRepository;
import com.example.store.Services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register (
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

//    @PostMapping("/register")
//    public String register(@ModelAttribute User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
//        return "redirect:/";
//    }


    @PostMapping("/login" )
    public String login(@Valid @ModelAttribute("request") AuthenticationRequest authenticationRequest
            , BindingResult bindingResult
            , HttpServletResponse response
            , Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        final String jwt;

        try {

            jwt = authenticationService.login(authenticationRequest).getToken();

        } catch (Exception e) {
            throw new RuntimeException("Invalid login credentials");
        }

        // Set JWT token in cookie
        Cookie cookie = new Cookie("JWT_TOKEN", jwt);
        cookie.setHttpOnly(true);  // Ensures that the cookie is accessible only through the HTTP protocol
        cookie.setMaxAge(10 * 60 * 60); // 10 hours expiration
        cookie.setPath("/"); // Set path to root
        response.addCookie(cookie);

        model.addAttribute("successMessage", "login successfully ");

        return "redirect:/home/";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("request", new AuthenticationRequest());
        return "login";
    }


    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Remove the JWT token from the client by setting a cookie with maxAge = 0
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);  // Invalidate the cookie
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/auth/login";  // Redirect to login page after logout
    }
}
