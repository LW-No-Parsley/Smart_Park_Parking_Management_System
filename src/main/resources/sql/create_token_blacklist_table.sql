-- 创建JWT jti黑名单表
-- 用于存储被拉黑的token的jti（JWT ID）
CREATE TABLE IF NOT EXISTS `sys_token_jti_blacklist` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    `jti` varchar(64) NOT NULL COMMENT 'JWT ID (jti)',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `expiration_time` datetime NOT NULL COMMENT '过期时间',
    `invalidated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间（加入黑名单的时间）',
    `reason` varchar(255) DEFAULT NULL COMMENT '加入黑名单原因：1-用户登出，2-密码修改，3-管理员强制下线',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_jti` (`jti`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expiration_time` (`expiration_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='JWT jti黑名单表';
