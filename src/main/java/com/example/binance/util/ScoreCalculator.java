package com.example.binance.util;

import com.example.binance.constant.KLineDirection;
import com.example.binance.constant.RiskMode;
import com.example.binance.entity.KlineEntity;
import com.example.binance.entity.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ScoreCalculator {

    public static BigDecimal updateScore(Position pos, KlineEntity k, KlineEntity prevK) {

        RiskMode mode = pos.getRiskMode();

        BigDecimal body = CandleFeature.bodyRatio(k);
        BigDecimal upper = CandleFeature.upperWickRatio(k);
        BigDecimal lower = CandleFeature.lowerWickRatio(k);
        BigDecimal closePos = CandleFeature.closePos(k);

        boolean isLong = pos.getType().equals("long");

        // ===== 加分 =====
        BigDecimal momentum =
                body.compareTo(new BigDecimal("0.7")) >= 0 ? BigDecimal.ONE :
                        body.compareTo(new BigDecimal("0.5")) >= 0 ? new BigDecimal("0.5") :
                                BigDecimal.ZERO;

        BigDecimal closeStrength = isLong ? closePos : BigDecimal.ONE.subtract(closePos);

        BigDecimal continuation =
                KlineUtil.getKLineDirection(k) == KlineUtil.getKLineDirection(prevK)
                        ? new BigDecimal("0.5") : BigDecimal.ZERO;

        BigDecimal scoreUp =
                mode.getW1().multiply(momentum)
                        .add(mode.getW2().multiply(closeStrength))
                        .add(mode.getW3().multiply(continuation));

        // ===== 扣分 =====
        BigDecimal pressure = isLong ? upper : lower;

        BigDecimal lowMomentum =
                body.compareTo(new BigDecimal("0.3")) < 0 ? BigDecimal.ONE :
                        body.compareTo(new BigDecimal("0.5")) < 0 ? new BigDecimal("0.5") :
                                BigDecimal.ZERO;

        boolean reverse = !KlineUtil.getKLineDirection(k)
                .equals(isLong ? KLineDirection.Bull : KLineDirection.Bear);

        BigDecimal reversePenalty = reverse ?
                (body.compareTo(new BigDecimal("0.6")) >= 0 ? BigDecimal.ONE : new BigDecimal("0.5"))
                : BigDecimal.ZERO;

        // ===== 区间震荡 =====
        pos.setZoneHigh(pos.getZoneHigh().max(k.getHighPriceValue()));
        pos.setZoneLow(pos.getZoneLow().min(k.getLowPriceValue()));

        BigDecimal zoneRange = pos.getZoneHigh().subtract(pos.getZoneLow());
        BigDecimal riskR = pos.getEntry().subtract(pos.getSl()).abs();

        BigDecimal oscillation = zoneRange.divide(riskR, 4, RoundingMode.HALF_UP);
        oscillation = oscillation.min(new BigDecimal("2.0"));

        BigDecimal scoreDown =
                mode.getW4().multiply(pressure)
                        .add(mode.getW5().multiply(lowMomentum))
                        .add(mode.getW6().multiply(reversePenalty))
                        .add(mode.getW7().multiply(oscillation));

        BigDecimal newScore = pos.getScore().add(scoreUp).subtract(scoreDown);

        return newScore.max(BigDecimal.ONE).min(new BigDecimal("10"));
    }
}