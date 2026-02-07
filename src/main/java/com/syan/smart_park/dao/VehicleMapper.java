package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆数据访问层
 */
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
