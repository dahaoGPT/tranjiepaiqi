package com.jiepaiqi.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * UUID 类型处理器。
 * 用于 MyBatis 在 Java UUID 类型和数据库 UUID 类型之间进行转换。
 */
@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {

    /**
     * 设置非空参数到 PreparedStatement。
     * @param ps PreparedStatement 对象
     * @param i 参数索引
     * @param parameter UUID 参数值
     * @param jdbcType JDBC 类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    /**
     * 根据列名从 ResultSet 获取 UUID 值。
     * @param rs ResultSet 对象
     * @param columnName 列名
     * @return UUID 值，如果列为空则返回 null
     */
    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return convertToUUID(obj);
    }

    /**
     * 根据列索引从 ResultSet 获取 UUID 值。
     * @param rs ResultSet 对象
     * @param columnIndex 列索引
     * @return UUID 值，如果列为空则返回 null
     */
    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return convertToUUID(obj);
    }

    /**
     * 根据列索引从 CallableStatement 获取 UUID 值。
     * @param cs CallableStatement 对象
     * @param columnIndex 列索引
     * @return UUID 值，如果列为空则返回 null
     */
    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return convertToUUID(obj);
    }

    /**
     * 将数据库返回的对象转换为 UUID。
     * 支持 UUID 对象、String 字符串和其他可转为字符串的对象。
     * @param obj 数据库返回的对象
     * @return UUID 值
     */
    private UUID convertToUUID(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof UUID) {
            return (UUID) obj;
        }
        if (obj instanceof String) {
            return UUID.fromString((String) obj);
        }
        return UUID.fromString(obj.toString());
    }
}