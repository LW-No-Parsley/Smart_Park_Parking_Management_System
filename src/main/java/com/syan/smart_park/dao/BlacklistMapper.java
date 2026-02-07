package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单数据访问层
 */
@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {
}
