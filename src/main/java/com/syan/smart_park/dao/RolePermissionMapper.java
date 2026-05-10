package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联数据访问层
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID物理删除角色权限关联
     * <p>
     * 使用物理删除而非软删除，因为关联表不需要保留历史记录，
     * 且唯一索引 uk_role_permission(role_id, permission_id) 与软删除不兼容。
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID物理删除角色权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID和权限ID物理删除单条关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     *
     * @param rolePermissionList 角色权限关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<RolePermission> rolePermissionList);

    /**
     * 根据角色ID和权限ID查询关联（包含软删除记录）
     */
    @Select("SELECT * FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    RolePermission selectByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 根据角色ID查询权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
