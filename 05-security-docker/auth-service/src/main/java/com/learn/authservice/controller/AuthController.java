package com.learn.authservice.controller;

import com.learn.authservice.dto.LoginRequest;
import com.learn.authservice.dto.TokenResponse;
import com.learn.authservice.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserDetailsService users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UserDetailsService users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        UserDetails user;
        try {
            user = users.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException e) {
            throw invalidCredentials();
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }
        return tokenService.createToken(user);
    }

    // Same message for unknown user and wrong password, so attackers cannot tell them apart
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
