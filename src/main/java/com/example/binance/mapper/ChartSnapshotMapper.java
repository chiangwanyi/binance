package com.example.binance.mapper;

import com.example.binance.entity.ChartSnapshot;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartSnapshotMapper {
    /**
     * 插入快照
     */
    @Insert("""
            INSERT INTO chart_snapshot (
                id,
                content,
                create_time
            )
            VALUES (
                #{id},
                #{content},
                #{createTime}
            )
            """)
    int insert(ChartSnapshot snapshot);


    /**
     * 根据ID查询
     */
    @Select("""
            SELECT
                id,
                content,
                create_time AS createTime
            FROM chart_snapshot
            WHERE id = #{id}
            """)
    ChartSnapshot selectById(@Param("id") String id);


    /**
     * 查询全部（按时间倒序）
     */
    @Select("""
            SELECT
                id,
                content,
                create_time AS createTime
            FROM chart_snapshot
            ORDER BY create_time DESC
            """)
    List<ChartSnapshot> selectAll();


    /**
     * 更新内容
     */
    @Update("""
            UPDATE chart_snapshot
            SET content = #{content}
            WHERE id = #{id}
            """)
    int updateById(ChartSnapshot snapshot);


    /**
     * 删除
     */
    @Delete("""
            DELETE FROM chart_snapshot
            WHERE id = #{id}
            """)
    int deleteById(@Param("id") String id);
}