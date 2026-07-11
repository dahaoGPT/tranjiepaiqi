package com.jiepaiqi.dashboard;

import com.jiepaiqi.dashboard.dto.ElderDashboardResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 看板控制器。
 * 提供移动端优先的聚合 API。
 */
@RestController
@RequestMapping("/api/elders/{elderId}/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * 构造函数。
     * 注入看板服务依赖。
     * 
     * @param dashboardService 看板服务实例
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取老人看板数据。
     * 返回包含老人状态、设备状态、今日用水时间线和待处理异常的聚合数据。
     * 
     * @param elderId 老人ID
     * @return 看板响应数据
     */
    @GetMapping
    public ElderDashboardResponse getDashboard(@PathVariable UUID elderId) {
        return dashboardService.getDashboard(elderId);
    }
}