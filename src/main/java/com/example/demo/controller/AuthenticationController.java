package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.UserService;
import com.example.demo.service.TelegramNotificationService;
import com.example.demo.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;

// Controller for handling authentication and session management
@Tag(name = "Authentication", description = "API для аутентификации и управления сессиями")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthServiceImpl authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TelegramNotificationService telegramNotificationService;

    // Authenticate user by login and password
    @Operation(summary = "Вход пользователя", description = "Аутентификация пользователя по логину и паролю. Возвращает JWT-токены.")
    @ApiResponse(responseCode = "200", description = "Успешная аутентификация")
    @ApiResponse(responseCode = "500", description = "Неверные учетные данные пользователя")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "access_token", required = false)
            String accessToken,
            @CookieValue(name = "refresh_token", required = false)
            String refreshToken,
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String ip = extractClientIp(request);
        try {
            ResponseEntity<LoginResponse> resp = authService.login(loginRequest, accessToken, refreshToken);
            telegramNotificationService.sendAuthNotification(loginRequest.username(), ip, "SUCCESS", LocalDateTime.now());
            return resp;
        } catch (Exception ex) {
            telegramNotificationService.sendAuthNotification(loginRequest.username(), ip, "FAILURE", LocalDateTime.now());
            throw ex;
        }
    }

    // Refresh access token using refresh token
    @Operation(summary = "Обновление токена", description = "Генерация нового access-токена по refresh-токену")
    @ApiResponse(responseCode = "200", description = "Токен успешно обновлен")
    @ApiResponse(responseCode = "400", description = "Недействительный refresh-токен (Refresh token is invalid)")
    @ApiResponse(responseCode = "404", description = "Не был получен токен")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.notFound().build();
        }
        return authService.refresh(refreshToken);
    }

    // Logout user and invalidate tokens
    @Operation(summary = "Выход из системы", description = "Инвалидация текущих JWT-токенов")
    @ApiResponse(responseCode = "200", description = "Сессия завершена")
    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletRequest request) {

        String ip = extractClientIp(request);
        String username = "-";
        try {
            UserLoggedDto dto = authService.getUserLoggedInfo();
            username = dto.username();
        } catch (Exception e) {
        }

        ResponseEntity<LoginResponse> resp = authService.logout(accessToken, refreshToken);
        telegramNotificationService.sendAuthNotification(username, ip, "LOGOUT", LocalDateTime.now());
        return resp;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Get current authenticated user info
    @Operation(summary = "Информация о пользователе", description = "Получение данных текущего аутентифицированного пользователя", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    @ApiResponse(responseCode = "401", description = "Требуется аутентификация")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> userLoggedInfo() {
        return ResponseEntity.ok(authService.getUserLoggedInfo());
    }

    // Change user password
    @Operation(summary = "Изменение пароля")
    @PutMapping("/change_password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

        if (!request.confirmPassword().equals(request.newPassword())) {
            return ResponseEntity.badRequest().build();
        }
        UserDto user = userService.getUser(authService.getUserLoggedInfo().username());
        if (passwordEncoder.matches(request.currentPassword(),
        user.password())) {
            userService.updateUser(user.id(),
                    new UserDto(user.id(), user.username(),
                            request.newPassword(), user.role(), user.permissions()));
            return ResponseEntity.ok("пароль успешно изменен");
        }
        return ResponseEntity.notFound().build();
    }
}
