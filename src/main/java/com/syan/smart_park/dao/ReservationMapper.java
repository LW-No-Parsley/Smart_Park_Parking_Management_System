package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约数据访问层
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    
    /**
     * 根据ID锁定预约记录（FOR UPDATE）
     * 用于乐观锁事务
     */
    @Select("SELECT * FROM reservation WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Reservation selectForUpdate(@Param("id") Long id);

    /**
     * 查询指定车位在指定时间是否有有效预约占用
     * 有效预约：已审批通过、未取消、在时间段内
     *
     * @param spaceId 车位ID
     * @param currentTime 当前时间
     * @return 有效预约占用数量
     */
    @Select("SELECT COUNT(*) FROM reservation WHERE space_id = #{spaceId} " +
            "AND start_time <= #{currentTime} AND end_time >= #{currentTime} " +
            "AND approval_status = 1 AND status IN (1, 2) AND deleted = 0")
    Long countReservedBySpaceIdAndTime(@Param("spaceId") Long spaceId,
                                       @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定园区在指定时间有多少个车位被有效预约占用
     */
    @Select("SELECT COUNT(DISTINCT r.space_id) FROM reservation r " +
            "INNER JOIN parking_space ps ON r.space_id = ps.id " +
            "WHERE ps.park_area_id = #{parkAreaId} AND ps.deleted = 0 " +
            "AND r.start_time <= #{currentTime} AND r.end_time >= #{currentTime} " +
            "AND r.approval_status = 1 AND r.status IN (1, 2) AND r.deleted = 0")
    Long countReservedSpacesByParkAreaIdAndTime(@Param("parkAreaId") Long parkAreaId,
                                                 @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询已过期的预约ID列表
     * 条件：end_time < NOW() 且 (approval_status=0 待审批 或 status=1 已预约未使用)
     *       且 deleted=0, status != 3
     *
     * @param now 当前时间
     * @return 过期的预约ID列表
     */
    @Select("SELECT id FROM reservation WHERE end_time < #{now} AND deleted = 0 " +
            "AND (approval_status = 0 OR status = 1) AND status != 3")
    List<Long> selectOverdueReservationIds(@Param("now") LocalDateTime now);

    /**
     * 批量将已过期的预约标记为"已过期"(status=3)
     * 条件：end_time < NOW() 且 (approval_status=0 待审批 或 status=1 已预约未使用)
     *       且 deleted=0
     *
     * @param now 当前时间
     * @return 更新的记录数
     */
    @org.apache.ibatis.annotations.Update(
        "UPDATE reservation SET status = 3, update_time = NOW() " +
        "WHERE end_time < #{now} AND deleted = 0 " +
        "AND (approval_status = 0 OR status = 1) " +
        "AND status != 3"
    )
    int batchExpireOverdueReservations(@Param("now") LocalDateTime now);
}
