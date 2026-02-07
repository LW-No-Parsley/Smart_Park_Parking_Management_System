package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ExceptionReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常上报数据访问层
 */
@Mapper
public interface ExceptionReportMapper extends BaseMapper<ExceptionReport> {
}
