package com.jiepaiqi.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

/**
 * 通用数据访问接口。
 */
@Mapper
public interface CommonMapper {
    @Select("SELECT CAST(#{value} AS TEXT)")
    String castToString(String value);
}