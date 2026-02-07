package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约数据访问层
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
