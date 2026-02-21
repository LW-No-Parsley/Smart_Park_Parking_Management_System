package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单DTO
 */
@Data
public class BlacklistDTO {
    
    /**
     * 黑名单ID
     */
    private Long id;
    
    /**
     * 车牌号
     */
    private String plateNumber;
    
    /**
     * 加入原因
     */
    private String reason;
    
    /**
     * 创建人（sys_user.id）
     */
    private Long createdBy;
    
    /**
     * 更新人（sys_user.id）
     */
    private Long updatedBy;
    
    /**
     * 生效时间
     */
    private LocalDateTime startTime;
    
    /**
     * 失效时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：0-禁用，1-生效
     */
    private Integer status;
    
    /**
     * 所属园区ID
     */
    private Long parkAreaId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 静态方法：从Blacklist实体转换为BlacklistDTO
     */
    public static BlacklistDTO fromBlacklist(Blacklist blacklist) {
        if (blacklist == null) {
            return null;
        }
        
        BlacklistDTO dto = new BlacklistDTO();
        dto.setId(blacklist.getId());
        dto.setPlateNumber(blacklist.getPlateNumber());
        dto.setReason(blacklist.getReason());
        dto.setCreatedBy(blacklist.getCreatedBy());
        dto.setUpdatedBy(blacklist.getUpdatedBy());
        dto.setStartTime(blacklist.getStartTime());
        dto.setEndTime(blacklist.getEndTime());
        dto.setStatus(blacklist.getStatus());
        dto.setParkAreaId(blacklist.getParkAreaId());
        dto.setCreateTime(blacklist.getCreateTime());
        dto.setUpdateTime(blacklist.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为Blacklist实体
     */
    public Blacklist toBlacklist() {
        Blacklist blacklist = new Blacklist();
        blacklist.setId(this.id);
        blacklist.setPlateNumber(this.plateNumber);
        blacklist.setReason(this.reason);
        blacklist.setCreatedBy(this.createdBy);
        blacklist.setUpdatedBy(this.updatedBy);
        blacklist.setStartTime(this.startTime);
        blacklist.setEndTime(this.endTime);
        blacklist.setStatus(this.status);
        blacklist.setParkAreaId(this.parkAreaId);
        
        return blacklist;
    }
}
