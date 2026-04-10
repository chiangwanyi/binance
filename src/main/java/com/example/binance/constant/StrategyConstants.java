package com.example.binance.constant;

import java.math.BigDecimal;

public class StrategyConstants {
                      // 杠杆
    public static final RiskMode RISK_MODE = RiskMode.AGGRESSIVE;         // 风险模式

    // EMA周期
    public static final int EMA_PERIOD = 20;

    // 时间参数
    public static final long KLINE_5M_MINUTES = 5;
    public static final long KLINE_30M_MINUTES = 30;
    public static final long LOOKBACK_6H_HOURS = 6;
    public static final long LOOKBACK_1H_HOURS = 1;
    public static final long LOOKBACK_96H_HOURS = 96;
}