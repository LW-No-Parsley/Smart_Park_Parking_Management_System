package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.PermissionMapper;
import com.syan.smart_park.dao.RolePermissionMapper;
import com.syan.smart_park.entity.Permission;
import com.syan.smart_park.entity.PermissionDTO;
import com.syan.smart_park.entity.RolePermission;
import com.syan.smart_park.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByAsc("sort_order", "create_time");
        List<Permission> permissions = permissionMapper.selectList(queryWrapper);
        return permissions.stream()
                .map(PermissionDTO::fromPermission)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionDTO> getPermissionTree() {
        List<PermissionDTO> allPermissions = getAllPermissions();
        if (allPermissions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<PermissionDTO>> parentIdMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() != 0)
                .collect(Collectors.groupingBy(PermissionDTO::getParentId));

        List<PermissionDTO> tree = allPermissions.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .sorted(Comparator.comparingInt(PermissionDTO::getSortOrder))
                .collect(Collectors.toList());

        buildChildren(tree, parentIdMap);
        return tree;
    }

    private void buildChildren(List<PermissionDTO> nodes, Map<Long, List<PermissionDTO>> parentIdMap) {
        for (PermissionDTO node : nodes) {
            List<PermissionDTO> children = parentIdMap.get(node.getId());
            if (children != null) {
                children.sort(Comparator.comparingInt(PermissionDTO::getSortOrder));
                node.setChildren(children);
                buildChildren(children, parentIdMap);
            }
        }
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null || permission.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC614); // 权限不存在
        }
        return PermissionDTO.fromPermission(permission);
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(Permission permission) {
        // 检查权限编码是否已存在
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_code", permission.getPermissionCode())
                   .eq("deleted", 0);
        Permission existing = permissionMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(ReturnCode.RC611); // 编码已存在，复用角色编码已存在的语义
        }

        // 如果指定了父权限，检查父权限是否存在
        if (permission.getParentId() != null && permission.getParentId() != 0) {
            Permission parent = permissionMapper.selectById(permission.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                throw new BusinessException(ReturnCode.RC614); // 父权限不存在
            }
        }

        permission.setStatus(permission.getStatus() != null ? permission.getStatus() : 1);
        permission.setSortOrder(permission.getSortOrder() != null ? permission.getSortOrder() : 0);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permission.setDeleted(0);

        permissionMapper.insert(permission);
        return PermissionDTO.fromPermission(permission);
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(Long id, Permission permission) {
        Permission existing = permissionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC614); // 权限不存在
        }

        // 检查权限编码是否与其他权限冲突
        if (permission.getPermissionCode() != null
                && !permission.getPermissionCode().equals(existing.getPermissionCode())) {
            QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("permission_code", permission.getPermissionCode())
                       .ne("id", id)
                       .eq("deleted", 0);
            Permission conflict = permissionMapper.selectOne(queryWrapper);
            if (conflict != null) {
                throw new BusinessException(ReturnCode.RC611); // 编码已存在
            }
        }

        permission.setId(id);
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(permission);
        return getPermissionById(id);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission existing = permissionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC614); // 权限不存在
        }

        // 检查是否有子权限
        QueryWrapper<Permission> childQuery = new QueryWrapper<>();
        childQuery.eq("parent_id", id)
                 .eq("deleted", 0);
        Long childCount = permissionMapper.selectCount(childQuery);
        if (childCount > 0) {
            throw new BusinessException(ReturnCode.RC612, "存在子权限，无法删除");
        }

        // 检查是否有角色关联该权限
        QueryWrapper<RolePermission> rolePermQuery = new QueryWrapper<>();
        rolePermQuery.eq("permission_id", id);
        Long roleCount = rolePermissionMapper.selectCount(rolePermQuery);
        if (roleCount > 0) {
            // 解除角色权限关联
            rolePermissionMapper.delete(rolePermQuery);
        }

        // 软删除
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(existing);
    }
}
