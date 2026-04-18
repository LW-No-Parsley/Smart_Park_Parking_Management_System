package com.syan.smart_park.task;

import com.syan.smart_park.service.ParkAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 园区总车位数定时维护任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ParkAreaTotalSpacesTask {

    private final ParkAreaService parkAreaService;

    /**
     * 每天凌晨2点执行，更新所有园区的总车位数
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateAllParkAreasTotalSpaces() {
        log.info("开始执行园区总车位数定时更新任务...");
        
        try {
            int updatedCount = parkAreaService.updateAllTotalSpaces();
            log.info("园区总车位数定时更新任务完成，共更新{}个园区", updatedCount);
        } catch (Exception e) {
            log.error("园区总车位数定时更新任务执行失败", e);
        }
    }

    /**
     * 每小时执行一次，更新所有园区的总车位数（用于测试）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateAllParkAreasTotalSpacesHourly() {
        log.info("开始执行园区总车位数每小时更新任务...");
        
        try {
            int updatedCount = parkAreaService.updateAllTotalSpaces();
            log.info("园区总车位数每小时更新任务完成，共更新{}个园区", updatedCount);
        } catch (Exception e) {
            log.error("园区总车位数每小时更新任务执行失败", e);
        }
    }
}
