package com.jiepaiqi.elder;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 老人档案数据访问接口。
 */
@Mapper
public interface ElderMapper {
    /**
     * 根据老人ID查询老人档案。
     * @param id 老人ID
     * @return 老人档案对象
     */
    @Select("SELECT * FROM elders WHERE id = #{id}")
    Elder findById(UUID id);

    /**
     * 根据用户ID查询绑定的老人列表。
     * @param userId 用户ID
     * @return 老人列表
     */
    @Select("SELECT e.* FROM elders e JOIN user_elder_bindings b ON e.id = b.elder_id WHERE b.user_id = #{userId}")
    List<Elder> findByUserId(UUID userId);

    /**
     * 插入老人档案。
     * @param elder 老人档案对象
     */
    void insert(Elder elder);

    /**
     * 更新老人档案。
     * @param elder 老人档案对象
     */
    void update(Elder elder);

    /**
     * 删除老人档案。
     * @param id 老人ID
     */
    void delete(UUID id);
}