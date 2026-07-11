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
    /**
     * 查询老人最新的节律基线。
     * 
     * @param elderId 老人ID
     * @return 最新的节律基线对象
     */
    @Select("SELECT * FROM rhythm_baselines WHERE elder_id = #{elderId} ORDER BY calculated_at DESC LIMIT 1")
    RhythmBaseline findLatestByElder(UUID elderId);

    /**
     * 查询老人的所有节律基线历史，按计算时间降序排列。
     * 
     * @param elderId 老人ID
     * @return 节律基线列表
     */
    @Select("SELECT * FROM rhythm_baselines WHERE elder_id = #{elderId} ORDER BY calculated_at DESC")
    List<RhythmBaseline> findByElder(UUID elderId);

    /**
     * 插入节律基线。
     * 
     * @param baseline 节律基线对象
     */
    void insert(RhythmBaseline baseline);
}