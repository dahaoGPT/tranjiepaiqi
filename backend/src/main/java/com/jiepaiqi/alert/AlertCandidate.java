package com.jiepaiqi.alert;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * 异常候选对象。
 * 由规则引擎生成，表示可能需要生成的异常提醒。
 */
@Data
@Builder
@AllArgsConstructor
public class AlertCandidate {
    private AlertType type;
    private String reason;
    private String suggestedAction;
    private Instant occurredAt;
}