package com.syan.smart_park.service;

import com.syan.smart_park.common.PageResult;
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
     * 统一查询管理员用户列表（支持多条件筛选 + 分页）
     *
     * @param status 用户状态：0-禁用，1-启用（可选）
     * @param page   页码
     * @param size   每页大小
     */
    PageResult<User> listUsers(Integer status, Integer page, Integer size);

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
}
