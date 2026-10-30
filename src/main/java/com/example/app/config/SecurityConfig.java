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

                // Content upload restricted ONLY to CONTENT_ADMIN
                .requestMatchers(
                    HttpMethod.POST, "/api/contents", "/api/contents/**"
                ).hasRole("CONTENT_ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/contents/**"
                ).hasRole("CONTENT_ADMIN")

                // Content library viewing and metrics
                .requestMatchers(
                    "/api/contents", "/api/contents/**"
                ).hasAnyRole("CENTRAL_ADMIN", "CONTENT_ADMIN")

                // Question Sets management and viewing
                .requestMatchers(
                    HttpMethod.POST, "/api/question-sets", "/api/question-sets/**"
                ).hasAnyRole("CONTENT_ADMIN", "CENTRAL_ADMIN")

                .requestMatchers(
                    HttpMethod.PUT, "/api/question-sets/**"
                ).hasAnyRole("CONTENT_ADMIN", "CENTRAL_ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/question-sets/**"
                ).hasAnyRole("CONTENT_ADMIN", "CENTRAL_ADMIN")

                .requestMatchers(
                    "/api/question-sets", "/api/question-sets/**"
                ).hasAnyRole("CENTRAL_ADMIN", "CONTENT_ADMIN")

                // Teacher management – SCHOOL_ADMIN only for write/export operations
                .requestMatchers(HttpMethod.POST,   "/api/teachers", "/api/teachers/**")
                    .hasRole("SCHOOL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/teachers/**")
                    .hasRole("SCHOOL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/teachers/**")
                    .hasRole("SCHOOL_ADMIN")
                .requestMatchers("/api/teachers/export", "/api/teachers/export/**")
                    .hasRole("SCHOOL_ADMIN")
                .requestMatchers("/api/teachers", "/api/teachers/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // Classroom Operator management – SCHOOL_ADMIN and CENTRAL_ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/classroom-operators", "/api/classroom-operators/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/classroom-operators/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/classroom-operators/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/classroom-operators/export", "/api/classroom-operators/export/**", "/api/classroom-operators/template")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/classroom-operators", "/api/classroom-operators/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // Student management – SCHOOL_ADMIN and CENTRAL_ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/students", "/api/students/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/students/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/students/export", "/api/students/export/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/students", "/api/students/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // Classes management – SCHOOL_ADMIN and CENTRAL_ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/classes", "/api/classes/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/classes/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/classes/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/classes/export", "/api/classes/export/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/classes", "/api/classes/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // Sections management – SCHOOL_ADMIN and CENTRAL_ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/sections", "/api/sections/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/sections/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/sections/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/sections/export", "/api/sections/export/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/sections", "/api/sections/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // Subjects management – SCHOOL_ADMIN and CENTRAL_ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/subjects", "/api/subjects/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/subjects/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/subjects/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/subjects/export", "/api/subjects/export/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")
                .requestMatchers("/api/subjects", "/api/subjects/**")
                    .hasAnyRole("SCHOOL_ADMIN", "CENTRAL_ADMIN")

                // All remaining endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
