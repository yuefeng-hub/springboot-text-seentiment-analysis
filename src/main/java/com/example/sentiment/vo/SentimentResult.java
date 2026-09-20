package com.example.sentiment.vo;

import lombok.Data;

import java.util.List;

/**
 * 情感分析结果 VO（同时作为引擎输出对象）
 */
@Data
public class SentimentResult {

    private String text;

    /** 1 正向 / 0 中性 / -1 负向 */
    private Integer sentimentType;

    /** 情感标签：正向 / 中性 / 负向 */
    private String sentimentLabel;

    /** 情感得分（>0.5 正向，<-0.5 负向） */
    private Double score;

    /** 置信度 0~1（启发式） */
    private Double confidence;

    /** 命中的情感关键词 */
    private List<String> keywords;

    /** 分析引擎标识 */
    private String engineType;
}
