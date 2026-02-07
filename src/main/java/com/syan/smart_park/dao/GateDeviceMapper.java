package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.GateDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 道闸设备数据访问层
 */
@Mapper
public interface GateDeviceMapper extends BaseMapper<GateDevice> {
}
