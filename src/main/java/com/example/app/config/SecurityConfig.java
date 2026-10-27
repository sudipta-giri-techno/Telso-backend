package com.example.app.config;

import com.example.app.security.CustomAccessDeniedHandler;
import com.example.app.security.JwtAuthenticationEntryPoint;
import com.example.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
           
            .authorizeHttpRequests(auth -> auth
                // Swagger UI and OpenAPI documentation
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Public endpoints
                .requestMatchers("/api/ping", "/api/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // Central admin only
                .requestMatchers(
                    HttpMethod.POST, "/api/users", "/api/users/**"
                ).hasRole("CENTRAL_ADMIN")

                .requestMatchers(
                    HttpMethod.POST, "/api/schools", "/api/schools/**"
                ).hasRole("CENTRAL_ADMIN")

                .requestMatchers(
                    "/api/users/export", "/api/users/export/**"
                ).hasRole("CENTRAL_ADMIN")

                .requestMatchers(
                    "/api/schools/export", "/api/schools/export/**"
                ).hasRole("CENTRAL_ADMIN")

                // Admin user listing and related endpoints
                .requestMatchers("/api/users", "/api/users/**")
                .hasAnyRole("CENTRAL_ADMIN", "CONTENT_ADMIN")

                .requestMatchers("/api/schools/stats")
                .hasAnyRole("CENTRAL_ADMIN", "CONTENT_ADMIN")

                // All remaining endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
