package com.example.sentiment.service;

import com.example.sentiment.dto.DictDTO;
import com.example.sentiment.entity.SentimentDict;

import java.util.List;

/**
 * 情感词典管理服务（ADMIN）
 */
public interface DictService {

    List<SentimentDict> listAll();

    void add(DictDTO dto);

    void update(DictDTO dto);

    void delete(Long id);
}
