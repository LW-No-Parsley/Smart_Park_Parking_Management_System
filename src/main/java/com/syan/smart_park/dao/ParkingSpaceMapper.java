package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车位数据访问层
 */
@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
}
