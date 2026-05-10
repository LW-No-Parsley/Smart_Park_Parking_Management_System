package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联数据访问层
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID物理删除用户角色关联
     * <p>
     * 使用物理删除避免与 uk_user_role(user_id, role_id) 唯一索引冲突。
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和角色ID物理删除单条关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     *
     * @param userRoleList 用户角色关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<UserRole> userRoleList);

    /**
     * 根据用户ID和角色ID查询关联（包含软删除记录）
     * <p>
     * 使用原生 SQL 绕过 @TableLogic 自动添加的 deleted=0 条件，
     * 用于在重新分配角色前检查是否存在软删除的旧记录。
     */
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    UserRole selectByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
