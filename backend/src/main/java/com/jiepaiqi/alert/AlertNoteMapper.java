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
    /**
     * 根据异常ID查询备注列表，按创建时间降序排列。
     * @param alertId 异常ID
     * @return 备注列表
     */
    @Select("SELECT * FROM alert_notes WHERE alert_id = #{alertId} ORDER BY created_at DESC")
    List<AlertNote> findByAlert(UUID alertId);

    /**
     * 插入异常备注。
     * @param note 备注对象
     */
    void insert(AlertNote note);
}