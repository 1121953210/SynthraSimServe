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

-- -----------------------------------------------------------
-- 4. 权限管理表
-- -----------------------------------------------------------

-- 菜单/权限表（RBAC权限模型的核心表）
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示顶级',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（如system:user:list）',
  `menu_type` CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否显示：0=隐藏，1=显示',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `icon` VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- 角色-菜单/权限关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单/权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

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

-- ============================================================
-- 初始化菜单权限数据
-- ============================================================
-- 菜单层级结构：
--   系统管理(M) id=1
--     ├── 用户管理(C) id=100
--     │     ├── 用户查询(F) id=1001  perms=system:user:list
--     │     ├── 用户详情(F) id=1002  perms=system:user:query
--     │     ├── 用户新增(F) id=1003  perms=system:user:add
--     │     ├── 用户修改(F) id=1004  perms=system:user:edit
--     │     ├── 用户删除(F) id=1005  perms=system:user:remove
--     │     └── 重置密码(F) id=1006  perms=system:user:resetPwd
--     └── 角色管理(C) id=101
--           ├── 角色查询(F) id=1011  perms=system:role:list
--           ├── 角色详情(F) id=1012  perms=system:role:query
--           ├── 角色新增(F) id=1013  perms=system:role:add
--           ├── 角色修改(F) id=1014  perms=system:role:edit
--           └── 角色删除(F) id=1015  perms=system:role:remove
--   系统工具(M) id=2
--     └── 代码生成(C) id=200
--           ├── 生成查询(F) id=2001  perms=tool:gen:list
--           ├── 生成预览(F) id=2002  perms=tool:gen:preview
--           └── 生成下载(F) id=2003  perms=tool:gen:download

-- 一级目录
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `path`, `perms`, `menu_type`, `icon`) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 'M', 'system'),
(2, '系统工具', 0, 2, 'tool',   NULL, 'M', 'tool');

-- 二级菜单（页面）
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `path`, `perms`, `menu_type`, `icon`) VALUES
(100, '用户管理', 1, 1, 'user', NULL, 'C', 'user'),
(101, '角色管理', 1, 2, 'role', NULL, 'C', 'peoples'),
(200, '代码生成', 2, 1, 'gen',  NULL, 'C', 'code');

-- 三级按钮（操作权限）—— 用户管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `perms`, `menu_type`) VALUES
(1001, '用户查询', 100, 1, 'system:user:list',     'F'),
(1002, '用户详情', 100, 2, 'system:user:query',    'F'),
(1003, '用户新增', 100, 3, 'system:user:add',      'F'),
(1004, '用户修改', 100, 4, 'system:user:edit',     'F'),
(1005, '用户删除', 100, 5, 'system:user:remove',   'F'),
(1006, '重置密码', 100, 6, 'system:user:resetPwd', 'F');

-- 三级按钮（操作权限）—— 角色管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `perms`, `menu_type`) VALUES
(1011, '角色查询', 101, 1, 'system:role:list',   'F'),
(1012, '角色详情', 101, 2, 'system:role:query',  'F'),
(1013, '角色新增', 101, 3, 'system:role:add',    'F'),
(1014, '角色修改', 101, 4, 'system:role:edit',   'F'),
(1015, '角色删除', 101, 5, 'system:role:remove', 'F');

-- 三级按钮（操作权限）—— 代码生成
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `perms`, `menu_type`) VALUES
(2001, '生成查询', 200, 1, 'tool:gen:list',     'F'),
(2002, '生成预览', 200, 2, 'tool:gen:preview',  'F'),
(2003, '生成下载', 200, 3, 'tool:gen:download', 'F');

-- ============================================================
-- 给普通用户角色(id=2)分配权限
-- 普通用户可以：查看用户列表、查看用户详情、查看角色列表、查看角色详情
-- 普通用户不可以：新增/修改/删除用户、新增/修改/删除角色、重置密码
-- ============================================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 1),     -- 系统管理目录
(2, 100),   -- 用户管理菜单
(2, 1001),  -- system:user:list  （查看用户列表）
(2, 1002),  -- system:user:query （查看用户详情）
(2, 101),   -- 角色管理菜单
(2, 1011),  -- system:role:list  （查看角色列表）
(2, 1012);  -- system:role:query （查看角色详情）

-- 注意：admin角色不需要在此分配权限，代码中检测到admin角色直接赋予 *:*:* 通配权限
