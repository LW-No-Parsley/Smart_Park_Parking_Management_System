package com.syan.smart_park.entity;

import lombok.Data;

/**
 * 园区占用统计信息
 */
@Data
public class ParkAreaOccupancyStats {

    /**
     * 园区ID
     */
    private Long parkAreaId;

    /**
     * 园区名称
     */
    private String parkAreaName;

    /**
     * 总车位数
     */
    private Integer totalSpaces;

    /**
     * 占用车位数（当前时间）
     */
    private Integer occupiedSpaces;

    /**
     * 空闲车位数（当前时间）
     */
    private Integer availableSpaces;

    /**
     * 占用率（百分比，0-100）
     */
    private Double occupancyRate;

    /**
     * 创建统计信息
     *
     * @param parkAreaId 园区ID
     * @param parkAreaName 园区名称
     * @param totalSpaces 总车位数
     * @param occupiedSpaces 占用车位数
     */
    public ParkAreaOccupancyStats(Long parkAreaId, String parkAreaName, 
                                 Integer totalSpaces, Integer occupiedSpaces) {
        this.parkAreaId = parkAreaId;
        this.parkAreaName = parkAreaName;
        this.totalSpaces = totalSpaces;
        this.occupiedSpaces = occupiedSpaces;
        this.availableSpaces = totalSpaces - occupiedSpaces;
        
        if (totalSpaces > 0) {
            this.occupancyRate = (occupiedSpaces * 100.0) / totalSpaces;
        } else {
            this.occupancyRate = 0.0;
        }
    }
}
