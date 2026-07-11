package com.jiepaiqi.device;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 设备数据访问接口。
 */
@Mapper
public interface DeviceMapper {
    @Select("SELECT * FROM devices WHERE id = #{id}")
    Device findById(UUID id);

    @Select("SELECT * FROM devices WHERE serial_number = #{serialNumber}")
    Device findBySerialNumber(String serialNumber);

    @Select("SELECT * FROM devices WHERE elder_id = #{elderId}")
    List<Device> findByElderId(UUID elderId);

    void insert(Device device);

    void update(Device device);

    @Select("UPDATE devices SET status = #{status}, last_seen_at = #{lastSeenAt} WHERE id = #{id}")
    void updateStatus(Device device);
}