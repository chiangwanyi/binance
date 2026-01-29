package com.example.binance.mapper.btcusdt;

import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.KlineBaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BTCUSDT1hPMapper extends KlineBaseMapper {

    /**
     * 根据开盘时间查询单条K线
     */
    @Override
    @Select("SELECT * FROM btcusdt_perpetual_1h_kline WHERE open_time = #{openTime}")
    KlineEntity selectByOpenTime(@Param("openTime") Long openTime);

    /**
     * 根据开盘时间删除K线数据
     */
    @Override
    @Delete("DELETE FROM btcusdt_perpetual_1h_kline WHERE open_time = #{openTime}")
    int deleteByOpenTime(@Param("openTime") Long openTime);

    /**
     * 查询所有K线数据
     */
    @Override
    @Select("SELECT * FROM btcusdt_perpetual_1h_kline ORDER BY open_time ASC")
    List<KlineEntity> selectAll();

    /**
     * 查询指定时间范围的K线数据
     */
    @Override
    @Select("SELECT * FROM btcusdt_perpetual_1h_kline WHERE open_time >= #{startTime} AND open_time <= #{endTime} ORDER BY open_time ASC")
    List<KlineEntity> selectByTimeRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Override
    @Insert("""
            <script>
                INSERT INTO btcusdt_perpetual_1h_kline (
                    open_time,
                    open_price,high_price,low_price,close_price,
                    volume,close_time,quote_volume,trade_count,
                    taker_buy_volume,taker_buy_quote_volume
                )
                VALUES
                <foreach collection="list" item="item" separator=",">
                    (
                        #{item.openTime},
                        #{item.openPrice},#{item.highPrice},#{item.lowPrice},#{item.closePrice},
                        #{item.volume},#{item.closeTime},#{item.quoteVolume},#{item.tradeCount},
                        #{item.takerBuyVolume},#{item.takerBuyQuoteVolume}
                    )
                </foreach>
                ON CONFLICT(open_time) DO UPDATE SET
                    open_price = excluded.open_price,high_price = excluded.high_price,low_price = excluded.low_price,close_price = excluded.close_price,
                    volume = excluded.volume,close_time = excluded.close_time,quote_volume = excluded.quote_volume,trade_count = excluded.trade_count,
                    taker_buy_volume = excluded.taker_buy_volume,taker_buy_quote_volume = excluded.taker_buy_quote_volume
            </script>
            """)
    int batchUpsert(@Param("list") List<KlineEntity> list);
}