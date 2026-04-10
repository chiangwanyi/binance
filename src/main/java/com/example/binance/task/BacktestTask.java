package com.example.binance.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.example.binance.constant.BinanceIntervalEnum;
import com.example.binance.constant.KLineDirection;
import com.example.binance.entity.Balance;
import com.example.binance.entity.KlineEntity;
import com.example.binance.entity.Position;
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
import java.util.concurrent.TimeUnit;

import static com.example.binance.constant.KLineDirection.Bear;
import static com.example.binance.constant.KLineDirection.Bull;

@Component
@Slf4j
public class BacktestTask {
    @Autowired
    private KlineService klineService;

    private static final String symbol = "BTCUSDT";
    private static final BigDecimal riskPerTrade = new BigDecimal("0.02"); // 单笔风险2%
    private static final BigDecimal makerFeeRates = new BigDecimal("0.0002");  // 挂单费率
    private static final BigDecimal takerFeeRates = new BigDecimal("0.0005");  // 吃单费率
    private static final BigDecimal leverage = new BigDecimal("60");

    @PostConstruct
    public void tryTest() throws InterruptedException {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);

                DateTime start = DateTime.of("2026-04-01 00:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                DateTime end = DateTime.of("2026-04-07 23:00", DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                log.info("开始回测，回测从 [{}开盘] -- [{}收盘]", start, end);

                Balance balance = Balance.init("100.0");
                // 尝试做多开仓标记
                boolean longReady = false;
                // 尝试做空开仓标记
                boolean shortReady = false;
                // 仓位
                Position position = null;

                log.info("初始资金：{}", balance);
                BinanceIntervalEnum interval = BinanceIntervalEnum.M30;
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                DateTime s = new DateTime(new DateTime(start).offsetNew(DateField.HOUR_OF_DAY, -96).toLocalDateTime().atZone(zoneId).toInstant());

                // ***********
                // *** 回测 ***
                // ***********
                for (DateTime current = new DateTime(start);
                     current.isBeforeOrEquals(end);
                     current = current.offsetNew(DateField.MINUTE, interval.getMinutes())) {

                    log.info("当前时间：{}", current);
                    DateTime e = new DateTime(current.offsetNew(DateField.MINUTE, -interval.getMinutes()).toLocalDateTime().atZone(zoneId).toInstant());
                    List<KlineEntity> klineData = klineService.getKlineDataBySymbolAndInterval(symbol, interval, s, e, false);
//                    log.info("获取【{}】K线数据，时间段：[{}开盘] -- [{}收盘]，共{}条", interval.getInterval(), s, e, klineData.size());

                    KlineEntity lastK = klineData.getLast();
                    log.info("[#{}]K线：{}", klineData.size(), lastK);

                    // 尝试做多，判断是否能触发
                    if (longReady) {
                        // 前一根信号K线
                        KlineEntity signalK = klineData.get(klineData.size() - 2);
                        // 开仓价格（做多要稍微把价格上移保证成交）
                        BigDecimal entryPrice = signalK.getHighPriceValue().add(BigDecimal.valueOf(0.5));
                        if (KlineUtil.greaterThan(lastK.getHighPriceValue(), entryPrice)) {
                            log.info("【开仓做多】[#{}]K线[high:{}]，触发多仓信号[#{}]K的开仓价格{}，订单成交", klineData.size(), signalK.getHighPrice(), klineData.size() - 1, entryPrice);
                            // 做多的止损价，使用信号K的最低价（稍微下移一点）
                            BigDecimal stopLossPrice = signalK.getLowPriceValue().subtract(BigDecimal.valueOf(-0.5));
                            // 风险距离
                            BigDecimal riskPrice = entryPrice.subtract(stopLossPrice);
                            // 1R止盈：1倍的风险距离
                            BigDecimal tp1 = entryPrice.add(riskPrice);
                            // 2R止盈：2倍的风险距离
                            BigDecimal tp2 = entryPrice.add(riskPrice.multiply(BigDecimal.valueOf(2)));
                            // 最大亏损
                            BigDecimal riskLoss = balance.getBalance().multiply(riskPerTrade);
                            // 仓位
                            BigDecimal qty = riskLoss.divide(((entryPrice.subtract(stopLossPrice).abs())
                                    .add(entryPrice.multiply(makerFeeRates)
                                            .add(stopLossPrice.multiply(takerFeeRates)))), 4, RoundingMode.HALF_UP);
                            // 保证金
                            BigDecimal margin = entryPrice.multiply(qty).divide(leverage, 4, RoundingMode.HALF_UP);
                            // 挂单开仓手续费
                            BigDecimal makerFee = entryPrice.multiply(makerFeeRates).multiply(qty);
                            log.info("===========>  【做多】开仓价格{}，止损平仓价格{}，最大亏损{}，开仓手续费{}，保证金{}，仓位{}  <===========", entryPrice, stopLossPrice, riskLoss, makerFee, margin, qty);
                            String link = String.format("http://localhost:63342/binance/index.html?links=%s|%s|%s|%s|utc8|%.2f|%.2f|%.2f|%.2f",
                                    symbol, s.toString("yyyy-MM-dd'T'HH:mm"), e.toString("yyyy-MM-dd'T'HH:mm"), interval.getInterval(),
                                    stopLossPrice, entryPrice, tp1, tp2);
                            log.info("回测链接：{}", link);
                            position = Position.builder().type("long").entry(entryPrice).margin(margin).sl(stopLossPrice).tp1(tp1).tp2(tp2).qty(qty).build();
                            // 扣除手续费
                            balance.sub(makerFee);
                            // 扣除保证金
                            balance.sub(margin);
                            log.info("===========>  当前【做多】仓位：{}，账户余额：{}  <===========", position, balance.getBalance());
                            System.in.read();
                        } else {
                            log.info("[#{}]K线[high:{}]，未触发多仓信号[#{}]K的开仓价格{}", klineData.size(), signalK.getHighPrice(), klineData.size() - 1, entryPrice);
                            longReady = false;
                        }
                    }
                    // 尝试做空，判断是否能触发
                    if (shortReady) {
                        // 前一根信号K线
                        KlineEntity signalK = klineData.get(klineData.size() - 2);
                        // 开仓价格（做空要稍微把价格下移保证成交）
                        BigDecimal entryPrice = signalK.getLowPriceValue().add(BigDecimal.valueOf(-0.5));
                        if (KlineUtil.lessThan(lastK.getLowPriceValue(), entryPrice)) {
                            log.info("【开仓做空】[#{}]K线[low:{}]，触发空仓信号[#{}]K的开仓价格{}，订单成交", klineData.size(), signalK.getLowPrice(), klineData.size() - 1, entryPrice);
                            // 做空的止损价，使用信号K的最高价（稍微上移一点）
                            BigDecimal stopLossPrice = signalK.getHighPriceValue().subtract(BigDecimal.valueOf(0.5));
                            // 风险距离
                            BigDecimal riskPrice = entryPrice.subtract(stopLossPrice);
                            // 1R止盈：1倍的风险距离
                            BigDecimal tp1 = entryPrice.add(riskPrice);
                            // 2R止盈：2倍的风险距离
                            BigDecimal tp2 = entryPrice.add(riskPrice.multiply(BigDecimal.valueOf(2)));
                            // 最大亏损
                            BigDecimal riskLoss = balance.getBalance().multiply(riskPerTrade);
                            // 仓位
                            BigDecimal qty = riskLoss.divide(((entryPrice.subtract(stopLossPrice).abs())
                                    .add(entryPrice.multiply(makerFeeRates)
                                            .add(stopLossPrice.multiply(takerFeeRates)))), 4, RoundingMode.HALF_UP);
                            // 保证金
                            BigDecimal margin = entryPrice.multiply(qty).divide(leverage, 4, RoundingMode.HALF_UP);
                            // 挂单开仓手续费
                            BigDecimal makerFee = entryPrice.multiply(makerFeeRates).multiply(qty);
                            log.info("===========>  【做空】开仓价格{}，止损平仓价格{}，最大亏损{}，开仓手续费{}，保证金{}，仓位{}  <===========", entryPrice, stopLossPrice, riskLoss, makerFee, margin, qty);
                            String link = String.format("http://localhost:63342/binance/index.html?links=%s|%s|%s|%s|utc8|%.2f|%.2f|%.2f|%.2f",
                                    symbol, s.toString("yyyy-MM-dd'T'HH:mm"), e.toString("yyyy-MM-dd'T'HH:mm"), interval.getInterval(),
                                    stopLossPrice, entryPrice, tp1, tp2);
                            log.info("回测链接：{}", link);
                            position = Position.builder().type("short").entry(entryPrice).sl(stopLossPrice).tp1(tp1).tp2(tp2).qty(qty).build();
                            // 扣除手续费
                            balance.sub(makerFee);
                            // 扣除保证金
                            balance.sub(margin);
                            log.info("===========>  当前【做空】仓位：{}，账户余额：{}  <===========", position, balance.getBalance());
                            System.in.read();
                        } else {
                            log.info("[#{}]K线[low:{}]，未触发空仓信号[#{}]K的开仓价格{}", klineData.size(), signalK.getLowPrice(), klineData.size() - 1, entryPrice);
                            shortReady = false;
                        }
                    }

                    if (position == null) {
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
                    }

                    log.info("==========================");
                }
            } catch (Exception e) {
                log.error("回测异常", e);
            }
        }).start();
    }
}
