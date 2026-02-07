package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.ParkUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车场小程序用户数据访问层
 */
@Mapper
public interface ParkUserMapper extends BaseMapper<ParkUser> {
    
    /**
     * 根据openid查询用户
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    ParkUser selectByOpenid(String openid);
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    ParkUser selectByPhone(String phone);
}
