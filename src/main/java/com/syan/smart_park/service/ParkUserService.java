package com.syan.smart_park.service;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;

import java.util.List;

/**
 * 停车场小程序用户服务接口
 */
public interface ParkUserService {

    /**
     * 根据openid查询用户
     */
    ParkUserDTO getByOpenid(String openid);

    /**
     * 根据手机号查询用户
     */
    ParkUserDTO getByPhone(String phone);

    /**
     * 创建或更新用户（微信登录）
     */
    ParkUserDTO createOrUpdate(ParkUser parkUser);

    /**
     * 更新用户最后登录时间
     */
    boolean updateLastLoginTime(Long userId);

    // ====== 园区用户管理 ======

    /**
     * 统一查询园区用户列表（支持多条件筛选 + 分页）
     */
    PageResult<ParkUserDTO> listParkUsers(Integer status, Integer userType, Integer page, Integer size);

    /**
     * 根据ID获取园区用户
     */
    ParkUserDTO getParkUserById(Long id);

    /**
     * 创建园区用户
     */
    ParkUserDTO createParkUser(ParkUser parkUser);

    /**
     * 更新园区用户
     */
    ParkUserDTO updateParkUser(Long id, ParkUser parkUser);

    /**
     * 删除园区用户（软删除）
     */
    void deleteParkUser(Long id);
}
