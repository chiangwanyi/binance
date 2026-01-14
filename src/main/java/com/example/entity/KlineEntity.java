package com.example.entity;

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

    public Long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Long openTime) {
        this.openTime = openTime;
    }

    public String getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(String openPrice) {
        this.openPrice = openPrice;
    }

    public String getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(String highPrice) {
        this.highPrice = highPrice;
    }

    public String getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(String lowPrice) {
        this.lowPrice = lowPrice;
    }

    public String getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(String closePrice) {
        this.closePrice = closePrice;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
    }

    public String getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(String quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public Integer getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Integer tradeCount) {
        this.tradeCount = tradeCount;
    }

    public String getTakerBuyVolume() {
        return takerBuyVolume;
    }

    public void setTakerBuyVolume(String takerBuyVolume) {
        this.takerBuyVolume = takerBuyVolume;
    }

    public String getTakerBuyQuoteVolume() {
        return takerBuyQuoteVolume;
    }

    public void setTakerBuyQuoteVolume(String takerBuyQuoteVolume) {
        this.takerBuyQuoteVolume = takerBuyQuoteVolume;
    }
}