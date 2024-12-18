package io.github.dav033.user_service.config;


import io.github.dav033.user_service.filters.JwtAuthenticationFilter;
import io.github.dav033.user_service.models.Token;
import io.github.dav033.user_service.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final TokenRepository tokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Deshabilita CSRF
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("/auth/logout").addLogoutHandler((request, response, authentication) -> {
                            final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                            logout(authHeader);
                        }).logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                        })
                )
        ;
        return http.build();
    }

    private void logout(final String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }

        final String jwtToken = token.substring(7);

        final Token foundToken = tokenRepository.findByToken(jwtToken).orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        foundToken.setExpired(true);
        foundToken.setRevoked(true);
        tokenRepository.save(foundToken);

    }
}

