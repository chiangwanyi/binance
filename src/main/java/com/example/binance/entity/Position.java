package com.example.binance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String type;             // long/short
    private BigDecimal entry;        // 入场价
    private BigDecimal sl;           // 止损价
    private BigDecimal tp1;          // 1R止盈价
    private BigDecimal tp2;          // 2R止盈价
    private BigDecimal qty;          // 持仓数量
    private LocalDateTime entryTime; // 入场时间
    private BigDecimal R;            // 风险R值
    private boolean halfClosed;      // 是否半仓平仓
    private BigDecimal maxMfe;       // 最大浮盈（R倍数）
    private boolean locked;          // 趋势锁定
}