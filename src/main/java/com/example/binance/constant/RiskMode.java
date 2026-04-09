package com.example.binance.constant;

public enum RiskMode {
    CONSERVATIVE("conservative"),
    BALANCED("balanced"),
    AGGRESSIVE("aggressive");

    private final String value;

    RiskMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}