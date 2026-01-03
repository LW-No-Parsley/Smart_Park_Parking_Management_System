package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
@TableName("sys_permission")
public class Permission {
    
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限编码
     */
    private String permissionCode;
    
    /**
     * 权限类型：1-菜单，2-按钮，3-接口
     */
    private Integer permissionType;
    
    /**
     * 权限路径/URL
     */
    @TableField("path")
    private String permissionPath;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 图标
     */
    @TableField("icon")
    private String icon;
    
    /**
     * 权限状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
