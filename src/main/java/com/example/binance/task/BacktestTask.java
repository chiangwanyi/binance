package com.example.binance.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import com.example.binance.service.KlineService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BacktestTask {
    @Autowired
    private KlineService klineService;

    @PostConstruct
    public void tryTest() throws InterruptedException {
        DateTime start = DateTime.of("2026-04-01 00:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        DateTime end = DateTime.of("2026-04-07 23:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        log.info("开始回测，回测从 [{}开盘] -- [{}收盘]", start, end);

        BigDecimal balance = new BigDecimal("100000");
        balance = balance.setScale(2, RoundingMode.HALF_UP);
        log.info("初始资金：{}", balance);

        BinanceIntervalEnum interval = BinanceIntervalEnum.M30;
        for (DateTime current = new DateTime(start);
             current.isBeforeOrEquals(end);
             current = current.offsetNew(DateField.MINUTE, interval.getMinutes())) {

            log.info("当前时间：{}", current);
            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            DateTime s = new DateTime(current.offsetNew(DateField.HOUR_OF_DAY, -96).toLocalDateTime().atZone(zoneId).toInstant());
            DateTime e = new DateTime(current.offsetNew(DateField.MINUTE, -interval.getMinutes()).toLocalDateTime().atZone(zoneId).toInstant());
            List<KlineEntity> klineData = klineService.getKlineDataBySymbolAndInterval("BTCUSDT", interval, s, e, false);
            log.info("获取【{}】K线数据，时间段：[{}开盘] -- [{}收盘]，共{}条",interval.getInterval(), s, e, klineData.size());

            TimeUnit.SECONDS.sleep(10);
        }
    }
}
