package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.FeeRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车计费规则Mapper
 */
@Mapper
public interface FeeRuleMapper extends BaseMapper<FeeRule> {
}
