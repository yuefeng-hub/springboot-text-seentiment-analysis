package com.example.sentiment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 情感词典实体（管理员自定义词）
 */
@Data
@TableName("sentiment_dict")
public class SentimentDict {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    /** 极性：1 正向 / -1 负向 */
    private Integer polarity;

    /** 情感强度权重 */
    private Double weight;

    private String category;

    /** 1 用户自定义 / 0 内置 */
    private Integer isCustom;

    private LocalDateTime createTime;
}
