package com.example.binance.constant;

import lombok.Getter;

@Getter
public enum RiskMode {
    // 保守
    CONSERVATIVE("conservative"),
    // 平衡
    BALANCED("balanced"),
    // 激进
    AGGRESSIVE("aggressive");

    private final String value;

    RiskMode(String value) {
        this.value = value;
    }
}