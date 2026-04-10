package com.example.binance.util;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BacktestStats {

    private int totalTrades = 0;
    private int winTrades = 0;
    private int lossTrades = 0;

    private int maxConsecutiveLoss = 0;
    private int currentConsecutiveLoss = 0;

    private BigDecimal maxDrawdown = BigDecimal.ZERO;
    private BigDecimal peak = BigDecimal.ZERO;

    public void recordTrade(BigDecimal pnl) {
        totalTrades++;

        if (pnl.compareTo(BigDecimal.ZERO) > 0) {
            winTrades++;
            currentConsecutiveLoss = 0;
        } else {
            lossTrades++;
            currentConsecutiveLoss++;
            maxConsecutiveLoss = Math.max(maxConsecutiveLoss, currentConsecutiveLoss);
        }
    }

    public void updateEquity(BigDecimal equity) {
        if (equity.compareTo(peak) > 0) {
            peak = equity;
        }

        BigDecimal dd = peak.subtract(equity);
        if (dd.compareTo(maxDrawdown) > 0) {
            maxDrawdown = dd;
        }
    }

    public void print() {
        System.out.println("总交易次数：" + totalTrades);
        System.out.println("胜率：" + (totalTrades == 0 ? 0 : (double) winTrades / totalTrades));
        System.out.println("盈利次数：" + winTrades);
        System.out.println("亏损次数：" + lossTrades);
        System.out.println("最大回撤：" + maxDrawdown);
        System.out.println("最大连续亏损：" + maxConsecutiveLoss);
    }
}
