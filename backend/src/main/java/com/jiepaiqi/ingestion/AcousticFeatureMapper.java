package com.jiepaiqi.ingestion;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 声学特征数据访问接口。
 */
@Mapper
public interface AcousticFeatureMapper {
    /**
     * 根据ID查询声学特征。
     * 
     * @param id 特征ID
     * @return 声学特征对象
     */
    @Select("SELECT * FROM acoustic_features WHERE id = #{id}")
    AcousticFeature findById(UUID id);

    /**
     * 根据设备ID和时间范围查询声学特征，按窗口开始时间升序排列。
     * 
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @return 声学特征列表
     */
    @Select("SELECT * FROM acoustic_features WHERE device_id = #{deviceId} AND window_started_at >= #{startTime} ORDER BY window_started_at")
    List<AcousticFeature> findByDeviceAndTimeRange(UUID deviceId, Instant startTime);

    /**
     * 插入声学特征。
     * 
     * @param feature 声学特征对象
     */
    void insert(AcousticFeature feature);

    /**
     * 批量插入声学特征。
     * 
     * @param features 声学特征列表
     */
    void batchInsert(List<AcousticFeature> features);

    /**
     * 删除指定时间范围内的声学特征（用于重新测试）。
     * 
     * @param deviceId  设备ID
     * @param startTime 开始时间
     */
    void deleteByDeviceAndTimeRange(UUID deviceId, Instant startTime);
}