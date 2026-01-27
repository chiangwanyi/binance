package com.example.binance.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.AjaxResult;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT1hPMapper;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/c/binance")
public class BinanceController {
    @Autowired
    private BTCUSDT5mPMapper btcusdt5mPMapper;
    @Autowired
    private BTCUSDT1hPMapper btcusdt1hPMapper;

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
                                    @RequestParam(value = "force", required = false) Boolean force,
                                    @RequestParam(value = "location") String location) {
        if (StrUtil.isBlank(symbol) || StrUtil.isBlank(interval) || StrUtil.isBlank(startTimeStr) || StrUtil.isBlank(endTimeStr)) {
            return AjaxResult.error("参数错误");
        }
        if (!Objects.equals(symbol, "BTCUSDT")) {
            return AjaxResult.error("暂不支持该交易对");
        }
        DateTime startTime = new DateTime(startTimeStr, "yyyy-MM-dd'T'HH:mm");
        DateTime endTime = new DateTime(endTimeStr, "yyyy-MM-dd'T'HH:mm");
        // 如果location为et（即美东时间），则将startTime和endTime分别加上13小时
        if (Objects.equals(location, "et")) {
            startTime = startTime.offset(DateField.HOUR_OF_DAY, 13);
            endTime = endTime.offset(DateField.HOUR_OF_DAY, 13);
        }
        if (startTime.isAfter(endTime)) {
            return AjaxResult.error("开始时间不能大于结束时间");
        }

        // 定义目标时间区间：9:30 和 16:10（转换为总分钟数）
        final int START_TOTAL_MIN = 9 * 60 + 30;  // 9:30 = 570分钟
        final int END_TOTAL_MIN = 16 * 60 + 10;   // 16:10 = 970分钟

        switch (interval) {
            case "5m": {
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
                values = values.stream()
                        .filter(value -> {
                            DateTime dateTime = DateUtil.date(value.getOpenTime()).offset(DateField.HOUR_OF_DAY, -13);
                            // 2. 正确获取24小时制小时和分钟（核心修正）
                            int hour = dateTime.getField(Calendar.HOUR_OF_DAY); // 24小时制小时
                            int minute = dateTime.getField(Calendar.MINUTE);    // 分钟
                            // 3. 转换为总分钟数，判断是否在区间内
                            int currentTotalMin = hour * 60 + minute;
                            return currentTotalMin >= START_TOTAL_MIN && currentTotalMin <= END_TOTAL_MIN;
                        }).collect(Collectors.toList());
                values.forEach(item -> {
                    // 将item的openTime + 8小时
                    item.setOpenTime(item.getOpenTime() + 8 * 60 * 60 * 1000);
                });
                return AjaxResult.success(values);
            }
            case "1h": {
                List<KlineEntity> values;
                if (force) {
                    values = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.H1, startTime.getTime(), endTime.getTime(), 1000);
                    btcusdt1hPMapper.batchUpsert(values);
                } else {
                    values = btcusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    if (values.isEmpty()) {
                        values = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.H1, startTime.getTime(), endTime.getTime(), 1000);
                        btcusdt1hPMapper.batchUpsert(values);
                    } else {
                        KlineEntity first = values.getFirst();
                        if (first.getOpenTime() > startTime.getTime()) {
                            List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.H1, startTime.getTime(), first.getOpenTime(), 1000);
                            btcusdt1hPMapper.batchUpsert(newValues);
                        }
                        KlineEntity last = values.getLast();
                        if (last.getOpenTime() < endTime.getTime()) {
                            List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.H1, last.getOpenTime(), endTime.getTime(), 1000);
                            btcusdt1hPMapper.batchUpsert(newValues);
                        }
                        values = btcusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    }
                }
                values.forEach(item -> {
                    // 将item的openTime + 8小时
                    item.setOpenTime(item.getOpenTime() + 8 * 60 * 60 * 1000);
                });
                return AjaxResult.success(values);
            }
            default:
                return AjaxResult.error("暂不支持该时间间隔");
        }
    }

    /**
     * 辅助方法：判断时分是否在指定区间内
     *
     * @param hour        当前小时
     * @param minute      当前分钟
     * @param startHour   开始小时
     * @param startMinute 开始分钟
     * @param endHour     结束小时
     * @param endMinute   结束分钟
     * @return 是否在区间内
     */
    private static boolean isTimeInRange(int hour, int minute, int startHour, int startMinute, int endHour, int endMinute) {
        // 转换为分钟数，简化区间判断（避免多条件嵌套）
        int currentTotalMin = hour * 60 + minute;
        int startTotalMin = startHour * 60 + startMinute;
        int endTotalMin = endHour * 60 + endMinute;

        return currentTotalMin >= startTotalMin && currentTotalMin <= endTotalMin;
    }
}
