package com.example.demo.service.impl;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.service.TelegramNotificationService;

@Service
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    private final RestTemplate rest = new RestTemplate();

    @Override
    public void sendAuthNotification(String username, String ip, String status, LocalDateTime time) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) return;

        String text = String.format("Попытка аутентификации\nUser: %s\nIP: %s\nStatus: %s\nTime: %s",
                username != null ? username : "-", ip != null ? ip : "-", status, time != null ? time.toString() : "-");

        URI uri = UriComponentsBuilder.fromUriString("https://api.telegram.org")
            .pathSegment("bot" + botToken, "sendMessage")
            .queryParam("chat_id", chatId)
            .queryParam("text", text)
            .build()
            .encode()
            .toUri();

        try {
            ResponseEntity<String> resp = rest.getForEntity(uri, String.class);

        } catch (Exception ex) {
            
        }
    }
}
