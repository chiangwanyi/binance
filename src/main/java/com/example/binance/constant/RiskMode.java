package com.example.binance.constant;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum RiskMode {

    CONSERVATIVE(
            // 加分权重
            new BigDecimal("0.8"),
            new BigDecimal("0.6"),
            new BigDecimal("0.4"),

            // 扣分权重
            new BigDecimal("1.2"),
            new BigDecimal("1.0"),
            new BigDecimal("1.5"),
            new BigDecimal("1.2"),

            // 阈值
            new BigDecimal("6.0"),
            new BigDecimal("6.5"),
            new BigDecimal("6.0")
    ),

    BALANCED(
            new BigDecimal("1.0"),
            new BigDecimal("0.8"),
            new BigDecimal("0.5"),

            new BigDecimal("1.0"),
            new BigDecimal("0.8"),
            new BigDecimal("1.2"),
            new BigDecimal("1.0"),

            new BigDecimal("5.0"),
            new BigDecimal("5.5"),
            new BigDecimal("5.0")
    ),

    AGGRESSIVE(
            new BigDecimal("1.2"),
            new BigDecimal("1.0"),
            new BigDecimal("0.6"),

            new BigDecimal("0.8"),
            new BigDecimal("0.6"),
            new BigDecimal("1.0"),
            new BigDecimal("0.8"),

            new BigDecimal("4.0"),
            new BigDecimal("4.5"),
            new BigDecimal("4.0")
    );

    // 加分
    private final BigDecimal w1;
    private final BigDecimal w2;
    private final BigDecimal w3;

    // 扣分
    private final BigDecimal w4;
    private final BigDecimal w5;
    private final BigDecimal w6;
    private final BigDecimal w7;

    // 阈值
    private final BigDecimal thresholdA;
    private final BigDecimal thresholdB;
    private final BigDecimal thresholdC;

    RiskMode(BigDecimal w1, BigDecimal w2, BigDecimal w3,
             BigDecimal w4, BigDecimal w5, BigDecimal w6, BigDecimal w7,
             BigDecimal a, BigDecimal b, BigDecimal c) {

        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;

        this.w4 = w4;
        this.w5 = w5;
        this.w6 = w6;
        this.w7 = w7;

        this.thresholdA = a;
        this.thresholdB = b;
        this.thresholdC = c;
    }
}