package com.example.sentiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sentiment.dto.LoginDTO;
import com.example.sentiment.dto.RegisterDTO;
import com.example.sentiment.entity.SysUser;
import com.example.sentiment.mapper.UserMapper;
import com.example.sentiment.service.UserService;
import com.example.sentiment.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> register(RegisterDTO dto) {
        Long exist = userMapper.selectCount(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        if (exist != null && exist > 0) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.createToken(user.getId(), user.getUsername()));
        result.put("user", toUserMap(user));
        return result;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.createToken(user.getId(), user.getUsername()));
        result.put("user", toUserMap(user));
        return result;
    }

    private Map<String, Object> toUserMap(SysUser user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("role", user.getRole());
        map.put("createTime", user.getCreateTime());
        return map;
    }
}
