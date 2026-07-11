package com.jiepaiqi.dashboard;

import com.jiepaiqi.dashboard.dto.ElderDashboardResponse;
import com.jiepaiqi.elder.Elder;
import com.jiepaiqi.elder.ElderMapper;
import com.jiepaiqi.device.Device;
import com.jiepaiqi.device.DeviceMapper;
import com.jiepaiqi.rhythm.WaterEvent;
import com.jiepaiqi.rhythm.WaterEventMapper;
import com.jiepaiqi.alert.Alert;
import com.jiepaiqi.alert.AlertMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 看板服务。
 * 聚合老人状态、设备状态、节奏时间线和异常提醒。
 */
@Service
public class DashboardService {
    private final ElderMapper elderMapper;
    private final DeviceMapper deviceMapper;
    private final WaterEventMapper waterEventMapper;
    private final AlertMapper alertMapper;

    public DashboardService(ElderMapper elderMapper, DeviceMapper deviceMapper,
                           WaterEventMapper waterEventMapper, AlertMapper alertMapper) {
        this.elderMapper = elderMapper;
        this.deviceMapper = deviceMapper;
        this.waterEventMapper = waterEventMapper;
        this.alertMapper = alertMapper;
    }

    public ElderDashboardResponse getDashboard(UUID elderId) {
        Elder elder = elderMapper.findById(elderId);
        if (elder == null) {
            throw new IllegalArgumentException("老人不存在");
        }

        List<Device> devices = deviceMapper.findByElderId(elderId);
        String deviceStatus = devices.isEmpty() ? "OFFLINE" : 
            devices.stream().anyMatch(d -> "ONLINE".equals(d.getStatus())) ? "ONLINE" : "OFFLINE";

        Instant todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        List<WaterEvent> todayEvents = waterEventMapper.findByElderAndTimeRange(elderId, todayStart);
        
        WaterEvent latestEvent = waterEventMapper.findLatestByElder(elderId);
        
        int openAlertCount = alertMapper.countOpenByElder(elderId);
        List<Alert> openAlerts = alertMapper.findByElderAndStatus(elderId, "OPEN");

        String todayStatus = openAlerts.stream()
            .anyMatch(a -> !"DEVICE_OFFLINE".equals(a.getType())) ? "ATTENTION" : "NORMAL";

        return ElderDashboardResponse.builder()
            .elderName(elder.getName())
            .todayStatus(todayStatus)
            .lastWaterEventAt(latestEvent != null ? latestEvent.getStartedAt() : null)
            .deviceStatus(deviceStatus)
            .openAlertCount(openAlertCount)
            .rhythmTimeline(todayEvents.stream()
                .map(e -> ElderDashboardResponse.TimelineItem.builder()
                    .time(e.getStartedAt())
                    .type("water")
                    .build())
                .collect(Collectors.toList()))
            .openAlerts(openAlerts.stream()
                .map(a -> ElderDashboardResponse.AlertSummary.builder()
                    .id(a.getId().toString())
                    .type(a.getType())
                    .reason(a.getReason())
                    .occurredAt(a.getOccurredAt())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}