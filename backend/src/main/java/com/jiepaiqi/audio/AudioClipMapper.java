package com.jiepaiqi.audio;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 音频片段元数据访问接口。
 */
@Mapper
public interface AudioClipMapper {
    @Select("SELECT * FROM audio_clips WHERE id = #{id}")
    AudioClip findById(UUID id);

    @Select("SELECT * FROM audio_clips WHERE device_id = #{deviceId} ORDER BY window_started_at")
    List<AudioClip> findByDeviceId(UUID deviceId);

    void insert(AudioClip audioClip);

    void delete(UUID id);
}