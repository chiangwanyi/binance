package com.example.binance.util;

import com.example.binance.constant.RiskMode;
import com.example.binance.entity.Balance;
import com.example.binance.entity.KlineEntity;
import com.example.binance.entity.Position;

import java.math.BigDecimal;

public class DecisionEngine {

    public static void evaluate(Position pos, KlineEntity k, Balance balance, BacktestStats stats) {

        if (pos.isClosed()) return;

        RiskMode mode = pos.getRiskMode();

        BigDecimal A = mode.getThresholdA();
        BigDecimal B = mode.getThresholdB();
        BigDecimal C = mode.getThresholdC();

        BigDecimal extremePrice =  pos.getDirection() > 0 ? k.getHighPriceValue() : k.getLowPriceValue();
        BigDecimal closePrice = k.getClosePriceValue();

        // ===== 做多止损（收盘价或者极值价） =====
        if (pos.getType().equals("long") && KlineUtil.lessThan(price, pos.getSl())) {
            close(pos, price, balance, stats);
            return;
        }

        if (pos.getType().equals("short") &&
                KlineUtil.greaterThan(price, pos.getSl())) {
            close(pos, price, balance, stats);
            return;
        }

        // ===== TP1 =====
        if (!pos.isTp1Hit()) {

            boolean hitTp1 = pos.getType().equals("long") ?
                    KlineUtil.greaterThan(price, pos.getTp1()) :
                    KlineUtil.lessThan(price, pos.getTp1());

            if (hitTp1) {
                pos.setTp1Hit(true);

                // 半仓
                pos.setRemainQty(pos.getQty().divide(new BigDecimal("2")));

                if (pos.getScore().compareTo(B) < 0) {
                    close(pos, price, balance, stats);
                }
                return;
            }
        }

        // ===== 0~1R =====
        if (!pos.isTp1Hit()) {
            if (pos.getScore().compareTo(A) < 0) {
                close(pos, price, balance, stats);
            }
        }

        // ===== 1R~2R =====
        if (pos.isTp1Hit()) {
            if (pos.getScore().compareTo(C) < 0) {
                close(pos, price, balance, stats);
            }
        }
    }

    private static void close(Position pos, BigDecimal price,
                              Balance balance, BacktestStats stats) {

        pos.setClosed(true);

        BigDecimal pnl = price.subtract(pos.getEntry())
                .multiply(pos.getType().equals("long") ? BigDecimal.ONE : BigDecimal.ONE.negate())
                .multiply(pos.getQty());

        balance.add(pnl);

        stats.recordTrade(pnl);
    }
}