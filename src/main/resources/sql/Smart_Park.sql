/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:3306
 Source Schema         : smart_park

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 30/04/2026 00:19:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for access_log
-- ----------------------------
DROP TABLE IF EXISTS `access_log`;
CREATE TABLE `access_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `park_area_id` bigint NOT NULL COMMENT '园区ID',
  `gate_id` bigint NOT NULL COMMENT '道闸ID',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '识别车牌号',
  `vehicle_id` bigint NULL DEFAULT NULL COMMENT '关联车辆ID（可为空）',
  `access_type` tinyint NOT NULL COMMENT '进出类型：1-入场，2-出场',
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '抓拍图片地址',
  `recognition_result` tinyint NULL DEFAULT 1 COMMENT '识别结果：0-失败，1-成功，2-黑名单',
  `access_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通行时间',
  `handled_by` bigint NULL DEFAULT NULL COMMENT '处理人员ID（后台 sys_user.id，用于手动干预）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注（手动放行原因等）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plate`(`plate_number` ASC) USING BTREE,
  INDEX `idx_access_time`(`access_time` ASC) USING BTREE,
  INDEX `idx_gate`(`gate_id` ASC) USING BTREE,
  INDEX `idx_vehicle`(`vehicle_id` ASC) USING BTREE,
  INDEX `idx_park_area`(`park_area_id` ASC) USING BTREE,
  INDEX `idx_handled_by`(`handled_by` ASC) USING BTREE,
  CONSTRAINT `fk_access_gate` FOREIGN KEY (`gate_id`) REFERENCES `gate_device` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_access_handled_by_sys_user` FOREIGN KEY (`handled_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_access_park_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_access_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '进出记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for blacklist
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加入原因',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人（sys_user.id）',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人（sys_user.id）',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-生效',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `park_area_id` bigint NULL DEFAULT NULL COMMENT '所属园区ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_plate_status_end_time`(`plate_number` ASC, `status` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_plate_start_time`(`plate_number` ASC, `start_time` ASC) USING BTREE,
  INDEX `fk_blacklist_created_by`(`created_by` ASC) USING BTREE,
  INDEX `fk_blacklist_updated_by`(`updated_by` ASC) USING BTREE,
  INDEX `idx_blacklist_area`(`park_area_id` ASC) USING BTREE,
  CONSTRAINT `fk_blacklist_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_blacklist_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_blacklist_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '黑名单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exception_report
-- ----------------------------
DROP TABLE IF EXISTS `exception_report`;
CREATE TABLE `exception_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异常ID',
  `user_id` bigint NOT NULL COMMENT '上报用户ID',
  `space_id` bigint NULL DEFAULT NULL COMMENT '关联车位ID',
  `report_type` tinyint NOT NULL COMMENT '异常类型：1-车位被占，2-设备故障，3-违停，4-其他',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '异常描述',
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片地址',
  `status` tinyint NULL DEFAULT 0 COMMENT '处理状态：0-未处理，1-已受理，2-已处理，3-已关闭',
  `handled_by` bigint NULL DEFAULT NULL COMMENT '处理人员ID（后台 sys_user.id）',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_handled_by`(`handled_by` ASC) USING BTREE,
  CONSTRAINT `fk_exception_handled_by_sys_user` FOREIGN KEY (`handled_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_exception_space` FOREIGN KEY (`space_id`) REFERENCES `parking_space` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_exception_user` FOREIGN KEY (`user_id`) REFERENCES `park_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '异常上报表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gate_device
-- ----------------------------
DROP TABLE IF EXISTS `gate_device`;
CREATE TABLE `gate_device`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `park_area_id` bigint NOT NULL COMMENT '园区ID',
  `gate_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '道闸名称（如东门入口、西门出口等）',
  `device_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备序列号',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备IP地址',
  `device_type` tinyint NULL DEFAULT 1 COMMENT '设备类型：1-入口道闸，2-出口道闸',
  `status` tinyint NULL DEFAULT 1 COMMENT '设备状态：0-离线，1-在线，2-故障',
  `last_heartbeat` datetime NULL DEFAULT NULL COMMENT '最后心跳时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_sn`(`device_sn` ASC) USING BTREE,
  INDEX `idx_park_area`(`park_area_id` ASC) USING BTREE,
  CONSTRAINT `fk_gate_park_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '道闸设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '操作用户ID（sys_user.id）',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模块名称',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作动作',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作详情（JSON格式）',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表（业务操作）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for park_area
-- ----------------------------
DROP TABLE IF EXISTS `park_area`;
CREATE TABLE `park_area`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '园区ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '园区名称',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '园区地址',
  `total_spaces` int NULL DEFAULT 0 COMMENT '总车位数量',
  `primary_admin_user_id` bigint NULL DEFAULT NULL COMMENT '主园区管理员ID（sys_user.id）',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '园区纬度坐标',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '园区经度坐标',
  `business_hours_start` time NULL DEFAULT '08:00:00' COMMENT '营业开始时间',
  `business_hours_end` time NULL DEFAULT '22:00:00' COMMENT '营业结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '园区状态：0-关闭，1-开放',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_primary_admin_user`(`primary_admin_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_park_area_primary_admin` FOREIGN KEY (`primary_admin_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '园区表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for park_user
-- ----------------------------
DROP TABLE IF EXISTS `park_user`;
CREATE TABLE `park_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名（微信昵称）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信openid（用于微信登录）',
  `user_type` tinyint NOT NULL COMMENT '用户类型：1-车主，2-访客。',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  `status` tinyint NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '园区用户表（车主/访客等业务用户）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for parking_fee_rule
-- ----------------------------
DROP TABLE IF EXISTS `parking_fee_rule`;
CREATE TABLE `parking_fee_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `park_area_id` bigint NULL DEFAULT NULL COMMENT '园区ID（NULL表示全局规则）',
  `vehicle_type` int NULL DEFAULT NULL COMMENT '车辆类型（1-小型车，2-大型车，NULL表示所有类型）',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称，如\"标准计费\"',
  `fee_mode` int NOT NULL COMMENT '计费模式：1-按时间段计费，2-按次计费，3-阶梯计费',
  `free_minutes` int NULL DEFAULT 0 COMMENT '免费时长（分钟）',
  `unit_duration` int NULL DEFAULT NULL COMMENT '计费单位时长（分钟），如20',
  `unit_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '每单位时长价格，如4.00',
  `daily_cap` decimal(10, 2) NULL DEFAULT NULL COMMENT '每日封顶金额（NULL表示不封顶）',
  `max_charge_hours` int NULL DEFAULT NULL COMMENT '最大计费小时数（超过后不再计费）',
  `time_periods` json NULL COMMENT '时段定价，如：[{\"start\":\"08:00\",\"end\":\"20:00\",\"unitPrice\":4},{\"start\":\"20:00\",\"end\":\"08:00\",\"unitPrice\":2}]',
  `tiered_pricing` json NULL COMMENT '阶梯定价，如：[{\"fromHour\":0,\"toHour\":2,\"price\":10},{\"fromHour\":2,\"toHour\":4,\"price\":15}]',
  `fixed_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '鎸夋?璁¤垂鐨勫浐瀹氫环鏍',
  `status` int NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序（优先级，数字越小优先级越高）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '停车计费规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for parking_space
-- ----------------------------
DROP TABLE IF EXISTS `parking_space`;
CREATE TABLE `parking_space`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车位ID',
  `park_area_id` bigint NOT NULL COMMENT '园区ID',
  `zone_id` bigint NULL DEFAULT NULL COMMENT '分区ID（parking_zone.id）',
  `space_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号（如A-101）',
  `space_type` tinyint NOT NULL DEFAULT 2 COMMENT '车位类型：1-固定，2-临时，3-访客，4-残障专用',
  `status` tinyint NULL DEFAULT 1 COMMENT '车位状态：0-禁用，1-正常，4-故障',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '车位纬度坐标（用于导航）',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '车位经度坐标（用于导航）',
  `bind_user_id` bigint NULL DEFAULT NULL COMMENT '绑定用户ID（固定车位，关联 park_user.id）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_bind_user`(`bind_user_id` ASC) USING BTREE,
  INDEX `idx_space_type`(`space_type` ASC) USING BTREE,
  INDEX `idx_zone_id`(`zone_id` ASC) USING BTREE,
  INDEX `idx_area_zone_status`(`park_area_id` ASC, `zone_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_space_bind_user` FOREIGN KEY (`bind_user_id`) REFERENCES `park_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_space_park_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_space_zone` FOREIGN KEY (`zone_id`) REFERENCES `parking_zone` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for parking_zone
-- ----------------------------
DROP TABLE IF EXISTS `parking_zone`;
CREATE TABLE `parking_zone`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  `park_area_id` bigint NOT NULL COMMENT '园区ID',
  `zone_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分区名称（如A区、B区）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分区描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_area_zone`(`park_area_id` ASC, `zone_name` ASC) USING BTREE,
  INDEX `idx_park_area_id`(`park_area_id` ASC) USING BTREE,
  CONSTRAINT `fk_zone_park_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位分区表（单层分区）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `reservation_id` bigint NOT NULL COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` tinyint NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-余额支付',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '第三方支付交易ID',
  `payment_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-支付成功，2-支付失败，3-已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reservation`(`reservation_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_payment_time`(`payment_time` ASC) USING BTREE,
  CONSTRAINT `fk_payment_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `park_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for refund_record
-- ----------------------------
DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款ID',
  `payment_id` bigint NOT NULL COMMENT '关联支付记录ID',
  `reservation_id` bigint NOT NULL COMMENT '关联预约ID',
  `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `refund_status` tinyint NULL DEFAULT 0 COMMENT '退款状态：0-处理中，1-成功，2-失败',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `transaction_refund_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方退款交易号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment`(`payment_id` ASC) USING BTREE,
  INDEX `idx_reservation`(`reservation_id` ASC) USING BTREE,
  CONSTRAINT `fk_refund_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_refund_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（park_user.id）',
  `vehicle_id` bigint NOT NULL COMMENT '车辆ID（vehicle.id）',
  `space_id` bigint NOT NULL COMMENT '车位ID（parking_space.id）',
  `reservation_type` tinyint NOT NULL DEFAULT 1 COMMENT '预约类型：1-车主预约，2-访客申请，3-保安现场',
  `approval_status` tinyint NOT NULL DEFAULT 1 COMMENT '审批状态：0-待审批，1-通过，2-拒绝',
  `approved_by` bigint NULL DEFAULT NULL COMMENT '审批人（sys_user.id）',
  `approved_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人（sys_user.id；小程序创建则为空）',
  `source` tinyint NOT NULL DEFAULT 1 COMMENT '创建来源：1-小程序，2-后台管理员，3-保安',
  `start_time` datetime(6) NOT NULL,
  `end_time` datetime(6) NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '预约状态：0-已取消，1-已预约，2-已使用，3-已过期',
  `arrive_time` datetime NULL DEFAULT NULL COMMENT '实际到达时间',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '实际离开时间',
  `total_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '最终结算停车费用（整单）',
  `paid_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '已支付金额（缓存字段，便于列表查询）',
  `settlement_time` datetime NULL DEFAULT NULL COMMENT '结算时间（通常为离场或管理员结算时）',
  `pay_status` tinyint NULL DEFAULT 0 COMMENT '整单支付状态：0-未支付，1-已支付，2-部分支付，3-已退款',
  `create_time` datetime(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  `update_time` datetime(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_vehicle`(`vehicle_id` ASC) USING BTREE,
  INDEX `idx_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_time_range`(`start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_approval_status`(`approval_status` ASC) USING BTREE,
  INDEX `idx_pay_status`(`pay_status` ASC) USING BTREE,
  INDEX `idx_space_time`(`space_id` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `fk_reservation_approved_by`(`approved_by` ASC) USING BTREE,
  INDEX `fk_reservation_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_space_status_time`(`space_id` ASC, `status` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  CONSTRAINT `fk_reservation_approved_by` FOREIGN KEY (`approved_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_reservation_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_reservation_space` FOREIGN KEY (`space_id`) REFERENCES `parking_space` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `park_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_reservation_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for space_occupy
-- ----------------------------
DROP TABLE IF EXISTS `space_occupy`;
CREATE TABLE `space_occupy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL COMMENT 'ID',
  `reservation_id` bigint NOT NULL COMMENT '预订ID',
  `start_time` datetime(6) NOT NULL COMMENT '开始时间',
  `end_time` datetime(6) NOT NULL COMMENT '结束时间',
  `create_time` datetime(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_space_time`(`space_id` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_space_range`(`space_id` ASC, `start_time` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '占用表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_captcha
-- ----------------------------
DROP TABLE IF EXISTS `sys_captcha`;
CREATE TABLE `sys_captcha`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `captcha_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验证码唯一标识',
  `captcha_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验证码',
  `captcha_type` tinyint NULL DEFAULT 1 COMMENT '验证码类型：1-登录，2-注册，3-重置密码',
  `expiration_time` datetime NOT NULL COMMENT '过期时间',
  `used` tinyint NULL DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_captcha_id`(`captcha_id` ASC) USING BTREE,
  INDEX `idx_expiration_time`(`expiration_time` ASC) USING BTREE,
  INDEX `idx_used`(`used` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 210 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '验证码表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `login_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint NOT NULL COMMENT '登录状态：0-失败，1-成功',
  `failure_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_login_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码',
  `permission_type` tinyint NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_permission_type`(`permission_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_token_jti_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `sys_token_jti_blacklist`;
CREATE TABLE `sys_token_jti_blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `jti` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'JWT ID (jti)',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联后台用户 sys_user.id',
  `expiration_time` datetime NOT NULL COMMENT 'token过期时间',
  `invalidated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拉黑原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_jti`(`jti` ASC) USING BTREE,
  INDEX `idx_expiration_time`(`expiration_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_jti_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'JWT jti黑名单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表（后台管理员）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_park_area
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_park_area`;
CREATE TABLE `sys_user_park_area`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint NOT NULL COMMENT '后台用户ID sys_user.id',
  `park_area_id` bigint NOT NULL COMMENT '园区ID park_area.id',
  `role_in_park` tinyint NOT NULL DEFAULT 1 COMMENT '园区内角色：1-园区管理员，2-保安',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_park`(`user_id` ASC, `park_area_id` ASC) USING BTREE,
  INDEX `idx_park_area_id`(`park_area_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_park_area` FOREIGN KEY (`park_area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_park_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台用户-园区授权范围表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `config_type` tinyint NULL DEFAULT 1 COMMENT '配置类型：1-系统配置，2-业务配置',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for vehicle
-- ----------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联 park_user.id）',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认车牌：0-否，1-是',
  `vehicle_type` tinyint NULL DEFAULT 1 COMMENT '车辆类型：1-小车，2-大车，3-新能源车',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆品牌',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆颜色',
  `status` tinyint NULL DEFAULT 1 COMMENT '车辆状态：0-禁用，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plate_deleted`(`plate_number` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_vehicle_user` FOREIGN KEY (`user_id`) REFERENCES `park_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车辆表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
