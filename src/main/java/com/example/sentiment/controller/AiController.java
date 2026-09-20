package com.example.sentiment.controller;

import com.example.sentiment.common.Result;
import com.example.sentiment.dto.AiChatRequest;
import com.example.sentiment.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 问答接口
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    /** AI 对话：支持多轮（history 传入历史对话） */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@Valid @RequestBody AiChatRequest request) {
        String reply = aiService.chat(request);
        Map<String, String> data = new HashMap<>();
        data.put("reply", reply);
        return Result.ok(data);
    }
}
