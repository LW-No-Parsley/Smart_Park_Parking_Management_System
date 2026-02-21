package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
