package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.Captcha;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码数据访问接口
 */
@Mapper
public interface CaptchaMapper extends BaseMapper<Captcha> {
}
