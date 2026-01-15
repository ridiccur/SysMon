package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.UserLoggedDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.jwt.JwtTokenProviderImpl;
import com.example.demo.util.CookieUtil;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    JwtTokenProviderImpl tokenProvider;

    @Mock
    CookieUtil cookieUtil;

    @Mock
    org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @InjectMocks
    com.example.demo.service.impl.AuthServiceImpl authService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserLoggedInfo_whenAuthenticated_returnsDto() {
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", "x", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User();
        user.setUsername("alice");
        Role role = new Role(); role.setName("ROLE_USER"); role.setPermissions(java.util.Set.of());
        user.setRole(role);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserLoggedDto dto = authService.getUserLoggedInfo();
        assertThat(dto.username()).isEqualTo("alice");
    }

}
