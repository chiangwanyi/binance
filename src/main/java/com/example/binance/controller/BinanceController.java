package com.example.binance.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.example.binance.config.AjaxResult;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/c/binance")
public class BinanceController {
    @Autowired
    private BTCUSDT5mPMapper btcusdt5mPMapper;

    @GetMapping("/ping")
    public AjaxResult ping() {
        return AjaxResult.success("pong");
    }

    @GetMapping("/kline_range")
    public AjaxResult getKlineRange(@RequestParam(value = "s") String symbol,
                                    @RequestParam(value = "i") String interval,
                                    @RequestParam(value = "start") String startTimeStr,
                                    @RequestParam(value = "end") String endTimeStr) {
        if (StrUtil.isBlank(symbol) || StrUtil.isBlank(interval) || StrUtil.isBlank(startTimeStr) || StrUtil.isBlank(endTimeStr)) {
            return AjaxResult.error("参数错误");
        }
        if (!Objects.equals(symbol, "BTCUSDT")) {
            return AjaxResult.error("暂不支持该交易对");
        }
        switch (interval) {
            case "5m":
                DateTime startTime = new DateTime(startTimeStr, "yyyy-MM-dd'T'HH:mm");
                DateTime endTime = new DateTime(endTimeStr, "yyyy-MM-dd'T'HH:mm");
                List<KlineEntity> values = btcusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                values.forEach(item -> {
                    // 将item的openTime + 8小时
                    item.setOpenTime(item.getOpenTime() + 8 * 60 * 60 * 1000);
                });
                return AjaxResult.success(values);
            default:
                return AjaxResult.error("暂不支持该时间间隔");
        }
    }
}
