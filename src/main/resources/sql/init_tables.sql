-- Smart Park Parking Management System 数据库表初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2024-11-30

-- 使用数据库
USE Smart_Park_Parking_Management_System;

-- 1. 用户表 (已存在)
-- CREATE TABLE IF NOT EXISTS sys_user ...

-- 2. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type TINYINT NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(255) COMMENT '权限路径',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    token VARCHAR(500) NOT NULL COMMENT 'JWT Token',
    expiration_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_token (token),
    INDEX idx_expiration_time (expiration_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- 7. 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '登录地点',
    browser VARCHAR(100) COMMENT '浏览器类型',
    os VARCHAR(50) COMMENT '操作系统',
    status TINYINT NOT NULL COMMENT '登录状态：0-失败，1-成功',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 8. 验证码表
CREATE TABLE IF NOT EXISTS sys_captcha (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证码ID',
    captcha_id VARCHAR(100) NOT NULL COMMENT '验证码唯一标识',
    captcha_code VARCHAR(20) NOT NULL COMMENT '验证码',
    captcha_type TINYINT DEFAULT 1 COMMENT '验证码类型：1-登录，2-注册，3-重置密码',
    expiration_time DATETIME NOT NULL COMMENT '过期时间',
    used TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_captcha_id (captcha_id),
    INDEX idx_expiration_time (expiration_time),
    INDEX idx_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- 9. 初始化默认数据

-- 插入默认角色
INSERT IGNORE INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '系统管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', 1),
(2, '普通用户', 'ROLE_USER', '普通用户，拥有基本权限', 1),
(3, '停车场管理员', 'ROLE_PARKING_MANAGER', '停车场管理员，管理停车场相关功能', 1);

-- 插入默认权限（示例）
INSERT IGNORE INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status) VALUES
-- 系统管理菜单
(1, '系统管理', 'system:manage', 1, 0, '/system', 'SettingOutlined', 100, 1),
(2, '用户管理', 'system:user', 1, 1, '/system/user', 'UserOutlined', 101, 1),
(3, '角色管理', 'system:role', 1, 1, '/system/role', 'TeamOutlined', 102, 1),
(4, '权限管理', 'system:permission', 1, 1, '/system/permission', 'SafetyCertificateOutlined', 103, 1),

-- 用户管理按钮权限
(5, '查询用户', 'system:user:query', 2, 2, NULL, NULL, 1, 1),
(6, '新增用户', 'system:user:add', 2, 2, NULL, NULL, 2, 1),
(7, '编辑用户', 'system:user:edit', 2, 2, NULL, NULL, 3, 1),
(8, '删除用户', 'system:user:delete', 2, 2, NULL, NULL, 4, 1),

-- 角色管理按钮权限
(9, '查询角色', 'system:role:query', 2, 3, NULL, NULL, 1, 1),
(10, '新增角色', 'system:role:add', 2, 3, NULL, NULL, 2, 1),
(11, '编辑角色', 'system:role:edit', 2, 3, NULL, NULL, 3, 1),
(12, '删除角色', 'system:role:delete', 2, 3, NULL, NULL, 4, 1),

-- 权限管理按钮权限
(13, '查询权限', 'system:permission:query', 2, 4, NULL, NULL, 1, 1),
(14, '新增权限', 'system:permission:add', 2, 4, NULL, NULL, 2, 1),
(15, '编辑权限', 'system:permission:edit', 2, 4, NULL, NULL, 3, 1),
(16, '删除权限', 'system:permission:delete', 2, 4, NULL, NULL, 4, 1),

-- 接口权限
(17, '用户登录', 'auth:login', 3, 0, '/api/auth/login', NULL, 1, 1),
(18, '用户注册', 'auth:register', 3, 0, '/api/auth/register', NULL, 2, 1),
(19, '获取验证码', 'auth:captcha', 3, 0, '/api/auth/captcha', NULL, 3, 1),
(20, '刷新Token', 'auth:refresh', 3, 0, '/api/auth/refresh', NULL, 4, 1),
(21, '用户登出', 'auth:logout', 3, 0, '/api/auth/logout', NULL, 5, 1);

-- 为管理员角色分配所有权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) 
SELECT 1, id FROM sys_permission;

-- 为普通用户角色分配基本权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(2, 17), -- 用户登录
(2, 18), -- 用户注册
(2, 19), -- 获取验证码
(2, 20), -- 刷新Token
(2, 21); -- 用户登出

-- 为停车场管理员角色分配相关权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(3, 17), -- 用户登录
(3, 19), -- 获取验证码
(3, 20), -- 刷新Token
(3, 21); -- 用户登出

-- 创建默认管理员用户（密码：admin123）
INSERT IGNORE INTO sys_user (id, username, password, phone, email, status) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 'admin@smartpark.com', 1);

-- 为管理员用户分配管理员角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 输出初始化完成信息
SELECT '数据库表初始化完成！' AS message;
