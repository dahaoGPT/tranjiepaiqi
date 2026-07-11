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
    @Select("SELECT * FROM acoustic_features WHERE id = #{id}")
    AcousticFeature findById(UUID id);

    @Select("SELECT * FROM acoustic_features WHERE device_id = #{deviceId} AND window_started_at >= #{startTime} ORDER BY window_started_at")
    List<AcousticFeature> findByDeviceAndTimeRange(UUID deviceId, Instant startTime);

    void insert(AcousticFeature feature);

    void batchInsert(List<AcousticFeature> features);
}