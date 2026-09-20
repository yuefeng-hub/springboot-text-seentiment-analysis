package com.example.sentiment.controller;

import com.example.sentiment.common.Result;
import com.example.sentiment.dto.AnalyzeDTO;
import com.example.sentiment.dto.BatchAnalyzeDTO;
import com.example.sentiment.service.AnalysisService;
import com.example.sentiment.vo.SentimentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 情感分析接口
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /** 单条文本情感分析 */
    @PostMapping("/analyze")
    public Result<SentimentResult> analyze(@RequestAttribute("userId") Long userId,
                                           @Valid @RequestBody AnalyzeDTO dto) {
        return Result.ok(analysisService.analyze(userId, dto.getContent()));
    }

    /** 批量文本情感分析 */
    @PostMapping("/batch")
    public Result<List<SentimentResult>> batch(@Valid @RequestBody BatchAnalyzeDTO dto) {
        return Result.ok(analysisService.batchAnalyze(dto.getContents()));
    }
}
