-- 停车计费规则表
-- 注意：如果表已存在但缺少字段，请执行 fix_parking_fee_rule_table.sql
CREATE TABLE IF NOT EXISTS `parking_fee_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `park_area_id` bigint DEFAULT NULL COMMENT '园区ID（NULL表示全局规则）',
  `vehicle_type` int DEFAULT NULL COMMENT '车辆类型（1-小型车，2-大型车，NULL表示所有类型）',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称，如"标准计费"',
  
  -- 计费模式
  `fee_mode` int NOT NULL COMMENT '计费模式：1-按时间段计费，2-按次计费，3-阶梯计费',
  
  -- 按时间段计费参数
  `free_minutes` int DEFAULT '0' COMMENT '免费时长（分钟）',
  `unit_duration` int DEFAULT NULL COMMENT '计费单位时长（分钟），如20',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '每单位时长价格，如4.00',
  
  -- 封顶设置
  `daily_cap` decimal(10,2) DEFAULT NULL COMMENT '每日封顶金额（NULL表示不封顶）',
  `max_charge_hours` int DEFAULT NULL COMMENT '最大计费小时数（超过后不再计费）',
  
  -- 时段差异化定价（JSON格式，使用text类型兼容更多MySQL版本）
  `time_periods` text DEFAULT NULL COMMENT '时段定价，如：[{"start":"08:00","end":"20:00","unitPrice":4,"unitDuration":20},{"start":"20:00","end":"08:00","unitPrice":2,"unitDuration":30}]',
  
  -- 阶梯计费参数（JSON格式，使用text类型兼容更多MySQL版本）
  `tiered_pricing` text DEFAULT NULL COMMENT '阶梯定价，如：[{"fromHour":0,"toHour":2,"price":10},{"fromHour":2,"toHour":4,"price":15,"perHour":true}]',
  
  -- 按次计费参数
  `fixed_price` decimal(10,2) DEFAULT NULL COMMENT '按次计费的固定价格',
  
  -- 基础信息
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小优先级越高）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注说明',
  
  -- 时间字段
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_park_area_id` (`park_area_id`) USING BTREE,
  KEY `idx_vehicle_type` (`vehicle_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车计费规则表';

-- 插入默认全局计费规则（20分钟4元）
INSERT IGNORE INTO `parking_fee_rule` (`rule_name`, `fee_mode`, `free_minutes`, `unit_duration`, `unit_price`, `daily_cap`, `status`, `sort_order`, `remark`) 
VALUES ('标准计费', 1, 0, 20, 4.00, 50.00, 1, 0, '默认计费规则：每20分钟4元，每日封顶50元');
