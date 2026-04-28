package com.physics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {
    @GetMapping("/ping")
    public String ping() {
        return "I am awake!";
    }
}
