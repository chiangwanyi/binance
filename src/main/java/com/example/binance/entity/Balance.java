package com.example.binance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 账户余额
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {
    // 余额
    private BigDecimal balance;

    public static Balance init(String value) {
        return new Balance(new BigDecimal(value));
    }

    public void add(BigDecimal value) {
        balance = balance.add(value);
    }

    public void sub(BigDecimal value) {
        balance = balance.subtract(value);
    }
}
