package com.jiepaiqi.elder;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 老人档案。
 * 作为设备、节奏和异常记录的核心归属对象。
 */
@Data
public class Elder {
    private UUID id;
    private String name;
    private String notes;
    private Instant createdAt;
}