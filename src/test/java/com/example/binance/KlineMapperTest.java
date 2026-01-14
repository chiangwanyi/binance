package com.example.binance;

import com.example.entity.KlineEntity;
import com.example.mapper.KlineMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class KlineMapperTest {

    @Autowired
    private KlineMapper klineMapper;

    // 测试新增单条数据
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

    // 测试批量新增
    @Test
    public void testBatchInsert() {
        List<KlineEntity> klineList = new ArrayList<>();
        // 第一条数据
        KlineEntity k1 = new KlineEntity();
        k1.setOpenTime(1499040000000L);
        k1.setOpenPrice("0.01634790");
        k1.setHighPrice("0.80000000");
        k1.setLowPrice("0.01575800");
        k1.setClosePrice("0.01577100");
        k1.setVolume("148976.11427815");
        k1.setCloseTime(1499644799999L);
        k1.setQuoteVolume("2434.19055334");
        k1.setTradeCount(308);
        k1.setTakerBuyVolume("1756.87402397");
        k1.setTakerBuyQuoteVolume("28.46694368");

        // 第二条数据（模拟下一个5m K线）
        KlineEntity k2 = new KlineEntity();
        k2.setOpenTime(1499040300000L);
        k2.setOpenPrice("0.01577100");
        k2.setHighPrice("0.01580000");
        k2.setLowPrice("0.01570000");
        k2.setClosePrice("0.01578000");
        k2.setVolume("100000.00000000");
        k2.setCloseTime(1499645099999L);
        k2.setQuoteVolume("2000.00000000");
        k2.setTradeCount(200);
        k2.setTakerBuyVolume("80000.00000000");
        k2.setTakerBuyQuoteVolume("20.00000000");

        klineList.add(k1);
        klineList.add(k2);

        int rows = klineMapper.batchInsertKline(klineList);
        System.out.println("批量新增行数：" + rows);
    }

    // 测试查询单条数据
    @Test
    public void testSelectByOpenTime() {
        KlineEntity kline = klineMapper.selectByOpenTime(1499040000000L);
        System.out.println("查询结果：" + kline);
    }

    // 测试查询时间范围数据
    @Test
    public void testSelectByTimeRange() {
        List<KlineEntity> list = klineMapper.selectByTimeRange(1499040000000L, 1499040300000L);
        System.out.println("时间范围内的K线数量：" + list.size());
        list.forEach(System.out::println);
    }

    // 测试更新数据
    @Test
    public void testUpdateKline() {
        KlineEntity kline = new KlineEntity();
        kline.setOpenTime(1499040000000L);
        kline.setClosePrice("0.01580000");  // 修改收盘价
        kline.setVolume("150000.00000000"); // 修改成交量

        int rows = klineMapper.updateKline(kline);
        System.out.println("更新行数：" + rows);
    }

    // 测试删除数据
    @Test
    public void testDeleteByOpenTime() {
        int rows = klineMapper.deleteByOpenTime(1499040000000L);
        System.out.println("删除行数：" + rows);
    }
}