package com.example.sentiment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 批量情感分析请求
 */
@Data
public class BatchAnalyzeDTO {

    @NotNull(message = "文本列表不能为空")
    private List<@NotBlank @Size(max = 2000, message = "单条文本不能超过 2000 字") String> contents;
}
