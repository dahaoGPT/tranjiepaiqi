package com.jiepaiqi.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

/**
 * 通用数据访问接口。
 */
@Mapper
public interface CommonMapper {
    /**
     * 将值转换为字符串类型。
     * 
     * @param value 待转换的值
     * @return 转换后的字符串
     */
    @Select("SELECT CAST(#{value} AS TEXT)")
    String castToString(String value);
}