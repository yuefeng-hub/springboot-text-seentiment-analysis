package com.example.sentiment.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 大模型接口配置（默认 DeepSeek，OpenAI 兼容协议）
 */
@Data
@Component
public class AiConfig {
    /** DeepSeek API Key，前往 https://platform.deepseek.com 申请 */
    @Value("${ai.api-key:}")
    private String apiKey;
    /** 接口地址；若用其他 OpenAI 兼容服务可整体替换（如通义千问兼容模式） */
    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;
    /** 模型名：deepseek-chat（对话）/ deepseek-reasoner（深度推理） */
    @Value("${ai.model:deepseek-chat}")
    private String model;
}
