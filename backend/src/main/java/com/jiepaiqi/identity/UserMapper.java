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
    /**
     * 根据用户ID查询用户。
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(UUID id);

    /**
     * 根据用户名查询用户。
     * 
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 插入用户记录。
     * 
     * @param user 用户对象
     */
    void insert(User user);

    /**
     * 更新用户记录。
     * 
     * @param user 用户对象
     */
    void update(User user);

    /**
     * 查询用户绑定的老人ID列表。
     * 
     * @param userId 用户ID
     * @return 老人ID列表
     */
    @Select("SELECT elder_id FROM user_elder_bindings WHERE user_id = #{userId}")
    List<UUID> findBoundElderIds(UUID userId);
}