package com.example.sentiment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.sentiment.mapper")
public class SentimentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentimentApplication.class, args);
        System.out.println("==============================================");
        System.out.println(" 情感分析系统启动成功，访问地址: http://localhost:8080");
        System.out.println(" 首次启动会自动创建管理员账号: admin / admin123");
        System.out.println("==============================================");
    }
}
