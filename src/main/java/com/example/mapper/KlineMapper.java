package com.example.mapper;

import com.example.entity.KlineEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlineMapper {

    /**
     * 新增单条K线数据
     */
    int insertKline(KlineEntity kline);

    /**
     * 批量新增K线数据（高效插入多条）
     */
    int batchInsertKline(@Param("klineList") List<KlineEntity> klineList);

    /**
     * 根据开盘时间查询单条K线
     */
    @Select("SELECT * FROM btcusdt_perpetual_5m_kline WHERE open_time = #{openTime}")
    KlineEntity selectByOpenTime(@Param("openTime") Long openTime);

    /**
     * 查询指定时间范围内的K线数据
     */
    List<KlineEntity> selectByTimeRange(
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);

    /**
     * 更新指定开盘时间的K线数据
     */
    int updateKline(KlineEntity kline);

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