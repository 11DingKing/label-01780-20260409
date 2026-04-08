-- Emergency Help Mini-Program Schema
-- MySQL 8.0

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `help_mp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `help_mp`;

-- ----------------------------
-- user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) NOT NULL COMMENT '微信 openid',
  `unionid` varchar(64) DEFAULT NULL,
  `session_key` varchar(64) DEFAULT NULL,
  `nick_name` varchar(64) DEFAULT NULL,
  `avatar_url` varchar(512) DEFAULT NULL,
  `phone_enc` varchar(256) DEFAULT NULL COMMENT '加密手机号',
  `phone_anon` tinyint DEFAULT 0 COMMENT '是否匿名手机 0否1是',
  `red_flower_total` int NOT NULL DEFAULT 0 COMMENT '累计小红花',
  `badge_level` tinyint NOT NULL DEFAULT 0 COMMENT '当前勋章等级 0无',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_red_flower` (`red_flower_total`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- help_request
-- ----------------------------
DROP TABLE IF EXISTS `help_request`;
CREATE TABLE `help_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `address` varchar(256) DEFAULT NULL COMMENT '位置描述',
  `address_anon` tinyint DEFAULT 0 COMMENT '是否匿名位置',
  `contact_id` bigint DEFAULT NULL COMMENT '关联紧急联系人ID',
  `urgency_level` tinyint NOT NULL COMMENT '1高2中3低',
  `content` text NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1已发布 2已关闭 0已删除',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_publish` (`status`,`publish_time`),
  KEY `idx_location` (`latitude`,`longitude`),
  KEY `idx_urgency` (`urgency_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求助信息';

-- ----------------------------
-- help_image
-- ----------------------------
DROP TABLE IF EXISTS `help_image`;
CREATE TABLE `help_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `help_id` bigint NOT NULL,
  `file_id` varchar(128) DEFAULT NULL COMMENT '微信 fileId',
  `url` varchar(512) NOT NULL COMMENT 'CDN/OSS URL',
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_help_id` (`help_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求助图片';

-- ----------------------------
-- help_contact
-- ----------------------------
DROP TABLE IF EXISTS `help_contact`;
CREATE TABLE `help_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name_enc` varchar(256) DEFAULT NULL COMMENT '加密姓名',
  `phone_enc` varchar(256) DEFAULT NULL,
  `relation` varchar(32) DEFAULT NULL COMMENT '关系',
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户紧急联系人';

-- ----------------------------
-- help_interaction
-- ----------------------------
DROP TABLE IF EXISTS `help_interaction`;
CREATE TABLE `help_interaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `help_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `type` varchar(16) NOT NULL COMMENT 'bless share tip',
  `red_flower_amount` int NOT NULL DEFAULT 0,
  `extra` varchar(256) DEFAULT NULL COMMENT '如 tip 订单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_help_user_type` (`help_id`,`user_id`,`type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_help_id` (`help_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动记录(祝福/转发/打赏)';

-- ----------------------------
-- tip_order
-- ----------------------------
DROP TABLE IF EXISTS `tip_order`;
CREATE TABLE `tip_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `help_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `amount_cents` int NOT NULL COMMENT '分',
  `wx_transaction_id` varchar(64) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭 3已退款',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` datetime DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_help_id` (`help_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打赏订单';

-- ----------------------------
-- red_flower_log
-- ----------------------------
DROP TABLE IF EXISTS `red_flower_log`;
CREATE TABLE `red_flower_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `biz_type` varchar(32) NOT NULL COMMENT 'bless share tip',
  `biz_id` varchar(64) DEFAULT NULL,
  `amount` int NOT NULL COMMENT '正收入负支出',
  `balance_after` int NOT NULL DEFAULT 0,
  `remark` varchar(128) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小红花流水';

-- ----------------------------
-- badge
-- ----------------------------
DROP TABLE IF EXISTS `badge`;
CREATE TABLE `badge` (
  `level` tinyint NOT NULL,
  `name` varchar(32) NOT NULL,
  `icon_url` varchar(512) DEFAULT NULL,
  `min_flowers` int NOT NULL COMMENT '达到该等级最少小红花',
  `description` varchar(256) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='勋章等级定义';

INSERT INTO `badge` (`level`,`name`,`icon_url`,`min_flowers`,`description`,`sort_order`) VALUES
(1,'初级守护者','/images/badge-1.png',10,'10朵',1),
(2,'中级守护者','/images/badge-2.png',50,'50朵',2),
(3,'高级守护者','/images/badge-3.png',200,'200朵',3),
(4,'资深守护者','/images/badge-4.png',500,'500朵',4),
(5,'荣耀守护者','/images/badge-5.png',1000,'1000朵',5);

-- ----------------------------
-- user_badge (冗余记录，当前以 user.badge_level 为准)
-- ----------------------------
DROP TABLE IF EXISTS `user_badge`;
CREATE TABLE `user_badge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `badge_level` tinyint NOT NULL,
  `grant_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_level` (`user_id`,`badge_level`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户勋章获得记录';

-- ----------------------------
-- push_rule
-- ----------------------------
DROP TABLE IF EXISTS `push_rule`;
CREATE TABLE `push_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `radius_km` decimal(6,2) NOT NULL DEFAULT 10.00 COMMENT '推送半径 km',
  `urgency_levels` varchar(32) NOT NULL DEFAULT '1,2,3' COMMENT '参与推送的紧急程度 逗号分隔',
  `enabled` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号推送规则';

-- ----------------------------
-- push_log (推送日志与到达率统计)
-- ----------------------------
DROP TABLE IF EXISTS `push_log`;
CREATE TABLE `push_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `help_id` bigint NOT NULL,
  `rule_id` bigint DEFAULT NULL,
  `total_count` int NOT NULL DEFAULT 0 COMMENT '推送总人数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `reach_rate` decimal(5,4) DEFAULT NULL COMMENT '到达率',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_help_id` (`help_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送日志';

-- ----------------------------
-- operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `module` varchar(32) NOT NULL,
  `action` varchar(32) NOT NULL,
  `operator_id` varchar(64) DEFAULT NULL,
  `target_id` varchar(64) DEFAULT NULL,
  `detail` varchar(512) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module_action` (`module`,`action`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

-- ----------------------------
-- admin_user (简单管理员)
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(128) NOT NULL,
  `nick_name` varchar(64) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员';

-- default admin admin / admin123 (BCrypt)
INSERT INTO `admin_user` (`username`,`password_hash`,`nick_name`) VALUES
('admin','$2a$10$Rm15aZXQJejAQZPNyJXehe41Ku5XvxcIFwzan6/ymtvA9WYwot5M6','系统管理员');

-- ----------------------------
-- user_location (公众号粉丝位置，用于地理推送过滤)
-- ----------------------------
DROP TABLE IF EXISTS `user_location`;
CREATE TABLE `user_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) NOT NULL COMMENT '公众号openid',
  `latitude` decimal(10,7) DEFAULT NULL,
  `longitude` decimal(10,7) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_location` (`latitude`,`longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='粉丝位置（公众号上报）';

-- ----------------------------
-- perf_log (性能监控日志)
-- ----------------------------
DROP TABLE IF EXISTS `perf_log`;
CREATE TABLE `perf_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uri` varchar(256) NOT NULL,
  `method` varchar(8) NOT NULL,
  `duration_ms` bigint NOT NULL COMMENT '响应耗时(ms)',
  `status_code` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uri` (`uri`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_duration` (`duration_ms`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口性能日志';

-- ----------------------------
-- experience_note (用户手动发布的助人经历)
-- ----------------------------
DROP TABLE IF EXISTS `experience_note`;
CREATE TABLE `experience_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(128) NOT NULL COMMENT '经历标题',
  `content` text NOT NULL COMMENT '经历描述',
  `help_id` bigint DEFAULT NULL COMMENT '关联求助ID（可选）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 0已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户助人经历';

SET FOREIGN_KEY_CHECKS = 1;
