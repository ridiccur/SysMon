package com.example.demo.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTokenProviderImplTest {

    @Test
    void generateAndValidateAccessToken() throws Exception {
        JwtTokenProviderImpl provider = new JwtTokenProviderImpl();

        // set a 32-byte secret base64 encoded
        String raw = "01234567890123456789012345678901"; // 32 bytes
        String encoded = java.util.Base64.getEncoder().encodeToString(raw.getBytes());

        Field secretField = JwtTokenProviderImpl.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(provider, encoded);

        UserDetails user = User.withUsername("bob").password("x").roles("USER").build();

        var token = provider.generateAccessToken(Map.of(), 1, ChronoUnit.HOURS, user);
        assertThat(token).isNotNull();
        assertThat(token.getValue()).isNotBlank();

        boolean valid = provider.validateToken(token.getValue());
        assertThat(valid).isTrue();

        String username = provider.getUsernameFromToken(token.getValue());
        assertThat(username).isEqualTo("bob");
    }
}
