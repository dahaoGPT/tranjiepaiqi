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
    /**
     * 根据事件ID查询用水事件。
     * @param id 事件ID
     * @return 用水事件对象
     */
    @Select("SELECT * FROM water_events WHERE id = #{id}")
    WaterEvent findById(UUID id);

    /**
     * 根据老人ID和时间范围查询用水事件，按开始时间升序排列。
     * @param elderId 老人ID
     * @param startTime 开始时间
     * @return 用水事件列表
     */
    @Select("SELECT * FROM water_events WHERE elder_id = #{elderId} AND started_at >= #{startTime} ORDER BY started_at")
    List<WaterEvent> findByElderAndTimeRange(UUID elderId, Instant startTime);

    /**
     * 查询老人最近一次用水事件。
     * @param elderId 老人ID
     * @return 最近的用水事件对象
     */
    @Select("SELECT * FROM water_events WHERE elder_id = #{elderId} ORDER BY started_at DESC LIMIT 1")
    WaterEvent findLatestByElder(UUID elderId);

    /**
     * 插入用水事件。
     * @param event 用水事件对象
     */
    void insert(WaterEvent event);

    /**
     * 批量插入用水事件。
     * @param events 用水事件列表
     */
    void batchInsert(List<WaterEvent> events);

    /**
     * 删除指定时间范围后的用水事件（用于重新聚合）。
     * @param elderId 老人ID
     * @param startTime 开始时间
     */
    void deleteByElderAndTimeRange(UUID elderId, Instant startTime);
}