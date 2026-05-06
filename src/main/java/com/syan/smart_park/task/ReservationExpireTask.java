package com.syan.smart_park.task;

import com.syan.smart_park.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 预约自动过期定时任务
 * 每隔5分钟检查一次，将 end_time 已过的待审批/已预约记录自动标记为"已过期"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpireTask {

    private final ReservationService reservationService;

    /**
     * 每5分钟执行一次预约过期检查
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void expireReservations() {
        log.debug("开始执行预约自动过期检查...");

        try {
            List<Long> expiredIds = reservationService.expireOverdueReservations();
            if (!expiredIds.isEmpty()) {
                log.info("预约自动过期任务完成，本次过期 {} 条预约: {}", expiredIds.size(), expiredIds);
            }
        } catch (Exception e) {
            log.error("预约自动过期任务执行失败", e);
        }
    }
}
