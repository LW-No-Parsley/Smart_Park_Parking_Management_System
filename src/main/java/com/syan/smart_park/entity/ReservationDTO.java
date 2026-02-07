package com.syan.smart_park.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约DTO
 */
@Data
public class ReservationDTO {
    
    /**
     * 预约ID
     */
    private Long id;
    
    /**
     * 用户ID（park_user.id）
     */
    private Long userId;
    
    /**
     * 车辆ID（vehicle.id）
     */
    private Long vehicleId;
    
    /**
     * 车位ID（parking_space.id）
     */
    private Long spaceId;
    
    /**
     * 预约类型：1-车主预约，2-访客申请，3-保安现场
     */
    private Integer reservationType;
    
    /**
     * 审批状态：0-待审批，1-通过，2-拒绝
     */
    private Integer approvalStatus;
    
    /**
     * 审批人（sys_user.id）
     */
    private Long approvedBy;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvedTime;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 创建人（sys_user.id；小程序创建则为空）
     */
    private Long createdBy;
    
    /**
     * 创建来源：1-小程序，2-后台管理员，3-保安
     */
    private Integer source;
    
    /**
     * 预约开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 预约结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 预约状态：0-已取消，1-已预约，2-已使用，3-已过期
     */
    private Integer status;
    
    /**
     * 实际到达时间
     */
    private LocalDateTime arriveTime;
    
    /**
     * 实际离开时间
     */
    private LocalDateTime leaveTime;
    
    /**
     * 最终结算停车费用（整单）
     */
    private BigDecimal totalFee;
    
    /**
     * 已支付金额（缓存字段，便于列表查询）
     */
    private BigDecimal paidAmount;
    
    /**
     * 结算时间（通常为离场或管理员结算时）
 */
    private LocalDateTime settlementTime;
    
    /**
     * 整单支付状态：0-未支付，1-已支付，2-部分支付，3-已退款
     */
    private Integer payStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 乐观锁版本号
     */
    private Integer version;
    
    /**
     * 静态方法：从Reservation实体转换为ReservationDTO
     */
    public static ReservationDTO fromReservation(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUserId());
        dto.setVehicleId(reservation.getVehicleId());
        dto.setSpaceId(reservation.getSpaceId());
        dto.setReservationType(reservation.getReservationType());
        dto.setApprovalStatus(reservation.getApprovalStatus());
        dto.setApprovedBy(reservation.getApprovedBy());
        dto.setApprovedTime(reservation.getApprovedTime());
        dto.setRejectReason(reservation.getRejectReason());
        dto.setCreatedBy(reservation.getCreatedBy());
        dto.setSource(reservation.getSource());
        dto.setStartTime(reservation.getStartTime());
        dto.setEndTime(reservation.getEndTime());
        dto.setStatus(reservation.getStatus());
        dto.setArriveTime(reservation.getArriveTime());
        dto.setLeaveTime(reservation.getLeaveTime());
        dto.setTotalFee(reservation.getTotalFee());
        dto.setPaidAmount(reservation.getPaidAmount());
        dto.setSettlementTime(reservation.getSettlementTime());
        dto.setPayStatus(reservation.getPayStatus());
        dto.setCreateTime(reservation.getCreateTime());
        dto.setUpdateTime(reservation.getUpdateTime());
        dto.setVersion(reservation.getVersion());
        
        return dto;
    }
    
    /**
     * 转换为Reservation实体
     */
    public Reservation toReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(this.id);
        reservation.setUserId(this.userId);
        reservation.setVehicleId(this.vehicleId);
        reservation.setSpaceId(this.spaceId);
        reservation.setReservationType(this.reservationType);
        reservation.setApprovalStatus(this.approvalStatus);
        reservation.setApprovedBy(this.approvedBy);
        reservation.setApprovedTime(this.approvedTime);
        reservation.setRejectReason(this.rejectReason);
        reservation.setCreatedBy(this.createdBy);
        reservation.setSource(this.source);
        reservation.setStartTime(this.startTime);
        reservation.setEndTime(this.endTime);
        reservation.setStatus(this.status);
        reservation.setArriveTime(this.arriveTime);
        reservation.setLeaveTime(this.leaveTime);
        reservation.setTotalFee(this.totalFee);
        reservation.setPaidAmount(this.paidAmount);
        reservation.setSettlementTime(this.settlementTime);
        reservation.setPayStatus(this.payStatus);
        reservation.setVersion(this.version);
        
        return reservation;
    }
}
