package com.example.sentiment.service;

import com.example.sentiment.vo.StatsVO;

/**
 * 统计分析服务
 */
public interface StatsService {

    StatsVO overview(Long userId);
}
