package com.syan.smart_park.service;

import com.syan.smart_park.entity.LoginLog;

import java.util.List;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {
    
    /**
     * 记录登录日志
     *
     * @param loginLog 登录日志信息
     * @return 是否成功
     */
    boolean recordLogin(LoginLog loginLog);
    
    /**
     * 获取用户的登录日志
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 登录日志列表
     */
    List<LoginLog> getUserLoginLogs(Long userId, int limit);
    
    /**
     * 获取所有登录日志
     *
     * @param page 页码
     * @param size 每页大小
     * @return 登录日志列表
     */
    List<LoginLog> getAllLoginLogs(int page, int size);
    
    /**
     * 获取登录失败次数
     *
     * @param username 用户名
     * @param minutes 时间范围（分钟）
     * @return 失败次数
     */
    int getFailedLoginCount(String username, int minutes);
    
    /**
     * 清理过期登录日志
     *
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredLogs(int days);
    
    /**
     * 获取登录统计信息
     *
     * @param days 统计天数
     * @return 统计信息
     */
    LoginStatistics getLoginStatistics(int days);
    
    /**
     * 登录统计信息类
     */
    class LoginStatistics {
        private int totalLogins;
        private int successfulLogins;
        private int failedLogins;
        private int uniqueUsers;
        
        public LoginStatistics(int totalLogins, int successfulLogins, int failedLogins, int uniqueUsers) {
            this.totalLogins = totalLogins;
            this.successfulLogins = successfulLogins;
            this.failedLogins = failedLogins;
            this.uniqueUsers = uniqueUsers;
        }
        
        public int getTotalLogins() {
            return totalLogins;
        }
        
        public int getSuccessfulLogins() {
            return successfulLogins;
        }
        
        public int getFailedLogins() {
            return failedLogins;
        }
        
        public int getUniqueUsers() {
            return uniqueUsers;
        }
    }
}
