package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syan.smart_park.dao.LoginLogMapper;
import com.syan.smart_park.entity.LoginLog;
import com.syan.smart_park.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public boolean recordLogin(LoginLog loginLog) {
        int result = loginLogMapper.insert(loginLog);
        return result > 0;
    }

    @Override
    public List<LoginLog> getUserLoginLogs(Long userId, int limit) {
        QueryWrapper<LoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("login_time")
                   .last("LIMIT " + limit);
        
        return loginLogMapper.selectList(queryWrapper);
    }

    @Override
    public List<LoginLog> getAllLoginLogs(int page, int size) {
        Page<LoginLog> pageParam = new Page<>(page, size);
        QueryWrapper<LoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("login_time");
        
        return loginLogMapper.selectPage(pageParam, queryWrapper).getRecords();
    }

    @Override
    public int getFailedLoginCount(String username, int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        
        QueryWrapper<LoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("status", 0) // 登录失败
                   .ge("login_time", startTime);
        
        Long count = loginLogMapper.selectCount(queryWrapper);
        return count != null ? count.intValue() : 0;
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public int cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        
        QueryWrapper<LoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("login_time", expireTime);
        
        return loginLogMapper.delete(queryWrapper);
    }

    @Override
    public LoginStatistics getLoginStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 总登录次数
        QueryWrapper<LoginLog> totalQuery = new QueryWrapper<>();
        totalQuery.ge("login_time", startTime);
        Long totalCount = loginLogMapper.selectCount(totalQuery);
        int totalLogins = totalCount != null ? totalCount.intValue() : 0;
        
        // 成功登录次数
        QueryWrapper<LoginLog> successQuery = new QueryWrapper<>();
        successQuery.ge("login_time", startTime)
                   .eq("status", 1);
        Long successCount = loginLogMapper.selectCount(successQuery);
        int successfulLogins = successCount != null ? successCount.intValue() : 0;
        
        // 失败登录次数
        QueryWrapper<LoginLog> failedQuery = new QueryWrapper<>();
        failedQuery.ge("login_time", startTime)
                  .eq("status", 0);
        Long failedCount = loginLogMapper.selectCount(failedQuery);
        int failedLogins = failedCount != null ? failedCount.intValue() : 0;
        
        // 独立用户数
        QueryWrapper<LoginLog> uniqueUserQuery = new QueryWrapper<>();
        uniqueUserQuery.select("DISTINCT user_id")
                      .ge("login_time", startTime);
        Long uniqueCount = loginLogMapper.selectCount(uniqueUserQuery);
        int uniqueUsers = uniqueCount != null ? uniqueCount.intValue() : 0;
        
        return new LoginStatistics(totalLogins, successfulLogins, failedLogins, uniqueUsers);
    }
}
