package com.example.sentiment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 情感分析记录实体
 */
@Data
@TableName("sentiment_record")
public class SentimentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 待分析文本 */
    private String content;

    /** 情感结果：1 正向 / 0 中性 / -1 负向 */
    private Integer sentimentType;

    /** 情感得分 */
    private Double score;

    /** 置信度 0~1 */
    private Double confidence;

    /** 命中的情感关键词（逗号分隔） */
    private String keywords;

    /** 分析引擎标识 */
    private String engineType;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
