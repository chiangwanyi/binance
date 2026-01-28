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
import com.example.binance.mapper.ETHUSDT1hPMapper;
import com.example.binance.mapper.ETHUSDT5mPMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
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
    private ETHUSDT5mPMapper ethusdt5mPMapper;
    @Autowired
    private ETHUSDT1hPMapper ethusdt1hPMapper;

    @Autowired
    private BinanceFApi binanceFApi;

    @GetMapping("/ping")
    public AjaxResult ping() {
        return AjaxResult.success("pong");
    }

    /**
     * 获取K线数据范围接口
     * 支持BTCUSDT/ETHUSDT交易对的5分钟和1小时K线数据查询，并提供强制更新功能
     *
     * @param symbol       交易对符号，支持BTCUSDT、ETHUSDT
     * @param interval     时间间隔，支持"5m"和"1h"
     * @param startTimeStr 开始时间字符串，格式为"yyyy-MM-dd'T'HH:mm"
     * @param endTimeStr   结束时间字符串，格式为"yyyy-MM-dd'T'HH:mm"
     * @param force        是否强制从API获取最新数据，默认false
     * @param location     时区标识，"et"表示美东时间，其他表示默认时区
     * @return AjaxResult 包含K线数据列表或错误信息的结果对象
     */
    @GetMapping("/kline_range")
    public AjaxResult getKlineRange(@RequestParam(value = "s") String symbol,
                                    @RequestParam(value = "i") String interval,
                                    @RequestParam(value = "start") String startTimeStr,
                                    @RequestParam(value = "end") String endTimeStr,
                                    @RequestParam(value = "force", required = false) Boolean force,
                                    @RequestParam(value = "location") String location) {

        // ========= 1. 参数校验 =========
        if (StrUtil.isBlank(symbol) || StrUtil.isBlank(interval)
                || StrUtil.isBlank(startTimeStr) || StrUtil.isBlank(endTimeStr)) {
            return AjaxResult.error("参数错误");
        }

        if (!Objects.equals(symbol, "BTCUSDT") && !Objects.equals(symbol, "ETHUSDT")) {
            return AjaxResult.error("暂不支持该交易对，仅支持BTCUSDT、ETHUSDT");
        }

        // ========= 2. 确定时区（DST 核心） =========
        ZoneId zoneId;
        if ("et".equalsIgnoreCase(location)) {
            // 美东时间（自动处理 EST / EDT）
            zoneId = ZoneId.of("America/New_York");
        } else {
            // 默认按中国时间理解前端传参
            zoneId = ZoneId.of("Asia/Shanghai");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // ========= 3. 前端时间 → ZonedDateTime =========
        ZonedDateTime startZdt;
        ZonedDateTime endZdt;
        try {
            startZdt = LocalDateTime.parse(startTimeStr, formatter).atZone(zoneId);
            endZdt = LocalDateTime.parse(endTimeStr, formatter).atZone(zoneId);
        } catch (Exception e) {
            return AjaxResult.error("时间格式错误，应为 yyyy-MM-dd'T'HH:mm");
        }

        if (startZdt.isAfter(endZdt)) {
            return AjaxResult.error("开始时间不能大于结束时间");
        }

        // ========= 4. 统一转 UTC（用于查询 / 存储） =========
        Instant startUtc = startZdt.toInstant();
        Instant endUtc = endZdt.toInstant();

        DateTime startUtcDate = new DateTime(startUtc);
        DateTime endUtcDate = new DateTime(endUtc);

        // ========= 5. 查询 K 线 =========
        List<KlineEntity> values;
        switch (interval) {
            case "5m":
                values = getKlineDataBySymbolAndInterval(
                        symbol, BinanceIntervalEnum.M5, startUtcDate, endUtcDate, force);
                break;
            case "1h":
                values = getKlineDataBySymbolAndInterval(
                        symbol, BinanceIntervalEnum.H1, startUtcDate, endUtcDate, force);
                break;
            default:
                return AjaxResult.error("暂不支持该时间间隔，仅支持5m、1h");
        }

        // ========= 6. 美东交易时段过滤（仅 5m） =========
        if ("et".equalsIgnoreCase(location) && "5m".equals(interval)) {

            // 美股 RTH：09:30 ~ 16:10（美东）
            final int START_TOTAL_MIN = 9 * 60 + 30;
            final int END_TOTAL_MIN = 16 * 60 + 10;

            ZoneId etZone = ZoneId.of("America/New_York");

            values = values.stream()
                    .filter(v -> {
                        // UTC → 美东（DST 自动生效）
                        ZonedDateTime etTime = Instant.ofEpochMilli(v.getOpenTime())
                                .atZone(etZone);

                        DayOfWeek day = etTime.getDayOfWeek();
                        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                            return false;
                        }

                        int totalMin = etTime.getHour() * 60 + etTime.getMinute();
                        return totalMin >= START_TOTAL_MIN && totalMin <= END_TOTAL_MIN;
                    })
                    .collect(Collectors.toList());
        }

        // ========= 7. 返回给前端：统一转中国时间 =========
        ZoneId cnZone = ZoneId.of("Asia/Shanghai");
        values.forEach(v -> {
            ZonedDateTime cnTime = Instant.ofEpochMilli(v.getOpenTime()).atZone(cnZone);
            v.setOpenTime(cnTime.toInstant().toEpochMilli());
        });

        return AjaxResult.success(values);
    }


    /**
     * 根据交易对和时间间隔获取K线数据（抽取通用逻辑，减少代码冗余）
     * @param symbol 交易对
     * @param interval 时间间隔枚举
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param force 是否强制更新
     * @return K线数据列表
     */
    private List<KlineEntity> getKlineDataBySymbolAndInterval(String symbol, BinanceIntervalEnum interval,
                                                              DateTime startTime, DateTime endTime, Boolean force) {
        List<KlineEntity> values;
        boolean isForce = force != null && force;

        // 根据交易对和时间间隔选择对应的Mapper
        if ("5m".equals(interval.getInterval())) {
            if (isForce) {
                // 强制模式：直接从Binance API获取数据并保存到数据库
                values = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), endTime.getTime(), 1000);
                if ("BTCUSDT".equals(symbol)) {
                    btcusdt5mPMapper.batchUpsert(values);
                } else if ("ETHUSDT".equals(symbol)) {
                    ethusdt5mPMapper.batchUpsert(values);
                }
            } else {
                // 非强制模式：先查库，无数据/数据不完整则补充
                if ("BTCUSDT".equals(symbol)) {
                    values = btcusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                } else {
                    values = ethusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                }

                if (values.isEmpty()) {
                    values = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), endTime.getTime(), 1000);
                    if ("BTCUSDT".equals(symbol)) {
                        btcusdt5mPMapper.batchUpsert(values);
                    } else {
                        ethusdt5mPMapper.batchUpsert(values);
                    }
                } else {
                    // 补充开始时间前的缺失数据
                    KlineEntity first = values.getFirst();
                    if (first.getOpenTime() > startTime.getTime()) {
                        List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), first.getOpenTime(), 1000);
                        if ("BTCUSDT".equals(symbol)) {
                            btcusdt5mPMapper.batchUpsert(newValues);
                        } else {
                            ethusdt5mPMapper.batchUpsert(newValues);
                        }
                    }
                    // 补充结束时间后的缺失数据
                    KlineEntity last = values.getLast();
                    if (last.getOpenTime() < endTime.getTime()) {
                        List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, interval, last.getOpenTime(), endTime.getTime(), 1000);
                        if ("BTCUSDT".equals(symbol)) {
                            btcusdt5mPMapper.batchUpsert(newValues);
                        } else {
                            ethusdt5mPMapper.batchUpsert(newValues);
                        }
                    }
                    // 重新查询完整数据
                    if ("BTCUSDT".equals(symbol)) {
                        values = btcusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    } else {
                        values = ethusdt5mPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    }
                }
            }
        } else if ("1h".equals(interval.getInterval())) {
            if (isForce) {
                // 强制模式：直接从Binance API获取数据并保存到数据库
                values = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), endTime.getTime(), 1000);
                if ("BTCUSDT".equals(symbol)) {
                    btcusdt1hPMapper.batchUpsert(values);
                } else if ("ETHUSDT".equals(symbol)) {
                    ethusdt1hPMapper.batchUpsert(values);
                }
            } else {
                // 非强制模式：先查库，无数据/数据不完整则补充
                if ("BTCUSDT".equals(symbol)) {
                    values = btcusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                } else {
                    values = ethusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                }

                if (values.isEmpty()) {
                    values = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), endTime.getTime(), 1000);
                    if ("BTCUSDT".equals(symbol)) {
                        btcusdt1hPMapper.batchUpsert(values);
                    } else {
                        ethusdt1hPMapper.batchUpsert(values);
                    }
                } else {
                    // 补充开始时间前的缺失数据
                    KlineEntity first = values.getFirst();
                    if (first.getOpenTime() > startTime.getTime()) {
                        List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, interval, startTime.getTime(), first.getOpenTime(), 1000);
                        if ("BTCUSDT".equals(symbol)) {
                            btcusdt1hPMapper.batchUpsert(newValues);
                        } else {
                            ethusdt1hPMapper.batchUpsert(newValues);
                        }
                    }
                    // 补充结束时间后的缺失数据
                    KlineEntity last = values.getLast();
                    if (last.getOpenTime() < endTime.getTime()) {
                        List<KlineEntity> newValues = binanceFApi.getKlineData(symbol, interval, last.getOpenTime(), endTime.getTime(), 1000);
                        if ("BTCUSDT".equals(symbol)) {
                            btcusdt1hPMapper.batchUpsert(newValues);
                        } else {
                            ethusdt1hPMapper.batchUpsert(newValues);
                        }
                    }
                    // 重新查询完整数据
                    if ("BTCUSDT".equals(symbol)) {
                        values = btcusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    } else {
                        values = ethusdt1hPMapper.selectByTimeRange(startTime.getTime(), endTime.getTime());
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("不支持的时间间隔");
        }

        return values;
    }
}