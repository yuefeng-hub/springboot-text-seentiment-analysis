package com.example.sentiment.service.impl;

import com.example.sentiment.engine.SentimentAnalyzer;
import com.example.sentiment.entity.SentimentRecord;
import com.example.sentiment.mapper.SentimentRecordMapper;
import com.example.sentiment.service.AnalysisService;
import com.example.sentiment.vo.SentimentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private SentimentAnalyzer sentimentAnalyzer;

    @Autowired
    private SentimentRecordMapper recordMapper;

    @Override
    public SentimentResult analyze(Long userId, String content) {
        SentimentResult result = sentimentAnalyzer.analyze(content);

        // 落库保存历史记录
        SentimentRecord record = new SentimentRecord();
        record.setUserId(userId);
        record.setContent(content);
        record.setSentimentType(result.getSentimentType());
        record.setScore(result.getScore());
        record.setConfidence(result.getConfidence());
        record.setKeywords(result.getKeywords() == null ? null : String.join(",", result.getKeywords()));
        record.setEngineType(result.getEngineType());
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        return result;
    }

    @Override
    public List<SentimentResult> batchAnalyze(List<String> contents) {
        List<SentimentResult> results = new ArrayList<>();
        for (String content : contents) {
            results.add(sentimentAnalyzer.analyze(content));
        }
        return results;
    }
}
