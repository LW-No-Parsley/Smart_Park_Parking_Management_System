package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志数据传输对象
 */
@Data
public class OperationLogDTO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 操作用户ID（park_user.id）
     */
    private Long userId;
    
    /**
     * 模块名称
     */
    private String module;
    
    /**
     * 操作动作
     */
    private String action;
    
    /**
     * 操作详情（JSON格式）
     */
    private String detail;
    
    /**
     * 操作IP
     */
    private String ip;
    
    /**
     * 操作时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 从OperationLog实体转换为DTO
     */
    public static OperationLogDTO fromOperationLog(OperationLog operationLog) {
        if (operationLog == null) {
            return null;
        }
        
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(operationLog.getId());
        dto.setUserId(operationLog.getUserId());
        dto.setModule(operationLog.getModule());
        dto.setAction(operationLog.getAction());
        dto.setDetail(operationLog.getDetail());
        dto.setIp(operationLog.getIp());
        dto.setCreateTime(operationLog.getCreateTime());
        dto.setUpdateTime(operationLog.getUpdateTime());
        dto.setDeleted(operationLog.getDeleted());
        
        return dto;
    }
    
    /**
     * 从DTO转换为OperationLog实体
     */
    public OperationLog toOperationLog() {
        OperationLog operationLog = new OperationLog();
        operationLog.setId(this.getId());
        operationLog.setUserId(this.getUserId());
        operationLog.setModule(this.getModule());
        operationLog.setAction(this.getAction());
        operationLog.setDetail(this.getDetail());
        operationLog.setIp(this.getIp());
        operationLog.setCreateTime(this.getCreateTime());
        operationLog.setUpdateTime(this.getUpdateTime());
        operationLog.setDeleted(this.getDeleted());
        
        return operationLog;
    }
}
