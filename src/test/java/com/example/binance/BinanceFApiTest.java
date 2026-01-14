package com.example.binance;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.example.binance.api.BinanceFApi;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
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

    @Test
    void getKlineData_shouldWork() {
        String startTimeStr = "2025-01-01 00:00:00";
        String endTimeStr = "2025-01-01 00:10:00";
        DateTime startTime = new DateTime(startTimeStr, DatePattern.NORM_DATETIME_FORMAT);
        DateTime endTime = new DateTime(endTimeStr, DatePattern.NORM_DATETIME_FORMAT);
        String symbol = "BTCUSDT";
        List<KlineEntity> list = binanceFApi.getKlineData(symbol, BinanceIntervalEnum.M5, startTime.getTime(), endTime.getTime(), 1000);
        for (KlineEntity k : list) {
            log.info("k: {}", k);
        }
    }
}
