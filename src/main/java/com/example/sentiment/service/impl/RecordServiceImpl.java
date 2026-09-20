package com.example.sentiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sentiment.entity.SentimentRecord;
import com.example.sentiment.mapper.SentimentRecordMapper;
import com.example.sentiment.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private SentimentRecordMapper recordMapper;

    @Override
    public Page<SentimentRecord> pageByUser(Long userId, long page, long size) {
        QueryWrapper<SentimentRecord> wrapper = new QueryWrapper<SentimentRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time");
        return recordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean delete(Long userId, Long id) {
        SentimentRecord record = recordMapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            return false;
        }
        // 逻辑删除（配置了 @TableLogic）
        return recordMapper.deleteById(id) > 0;
    }
}
