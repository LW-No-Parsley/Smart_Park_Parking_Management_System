package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.SpaceOccupy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 车位占用记录Mapper接口
 */
@Mapper
public interface SpaceOccupyMapper extends BaseMapper<SpaceOccupy> {

    /**
     * 查询指定车位在当前时间是否被占用
     *
     * @param spaceId 车位ID
     * @param currentTime 当前时间
     * @return 占用记录数量
     */
    @Select("SELECT COUNT(*) FROM space_occupy WHERE space_id = #{spaceId} " +
            "AND start_time <= #{currentTime} AND end_time >= #{currentTime}")
    Long countOccupiedBySpaceIdAndTime(@Param("spaceId") Long spaceId, 
                                      @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定园区在当前时间被占用的车位数量
     *
     * @param parkAreaId 园区ID
     * @param currentTime 当前时间
     * @return 占用车位数量
     */
    @Select("SELECT COUNT(DISTINCT so.space_id) FROM space_occupy so " +
            "INNER JOIN parking_space ps ON so.space_id = ps.id " +
            "WHERE ps.park_area_id = #{parkAreaId} AND ps.deleted = 0 " +
            "AND so.start_time <= #{currentTime} AND so.end_time >= #{currentTime}")
    Long countOccupiedSpacesByParkAreaIdAndTime(@Param("parkAreaId") Long parkAreaId,
                                               @Param("currentTime") LocalDateTime currentTime);
}
