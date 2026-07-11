package com.jiepaiqi.rhythm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 用水事件数据访问接口。
 */
@Mapper
public interface WaterEventMapper {
    @Select("SELECT * FROM water_events WHERE id = #{id}")
    WaterEvent findById(UUID id);

    @Select("SELECT * FROM water_events WHERE elder_id = #{elderId} AND started_at >= #{startTime} ORDER BY started_at")
    List<WaterEvent> findByElderAndTimeRange(UUID elderId, Instant startTime);

    @Select("SELECT * FROM water_events WHERE elder_id = #{elderId} ORDER BY started_at DESC LIMIT 1")
    WaterEvent findLatestByElder(UUID elderId);

    void insert(WaterEvent event);

    void batchInsert(List<WaterEvent> events);

    void deleteByElderAndTimeRange(UUID elderId, Instant startTime);
}