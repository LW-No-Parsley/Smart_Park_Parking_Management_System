package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ParkArea;
import org.apache.ibatis.annotations.Mapper;

/**
 * 园区数据访问层
 */
@Mapper
public interface ParkAreaMapper extends BaseMapper<ParkArea> {
}
