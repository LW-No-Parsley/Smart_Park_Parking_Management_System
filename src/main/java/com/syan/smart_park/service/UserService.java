package com.syan.smart_park.service;

import com.syan.smart_park.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 注册用户
     */
    boolean register(User user);

    // ====== 管理员用户管理 ======

    /**
     * 获取所有管理员用户
     */
    List<User> getAllUsers();

    /**
     * 根据ID获取管理员用户
     */
    User getUserById(Long id);

    /**
     * 创建管理员用户（自动加密密码）
     */
    User createUser(User user);

    /**
     * 更新管理员用户
     */
    User updateUser(Long id, User user);

    /**
     * 删除管理员用户（软删除）
     */
    void deleteUser(Long id);

    /**
     * 根据状态获取管理员用户列表
     */
    List<User> getUsersByStatus(Integer status);
}
