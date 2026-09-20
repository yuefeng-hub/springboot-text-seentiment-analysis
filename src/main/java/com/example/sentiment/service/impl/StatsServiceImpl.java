package com.example.sentiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sentiment.entity.SentimentRecord;
import com.example.sentiment.mapper.SentimentRecordMapper;
import com.example.sentiment.service.StatsService;
import com.example.sentiment.vo.StatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private SentimentRecordMapper recordMapper;

    @Override
    public StatsVO overview(Long userId) {
        StatsVO vo = new StatsVO();

        // 总量
        Long total = recordMapper.selectCount(
                new QueryWrapper<SentimentRecord>().eq("user_id", userId));
        vo.setTotal(total == null ? 0L : total);

        // 分布
        Map<String, Long> distribution = new HashMap<>();
        long positive = 0;
        for (Map<String, Object> row : recordMapper.countByType(userId)) {
            String type = String.valueOf(row.get("type"));
            long cnt = ((Number) row.get("cnt")).longValue();
            distribution.put(type, cnt);
            if ("1".equals(type)) {
                positive = cnt;
            }
        }
        vo.setDistribution(distribution);

        // 正向率
        double rate = total == null || total == 0 ? 0.0 : (double) positive / total * 100;
        vo.setPositiveRate(Math.round(rate * 100.0) / 100.0);

        // 近 7 日趋势
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        vo.setTrend(recordMapper.trendSince(userId, start));

        return vo;
    }
}
