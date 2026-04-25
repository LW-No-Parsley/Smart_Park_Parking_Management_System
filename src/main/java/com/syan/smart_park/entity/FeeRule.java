package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车计费规则实体类
 * 对应数据库表：parking_fee_rule
 */
@Data
@TableName("parking_fee_rule")
public class FeeRule {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 园区ID（NULL表示全局规则）
     */
    private Long parkAreaId;

    /**
     * 车辆类型（1-小型车，2-大型车，NULL表示所有类型）
     */
    private Integer vehicleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 计费模式：1-按时间段计费，2-按次计费，3-阶梯计费
     */
    private Integer feeMode;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;
}
