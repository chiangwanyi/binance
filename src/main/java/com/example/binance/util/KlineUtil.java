package com.example.binance.util;

import com.example.binance.entity.KlineEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class KlineUtil {
    // EMA20 固定周期
    private static final int PERIOD = 20;
    // 平滑系数 α = 2/(周期+1)
    private static final BigDecimal ALPHA = new BigDecimal(2).divide(new BigDecimal(PERIOD + 1), 12, RoundingMode.HALF_UP);
    // 1 - α
    private static final BigDecimal ONE_MINUS_ALPHA = BigDecimal.ONE.subtract(ALPHA);
    // 计算精度（保留8位小数，适配币安价格精度）
    private static final int SCALE = 8;

    /**
     * 计算EMA20，并直接赋值给 原始KlineEntity 的 ema20 字段
     * @param klineList K线列表（必须按时间【正序】排列）
     */
    public static void calculateEma20(List<KlineEntity> klineList) {
        // 空列表直接返回
        if (klineList == null || klineList.isEmpty()) {
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal previousEma = null;

        for (int i = 0; i < klineList.size(); i++) {
            KlineEntity entity = klineList.get(i);
            BigDecimal close = convertToBigDecimal(entity.getClosePrice());
            BigDecimal currentEma;

            // 1. 前20根K线：累加求和，计算SMA作为EMA初始值
            if (i < PERIOD) {
                sum = sum.add(close);
                currentEma = sum.divide(new BigDecimal(i + 1), SCALE, RoundingMode.HALF_UP);
            }
            // 2. 第21根及以后：使用EMA递推公式计算
            else {
                currentEma = close.multiply(ALPHA)
                        .add(previousEma.multiply(ONE_MINUS_ALPHA))
                        .setScale(SCALE, RoundingMode.HALF_UP);
            }

            // ✅ 核心：直接将计算结果赋值给 原始对象 的 ema20 字段
            entity.setEma20(currentEma);
            // 更新上一期EMA，用于下一次计算
            previousEma = currentEma;
        }
    }

    /**
     * 字符串收盘价 转 BigDecimal（兼容null/空字符串）
     */
    private static BigDecimal convertToBigDecimal(String price) {
        if (price == null || price.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(price.trim());
    }
}
