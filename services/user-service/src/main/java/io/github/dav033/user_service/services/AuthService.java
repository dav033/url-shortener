package io.github.dav033.user_service.services;

import io.github.dav033.user_service.dto.LoginRequest;
import io.github.dav033.user_service.dto.RegisterRequest;
import io.github.dav033.user_service.dto.TokenResponse;
import io.github.dav033.user_service.models.Token;
import io.github.dav033.user_service.models.User;
import io.github.dav033.user_service.repositories.TokenRepository;
import io.github.dav033.user_service.repositories.UserRepository;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request) {
        final User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();


        final User savedUser = userRepository.save(user);

        final String jwtToken = jwtService.generateToken(savedUser);

        final String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveUserToken(savedUser, jwtToken);

        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email()).orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeUserTokens(user);
        saveUserToken(user, jwtToken);
        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        final User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException(userEmail));


        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        final String jwtToken = jwtService.generateToken(user);
        revokeUserTokens(user);
        saveUserToken(user, jwtToken);
        return new TokenResponse(jwtToken, refreshToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.FindAllValidTokensByUserId(user.getId());

        if (!validUserTokens.isEmpty()) {
            for (final Token token : validUserTokens) {
                token.setRevoked(true);
                token.setExpired(true);
                tokenRepository.save(token);
            }
            tokenRepository.saveAll(validUserTokens);
        }

    }

}
