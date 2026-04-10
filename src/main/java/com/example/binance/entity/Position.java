package com.example.binance.entity;

import com.example.binance.constant.RiskMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String type;             // long/short
    private BigDecimal margin;       // 仓位保证金
    private BigDecimal entry;        // 入场价
    private BigDecimal sl;           // 止损价
    private BigDecimal tp1;          // 1R止盈价
    private BigDecimal tp2;          // 2R止盈价
    private BigDecimal qty;          // 持仓数量

    // ===== v1.1新增 =====
    private BigDecimal score; // 当前评分

    private boolean tp1Hit;   // 是否已触发1R
    private boolean closed;   // 是否已平仓

    private BigDecimal remainQty; // 剩余仓位

    // 区间统计
    private BigDecimal zoneHigh;
    private BigDecimal zoneLow;

    private RiskMode riskMode;

    @Override
    public String toString() {
        return "仓位{" +
                "type='" + type + '\'' +
                ", 入场价格=" + entry +
                ", 止损=" + sl +
                ", 仓位大小=" + qty +
                '}';
    }

    public int getDirection() {
        return type.equals("long") ? 1 : -1;
    }
}