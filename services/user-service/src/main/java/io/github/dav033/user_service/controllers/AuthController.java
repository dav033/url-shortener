package io.github.dav033.user_service.controllers;

import io.github.dav033.user_service.dto.LoginRequest;
import io.github.dav033.user_service.dto.RegisterRequest;
import io.github.dav033.user_service.dto.TokenResponse;
import io.github.dav033.user_service.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest request) {

        final TokenResponse response = authService.register(request);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {

        final TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {

        return authService.refreshToken(authHeader);
    }

}

