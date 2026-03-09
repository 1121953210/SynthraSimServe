-- ============================================================
-- SynthraSim 工业仿真平台 - 数据库初始化脚本
-- 数据库: synthrasim_server
-- 字符集: utf8mb4 | 引擎: InnoDB
-- ============================================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS `synthrasim_server` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `synthrasim_server`;

-- -----------------------------------------------------------
-- 1. 用户管理模块
-- -----------------------------------------------------------

-- 1.1 用户基本信息表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名（登录账号）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
  `office_phone` VARCHAR(32) DEFAULT NULL COMMENT '办公电话',
  `work_location` VARCHAR(255) DEFAULT NULL COMMENT '工作地',
  `org_id` BIGINT DEFAULT NULL COMMENT '所属组织机构ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账号状态：0=禁用，1=启用',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户基本信息表';

-- 1.2 组织机构表
DROP TABLE IF EXISTS `sys_organization`;
CREATE TABLE `sys_organization` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级机构ID，0表示顶级',
  `org_name` VARCHAR(128) NOT NULL COMMENT '机构名称',
  `org_code` VARCHAR(64) DEFAULT NULL COMMENT '机构编码',
  `org_path` VARCHAR(512) DEFAULT NULL COMMENT '完整路径',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织机构表';

-- 1.3 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 1.4 用户-角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- 1.5 用户登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `operation_type` TINYINT(1) NOT NULL COMMENT '操作类型：1=登录，2=注销登录',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '浏览器User-Agent',
  `login_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作结果：0=失败，1=成功',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户登录日志表';

-- 1.6 帮助文档表
DROP TABLE IF EXISTS `sys_help_doc`;
CREATE TABLE `sys_help_doc` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文档ID，0表示顶级',
  `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
  `content` LONGTEXT DEFAULT NULL COMMENT '文档内容',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=草稿，1=已发布',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帮助文档表';

-- -----------------------------------------------------------
-- 2. 项目管理模块
-- -----------------------------------------------------------

DROP TABLE IF EXISTS `biz_project`;
CREATE TABLE `biz_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '项目简介',
  `cover_image` VARCHAR(512) DEFAULT NULL COMMENT '封面图URL',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '项目状态：0=已完成，1=进行中',
  `owner_id` BIGINT NOT NULL COMMENT '项目所有者用户ID',
  `last_access_time` DATETIME DEFAULT NULL COMMENT '最后访问时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_update_time` (`update_time`),
  KEY `idx_last_access_time` (`last_access_time`),
  KEY `idx_project_name` (`project_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目表';

-- -----------------------------------------------------------
-- 3. 代码生成模块辅助表
-- -----------------------------------------------------------

DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` VARCHAR(200) DEFAULT '' COMMENT '表名称',
  `table_comment` VARCHAR(500) DEFAULT '' COMMENT '表描述',
  `class_name` VARCHAR(100) DEFAULT '' COMMENT '实体类名称',
  `package_name` VARCHAR(100) DEFAULT '' COMMENT '生成包路径',
  `module_name` VARCHAR(30) DEFAULT '' COMMENT '生成模块名',
  `business_name` VARCHAR(30) DEFAULT '' COMMENT '生成业务名',
  `function_name` VARCHAR(50) DEFAULT '' COMMENT '生成功能名',
  `function_author` VARCHAR(50) DEFAULT '' COMMENT '生成功能作者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表';

DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` BIGINT DEFAULT NULL COMMENT '归属表编号',
  `column_name` VARCHAR(200) DEFAULT '' COMMENT '列名称',
  `column_comment` VARCHAR(500) DEFAULT '' COMMENT '列描述',
  `column_type` VARCHAR(100) DEFAULT '' COMMENT '列类型',
  `java_type` VARCHAR(500) DEFAULT '' COMMENT 'JAVA类型',
  `java_field` VARCHAR(200) DEFAULT '' COMMENT 'JAVA字段名',
  `is_pk` CHAR(1) DEFAULT '0' COMMENT '是否主键（1是）',
  `is_increment` CHAR(1) DEFAULT '0' COMMENT '是否自增（1是）',
  `is_required` CHAR(1) DEFAULT '0' COMMENT '是否必填（1是）',
  `is_list` CHAR(1) DEFAULT '0' COMMENT '是否列表字段（1是）',
  `is_query` CHAR(1) DEFAULT '0' COMMENT '是否查询字段（1是）',
  `is_edit` CHAR(1) DEFAULT '0' COMMENT '是否编辑字段（1是）',
  `query_type` VARCHAR(200) DEFAULT 'EQ' COMMENT '查询方式',
  `html_type` VARCHAR(200) DEFAULT '' COMMENT '显示类型',
  `sort` INT DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表字段';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化组织机构
INSERT INTO `sys_organization` (`id`, `parent_id`, `org_name`, `org_code`, `org_path`, `sort_order`) VALUES
(1, 0, 'SynthraSim科技有限公司', 'ROOT', 'SynthraSim科技有限公司', 0),
(2, 1, '研发部', 'DEV', 'SynthraSim科技有限公司/研发部', 1),
(3, 1, '测试部', 'QA', 'SynthraSim科技有限公司/测试部', 2);

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'admin', '拥有系统所有权限'),
(2, '普通用户', 'user', '普通用户角色，具备基本操作权限');

-- 初始化管理员账号（密码：admin123，BCrypt加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `org_id`, `status`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'admin@synthrasim.com', '13800000000', 1, 1);

-- 初始化普通用户（密码：user123，BCrypt加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `org_id`, `status`) VALUES
(2, 'zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', 'zhangsan@synthrasim.com', '13800000001', 2, 1);

-- 管理员分配admin角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
-- 普通用户分配user角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2);
