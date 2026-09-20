package com.example.sentiment.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sentiment.entity.SysUser;
import com.example.sentiment.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化数据：首次启动自动创建管理员账号 admin/admin123
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long count = userMapper.selectCount(new QueryWrapper<SysUser>().eq("username", "admin"));
        if (count != null && count > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("系统管理员");
        admin.setRole("ADMIN");
        userMapper.insert(admin);
        log.info("已创建默认管理员账号：admin / admin123");
    }
}
