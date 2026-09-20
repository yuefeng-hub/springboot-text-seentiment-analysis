package com.example.sentiment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 词典新增/修改请求
 */
@Data
public class DictDTO {

    private Long id;

    @NotBlank(message = "情感词不能为空")
    private String word;

    /** 1 正向 / -1 负向 */
    @NotNull(message = "极性不能为空")
    private Integer polarity;

    private Double weight;

    private String category;
}
