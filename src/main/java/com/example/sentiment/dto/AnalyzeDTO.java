package com.example.sentiment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 单条情感分析请求
 */
@Data
public class AnalyzeDTO {

    @NotBlank(message = "待分析文本不能为空")
    @Size(max = 2000, message = "单条文本不能超过 2000 字")
    private String content;
}
