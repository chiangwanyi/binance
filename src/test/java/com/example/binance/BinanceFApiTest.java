package com.example.binance;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class BinanceFApiTest {
    @Autowired
    private BinanceFApi binanceFApi;
    @Autowired
    private BTCUSDT5mPMapper BTCUSDT5mPMapper;

    @Test
    void getKlineData_shouldWork() {
        String startTimeStr = "2025-01-02 00:00:00";
        String endTimeStr = "2025-01-02 23:59:59";
        DateTime startTime = new DateTime(startTimeStr, DatePattern.NORM_DATETIME_FORMAT);
        DateTime endTime = new DateTime(endTimeStr, DatePattern.NORM_DATETIME_FORMAT);
        String symbol = "BTCUSDT";
        List<KlineEntity> list = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, startTime.getTime(), endTime.getTime(), 1000);
        for (KlineEntity k : list) {
            log.info("k: {}", k);
        }
        int i = BTCUSDT5mPMapper.batchUpsert(list);
        log.info("insert count: {}", i);
    }

    @Test
    void download_2025_BTCUSDT_5m_kline_by_day() throws InterruptedException {

        String symbol = "BTCUSDT";
        int year = 2025;

        final int limit = 300;           // 1 天最多 288 根
        final long sleepMs = 300L;

        int totalInserted = 0;

        for (int month = 1; month <= 12; month++) {

            int daysInMonth = DateUtil.getLastDayOfMonth(
                    DateUtil.parse(year + "-" + month + "-01")
            );

            for (int day = 1; day <= daysInMonth; day++) {

                String dateStr = String.format("%04d-%02d-%02d", year, month, day);

                DateTime startTime = DateUtil.parse(
                        dateStr + " 00:00:00",
                        DatePattern.NORM_DATETIME_FORMAT
                );
                DateTime endTime = DateUtil.parse(
                        dateStr + " 23:59:59",
                        DatePattern.NORM_DATETIME_FORMAT
                );

                List<KlineEntity> list = binanceFApi.getKlineData(
                        symbol,
                        BinanceIntervalEnum.M5,
                        startTime.getTime(),
                        endTime.getTime(),
                        limit
                );

                if (list == null || list.isEmpty()) {
                    log.warn("No data: {}", dateStr);
                    continue;
                }

                int inserted = BTCUSDT5mPMapper.batchUpsert(list);
                totalInserted += inserted;

                log.info(
                        "Fetched {} {} records={}, inserted={}",
                        symbol,
                        dateStr,
                        list.size(),
                        inserted
                );

                // ⭐ 控制请求频率
                Thread.sleep(sleepMs);
            }
        }

        log.info("2025 BTCUSDT 5m download finished, total inserted={}", totalInserted);
    }
}
