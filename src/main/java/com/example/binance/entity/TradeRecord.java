package com.example.binance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRecord {
    private BigDecimal entry;    // 入场价
    private BigDecimal exit;     // 出场价
    private BigDecimal pnl;      // 盈亏（扣除手续费）
    private String reason;       // 平仓原因
}