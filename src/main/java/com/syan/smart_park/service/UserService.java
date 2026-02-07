package com.syan.smart_park.service;

import com.syan.smart_park.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);
    
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String username, String password);
    
    /**
     * 注册用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean register(User user);
}
