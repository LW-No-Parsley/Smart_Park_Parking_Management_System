package com.syan.smart_park.entity;

import lombok.Data;

/**
 * 闸机出入场处理结果DTO
 */
@Data
public class GateAccessResult {

    /**
     * 是否允许通行
     */
    private Boolean allowed;

    /**
     * 通行记录ID（access_log.id）
     */
    private Long accessLogId;

    /**
     * 关联车辆ID
     */
    private Long vehicleId;

    /**
     * 关联用户ID（park_user.id）
     */
    private Long userId;

    /**
     * 车辆类型：1-小车，2-大车，3-新能源车
     */
    private Integer vehicleType;

    /**
     * 是否是默认车辆（业主）
     */
    private Boolean isDefaultVehicle;

    /**
     * 是否有预约
     */
    private Boolean hasReservation;

    /**
     * 预约信息（如果有）
     */
    private ReservationDTO reservation;

    /**
     * 提示消息（如：欢迎入场、黑名单禁止入场、未找到入场记录等）
     */
    private String message;

    /**
     * 识别结果：0-失败，1-成功，2-黑名单
     */
    private Integer recognitionResult;

    /**
     * 配额信息（仅出场时返回，供前端展示）
     */
    private FeeCalculationResult feeInfo;

    public GateAccessResult() {
        this.allowed = false;
        this.isDefaultVehicle = false;
        this.hasReservation = false;
    }
}
