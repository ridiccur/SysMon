package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.UserService;
import com.example.demo.service.TelegramNotificationService;
import com.example.demo.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthServiceImpl authService;

    @Mock
    UserService userService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    TelegramNotificationService telegramNotificationService;

    @InjectMocks
    AuthenticationController controller;

    @Test
    void login_success_sendsNotification() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "1.2.3.4");

        LoginRequest lr = new LoginRequest("bob", "pwd");

        when(authService.login(lr, null, null)).thenReturn(ResponseEntity.ok(new LoginResponse(true, "ROLE_USER")));

        ResponseEntity<?> resp = controller.login(null, null, lr, req);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void refreshToken_missing_returnsNotFound() {
        ResponseEntity<?> resp = controller.refreshToken(null);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void changePassword_mismatch_returnsBadRequest() {
        var req = new com.example.demo.dto.ChangePasswordRequest("a", "b", "c");
        var resp = controller.changePassword(req);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void userLoggedInfo_delegatesToAuthService() {
        UserLoggedDto dto = new UserLoggedDto("bob", "USER", java.util.Set.of());
        when(authService.getUserLoggedInfo()).thenReturn(dto);
        var resp = controller.userLoggedInfo();
        assertThat(resp.getBody()).isEqualTo(dto);
    }
}
