package com.petadoption.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        response.put("username", authentication.getName());
        response.put("roles", authentication.getAuthorities());

        return response;
    }
}