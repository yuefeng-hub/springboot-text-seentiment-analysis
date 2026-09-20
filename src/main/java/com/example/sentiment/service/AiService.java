package com.example.sentiment.service;

import com.example.sentiment.config.AiConfig;
import com.example.sentiment.dto.AiChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 问答服务：调用大模型接口（默认 DeepSeek，OpenAI 兼容协议）
 */
@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 系统提示词：给模型设定角色，让回答更贴合本系统场景 */
    private static final String SYSTEM_PROMPT =
            "你是在线文本情感分析系统的 AI 助手。你可以：1) 帮助用户理解情感分析的结果和原理；" +
            "2) 对任意中文文本进行情感倾向分析（正向/中性/负向）并简要说明理由；" +
            "3) 提供情绪调节和心理健康的一般性建议；" +
            "4) 回答情感分析、自然语言处理、SpringBoot 开发等相关技术问题；" +
            "5) 进行日常问答。请始终使用中文，回答简洁、友好、有条理。";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AiConfig aiConfig;

    /**
     * 发起一次对话，返回模型回复文本
     */
    public String chat(AiChatRequest request) {
        String key = aiConfig.getApiKey();
        if (!StringUtils.hasText(key) || "你的DeepSeek密钥".equals(key.trim())) {
            throw new RuntimeException("未配置大模型密钥：请在 application.yml 的 ai.api-key 中填入你的 DeepSeek API Key（前往 https://platform.deepseek.com 申请）");
        }

        // 1. 组装 messages：系统提示词 + 历史对话 + 当前问题
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", SYSTEM_PROMPT));
        if (request.getHistory() != null) {
            for (AiChatRequest.MessageItem item : request.getHistory()) {
                if (item != null && StringUtils.hasText(item.getRole()) && StringUtils.hasText(item.getContent())) {
                    messages.add(msg(item.getRole(), item.getContent()));
                }
            }
        }
        messages.add(msg("user", request.getMessage()));

        // 2. 组装请求体（OpenAI 兼容格式）
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getModel());
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        String url = aiConfig.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<?, ?> data = resp.getBody();
            if (data == null) {
                throw new RuntimeException("AI 服务返回为空");
            }
            Object choices = data.get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<?>) choices).get(0);
                if (first instanceof Map) {
                    Object msg = ((Map<?, ?>) first).get("message");
                    if (msg instanceof Map) {
                        Object content = ((Map<?, ?>) msg).get("content");
                        if (content != null) {
                            return content.toString();
                        }
                    }
                }
            }
            throw new RuntimeException("AI 响应解析失败，请检查接口返回格式");
        } catch (HttpClientErrorException e) {
            String detail = extractError(e.getResponseBodyAsString());
            log.warn("AI 接口调用失败: {} {}", e.getRawStatusCode(), detail);
            throw new RuntimeException("AI 接口调用失败(" + e.getRawStatusCode() + "): " + detail);
        } catch (ResourceAccessException e) {
            log.warn("连接 AI 服务失败", e);
            throw new RuntimeException("无法连接 AI 服务，请检查网络或 ai.base-url 配置");
        }
    }

    /** 构造一条消息（Java 8 兼容，不使用 Map.of） */
    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /** 从错误响应体中提取 message 字段，便于展示真实原因 */
    private String extractError(String body) {
        if (!StringUtils.hasText(body)) {
            return "未知错误";
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            String msg = node.path("error").path("message").asText("");
            if (StringUtils.hasText(msg)) {
                return msg;
            }
            return node.path("message").asText(body);
        } catch (Exception ex) {
            return body;
        }
    }
}
