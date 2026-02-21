package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 车位数据访问层
 */
@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
    
    /**
     * 根据ID锁定车位记录（FOR UPDATE）
     * 用于乐观锁事务
     */
    @Select("SELECT * FROM parking_space WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    ParkingSpace selectForUpdate(@Param("id") Long id);
}
