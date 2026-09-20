package com.example.sentiment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sentiment.entity.SentimentRecord;

/**
 * 分析记录服务
 */
public interface RecordService {

    Page<SentimentRecord> pageByUser(Long userId, long page, long size);

    boolean delete(Long userId, Long id);
}
