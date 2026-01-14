package com.example.binance.entity;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineEntity {
    /** 开盘时间（毫秒级时间戳，主键） */
    private Long openTime;
    /** 开盘价 */
    private String openPrice;
    /** 最高价 */
    private String highPrice;
    /** 最低价 */
    private String lowPrice;
    /** 收盘价 */
    private String closePrice;
    /** 成交量 */
    private String volume;
    /** 收盘时间 */
    private Long closeTime;
    /** 成交额 */
    private String quoteVolume;
    /** 成交笔数 */
    private Integer tradeCount;
    /** 主动买入成交量 */
    private String takerBuyVolume;
    /** 主动买入成交额 */
    private String takerBuyQuoteVolume;

    @Override
    public String toString() {
        return "KlineEntity{" +
                "开盘时间=" + DateTime.of(openTime) +
                ", 开盘价='" + openPrice + '\'' +
                ", 最高价='" + highPrice + '\'' +
                ", 最低价='" + lowPrice + '\'' +
                ", 收盘价='" + closePrice + '\'' +'}';
    }
}