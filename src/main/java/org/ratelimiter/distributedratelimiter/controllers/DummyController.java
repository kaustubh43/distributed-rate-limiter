package org.ratelimiter.distributedratelimiter.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
    @GetMapping("/api/test")
    public String test(HttpServletRequest request) {
        return "Success";
    }
}
