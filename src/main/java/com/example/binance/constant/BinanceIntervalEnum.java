package com.example.binance.constant;

import lombok.Getter;

@Getter
public enum BinanceIntervalEnum {
    M1("1m", 1),
    M5("5m", 5),
    M15("15m", 15),
    M30("30m", 30),
    H1("1h", 60),
    D1("1d", 24 * 60),
    ;
    private final String interval;
    private final int minutes;

    BinanceIntervalEnum(String s, int i) {
        interval = s;
        minutes = i;
    }

}
