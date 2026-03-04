package com.example.binance.controller;

import cn.hutool.core.lang.UUID;
import com.example.binance.config.AjaxResult;
import com.example.binance.entity.ChartSnapshot;
import com.example.binance.mapper.ChartSnapshotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chart")
public class ChartSnapshotController {
    @Autowired
    private ChartSnapshotMapper chartSnapshotMapper;

    @PostMapping
    public AjaxResult save(@RequestBody String content) {

        String id = UUID.fastUUID().toString(true);

        ChartSnapshot snapshot = new ChartSnapshot();
        snapshot.setId(id);
        snapshot.setContent(content);
        snapshot.setCreateTime(System.currentTimeMillis());

        chartSnapshotMapper.insert(snapshot);

        return AjaxResult.success(id);
    }

    // 获取快照
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable String id) {
        ChartSnapshot snapshot = chartSnapshotMapper.selectById(id);
        if (snapshot == null) {
            return AjaxResult.error("快照不存在");
        }
        return AjaxResult.success(snapshot);
    }
}
