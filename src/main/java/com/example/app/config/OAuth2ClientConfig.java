package com.example.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.oauth2.google.client-id:}") String clientId,
            @Value("${app.oauth2.google.client-secret:}") String clientSecret) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            // Keep local/test startup working when OAuth credentials have not been supplied.
            return registrationId -> null;
        }

        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        return new org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository(google);
    }
}
