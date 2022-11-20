package com.wildmeet.userservice.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Value("${server.port}")
    String port;

    @GetMapping("/user/info")
    public String info() {
        return "User 서비스의 기본 동작 Port: {" + port + "}";
    }
}
