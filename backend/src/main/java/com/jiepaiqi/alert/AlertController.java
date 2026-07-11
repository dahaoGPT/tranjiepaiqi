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

    public AlertController(AlertMapper alertMapper, AlertNoteMapper alertNoteMapper,
            DeviceMapper deviceMapper, AudioClipMapper audioClipMapper) {
        this.alertMapper = alertMapper;
        this.alertNoteMapper = alertNoteMapper;
        this.deviceMapper = deviceMapper;
        this.audioClipMapper = audioClipMapper;
    }

    @GetMapping("/elder/{elderId}")
    public List<Alert> getAlertsByElder(@PathVariable UUID elderId) {
        return alertMapper.findByElder(elderId);
    }

    @GetMapping("/{alertId}")
    public Alert getAlert(@PathVariable UUID alertId) {
        Alert alert = alertMapper.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("异常不存在");
        }
        return alert;
    }

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