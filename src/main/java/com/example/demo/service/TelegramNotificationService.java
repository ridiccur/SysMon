package com.example.demo.service;

import java.time.LocalDateTime;

public interface TelegramNotificationService {
    void sendAuthNotification(String username, String ip, String status, LocalDateTime time);
}
