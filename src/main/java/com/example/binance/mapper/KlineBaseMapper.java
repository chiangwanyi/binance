package com.example.binance.mapper;

import com.example.binance.entity.KlineEntity;

import java.util.List;

public interface KlineBaseMapper {
    /**
     * 根据开盘时间查询单条K线
     */
    KlineEntity selectByOpenTime(Long openTime);

    /**
     * 根据开盘时间删除K线数据
     */
    int deleteByOpenTime(Long openTime);

    /**
     * 查询所有K线数据
     */
    List<KlineEntity> selectAll();
    /**
     * 按时间范围查询K线数据
     */
    List<KlineEntity> selectByTimeRange(Long startTime, Long endTime);

    /**
     * 批量插入/更新K线数据
     */
    int batchUpsert(List<KlineEntity> klineEntities);
}
