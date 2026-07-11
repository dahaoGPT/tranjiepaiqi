package com.jiepaiqi.rhythm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 节奏基线数据访问接口。
 */
@Mapper
public interface RhythmBaselineMapper {
    @Select("SELECT * FROM rhythm_baselines WHERE elder_id = #{elderId} ORDER BY calculated_at DESC LIMIT 1")
    RhythmBaseline findLatestByElder(UUID elderId);

    @Select("SELECT * FROM rhythm_baselines WHERE elder_id = #{elderId} ORDER BY calculated_at DESC")
    List<RhythmBaseline> findByElder(UUID elderId);

    void insert(RhythmBaseline baseline);
}