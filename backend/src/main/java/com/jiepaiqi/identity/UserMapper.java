package com.jiepaiqi.identity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(UUID id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    void insert(User user);

    void update(User user);

    @Select("SELECT elder_id FROM user_elder_bindings WHERE user_id = #{userId}")
    List<UUID> findBoundElderIds(UUID userId);
}