package com.example.binance.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.AjaxResult;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/c/binance")
public class BinanceController {
    @Autowired
    private BTCUSDT5mPMapper btcusdt5mPMapper;
    @Autowired
    private BinanceFApi binanceFApi;

    @GetMapping("/ping")
    public AjaxResult ping() {
        return AjaxResult.success("pong");
    }

    @GetMapping("/kline_range")
    public AjaxResult getKlineRange(@RequestParam(value = "s") String symbol,
                                    @RequestParam(value = "i") String interval,
                                    @RequestParam(value = "start") String startTimeStr,
                                    @RequestParam(value = "end") String endTimeStr,
                                    @RequestParam(value = "force", required = false) Boolean force) {
        if (StrUtil.isBlank(symbol) || StrUtil.isBlank(interval) || StrUtil.isBlank(startTimeStr) || StrUtil.isBlank(endTimeStr)) {
            return AjaxResult.error("参数错误");
        }
        if (!Objects.equals(symbol, "BTCUSDT")) {
            return AjaxResult.error("暂不支持该交易对");
        }
        DateTime startTime = new DateTime(startTimeStr, "yyyy-MM-dd'T'HH:mm");
        DateTime endTime = new DateTime(endTimeStr, "yyyy-MM-dd'T'HH:mm");
        if (startTime.isAfter(endTime)) {
            return AjaxResult.error("开始时间不能大于结束时间");
        }
        switch (interval) {
            case "5m":
                List<KlineEntity> values;
                if (force) {
                    values = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, startTime.getTime(), endTime.getTime(), 1000);
                    btcusdt5mPMapper.batchUpsert(values);
                } else {
                    values = btcusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    if (values.isEmpty()) {
                        values = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, startTime.getTime(), endTime.getTime(), 1000);
                        btcusdt5mPMapper.batchUpsert(values);
                    } else {
                        KlineEntity first = values.getFirst();
                        if (first.getOpenTime() > startTime.getTime()) {
                            List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, startTime.getTime(), first.getOpenTime(), 1000);
                            btcusdt5mPMapper.batchUpsert(newValues);
                        }
                        KlineEntity last = values.getLast();
                        if (last.getOpenTime() < endTime.getTime()) {
                            List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, last.getOpenTime(), endTime.getTime(), 1000);
                            btcusdt5mPMapper.batchUpsert(newValues);
                        }
                        values = btcusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    }
                }
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
