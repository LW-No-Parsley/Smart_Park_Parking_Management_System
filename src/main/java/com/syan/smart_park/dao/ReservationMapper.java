package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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
            "AND approval_status = 1 AND status != 0 AND deleted = 0")
    Long countReservedBySpaceIdAndTime(@Param("spaceId") Long spaceId,
                                       @Param("currentTime") LocalDateTime currentTime);
}
