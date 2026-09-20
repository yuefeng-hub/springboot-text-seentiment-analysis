package com.example.sentiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sentiment.dto.DictDTO;
import com.example.sentiment.engine.EmotionDictManager;
import com.example.sentiment.entity.SentimentDict;
import com.example.sentiment.mapper.SentimentDictMapper;
import com.example.sentiment.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private SentimentDictMapper dictMapper;

    @Autowired
    private EmotionDictManager emotionDictManager;

    @Override
    public List<SentimentDict> listAll() {
        return dictMapper.selectList(new QueryWrapper<SentimentDict>().orderByAsc("id"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(DictDTO dto) {
        Long exist = dictMapper.selectCount(new QueryWrapper<SentimentDict>().eq("word", dto.getWord()));
        if (exist != null && exist > 0) {
            throw new RuntimeException("该情感词已存在");
        }
        SentimentDict dict = new SentimentDict();
        dict.setWord(dto.getWord());
        dict.setPolarity(dto.getPolarity());
        dict.setWeight(dto.getWeight() == null ? 1.0 : dto.getWeight());
        dict.setCategory(dto.getCategory());
        dict.setIsCustom(1);
        dict.setCreateTime(LocalDateTime.now());
        dictMapper.insert(dict);
        reloadEngine();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDTO dto) {
        SentimentDict dict = dictMapper.selectById(dto.getId());
        if (dict == null) {
            throw new RuntimeException("词典不存在");
        }
        dict.setWord(dto.getWord());
        dict.setPolarity(dto.getPolarity());
        dict.setWeight(dto.getWeight() == null ? 1.0 : dto.getWeight());
        dict.setCategory(dto.getCategory());
        dictMapper.updateById(dict);
        reloadEngine();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        dictMapper.deleteById(id);
        reloadEngine();
    }

    /** 增删改后让引擎实时生效 */
    private void reloadEngine() {
        List<SentimentDict> custom = dictMapper.selectList(null);
        emotionDictManager.reload(custom);
    }
}
