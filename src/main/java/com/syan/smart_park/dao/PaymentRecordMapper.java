package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录数据访问层
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
