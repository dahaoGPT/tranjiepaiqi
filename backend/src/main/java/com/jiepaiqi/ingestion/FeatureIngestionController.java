package com.jiepaiqi.ingestion;

import com.jiepaiqi.ingestion.dto.FeatureIngestionRequest;
import com.jiepaiqi.ingestion.dto.FeatureIngestionResponse;
import com.jiepaiqi.device.DeviceMapper;
import com.jiepaiqi.device.Device;
import com.jiepaiqi.rhythm.WaterEventAggregator;
import com.jiepaiqi.rhythm.WaterEvent;
import com.jiepaiqi.rhythm.WaterEventMapper;
import com.jiepaiqi.rhythm.AcousticFeatureWindow;
import com.jiepaiqi.rhythm.RhythmBaseline;
import com.jiepaiqi.rhythm.RhythmBaselineMapper;
import com.jiepaiqi.alert.AlertRuleEngine;
import com.jiepaiqi.alert.AlertCandidate;
import com.jiepaiqi.alert.AlertMapper;
import com.jiepaiqi.alert.Alert;
import com.jiepaiqi.alert.AlertType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 声学特征接收控制器。
 * 接收设备上报的声学特征数据，聚合为用水事件，并检查告警规则。
 */
@RestController
@RequestMapping("/api/devices/{deviceId}/features")
public class FeatureIngestionController {
    private final AcousticFeatureMapper featureMapper;
    private final DeviceMapper deviceMapper;
    private final WaterEventMapper waterEventMapper;
    private final AlertMapper alertMapper;
    private final RhythmBaselineMapper baselineMapper;
    private final WaterEventAggregator aggregator;
    private final AlertRuleEngine alertRuleEngine;

    /**
     * 构造函数。
     * 注入所需的Mapper依赖，并初始化用水事件聚合器和异常规则引擎。
     * 
     * @param featureMapper    声学特征数据访问接口
     * @param deviceMapper     设备数据访问接口
     * @param waterEventMapper 用水事件数据访问接口
     * @param alertMapper      异常提醒数据访问接口
     * @param baselineMapper   节律基线数据访问接口
     */
    public FeatureIngestionController(AcousticFeatureMapper featureMapper, DeviceMapper deviceMapper,
            WaterEventMapper waterEventMapper, AlertMapper alertMapper,
            RhythmBaselineMapper baselineMapper) {
        this.featureMapper = featureMapper;
        this.deviceMapper = deviceMapper;
        this.waterEventMapper = waterEventMapper;
        this.alertMapper = alertMapper;
        this.baselineMapper = baselineMapper;
        this.aggregator = new WaterEventAggregator(0.80, 15);
        this.alertRuleEngine = new AlertRuleEngine();
    }

    /**
     * 接收声学特征数据。
     * 验证设备和特征数据，存储有效特征，并触发用水事件聚合和异常规则检查。
     * 
     * @param deviceId 设备序列号
     * @param request  特征接收请求，包含多个特征数据
     * @return 接收响应，包含接受和拒绝的特征数量
     */
    @PostMapping
    public FeatureIngestionResponse ingestFeatures(@PathVariable String deviceId,
            @RequestBody FeatureIngestionRequest request) {
        int accepted = 0;
        int rejected = 0;
        List<AcousticFeatureWindow> windows = new ArrayList<>();

        Device device = deviceMapper.findBySerialNumber(deviceId);
        if (device == null) {
            return FeatureIngestionResponse.builder()
                    .acceptedCount(0)
                    .rejectedCount(request.getFeatures().size())
                    .build();
        }

        for (FeatureIngestionRequest.FeatureDto dto : request.getFeatures()) {
            if (isValidFeature(dto)) {
                AcousticFeature feature = new AcousticFeature();
                feature.setId(UUID.randomUUID());
                feature.setDeviceId(device.getId());
                feature.setAudioClipId(dto.getAudioClipId());
                feature.setWindowStartedAt(dto.getWindowStartedAt());
                feature.setWindowEndedAt(dto.getWindowEndedAt());
                feature.setAverageDecibels(dto.getAverageDecibels());
                feature.setPeakDecibels(dto.getPeakDecibels());
                feature.setLowBandEnergy(dto.getLowBandEnergy());
                feature.setMidBandEnergy(dto.getMidBandEnergy());
                feature.setHighBandEnergy(dto.getHighBandEnergy());
                feature.setFlowConfidence(dto.getFlowConfidence());
                feature.setCreatedAt(Instant.now());

                featureMapper.insert(feature);

                AcousticFeatureWindow window = new AcousticFeatureWindow();
                window.setWindowStartedAt(dto.getWindowStartedAt());
                window.setWindowEndedAt(dto.getWindowEndedAt());
                window.setFlowConfidence(dto.getFlowConfidence());
                windows.add(window);

                accepted++;
            } else {
                rejected++;
            }
        }

        device.setStatus("ONLINE");
        device.setLastSeenAt(Instant.now());
        deviceMapper.updateStatus(device);

        processWaterEventsAndAlerts(device, windows);

        return FeatureIngestionResponse.builder()
                .acceptedCount(accepted)
                .rejectedCount(rejected)
                .build();
    }

    /**
     * 处理用水事件和异常检测。
     * 从今日所有声学特征聚合用水事件，并检查异常规则。
     * 
     * @param device  设备对象
     * @param windows 新接收的特征窗口列表
     */
    private void processWaterEventsAndAlerts(Device device, List<AcousticFeatureWindow> windows) {
        Instant todayStart = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        List<AcousticFeature> todayFeatures = featureMapper.findByDeviceAndTimeRange(device.getId(), todayStart);

        List<AcousticFeatureWindow> allWindows = new ArrayList<>();
        for (AcousticFeature feature : todayFeatures) {
            AcousticFeatureWindow window = new AcousticFeatureWindow();
            window.setWindowStartedAt(feature.getWindowStartedAt());
            window.setWindowEndedAt(feature.getWindowEndedAt());
            window.setFlowConfidence(feature.getFlowConfidence());
            allWindows.add(window);
        }

        waterEventMapper.deleteByElderAndTimeRange(device.getElderId(), todayStart);

        List<WaterEvent> events = aggregator.aggregate(allWindows);
        if (!events.isEmpty()) {
            for (WaterEvent event : events) {
                event.setId(UUID.randomUUID());
                event.setElderId(device.getElderId());
                event.setDeviceId(device.getId());
                event.setCreatedAt(Instant.now());
                waterEventMapper.insert(event);
            }
        }

        List<WaterEvent> todayEvents = waterEventMapper.findByElderAndTimeRange(device.getElderId(), todayStart);

        RhythmBaseline baseline = baselineMapper.findLatestByElder(device.getElderId());
        if (baseline == null) {
            baseline = createDefaultBaseline(device.getElderId());
        }

        List<AlertCandidate> morningAlerts = alertRuleEngine.evaluateMorning(Instant.now(), baseline, todayEvents);
        List<AlertCandidate> longFlowAlerts = alertRuleEngine.evaluateLongFlow(todayEvents, 20 * 60);
        List<AlertCandidate> lowActivityAlerts = alertRuleEngine.evaluateLowDailyActivity(todayEvents, baseline);

        saveAlerts(device.getElderId(), device.getId(), morningAlerts);
        saveAlerts(device.getElderId(), device.getId(), longFlowAlerts);
        saveAlerts(device.getElderId(), device.getId(), lowActivityAlerts);
    }

    /**
     * 保存异常提醒。
     * 检查是否存在重复的同类型OPEN状态异常，避免重复告警。
     * 
     * @param elderId    老人ID
     * @param deviceId   设备ID
     * @param candidates 异常候选列表
     */
    private void saveAlerts(UUID elderId, UUID deviceId, List<AlertCandidate> candidates) {
        for (AlertCandidate candidate : candidates) {
            if (!hasDuplicateAlert(elderId, candidate.getType())) {
                Alert alert = new Alert();
                alert.setId(UUID.randomUUID());
                alert.setElderId(elderId);
                alert.setDeviceId(deviceId);
                alert.setType(candidate.getType().name());
                alert.setStatus("OPEN");
                alert.setReason(candidate.getReason());
                alert.setSuggestedAction(candidate.getSuggestedAction());
                alert.setOccurredAt(candidate.getOccurredAt());
                alert.setCreatedAt(Instant.now());
                alertMapper.insert(alert);
            }
        }
    }

    /**
     * 检查是否存在重复的异常提醒。
     * 
     * @param elderId 老人ID
     * @param type    异常类型
     * @return true表示存在重复，false表示不存在
     */
    private boolean hasDuplicateAlert(UUID elderId, AlertType type) {
        List<Alert> existingAlerts = alertMapper.findByElderAndStatus(elderId, "OPEN");
        return existingAlerts.stream()
                .anyMatch(a -> a.getType().equals(type.name()));
    }

    /**
     * 创建默认节律基线。
     * 当老人没有基线时使用默认值创建，晨间窗口为6:30-9:00，日均5次，时长900秒。
     * 
     * @param elderId 老人ID
     * @return 创建的节律基线对象
     */
    private RhythmBaseline createDefaultBaseline(UUID elderId) {
        RhythmBaseline baseline = new RhythmBaseline();
        baseline.setId(UUID.randomUUID());
        baseline.setElderId(elderId);
        baseline.setCalculatedAt(Instant.now());
        baseline.setMorningWindowStart(java.time.LocalTime.of(6, 30));
        baseline.setMorningWindowEnd(java.time.LocalTime.of(9, 0));
        baseline.setAverageDailyEventCount(5.0);
        baseline.setAverageDailyDurationSeconds(900.0);
        baselineMapper.insert(baseline);
        return baseline;
    }

    /**
     * 验证特征数据有效性。
     * 检查时间窗口和置信度是否有效。
     * 
     * @param dto 特征数据传输对象
     * @return true表示有效，false表示无效
     */
    private boolean isValidFeature(FeatureIngestionRequest.FeatureDto dto) {
        if (dto.getWindowStartedAt() == null || dto.getWindowEndedAt() == null) {
            return false;
        }
        if (dto.getWindowEndedAt().isBefore(dto.getWindowStartedAt())) {
            return false;
        }
        if (dto.getFlowConfidence() == null || dto.getFlowConfidence() < 0.0 || dto.getFlowConfidence() > 1.0) {
            return false;
        }
        return dto.getAverageDecibels() != null && dto.getPeakDecibels() != null;
    }

    /**
     * 清除指定设备的今日测试数据（仅用于测试）。
     * 
     * @param deviceId 设备序列号
     * @return 清除结果
     */
    @DeleteMapping("/reset")
    public String resetTestData(@PathVariable String deviceId) {
        Device device = deviceMapper.findBySerialNumber(deviceId);
        if (device == null) {
            return "设备不存在";
        }

        Instant todayStart = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        featureMapper.deleteByDeviceAndTimeRange(device.getId(), todayStart);
        waterEventMapper.deleteByElderAndTimeRange(device.getElderId(), todayStart);

        return "已清除今日数据";
    }
}