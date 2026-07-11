package com.jiepaiqi.alert;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * 异常备注数据访问接口。
 */
@Mapper
public interface AlertNoteMapper {
    @Select("SELECT * FROM alert_notes WHERE alert_id = #{alertId} ORDER BY created_at DESC")
    List<AlertNote> findByAlert(UUID alertId);

    void insert(AlertNote note);
}