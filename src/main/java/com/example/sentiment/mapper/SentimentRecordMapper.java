package com.example.sentiment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sentiment.entity.SentimentRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SentimentRecordMapper extends BaseMapper<SentimentRecord> {

    /**
     * 按情感类型统计数量（用于饼图）
     */
    @Select("SELECT sentiment_type AS type, COUNT(*) AS cnt FROM sentiment_record " +
            "WHERE user_id = #{userId} AND deleted = 0 GROUP BY sentiment_type")
    List<Map<String, Object>> countByType(@Param("userId") Long userId);

    /**
     * 按天统计某时间范围后的情感分布（用于趋势折线图）
     */
    @Select("SELECT DATE(create_time) AS d, sentiment_type AS type, COUNT(*) AS cnt " +
            "FROM sentiment_record " +
            "WHERE user_id = #{userId} AND create_time >= #{start} AND deleted = 0 " +
            "GROUP BY DATE(create_time), sentiment_type ORDER BY d")
    List<Map<String, Object>> trendSince(@Param("userId") Long userId, @Param("start") LocalDateTime start);
}
