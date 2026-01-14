package com.example.binance;

import com.example.entity.KlineEntity;
import com.example.mapper.KlineMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // 仅需这一个注解即可
public class KlineMapperTest {

    @Autowired
    private KlineMapper klineMapper;

    @Test
    public void testInsertKline() {
        KlineEntity kline = new KlineEntity();
        kline.setOpenTime(1499040000000L);
        kline.setOpenPrice("0.01634790");
        kline.setHighPrice("0.80000000");
        kline.setLowPrice("0.01575800");
        kline.setClosePrice("0.01577100");
        kline.setVolume("148976.11427815");
        kline.setCloseTime(1499644799999L);
        kline.setQuoteVolume("2434.19055334");
        kline.setTradeCount(308);
        kline.setTakerBuyVolume("1756.87402397");
        kline.setTakerBuyQuoteVolume("28.46694368");

        int rows = klineMapper.insertKline(kline);
        System.out.println("新增行数：" + rows);
    }
}