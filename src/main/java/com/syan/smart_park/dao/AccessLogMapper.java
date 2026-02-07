package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 进出记录数据访问层
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {
}
