package com.example.binance.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.example.binance.constant.BinanceIntervalEnum;
import com.example.binance.constant.KLineDirection;
import com.example.binance.entity.KlineEntity;
import com.example.binance.service.KlineService;
import com.example.binance.util.KlineUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.example.binance.constant.KLineDirection.Bear;
import static com.example.binance.constant.KLineDirection.Bull;

@Component
@Slf4j
public class BacktestTask {
    @Autowired
    private KlineService klineService;

    @PostConstruct
    public void tryTest() throws InterruptedException {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);

                DateTime start = DateTime.of("2026-04-01 00:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                DateTime end = DateTime.of("2026-04-07 23:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                log.info("开始回测，回测从 [{}开盘] -- [{}收盘]", start, end);


                // 尝试做多开仓标记
                boolean longReady = false;
                // 尝试做空开仓标记
                boolean shortReady = false;

                BigDecimal balance = new BigDecimal("100000");
                balance = balance.setScale(2, RoundingMode.HALF_UP);
                log.info("初始资金：{}", balance);

                BinanceIntervalEnum interval = BinanceIntervalEnum.M30;
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                DateTime s = new DateTime(new DateTime(start).offsetNew(DateField.HOUR_OF_DAY, -96).toLocalDateTime().atZone(zoneId).toInstant());
                for (DateTime current = new DateTime(start);
                     current.isBeforeOrEquals(end);
                     current = current.offsetNew(DateField.MINUTE, interval.getMinutes())) {

                    log.info("当前时间：{}", current);
                    DateTime e = new DateTime(current.offsetNew(DateField.MINUTE, -interval.getMinutes()).toLocalDateTime().atZone(zoneId).toInstant());
                    List<KlineEntity> klineData = klineService.getKlineDataBySymbolAndInterval("BTCUSDT", interval, s, e, false);
                    log.info("获取【{}】K线数据，时间段：[{}开盘] -- [{}收盘]，共{}条",interval.getInterval(), s, e, klineData.size());

                    KlineEntity lastK = klineData.getLast();
                    log.info("[#{}]K线：{}", klineData.size(), lastK);

                    if (longReady) {
                        KlineEntity signalK = klineData.get(klineData.size() - 2);
                        BigDecimal entryPrice = signalK.getHighPriceValue();
                        if (KlineUtil.greaterThan(lastK.getHighPriceValue(), entryPrice)) {
                            log.info("[#{}]K线[high:{}]，触发多仓信号[#{}]K的开仓价格{}，订单成交，【做多】", klineData.size(), signalK.getHighPrice(), klineData.size() - 1, entryPrice);
                            System.in.read();
                        } else {
                            log.info("[#{}]K线[high:{}]，未触发多仓信号[#{}]K的开仓价格{}", klineData.size(), signalK.getHighPrice(), klineData.size() - 1, entryPrice);
                            longReady = false;
                        }
                    }
                    // 尝试做空，判断是否能触发
                    if (shortReady) {
                        KlineEntity signalK = klineData.get(klineData.size() - 2);
                        BigDecimal entryPrice = signalK.getLowPriceValue();
                        if (KlineUtil.lessThan(lastK.getLowPriceValue(), entryPrice)) {
                            log.info("[#{}]K线[low:{}]，触发空仓信号[#{}]K的开仓价格{}，订单成交，【做空】", klineData.size(), signalK.getLowPrice(), klineData.size() - 1, entryPrice);
                            System.in.read();
                        } else {
                            log.info("[#{}]K线[low:{}]，未触发空仓信号[#{}]K的开仓价格{}", klineData.size(), signalK.getLowPrice(), klineData.size() - 1, entryPrice);
                            shortReady = false;
                        }
                    }

                    // 最新K线方向
                    KLineDirection direction = KlineUtil.getKLineDirection(lastK);
                    switch (direction) {
                        // 阳线
                        case Bull -> {
                            boolean a = KlineUtil.lessThan(lastK.getOpenPriceValue(), lastK.getEma20());
                            boolean b = KlineUtil.greaterThan(lastK.getClosePriceValue(), lastK.getEma20());
                            if (a && b) {
                                log.info("[#{}]K线收【{}】，[open:{}, close:{}, ema20:{}]，向上穿过EMA20，尝试做多", klineData.size(), Bull.getDesc(), lastK.getOpenPrice(), lastK.getClosePrice(), lastK.getEma20());
                                longReady = true;
                            }
                        }
                        // 阴线
                        case Bear -> {
                            boolean a = KlineUtil.greaterThan(lastK.getOpenPriceValue(), lastK.getEma20());
                            boolean b = KlineUtil.lessThan(lastK.getClosePriceValue(), lastK.getEma20());
                            if (a && b) {
                                log.info("[#{}]K线收【{}】，[open:{}, close:{}, ema20:{}]，向下穿过EMA20，尝试做空", klineData.size(), Bear.getDesc(), lastK.getOpenPrice(), lastK.getClosePrice(), lastK.getEma20());
                                shortReady = true;
                            }
                        }
                    }

                    log.info("==========================");
                }
            } catch (Exception e) {
                log.error("回测异常", e);
            }
        }).start();
    }
}
