package com.example.binance.service;

import cn.hutool.core.date.DateTime;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.KlineBaseMapper;
import com.example.binance.mapper.btcusdt.BTCUSDT1hPMapper;
import com.example.binance.mapper.btcusdt.BTCUSDT1mPMapper;
import com.example.binance.mapper.btcusdt.BTCUSDT30mPMapper;
import com.example.binance.mapper.btcusdt.BTCUSDT5mPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT1hPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT1mPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT30mPMapper;
import com.example.binance.mapper.ethusdt.ETHUSDT5mPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT1hPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT1mPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT30mPMapper;
import com.example.binance.mapper.xagusdt.XAGUSDT5mPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT1hPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT1mPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT30mPMapper;
import com.example.binance.mapper.xauusdt.XAUUSDT5mPMapper;
import com.example.binance.util.KlineUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KlineService {
    @Autowired
    private BTCUSDT1mPMapper btcusdt1mPMapper;
    @Autowired
    private BTCUSDT5mPMapper btcusdt5mPMapper;
    @Autowired
    private BTCUSDT30mPMapper btcusdt30mPMapper;
    @Autowired
    private BTCUSDT1hPMapper btcusdt1hPMapper;

    @Autowired
    private ETHUSDT1mPMapper ethusdt1mPMapper;
    @Autowired
    private ETHUSDT5mPMapper ethusdt5mPMapper;
    @Autowired
    private ETHUSDT30mPMapper ethusdt30mPMapper;
    @Autowired
    private ETHUSDT1hPMapper ethusdt1hPMapper;

    @Autowired
    private XAUUSDT1mPMapper xauusdt1mPMapper;
    @Autowired
    private XAUUSDT5mPMapper xauusdt5mPMapper;
    @Autowired
    private XAUUSDT30mPMapper xauusdt30mPMapper;
    @Autowired
    private XAUUSDT1hPMapper xauusdt1hPMapper;

    @Autowired
    private XAGUSDT1mPMapper xagusdt1mPMapper;
    @Autowired
    private XAGUSDT5mPMapper xagusdt5mPMapper;
    @Autowired
    private XAGUSDT30mPMapper xagusdt30mPMapper;
    @Autowired
    private XAGUSDT1hPMapper xagusdt1hPMapper;

    @Autowired
    private BinanceFApi binanceFApi;

    @Getter
    private Map<String, KlineBaseMapper> mapperRegistry;

    @PostConstruct
    public void initMapperRegistry() {
        mapperRegistry = Map.ofEntries(
                Map.entry("BTCUSDT_1m", btcusdt1mPMapper),
                Map.entry("BTCUSDT_5m", btcusdt5mPMapper),
                Map.entry("BTCUSDT_30m", btcusdt30mPMapper),
                Map.entry("BTCUSDT_1h", btcusdt1hPMapper),

                Map.entry("ETHUSDT_1m", ethusdt1mPMapper),
                Map.entry("ETHUSDT_5m", ethusdt5mPMapper),
                Map.entry("ETHUSDT_30m", ethusdt30mPMapper),
                Map.entry("ETHUSDT_1h", ethusdt1hPMapper),

                Map.entry("XAUUSDT_1m", xauusdt1mPMapper),
                Map.entry("XAUUSDT_5m", xauusdt5mPMapper),
                Map.entry("XAUUSDT_30m", xauusdt30mPMapper),
                Map.entry("XAUUSDT_1h", xauusdt1hPMapper),

                Map.entry("XAGUSDT_1m", xagusdt1mPMapper),
                Map.entry("XAGUSDT_5m", xagusdt5mPMapper),
                Map.entry("XAGUSDT_30m", xagusdt30mPMapper),
                Map.entry("XAGUSDT_1h", xagusdt1hPMapper)
        );
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
    public List<KlineEntity> getKlineDataBySymbolAndInterval(
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
        List<KlineEntity> entities = mapper.selectByTimeRange(
                startTime.getTime(),
                endTime.getTime()
        );
        KlineUtil.calculateEma20(entities);
        return entities;
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
