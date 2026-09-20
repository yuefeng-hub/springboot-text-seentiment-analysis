package com.example.sentiment.controller;

import com.example.sentiment.common.Result;
import com.example.sentiment.service.StatsService;
import com.example.sentiment.vo.StatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析接口
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /** 统计概览：总量 / 正向率 / 分布 / 近7日趋势 */
    @GetMapping("/overview")
    public Result<StatsVO> overview(@RequestAttribute("userId") Long userId) {
        return Result.ok(statsService.overview(userId));
    }
}
