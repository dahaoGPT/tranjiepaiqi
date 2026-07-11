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
    /**
     * 根据异常ID查询异常详情。
     * @param id 异常ID
     * @return 异常对象
     */
    @Select("SELECT * FROM alerts WHERE id = #{id}")
    Alert findById(UUID id);

    /**
     * 根据老人ID查询异常列表，按发生时间降序排列。
     * @param elderId 老人ID
     * @return 异常列表
     */
    @Select("SELECT * FROM alerts WHERE elder_id = #{elderId} ORDER BY occurred_at DESC")
    List<Alert> findByElder(UUID elderId);

    /**
     * 根据老人ID和状态查询异常列表，按发生时间降序排列。
     * @param elderId 老人ID
     * @param status 异常状态
     * @return 异常列表
     */
    @Select("SELECT * FROM alerts WHERE elder_id = #{elderId} AND status = #{status} ORDER BY occurred_at DESC")
    List<Alert> findByElderAndStatus(UUID elderId, String status);

    /**
     * 统计老人的未处理异常数量。
     * @param elderId 老人ID
     * @return 未处理异常数量
     */
    @Select("SELECT COUNT(*) FROM alerts WHERE elder_id = #{elderId} AND status = 'OPEN'")
    int countOpenByElder(UUID elderId);

    /**
     * 插入异常记录。
     * @param alert 异常对象
     */
    void insert(Alert alert);

    /**
     * 更新异常记录。
     * @param alert 异常对象
     */
    void update(Alert alert);

    /**
     * 确认异常。
     * @param alert 异常对象，包含ID和确认时间
     */
    @Select("UPDATE alerts SET status = 'ACKNOWLEDGED', acknowledged_at = #{acknowledgedAt} WHERE id = #{id}")
    void acknowledge(Alert alert);

    /**
     * 解决异常。
     * @param alert 异常对象，包含ID和解决时间
     */
    @Select("UPDATE alerts SET status = 'RESOLVED', resolved_at = #{resolvedAt} WHERE id = #{id}")
    void resolve(Alert alert);
}