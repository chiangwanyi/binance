package com.example.binance.constant;

import java.math.BigDecimal;

public class StrategyConstants {
    // 基础参数
    public static final String SYMBOL = "BTCUSDT";
    public static final BigDecimal INIT_BALANCE = new BigDecimal("10000");
    public static final BigDecimal RISK_PER_TRADE = new BigDecimal("0.02"); // 单笔风险2%
    public static final BigDecimal MAKER_FEE = new BigDecimal("0.0002");  // 挂单费率
    public static final BigDecimal TAKER_FEE = new BigDecimal("0.0005");  // 吃单费率
    public static final int LEVERAGE = 60;                                // 杠杆
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