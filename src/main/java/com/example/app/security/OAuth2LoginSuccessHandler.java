package com.example.app.security;

import com.example.app.dto.response.LoginResponse;
import com.example.app.entity.User;
import com.example.app.enums.Role;
import com.example.app.enums.UserStatus;
import com.example.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

/** Issues the same application JWT after a successful Google OAuth2 sign-in. */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        Boolean emailVerified = principal.getAttribute("email_verified");

        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            throw new OAuth2AuthenticationException("Google account must provide a verified email address");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> createUser(principal, normalizedEmail));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new OAuth2AuthenticationException("Account is inactive or locked");
        }

        user.setLastLogin(LocalDateTime.now());
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateAccessToken(user);
        LoginResponse loginResponse = new LoginResponse(
                token, user.getEmail(), user.getRole(), user.getPermissions());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), loginResponse);
    }

    private User createUser(OAuth2User principal, String email) {
        String fullName = principal.getAttribute("name");
        String givenName = principal.getAttribute("given_name");
        String familyName = principal.getAttribute("family_name");

        if ((givenName == null || givenName.isBlank()) && fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            givenName = parts[0];
            if ((familyName == null || familyName.isBlank()) && parts.length > 1) {
                familyName = parts[1];
            }
        }

        User user = new User();
        user.setFirstName(givenName == null || givenName.isBlank() ? "Google" : givenName.trim());
        user.setLastName(familyName == null ? "User" : familyName.trim());
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
