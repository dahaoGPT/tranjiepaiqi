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
    /**
     * 根据设备ID查询设备。
     * 
     * @param id 设备ID
     * @return 设备对象
     */
    @Select("SELECT * FROM devices WHERE id = #{id}")
    Device findById(UUID id);

    /**
     * 根据设备序列号查询设备。
     * 
     * @param serialNumber 设备序列号
     * @return 设备对象
     */
    @Select("SELECT * FROM devices WHERE serial_number = #{serialNumber}")
    Device findBySerialNumber(String serialNumber);

    /**
     * 根据老人ID查询绑定的设备列表。
     * 
     * @param elderId 老人ID
     * @return 设备列表
     */
    @Select("SELECT * FROM devices WHERE elder_id = #{elderId}")
    List<Device> findByElderId(UUID elderId);

    /**
     * 插入设备记录。
     * 
     * @param device 设备对象
     */
    void insert(Device device);

    /**
     * 更新设备记录。
     * 
     * @param device 设备对象
     */
    void update(Device device);

    /**
     * 更新设备状态和最后在线时间。
     * 
     * @param device 设备对象，包含ID、状态和最后在线时间
     */
    @Select("UPDATE devices SET status = #{status}, last_seen_at = #{lastSeenAt} WHERE id = #{id}")
    void updateStatus(Device device);
}