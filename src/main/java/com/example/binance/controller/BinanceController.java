package com.example.binance.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.AjaxResult;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.*;
import com.example.binance.mapper.btcusdt.BTCUSDT1hPMapper;
import com.example.binance.mapper.btcusdt.BTCUSDT5mPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT1hPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT5mPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT1hPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT5mPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT1hPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT5mPMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private XAUUSDT5mPMapper xauusdt5mPMapper;
    @Autowired
    private XAUUSDT1hPMapper xauusdt1hPMapper;
    @Autowired
    private XAGUSDT5mPMapper xagusdt5mPMapper;
    @Autowired
    private XAGUSDT1hPMapper xagusdt1hPMapper;

    @Autowired
    private BinanceFApi binanceFApi;

    private Map<String, KlineBaseMapper> mapperRegistry;

    @PostConstruct
    public void initMapperRegistry() {
        mapperRegistry = Map.of(
                "BTCUSDT_5m", btcusdt5mPMapper,
                "BTCUSDT_1h", btcusdt1hPMapper,
                "ETHUSDT_5m", ethusdt5mPMapper,
                "ETHUSDT_1h", ethusdt1hPMapper,
                "XAUUSDT_5m", xauusdt5mPMapper,
                "XAUUSDT_1h", xauusdt1hPMapper,
                "XAGUSDT_5m", xagusdt5mPMapper,
                "XAGUSDT_1h", xagusdt1hPMapper
        );
    }


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

        Optional<String> any = mapperRegistry.keySet().stream()
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
    private List<KlineEntity> getKlineDataBySymbolAndInterval(
            String symbol,
            BinanceIntervalEnum interval,
            DateTime startTime,
            DateTime endTime,
            Boolean force) {

        boolean isForce = Boolean.TRUE.equals(force);
        KlineBaseMapper mapper = getMapper(symbol, interval);

        // 1. 强制模式：直接 API → DB → 返回
        if (isForce) {
            List<KlineEntity> apiData = binanceFApi.getKlineData(
                    symbol, interval,
                    startTime.getTime(),
                    endTime.getTime(),
                    1000
            );
            mapper.batchUpsert(apiData);
            return apiData;
        }

        // 2. 非强制：先查库
        List<KlineEntity> values =
                mapper.selectByTimeRange(startTime.getTime(), endTime.getTime());

        // 3. 库里完全没数据
        if (values.isEmpty()) {
            List<KlineEntity> apiData = binanceFApi.getKlineData(
                    symbol, interval,
                    startTime.getTime(),
                    endTime.getTime(),
                    1000
            );
            mapper.batchUpsert(apiData);
            return apiData;
        }

        // 4. 补前
        KlineEntity first = values.getFirst();
        if (first.getOpenTime() > startTime.getTime()) {
            List<KlineEntity> before = binanceFApi.getKlineData(
                    symbol, interval,
                    startTime.getTime(),
                    first.getOpenTime(),
                    1000
            );
            mapper.batchUpsert(before);
        }

        // 5. 补后
        KlineEntity last = values.getLast();
        if (last.getOpenTime() < endTime.getTime()) {
            List<KlineEntity> after = binanceFApi.getKlineData(
                    symbol, interval,
                    last.getOpenTime(),
                    endTime.getTime(),
                    1000
            );
            mapper.batchUpsert(after);
        }

        // 6. 重新查询完整数据
        return mapper.selectByTimeRange(
                startTime.getTime(),
                endTime.getTime()
        );
    }

    private KlineBaseMapper getMapper(String symbol, BinanceIntervalEnum interval) {
        KlineBaseMapper mapper =
                mapperRegistry.get(symbol + "_" + interval.getInterval());
        if (mapper == null) {
            throw new IllegalArgumentException("不支持的交易对或周期");
        }
        return mapper;
    }
}