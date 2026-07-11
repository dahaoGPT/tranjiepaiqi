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
    @Select("SELECT * FROM elders WHERE id = #{id}")
    Elder findById(UUID id);

    @Select("SELECT e.* FROM elders e JOIN user_elder_bindings b ON e.id = b.elder_id WHERE b.user_id = #{userId}")
    List<Elder> findByUserId(UUID userId);

    void insert(Elder elder);

    void update(Elder elder);

    void delete(UUID id);
}