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

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ElderDashboardResponse getDashboard(@PathVariable UUID elderId) {
        return dashboardService.getDashboard(elderId);
    }
}