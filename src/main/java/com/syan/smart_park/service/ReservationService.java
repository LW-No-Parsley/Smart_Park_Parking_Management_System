package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.Reservation;
import com.syan.smart_park.entity.ReservationDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务接口
 */
public interface ReservationService extends IService<Reservation> {
    
    /**
     * 获取所有预约
     */
    List<ReservationDTO> getAllReservations();
    
    /**
     * 根据ID获取预约
     */
    ReservationDTO getReservationById(Long id);
    
    /**
     * 创建预约
     */
    ReservationDTO createReservation(ReservationDTO reservationDTO);
    
    /**
     * 更新预约
     */
    ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO);
    
    /**
     * 删除预约
     */
    boolean deleteReservation(Long id);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<ReservationDTO> getReservationsByUserId(Long userId);
    
    /**
     * 根据车辆ID获取预约列表
     */
    List<ReservationDTO> getReservationsByVehicleId(Long vehicleId);
    
    /**
     * 根据车位ID获取预约列表
     */
    List<ReservationDTO> getReservationsBySpaceId(Long spaceId);
    
    /**
     * 根据预约类型获取预约列表
     */
    List<ReservationDTO> getReservationsByType(Integer reservationType);
    
    /**
     * 根据审批状态获取预约列表
     */
    List<ReservationDTO> getReservationsByApprovalStatus(Integer approvalStatus);
    
    /**
     * 根据预约状态获取预约列表
     */
    List<ReservationDTO> getReservationsByStatus(Integer status);
    
    /**
     * 根据支付状态获取预约列表
     */
    List<ReservationDTO> getReservationsByPayStatus(Integer payStatus);
    
    /**
     * 根据创建来源获取预约列表
     */
    List<ReservationDTO> getReservationsBySource(Integer source);
    
    /**
     * 获取指定时间范围内的预约列表
     */
    List<ReservationDTO> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 审批预约
     */
    boolean approveReservation(Long id, Long approvedBy, String rejectReason);
    
    /**
     * 拒绝预约
     */
    boolean rejectReservation(Long id, Long approvedBy, String rejectReason);
    
    /**
     * 更新预约状态
     */
    boolean updateReservationStatus(Long id, Integer status);
    
    /**
     * 更新支付状态和金额
     */
    boolean updatePaymentStatus(Long id, Integer payStatus, BigDecimal paidAmount);
    
    /**
     * 记录到达时间
     */
    boolean recordArrival(Long id, LocalDateTime arriveTime);
    
    /**
     * 记录离开时间
     */
    boolean recordDeparture(Long id, LocalDateTime leaveTime, BigDecimal totalFee);
    
    /**
     * 批量更新预约状态
     */
    boolean batchUpdateReservationStatus(List<Long> ids, Integer status);
    
    /**
     * 获取待审批的预约列表
     */
    List<ReservationDTO> getPendingApprovalReservations();
    
    /**
     * 获取用户当前有效的预约
     */
    ReservationDTO getUserCurrentValidReservation(Long userId);
    
    /**
     * 检查车位在指定时间是否可用
     */
    boolean isSpaceAvailable(Long spaceId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId);
}
