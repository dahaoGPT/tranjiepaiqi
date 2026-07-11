package com.jiepaiqi.alert;

import com.jiepaiqi.audio.AudioClip;
import com.jiepaiqi.audio.AudioClipMapper;
import com.jiepaiqi.device.Device;
import com.jiepaiqi.device.DeviceMapper;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 异常提醒控制器。
 * 处理异常查询、确认、解决和备注。
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertMapper alertMapper;
    private final AlertNoteMapper alertNoteMapper;
    private final DeviceMapper deviceMapper;
    private final AudioClipMapper audioClipMapper;

    /**
     * 构造函数。
     * 注入所需的Mapper依赖。
     * 
     * @param alertMapper     异常提醒数据访问接口
     * @param alertNoteMapper 异常备注数据访问接口
     * @param deviceMapper    设备数据访问接口
     * @param audioClipMapper 音频片段数据访问接口
     */
    public AlertController(AlertMapper alertMapper, AlertNoteMapper alertNoteMapper,
            DeviceMapper deviceMapper, AudioClipMapper audioClipMapper) {
        this.alertMapper = alertMapper;
        this.alertNoteMapper = alertNoteMapper;
        this.deviceMapper = deviceMapper;
        this.audioClipMapper = audioClipMapper;
    }

    /**
     * 根据老人ID查询异常提醒列表。
     * 
     * @param elderId 老人ID
     * @return 异常提醒列表，按发生时间降序排列
     */
    @GetMapping("/elder/{elderId}")
    public List<Alert> getAlertsByElder(@PathVariable UUID elderId) {
        return alertMapper.findByElder(elderId);
    }

    /**
     * 根据异常ID查询异常详情。
     * 
     * @param alertId 异常ID
     * @return 异常详情
     * @throws IllegalArgumentException 异常不存在时抛出
     */
    @GetMapping("/{alertId}")
    public Alert getAlert(@PathVariable UUID alertId) {
        Alert alert = alertMapper.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("异常不存在");
        }
        return alert;
    }

    /**
     * 确认异常。
     * 将异常状态从 OPEN 改为 ACKNOWLEDGED，并记录确认时间。
     * 
     * @param alertId 异常ID
     * @throws IllegalArgumentException 异常不存在时抛出
     * @throws IllegalStateException    异常已解决时抛出
     */
    @PostMapping("/{alertId}/acknowledge")
    public void acknowledgeAlert(@PathVariable UUID alertId) {
        Alert alert = alertMapper.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("异常不存在");
        }
        if ("RESOLVED".equals(alert.getStatus())) {
            throw new IllegalStateException("已解决的异常不能再次确认");
        }
        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(Instant.now());
        alertMapper.update(alert);
    }

    /**
     * 解决异常。
     * 将异常状态改为 RESOLVED，并记录解决时间。
     * 
     * @param alertId 异常ID
     * @throws IllegalArgumentException 异常不存在时抛出
     * @throws IllegalStateException    异常已解决时抛出
     */
    @PostMapping("/{alertId}/resolve")
    public void resolveAlert(@PathVariable UUID alertId) {
        Alert alert = alertMapper.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("异常不存在");
        }
        if ("RESOLVED".equals(alert.getStatus())) {
            throw new IllegalStateException("异常已解决");
        }
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(Instant.now());
        alertMapper.update(alert);
    }

    /**
     * 为异常添加备注。
     * 记录人工处理信息，如确认、联系、上门等。
     * 
     * @param alertId 异常ID
     * @param request 备注请求，包含作者ID和备注内容
     */
    @PostMapping("/{alertId}/notes")
    public void addNote(@PathVariable UUID alertId, @RequestBody NoteRequest request) {
        AlertNote note = new AlertNote();
        note.setId(UUID.randomUUID());
        note.setAlertId(alertId);
        note.setAuthorUserId(request.getAuthorUserId());
        note.setBody(request.getBody());
        note.setCreatedAt(Instant.now());
        alertNoteMapper.insert(note);
    }

    /**
     * 查询异常关联的音频片段列表。
     * 通过异常关联的设备ID查询音频片段。
     * 
     * @param alertId 异常ID
     * @return 音频片段列表，无关联设备时返回空列表
     * @throws IllegalArgumentException 异常不存在时抛出
     */
    @GetMapping("/{alertId}/audio-clips")
    public List<AudioClip> getAlertAudioClips(@PathVariable UUID alertId) {
        Alert alert = alertMapper.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("异常不存在");
        }

        if (alert.getDeviceId() == null) {
            return Collections.emptyList();
        }

        return audioClipMapper.findByDeviceId(alert.getDeviceId());
    }

    /**
     * 获取音频片段的播放数据。
     * 
     * @param audioClipId 音频片段ID
     * @return 音频文件字节数组
     * @throws IllegalArgumentException 音频片段不存在时抛出
     */
    @GetMapping("/audio-clips/{audioClipId}/playback")
    public byte[] getAudioPlayback(@PathVariable UUID audioClipId) {
        AudioClip audioClip = audioClipMapper.findById(audioClipId);
        if (audioClip == null) {
            throw new IllegalArgumentException("音频片段不存在");
        }
        return new byte[0];
    }

    @lombok.Data
    public static class NoteRequest {
        private UUID authorUserId;
        private String body;
    }
}