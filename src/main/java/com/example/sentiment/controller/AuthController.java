package com.example.sentiment.controller;

import com.example.sentiment.common.Result;
import com.example.sentiment.dto.LoginDTO;
import com.example.sentiment.dto.RegisterDTO;
import com.example.sentiment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * 认证接口：注册 / 登录
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }
}
