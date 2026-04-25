package com.syan.smart_park.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车计费规则DTO
 */
@Data
public class FeeRuleDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 园区ID（NULL表示全局规则）
     */
    private Long parkAreaId;

    /**
     * 园区名称（展示用）
     */
    private String parkAreaName;

    /**
     * 车辆类型（1-小型车，2-大型车，NULL表示所有类型）
     */
    private Integer vehicleType;

    /**
     * 车辆类型名称（展示用）
     */
    private String vehicleTypeName;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 计费模式：1-按时间段计费，2-按次计费，3-阶梯计费
     */
    private Integer feeMode;

    /**
     * 计费模式名称（展示用）
     */
    private String feeModeName;

    /**
     * 免费时长（分钟）
     */
    private Integer freeMinutes;

    /**
     * 计费单位时长（分钟），如20
     */
    private Integer unitDuration;

    /**
     * 每单位时长价格，如4.00
     */
    private BigDecimal unitPrice;

    /**
     * 每日封顶金额（NULL表示不封顶）
     */
    private BigDecimal dailyCap;

    /**
     * 最大计费小时数（超过后不再计费）
     */
    private Integer maxChargeHours;

    /**
     * 时段定价（JSON格式）
     */
    private String timePeriods;

    /**
     * 阶梯定价（JSON格式）
     */
    private String tieredPricing;

    /**
     * 按次计费的固定价格
     */
    private BigDecimal fixedPrice;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序（数字越小优先级越高）
     */
    private Integer sortOrder;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 静态方法：从FeeRule实体转换为FeeRuleDTO
     */
    public static FeeRuleDTO fromFeeRule(FeeRule feeRule) {
        if (feeRule == null) {
            return null;
        }

        FeeRuleDTO dto = new FeeRuleDTO();
        dto.setId(feeRule.getId());
        dto.setParkAreaId(feeRule.getParkAreaId());
        dto.setVehicleType(feeRule.getVehicleType());
        dto.setRuleName(feeRule.getRuleName());
        dto.setFeeMode(feeRule.getFeeMode());
        dto.setFreeMinutes(feeRule.getFreeMinutes());
        dto.setUnitDuration(feeRule.getUnitDuration());
        dto.setUnitPrice(feeRule.getUnitPrice());
        dto.setDailyCap(feeRule.getDailyCap());
        dto.setMaxChargeHours(feeRule.getMaxChargeHours());
        dto.setTimePeriods(feeRule.getTimePeriods());
        dto.setTieredPricing(feeRule.getTieredPricing());
        dto.setFixedPrice(feeRule.getFixedPrice());
        dto.setStatus(feeRule.getStatus());
        dto.setSortOrder(feeRule.getSortOrder());
        dto.setRemark(feeRule.getRemark());
        dto.setCreateTime(feeRule.getCreateTime());
        dto.setUpdateTime(feeRule.getUpdateTime());
        dto.setCreateBy(feeRule.getCreateBy());
        dto.setUpdateBy(feeRule.getUpdateBy());

        // 设置展示名称
        if (feeRule.getFeeMode() != null) {
            switch (feeRule.getFeeMode()) {
                case 1: dto.setFeeModeName("按时间段计费"); break;
                case 2: dto.setFeeModeName("按次计费"); break;
                case 3: dto.setFeeModeName("阶梯计费"); break;
            }
        }
        if (feeRule.getVehicleType() != null) {
            switch (feeRule.getVehicleType()) {
                case 1: dto.setVehicleTypeName("小型车"); break;
                case 2: dto.setVehicleTypeName("大型车"); break;
            }
        }

        return dto;
    }

    /**
     * 转换为FeeRule实体
     */
    public FeeRule toFeeRule() {
        FeeRule feeRule = new FeeRule();
        feeRule.setId(this.id);
        feeRule.setParkAreaId(this.parkAreaId);
        feeRule.setVehicleType(this.vehicleType);
        feeRule.setRuleName(this.ruleName);
        feeRule.setFeeMode(this.feeMode);
        feeRule.setFreeMinutes(this.freeMinutes);
        feeRule.setUnitDuration(this.unitDuration);
        feeRule.setUnitPrice(this.unitPrice);
        feeRule.setDailyCap(this.dailyCap);
        feeRule.setMaxChargeHours(this.maxChargeHours);
        feeRule.setTimePeriods(this.timePeriods);
        feeRule.setTieredPricing(this.tieredPricing);
        feeRule.setFixedPrice(this.fixedPrice);
        feeRule.setStatus(this.status);
        feeRule.setSortOrder(this.sortOrder);
        feeRule.setRemark(this.remark);
        feeRule.setCreateBy(this.createBy);
        feeRule.setUpdateBy(this.updateBy);
        return feeRule;
    }
}
