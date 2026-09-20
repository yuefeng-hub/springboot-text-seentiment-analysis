package com.example.sentiment.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统计概览 VO
 */
@Data
public class StatsVO {

    /** 分析总条数 */
    private Long total;

    /** 正向率（%） */
    private Double positiveRate;

    /** 情感分布：key=sentimentType, value=count */
    private Map<String, Long> distribution;

    /** 近 7 日趋势：元素含 d(日期)、type、cnt */
    private List<Map<String, Object>> trend;
}
