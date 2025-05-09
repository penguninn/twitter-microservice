package com.david.api_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping("/user-fallback")
    public String userFallback() {
        return "User service is not available";
    }

    @RequestMapping(value = "/auth-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String authFallback() {
        return "Auth service is not available";
    }
}
