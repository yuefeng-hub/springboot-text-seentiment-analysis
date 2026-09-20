package com.example.sentiment.controller;

import com.example.sentiment.common.Result;
import com.example.sentiment.dto.DictDTO;
import com.example.sentiment.entity.SentimentDict;
import com.example.sentiment.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 情感词典管理接口（生产环境应加 ADMIN 权限校验，毕设阶段可从前端做角色控制）
 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/list")
    public Result<List<SentimentDict>> list() {
        return Result.ok(dictService.listAll());
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody DictDTO dto) {
        dictService.add(dto);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody DictDTO dto) {
        dictService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.ok();
    }
}
