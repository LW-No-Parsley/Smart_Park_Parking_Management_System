package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ParkingZone;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车位分区数据访问层
 */
@Mapper
public interface ParkingZoneMapper extends BaseMapper<ParkingZone> {
}
