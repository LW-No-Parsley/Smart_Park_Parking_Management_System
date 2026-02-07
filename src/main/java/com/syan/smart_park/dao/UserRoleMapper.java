package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联数据访问层
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户角色关联
     *
     * @param userRoleList 用户角色关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<UserRole> userRoleList);
    
    /**
     * 根据用户ID和角色ID查询关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联
     */
    UserRole selectByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
