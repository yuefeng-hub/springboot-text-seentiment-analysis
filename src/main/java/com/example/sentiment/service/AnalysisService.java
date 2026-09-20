package com.example.sentiment.service;

import com.example.sentiment.vo.SentimentResult;

import java.util.List;

/**
 * 情感分析服务
 */
public interface AnalysisService {

    /** 单条分析并保存记录 */
    SentimentResult analyze(Long userId, String content);

    /** 批量分析（不落库，仅返回结果列表） */
    List<SentimentResult> batchAnalyze(List<String> contents);
}
