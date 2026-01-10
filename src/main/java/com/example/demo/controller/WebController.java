package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class WebController {

    @GetMapping("/")
    public String redirectToDashboard() {
        log.info("Перенаправление на главную страницу");
        return "redirect:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        log.info("Открытие панели управления");
        return "forward:/index.html";
    }
}