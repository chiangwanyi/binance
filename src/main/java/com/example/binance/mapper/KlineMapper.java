package com.example.binance.mapper;

import com.example.binance.entity.KlineEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlineMapper {

    /**
     * 根据开盘时间查询单条K线
     */
    @Select("SELECT * FROM btcusdt_perpetual_5m_kline WHERE open_time = #{openTime}")
    KlineEntity selectByOpenTime(@Param("openTime") Long openTime);

    /**
     * 根据开盘时间删除K线数据
     */
    @Delete("DELETE FROM btcusdt_perpetual_5m_kline WHERE open_time = #{openTime}")
    int deleteByOpenTime(@Param("openTime") Long openTime);

    /**
     * 查询所有K线数据
     */
    @Select("SELECT * FROM btcusdt_perpetual_5m_kline ORDER BY open_time ASC")
    List<KlineEntity> selectAll();
}