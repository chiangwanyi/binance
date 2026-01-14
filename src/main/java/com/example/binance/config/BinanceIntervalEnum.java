package com.example.binance.config;

public enum BinanceIntervalEnum {
    M1("1m"),
    M5("5m"),
    M15("15m"),
    M30("30m"),
    H1("1h"),
    D1("1d"),
    ;
    private final String interval;

    BinanceIntervalEnum(String s) {
        interval = s;
    }

    public String getInterval() {
        return interval;
    }
}
