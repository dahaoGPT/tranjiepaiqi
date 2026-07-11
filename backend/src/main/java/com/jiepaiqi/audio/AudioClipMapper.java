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
    /**
     * 根据音频片段ID查询音频片段。
     * @param id 音频片段ID
     * @return 音频片段对象
     */
    @Select("SELECT * FROM audio_clips WHERE id = #{id}")
    AudioClip findById(UUID id);

    /**
     * 根据设备ID查询音频片段列表，按窗口开始时间升序排列。
     * @param deviceId 设备ID
     * @return 音频片段列表
     */
    @Select("SELECT * FROM audio_clips WHERE device_id = #{deviceId} ORDER BY window_started_at")
    List<AudioClip> findByDeviceId(UUID deviceId);

    /**
     * 插入音频片段记录。
     * @param audioClip 音频片段对象
     */
    void insert(AudioClip audioClip);

    /**
     * 删除音频片段记录。
     * @param id 音频片段ID
     */
    void delete(UUID id);
}