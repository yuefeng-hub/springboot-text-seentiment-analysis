package com.example.sentiment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sentiment.common.Result;
import com.example.sentiment.entity.SentimentRecord;
import com.example.sentiment.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分析记录接口
 */
@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    /** 分页查询本人的分析记录 */
    @GetMapping("/list")
    public Result<Page<SentimentRecord>> list(@RequestAttribute("userId") Long userId,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size) {
        return Result.ok(recordService.pageByUser(userId, page, size));
    }

    /** 删除记录 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        boolean ok = recordService.delete(userId, id);
        return ok ? Result.ok() : Result.fail(400, "记录不存在或无权删除");
    }
}
