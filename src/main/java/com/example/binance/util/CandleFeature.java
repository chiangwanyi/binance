package com.example.binance.util;

import com.example.binance.entity.KlineEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CandleFeature {
    public static BigDecimal bodyRatio(KlineEntity k) {
        BigDecimal range = k.getHighPriceValue().subtract(k.getLowPriceValue());
        if (range.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal body = k.getClosePriceValue().subtract(k.getOpenPriceValue()).abs();
        return body.divide(range, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal upperWickRatio(KlineEntity k) {
        BigDecimal range = k.getHighPriceValue().subtract(k.getLowPriceValue());
        if (range.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal max = k.getOpenPriceValue().max(k.getClosePriceValue());
        return k.getHighPriceValue().subtract(max).divide(range, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal lowerWickRatio(KlineEntity k) {
        BigDecimal range = k.getHighPriceValue().subtract(k.getLowPriceValue());
        if (range.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal min = k.getOpenPriceValue().min(k.getClosePriceValue());
        return min.subtract(k.getLowPriceValue()).divide(range, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal closePos(KlineEntity k) {
        BigDecimal range = k.getHighPriceValue().subtract(k.getLowPriceValue());
        if (range.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return k.getClosePriceValue().subtract(k.getLowPriceValue())
                .divide(range, 4, RoundingMode.HALF_UP);
    }
}
