package com.example.sentiment.service;

import com.example.sentiment.dto.LoginDTO;
import com.example.sentiment.dto.RegisterDTO;

import java.util.Map;

/**
 * 用户服务：注册 / 登录
 */
public interface UserService {

    /** 注册，成功返回用户信息（不含密码） */
    Map<String, Object> register(RegisterDTO dto);

    /** 登录，成功返回 token + 用户信息 */
    Map<String, Object> login(LoginDTO dto);
}
