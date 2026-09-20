package com.example.sentiment.engine;

import com.example.sentiment.vo.SentimentResult;

/**
 * 情感分析引擎接口
 * 便于后续扩展：词典法、机器学习模型等可分别实现并在 Service 中切换
 */
public interface SentimentAnalyzer {

    SentimentResult analyze(String text);

    /** 引擎名称标识 */
    String engineName();
}
