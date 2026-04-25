package com.syan.smart_park.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车费用计算结果
 */
@Data
public class FeeCalculationResult {

    /**
     * 使用的计费规则ID
     */
    private Long ruleId;

    /**
     * 使用的计费规则名称
     */
    private String ruleName;

    /**
     * 计费模式
     */
    private Integer feeMode;

    /**
     * 计费模式名称
     */
    private String feeModeName;

    /**
     * 园区ID
     */
    private Long parkAreaId;

    /**
     * 车辆类型
     */
    private Integer vehicleType;

    /**
     * 停车开始时间
     */
    private LocalDateTime startTime;

    /**
     * 停车结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总停车时长（分钟）
     */
    private Long totalMinutes;

    /**
     * 免费时长（分钟）
     */
    private Integer freeMinutes;

    /**
     * 计费时长（分钟）
     */
    private Long chargeableMinutes;

    /**
     * 总费用
     */
    private BigDecimal totalFee;

    /**
     * 每日封顶金额
     */
    private BigDecimal dailyCap;

    /**
     * 是否达到封顶
     */
    private Boolean capped;

    /**
     * 费用明细列表
     */
    private List<FeeDetailItem> details;

    /**
     * 费用明细项
     */
    @Data
    public static class FeeDetailItem {
        /**
         * 描述，如"08:00-20:00 时段"、"第1-2小时"
         */
        private String description;

        /**
         * 时长（分钟）
         */
        private Long durationMinutes;

        /**
         * 单价
         */
        private BigDecimal unitPrice;

        /**
         * 小计金额
         */
        private BigDecimal subtotal;
    }
}
