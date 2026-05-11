package com.syan.smart_park.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.UserMapper;
import com.syan.smart_park.entity.User;
import com.syan.smart_park.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("deleted", 0);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User login(String username, String password) {
        // 查询用户
        User user = findByUsername(username);
        if (user == null) {
            throw new BusinessException(ReturnCode.RC601); // 统一为"用户名或密码错误"，防止枚举用户
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ReturnCode.RC602); // 用户已被禁用
        }

        // 验证密码（这里使用MD5加密，实际项目中建议使用更安全的加密方式）
        String encryptedPassword = DigestUtil.md5Hex(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(ReturnCode.RC601); // 用户名或密码错误
        }

        return user;
    }

    /**
     * 检查密码强度
     * 密码要求：
     * 1. 长度至少8位
     * 2. 包含至少一个大写字母
     * 3. 包含至少一个小写字母
     * 4. 包含至少一个数字
     * 5. 包含至少一个特殊字符
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existingUser = findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ReturnCode.RC603); // 用户已存在
        }

        // 检查密码强度
        if (!isPasswordStrong(user.getPassword())) {
            throw new BusinessException(ReturnCode.RC604); // 密码强度不足
        }

        // 设置默认值
        user.setPassword(DigestUtil.md5Hex(user.getPassword())); // 密码加密
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);

        // 保存用户
        int result = userMapper.insert(user);
        return result > 0;
    }

    // ====== 管理员用户管理 ======

    @Override
    public List<User> getAllUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByAsc("create_time");
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            throw new BusinessException(ReturnCode.RC603); // 用户已存在
        }

        // 检查密码强度
        if (!isPasswordStrong(user.getPassword())) {
            throw new BusinessException(ReturnCode.RC604); // 密码强度不足
        }

        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);

        // 如果修改了用户名，检查唯一性
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername())
                       .ne("id", id)
                       .eq("deleted", 0);
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException(ReturnCode.RC603); // 用户已存在
            }
        }

        // 如果传了密码，重新加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!isPasswordStrong(user.getPassword())) {
                throw new BusinessException(ReturnCode.RC604); // 密码强度不足
            }
            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }

        user.setId(id);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public List<User> getUsersByStatus(Integer status) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("status", status)
                   .orderByAsc("create_time");
        return userMapper.selectList(queryWrapper);
    }
}
