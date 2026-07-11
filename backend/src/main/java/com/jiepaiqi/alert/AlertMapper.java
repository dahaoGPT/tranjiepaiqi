package com.jiepaiqi.alert;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 异常提醒数据访问接口。
 */
@Mapper
public interface AlertMapper {
    @Select("SELECT * FROM alerts WHERE id = #{id}")
    Alert findById(UUID id);

    @Select("SELECT * FROM alerts WHERE elder_id = #{elderId} ORDER BY occurred_at DESC")
    List<Alert> findByElder(UUID elderId);

    @Select("SELECT * FROM alerts WHERE elder_id = #{elderId} AND status = #{status} ORDER BY occurred_at DESC")
    List<Alert> findByElderAndStatus(UUID elderId, String status);

    @Select("SELECT COUNT(*) FROM alerts WHERE elder_id = #{elderId} AND status = 'OPEN'")
    int countOpenByElder(UUID elderId);

    void insert(Alert alert);

    void update(Alert alert);

    @Select("UPDATE alerts SET status = 'ACKNOWLEDGED', acknowledged_at = #{acknowledgedAt} WHERE id = #{id}")
    void acknowledge(Alert alert);

    @Select("UPDATE alerts SET status = 'RESOLVED', resolved_at = #{resolvedAt} WHERE id = #{id}")
    void resolve(Alert alert);
}