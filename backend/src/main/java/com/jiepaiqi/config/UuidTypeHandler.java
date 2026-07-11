package com.jiepaiqi.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return convertToUUID(obj);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return convertToUUID(obj);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return convertToUUID(obj);
    }

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