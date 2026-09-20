package com.example.sentiment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * 通用 Bean 配置
 */
@Configuration
public class BeanConfig {

    /**
     * 密码加密器（BCrypt），仅引入 spring-security-crypto，不引入完整 Security 过滤链
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 客户端（供 AI 问答调用大模型接口使用）
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        return new RestTemplate(factory);
    }
}
