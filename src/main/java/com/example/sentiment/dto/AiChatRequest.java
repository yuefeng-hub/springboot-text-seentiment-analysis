package com.example.sentiment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * AI 问答请求
 */
@Data
public class AiChatRequest {
    /** 用户当前输入 */
    @NotBlank(message = "请输入问题内容")
    private String message;
    /** 可选历史对话记录：[{role: user/assistant, content: 文本}] */
    private List<MessageItem> history;

    @Data
    public static class MessageItem {
        private String role;
        private String content;
    }
}
