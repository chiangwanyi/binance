package com.example.binance.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.example.binance.config.AjaxResult;
import com.example.binance.constant.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.service.KlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/c/binance")
public class BinanceController {
    private final String excludeDates = "01-01,12-25";
    @Autowired
    private KlineService klineService;

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

        Optional<String> any = klineService.getMapperRegistry().keySet().stream()
                .filter(key -> key.startsWith(symbol))
                .findAny();
        if (any.isEmpty()) {
            return AjaxResult.error("暂不支持该交易对");
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
            case "1m":
                values = klineService.getKlineDataBySymbolAndInterval(
                        symbol, BinanceIntervalEnum.M1, startUtcDate, endUtcDate, force);
                break;
            case "5m":
                values = klineService.getKlineDataBySymbolAndInterval(
                        symbol, BinanceIntervalEnum.M5, startUtcDate, endUtcDate, force);
                break;
            case "30m":
            values = klineService.getKlineDataBySymbolAndInterval(
                    symbol, BinanceIntervalEnum.M30, startUtcDate, endUtcDate, force);
            break;
            case "1h":
                values = klineService.getKlineDataBySymbolAndInterval(
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
            // 解析需要过滤的日期（MM-dd格式）
            Set<String> excludeDateSet = new HashSet<>();
            if (StrUtil.isNotBlank(excludeDates)) {
                // 拆分多个日期，去重
                String[] dateArray = excludeDates.split(",");
                for (String date : dateArray) {
                    String trimDate = date.trim();
                    // 简单校验日期格式（MM-dd）
                    if (trimDate.matches("^\\d{2}-\\d{2}$")) {
                        excludeDateSet.add(trimDate);
                    }
                }
            }

            values = values.stream()
                    .filter(v -> {
                        // UTC → 美东（DST 自动生效）
                        ZonedDateTime etTime = Instant.ofEpochMilli(v.getOpenTime())
                                .atZone(etZone);

                        // 1. 过滤周末
                        DayOfWeek day = etTime.getDayOfWeek();
                        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                            return false;
                        }

                        // 2. 过滤固定节假日（MM-dd）
                        String mmDd = String.format("%02d-%02d",
                                etTime.get(ChronoField.MONTH_OF_YEAR),
                                etTime.get(ChronoField.DAY_OF_MONTH));
                        if (excludeDateSet.contains(mmDd)) {
                            return false;
                        }

                        // 3. 过滤 11月第四个星期四（感恩节）
                        LocalDate thanksgiving = getThanksgiving(etTime.getYear());
                        if (etTime.toLocalDate().equals(thanksgiving)) {
                            return false;
                        }

                        // 4. 过滤交易时段外的K线
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
     * 计算指定年份的感恩节：11月第四个星期四
     */
    private LocalDate getThanksgiving(int year) {
        return LocalDate.of(year, 11, 1)
                .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
    }
}