package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 道闸设备DTO
 */
@Data
public class GateDeviceDTO {
    
    /**
     * 设备ID
     */
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
    /**
     * 道闸名称（如东门入口、西门出口等）
     */
    private String gateName;
    
    /**
     * 设备序列号
     */
    private String deviceSn;
    
    /**
     * 设备IP地址
     */
    private String ipAddress;
    
    /**
     * 设备类型：1-入口道闸，2-出口道闸
     */
    private Integer deviceType;
    
    /**
     * 设备状态：0-离线，1-在线，2-故障
     */
    private Integer status;
    
    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 静态方法：从GateDevice实体转换为GateDeviceDTO
     */
    public static GateDeviceDTO fromGateDevice(GateDevice gateDevice) {
        if (gateDevice == null) {
            return null;
        }
        
        GateDeviceDTO dto = new GateDeviceDTO();
        dto.setId(gateDevice.getId());
        dto.setParkAreaId(gateDevice.getParkAreaId());
        dto.setGateName(gateDevice.getGateName());
        dto.setDeviceSn(gateDevice.getDeviceSn());
        dto.setIpAddress(gateDevice.getIpAddress());
        dto.setDeviceType(gateDevice.getDeviceType());
        dto.setStatus(gateDevice.getStatus());
        dto.setLastHeartbeat(gateDevice.getLastHeartbeat());
        dto.setCreateTime(gateDevice.getCreateTime());
        dto.setUpdateTime(gateDevice.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为GateDevice实体
     */
    public GateDevice toGateDevice() {
        GateDevice gateDevice = new GateDevice();
        gateDevice.setId(this.id);
        gateDevice.setParkAreaId(this.parkAreaId);
        gateDevice.setGateName(this.gateName);
        gateDevice.setDeviceSn(this.deviceSn);
        gateDevice.setIpAddress(this.ipAddress);
        gateDevice.setDeviceType(this.deviceType);
        gateDevice.setStatus(this.status);
        gateDevice.setLastHeartbeat(this.lastHeartbeat);
        
        return gateDevice;
    }
}
