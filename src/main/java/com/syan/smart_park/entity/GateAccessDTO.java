package com.syan.smart_park.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 闸机通行请求DTO
 * 替代原有的AccessLogDTO，只包含闸机设备实际能提供的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateAccessDTO {
    /** 园区ID */
    private Long parkAreaId;
    
    /** 设备编号（序列号） */
    private String deviceSn;
    
    /** 道闸ID（设备编号查出来后再填充） */
    private Long gateId;
    
    /** 识别车牌号 */
    private String plateNumber;
    
    /** 进出类型：1-入场，2-出场 */
    private Integer accessType;
    
    /** 抓拍图片地址 */
    private String imageUrl;
    
    /** 识别结果：0-失败，1-成功，2-黑名单 */
    private Integer recognitionResult;
    
    /** 通行时间 */
    private java.time.LocalDateTime accessTime;
    
    /** 处理人员ID（后台手动干预时） */
    private Long handledBy;
    
    /** 备注 */
    private String remark;
}
