package com.example.binance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartSnapshot {
    private String id;
    private String content;
    private long createTime;
}
