# 工业仿真平台 MySQL 数据库表结构设计文档

## 1. 文档概述

### 1.1 文档目的

本文档定义工业仿真平台的 MySQL 数据库表结构设计，涵盖用户管理、项目管理、建模管理、仿真管理、后处理、模型库管理、仿真结果集管理、报告库管理等核心业务模块的数据存储方案。

### 1.2 设计规范

| 规范项 | 说明 |
|--------|------|
| 存储引擎 | InnoDB |
| 字符集 | utf8mb4 |
| 排序规则 | utf8mb4_general_ci |
| 主键策略 | 自增 BIGINT（`id`） |
| 逻辑删除 | `is_deleted` TINYINT(1)，0=未删除，1=已删除 |
| 时间字段 | `create_time` / `update_time`，类型 DATETIME，默认 CURRENT_TIMESTAMP |
| 命名风格 | 小写 + 下划线（snake_case） |

### 1.3 数据库表总览

| 序号 | 表名 | 所属模块 | 说明 |
|------|------|----------|------|
| 1 | sys_user | 用户管理 | 用户基本信息表 |
| 2 | sys_organization | 用户管理 | 组织机构表 |
| 3 | sys_role | 用户管理 | 角色表 |
| 4 | sys_user_role | 用户管理 | 用户-角色关联表 |
| 5 | sys_login_log | 用户管理 | 用户登录日志表 |
| 6 | biz_project | 项目管理 | 项目表 |
| 7 | biz_model_category | 建模管理 | 模型库浏览器分类表（内置模型分类） |
| 8 | biz_model_template | 建模管理 | 模型模板表（内置模型定义） |
| 9 | biz_model_template_param | 建模管理 | 模型模板参数定义表 |
| 10 | biz_model_template_port | 建模管理 | 模型模板接口定义表 |
| 11 | biz_user_model | 建模管理 | 用户模型表（画布中的模型实例） |
| 12 | biz_user_model_param_value | 建模管理 | 用户模型参数值表 |
| 13 | biz_canvas | 建模管理 | 画布表 |
| 14 | biz_canvas_component | 建模管理 | 画布组件实例表 |
| 15 | biz_canvas_connection | 建模管理 | 画布组件连线表 |
| 16 | biz_simulation_config | 仿真管理 | 仿真配置表 |
| 17 | biz_simulation_task | 仿真管理 | 仿真任务执行表 |
| 18 | biz_simulation_log | 仿真管理 | 仿真日志表 |
| 19 | biz_simulation_result | 仿真管理 | 仿真结果数据表 |
| 20 | biz_postprocess_chart | 后处理 | 后处理图表配置表 |
| 21 | biz_postprocess_curve | 后处理 | 后处理曲线数据表 |
| 22 | biz_model_lib_folder | 模型库管理 | 个人模型库文件夹表（树结构） |
| 23 | biz_model_lib_file | 模型库管理 | 个人模型库文件表 |
| 24 | biz_result_set_folder | 仿真结果集 | 仿真结果集文件夹表（树结构） |
| 25 | biz_result_set_file | 仿真结果集 | 仿真结果集文件表 |
| 26 | biz_report_folder | 报告库 | 报告库文件夹表（树结构） |
| 27 | biz_report | 报告库 | 报告表 |
| 28 | biz_report_template | 报告库 | 报告模板表 |
| 29 | biz_share_record | 共享管理 | 资源共享记录表 |
| 30 | sys_help_doc | 系统管理 | 帮助文档表 |

---

## 2. 用户管理模块

### 2.1 用户基本信息表（sys_user）

存储平台所有用户的基本信息、账号凭证及联系方式。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| username | VARCHAR(64) | NOT NULL | — | 用户名（登录账号，唯一） |
| password | VARCHAR(255) | NOT NULL | — | 密码（加密存储） |
| real_name | VARCHAR(64) | NULL | NULL | 真实姓名 |
| avatar | VARCHAR(512) | NULL | NULL | 头像URL |
| email | VARCHAR(128) | NULL | NULL | 电子邮箱 |
| phone | VARCHAR(20) | NULL | NULL | 手机号码 |
| office_phone | VARCHAR(32) | NULL | NULL | 办公电话 |
| work_location | VARCHAR(255) | NULL | NULL | 工作地 |
| org_id | BIGINT | NULL | NULL | 所属组织机构ID |
| status | TINYINT(1) | NOT NULL | 1 | 账号状态：0=禁用，1=启用 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |
| update_by | BIGINT | NULL | NULL | 更新人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| uk_username | UNIQUE | username | 用户名唯一索引 |
| idx_email | NORMAL | email | 邮箱查询索引 |
| idx_phone | NORMAL | phone | 手机号查询索引 |
| idx_org_id | NORMAL | org_id | 组织机构查询索引 |

### 2.2 组织机构表（sys_organization）

存储组织机构的层级关系，支持多级组织结构（如公司/部门/科室）。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父级机构ID，0表示顶级 |
| org_name | VARCHAR(128) | NOT NULL | — | 机构名称 |
| org_code | VARCHAR(64) | NULL | NULL | 机构编码 |
| org_path | VARCHAR(512) | NULL | NULL | 完整路径（如 A公司/B部门/C科室） |
| sort_order | INT | NOT NULL | 0 | 显示排序 |
| status | TINYINT(1) | NOT NULL | 1 | 状态：0=禁用，1=启用 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父级机构查询索引 |
| uk_org_code | UNIQUE | org_code | 机构编码唯一索引 |

### 2.3 角色表（sys_role）

定义系统中的角色，用于权限控制。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| role_name | VARCHAR(64) | NOT NULL | — | 角色名称 |
| role_code | VARCHAR(64) | NOT NULL | — | 角色编码（唯一） |
| description | VARCHAR(255) | NULL | NULL | 角色描述 |
| status | TINYINT(1) | NOT NULL | 1 | 状态：0=禁用，1=启用 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| uk_role_code | UNIQUE | role_code | 角色编码唯一索引 |

### 2.4 用户-角色关联表（sys_user_role）

用户与角色的多对多关联表。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | NOT NULL | — | 用户ID |
| role_id | BIGINT | NOT NULL | — | 角色ID |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| uk_user_role | UNIQUE | user_id, role_id | 用户角色联合唯一索引 |
| idx_role_id | NORMAL | role_id | 角色查询索引 |

### 2.5 用户登录日志表（sys_login_log）

记录用户的登录/注销操作日志，支持分页查询及按时间倒序展示。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | NOT NULL | — | 用户ID |
| username | VARCHAR(64) | NOT NULL | — | 用户名（冗余字段，便于查询展示） |
| operation_type | TINYINT(1) | NOT NULL | — | 操作类型：1=登录，2=注销登录 |
| ip_address | VARCHAR(64) | NULL | NULL | 操作IP地址 |
| user_agent | VARCHAR(512) | NULL | NULL | 浏览器 User-Agent |
| login_status | TINYINT(1) | NOT NULL | 1 | 操作结果：0=失败，1=成功 |
| fail_reason | VARCHAR(255) | NULL | NULL | 失败原因 |
| operation_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 操作时间（精确到分钟展示） |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_user_id | NORMAL | user_id | 用户ID查询索引 |
| idx_username | NORMAL | username | 用户名查询索引 |
| idx_operation_time | NORMAL | operation_time | 操作时间排序索引 |

---

## 3. 项目管理模块

### 3.1 项目表（biz_project）

存储用户创建的建模仿真项目信息，支持按时间排序、搜索和分页展示。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| project_name | VARCHAR(128) | NOT NULL | — | 项目名称 |
| description | VARCHAR(512) | NULL | NULL | 项目简介 |
| cover_image | VARCHAR(512) | NULL | NULL | 项目封面图/缩略图URL |
| status | TINYINT(1) | NOT NULL | 1 | 项目状态：0=已完成，1=进行中 |
| owner_id | BIGINT | NOT NULL | — | 项目所有者（创建者）用户ID |
| last_access_time | DATETIME | NULL | NULL | 最后访问时间（用于"最近访问"排序） |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |
| update_by | BIGINT | NULL | NULL | 更新人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_owner_id | NORMAL | owner_id | 项目所有者查询索引 |
| idx_update_time | NORMAL | update_time | 按编辑时间排序索引 |
| idx_last_access_time | NORMAL | last_access_time | 按最后访问时间排序索引 |
| idx_project_name | NORMAL | project_name | 项目名称搜索索引 |

---

## 4. 建模管理模块

### 4.1 模型库浏览器分类表（biz_model_category）

定义内置模型库的分类树结构，支持多级分类。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父分类ID，0表示顶级分类 |
| category_name | VARCHAR(128) | NOT NULL | — | 分类名称 |
| icon | VARCHAR(255) | NULL | NULL | 分类图标 |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父分类查询索引 |

### 4.2 模型模板表（biz_model_template）

定义平台内置的模型模板（如电池性能安全演化模型、电化学模型、双态热模型等），用户从模型库浏览器拖拽到画布时引用此表数据。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | VARCHAR(128) | NOT NULL | UUID | 主键ID |
| category_id | BIGINT | NOT NULL | — | 所属分类ID，关联 biz_model_category 的 ID |
| template_name | VARCHAR(128) | NOT NULL | — | 模型名称 |
| model_name | VARCHAR(128) | NOT NULL | — | 模型真实名称（如 \*\*\*.mph） |
| version | VARCHAR(32) | NULL | '1.0.0' | 模型版本号 |
| model_type | BIGINT | NOT NULL | — | 隶属哪个仿真工具，0:COMSOL、1:MATLAB、2:ANSYS |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_category_id | NORMAL | category_id | 分类查询索引 |

### 4.3 模型模板参数定义表（biz_model_template_param）

定义模型模板所包含的参数项（数值型、文件型、矩阵型等），供画布中组件参数设置使用。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| template_id | BIGINT | NOT NULL | — | 所属模型模板ID，关联 biz_model_template 的 id |
| param_name | VARCHAR(128) | NOT NULL | — | 参数名称 |
| param_code | VARCHAR(64) | NOT NULL | — | 参数编码 |
| param_type | VARCHAR(32) | NOT NULL | — | 参数类型：number/file/matrix/select/text |
| param_unit | VARCHAR(32) | NULL | NULL | 参数单位（如 V、A、℃、mm） |
| default_value | TEXT | NULL | NULL | 默认值（JSON格式存储复杂类型） |
| min_value | DECIMAL(20,6) | NULL | NULL | 最小值（数值型参数） |
| max_value | DECIMAL(20,6) | NULL | NULL | 最大值（数值型参数） |
| options | TEXT | NULL | NULL | 可选值列表（JSON数组，select类型使用） |
| is_required | TINYINT(1) | NOT NULL | 1 | 是否必填：0=否，1=是 |
| param_group | VARCHAR(64) | NULL | NULL | 参数分组名称（如"全局参数"/"矩阵参数"） |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| param_direction | INT | NULL | NULL | 输入输出方向，0:IN、1:OUT |
| param_enname | VARCHAR(128) | NOT NULL | — | 参数英文名称 |
| description | VARCHAR(512) | NULL | NULL | 参数描述/提示信息 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_template_id | NORMAL | template_id | 模型模板查询索引 |
| uk_template_param | UNIQUE | template_id, param_code | 模板参数编码联合唯一索引 |

### 4.4 模型模板接口定义表（biz_model_template_port）

定义模型的输入/输出接口，用于组件之间的连线验证和数据流转。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| template_id | BIGINT | NOT NULL | — | 所属模型模板ID |
| port_name | VARCHAR(64) | NOT NULL | — | 接口名称 |
| port_code | VARCHAR(64) | NOT NULL | — | 接口编码 |
| port_direction | VARCHAR(10) | NOT NULL | — | 接口方向：input/output |
| data_type | VARCHAR(64) | NULL | NULL | 数据类型标识（用于连线兼容性校验） |
| position | VARCHAR(16) | NULL | NULL | 接口在组件上的位置：top/bottom/left/right |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| description | VARCHAR(255) | NULL | NULL | 接口描述 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_template_id | NORMAL | template_id | 模型模板查询索引 |

### 4.5 用户模型表（biz_user_model）

用户在项目中创建的自定义模型（显示在用户模型库中）。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| project_id | BIGINT | NOT NULL | — | 所属项目ID |
| model_name | VARCHAR(128) | NOT NULL | — | 模型名称 |
| canvas_id | BIGINT | NULL | NULL | 关联的画布ID |
| owner_id | BIGINT | NOT NULL | — | 所有者用户ID |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_project_id | NORMAL | project_id | 项目查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |

### 4.6 用户画布组件实例参数值表（biz_user_model_param_value）

存储用户在参数设置中为组件实例配置的实际参数值。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| component_id | BIGINT | NOT NULL | — | 画布组件实例ID |
| param_id | BIGINT | NOT NULL | — | 参数定义ID（关联模板参数表） |
| param_value | TEXT | NULL | NULL | 参数值（JSON格式，支持各种参数类型） |
| file_url | VARCHAR(512) | NULL | NULL | 文件型参数的文件路径 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_component_id | NORMAL | component_id | 组件实例查询索引 |
| uk_component_param | UNIQUE | component_id, param_id | 组件参数联合唯一索引 |

### 4.7 画布表（biz_canvas）

每个用户模型对应一个画布，存储画布的全局配置信息。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| project_id | BIGINT | NOT NULL | — | 所属项目ID |
| user_model_id | BIGINT | NULL | — | 关联用户模型ID |
| canvas_name | VARCHAR(128) | NULL | NULL | 画布名称 |
| zoom_level | DECIMAL(5,2) | NULL | 1.00 | 缩放级别 |
| offset_x | DECIMAL(10,2) | NULL | 0.00 | 画布X轴偏移量 |
| offset_y | DECIMAL(10,2) | NULL | 0.00 | 画布Y轴偏移量 |
| canvas_data | JSON | NULL | NULL | 画布额外配置数据（JSON） |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_project_id | NORMAL | project_id | 项目查询索引 |
| idx_user_model_id | NORMAL | user_model_id | 用户模型查询索引 |

### 4.8 画布组件实例表（biz_canvas_component）

记录用户从模型库拖拽到画布中的组件实例信息，包括位置和变换状态。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | 前端生成 | 主键ID（由前端生成） |
| canvas_id | BIGINT | NOT NULL | — | 所属画布ID |
| template_id | BIGINT | NOT NULL | — | 引用的模型模板ID |
| instance_name | VARCHAR(128) | NOT NULL | — | 组件实例名称（支持重命名） |
| position_x | DECIMAL(10,2) | NOT NULL | 0.00 | 在画布上的X坐标 |
| position_y | DECIMAL(10,2) | NOT NULL | 0.00 | 在画布上的Y坐标 |
| width | DECIMAL(10,2) | NULL | NULL | 组件宽度 |
| height | DECIMAL(10,2) | NULL | NULL | 组件高度 |
| rotation | INT | NOT NULL | 0 | 旋转角度（0/90/180/270） |
| flip_horizontal | TINYINT(1) | NOT NULL | 0 | 是否水平翻转：0=否，1=是 |
| flip_vertical | TINYINT(1) | NOT NULL | 0 | 是否竖直翻转：0=否，1=是 |
| z_index | INT | NOT NULL | 0 | 层级顺序 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_canvas_id | NORMAL | canvas_id | 画布查询索引 |
| idx_template_id | NORMAL | template_id | 模型模板查询索引 |

### 4.9 画布组件连线表（biz_canvas_connection）

记录画布中组件之间的连线关系（数据流/仿真执行顺序），连线成功表示存在仿真上下游关系。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| canvas_id | BIGINT | NOT NULL | — | 所属画布ID |
| source_component_id | BIGINT | NOT NULL | — | 源组件（上游）实例ID |
| source_port_id | BIGINT | NOT NULL | — | 源组件输出接口ID |
| target_component_id | BIGINT | NOT NULL | — | 目标组件（下游）实例ID |
| target_port_id | BIGINT | NOT NULL | — | 目标组件输入接口ID |
| line_start | VARCHAR(32) | NULL | — | 连线起始位置，用于前端识别 |
| line_end | VARCHAR(16) | NULL | — | 连线结束位置，用于前端识别 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_canvas_id | NORMAL | canvas_id | 画布查询索引 |
| idx_source_component | NORMAL | source_component_id | 源组件查询索引 |
| idx_target_component | NORMAL | target_component_id | 目标组件查询索引 |

---

## 5. 仿真管理模块

### 5.1 仿真配置表（biz_simulation_config）

存储仿真运行前的配置信息，包括算法、步长、步数等仿真设置参数。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| component_id | BIGINT | NOT NULL | — | 关联画布组件实例ID，关联 biz_canvas_component 的 id |
| config_name | VARCHAR(128) | NULL | NULL | 配置名称 |
| solver_type | VARCHAR(64) | NULL | NULL | 求解器/算法类型 |
| time_step | DECIMAL(20,10) | NULL | NULL | 仿真步长 |
| total_steps | INT | NULL | NULL | 仿真总步数 |
| start_time | DECIMAL(20,10) | NULL | 0.0 | 仿真起始时间 |
| end_time | DECIMAL(20,10) | NULL | NULL | 仿真结束时间 |
| tolerance | DECIMAL(20,15) | NULL | NULL | 收敛容差 |
| max_iterations | INT | NULL | NULL | 最大迭代次数 |
| extra_config | JSON | NULL | NULL | 扩展配置参数（JSON格式） |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_component_id | NORMAL | component_id | 画布组件实例查询索引 |

### 5.2 仿真任务执行表（biz_simulation_task）

记录每次仿真运行的任务信息和执行状态。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 主键ID（UUID） |
| component_id | BIGINT | NOT NULL | — | 关联画布组件实例ID，关联 biz_canvas_component 的 id |
| task_status | TINYINT | NOT NULL | 0 | 任务状态：0=待运行，1=运行中，2=已完成，3=失败，4=已取消 |
| progress | DECIMAL(5,2) | NULL | 0.00 | 执行进度（百分比 0-100） |
| start_time | DATETIME | NULL | NULL | 开始执行时间 |
| end_time | DATETIME | NULL | NULL | 结束执行时间 |
| duration | INT | NULL | NULL | 执行耗时（秒） |
| error_message | TEXT | NULL | NULL | 错误信息（失败时记录） |
| executor_id | BIGINT | NOT NULL | — | 执行人用户ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_component_id | NORMAL | component_id | 画布组件实例查询索引 |
| idx_executor_id | NORMAL | executor_id | 执行人查询索引 |
| idx_task_status | NORMAL | task_status | 任务状态查询索引 |
| idx_create_time | NORMAL | create_time | 创建时间排序索引 |

### 5.3 仿真日志表（biz_simulation_log）

记录仿真执行过程中的详细日志输出，在仿真日志窗口中展示。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| task_id | BIGINT | NOT NULL | — | 关联仿真任务ID |
| log_level | VARCHAR(16) | NOT NULL | 'INFO' | 日志级别：DEBUG/INFO/WARN/ERROR |
| log_content | TEXT | NOT NULL | — | 日志内容 |
| log_time | DATETIME(3) | NOT NULL | CURRENT_TIMESTAMP(3) | 日志时间（精确到毫秒） |
| component_id | BIGINT | NULL | NULL | 关联组件实例ID（标识日志来源组件） |
| step_index | INT | NULL | NULL | 当前执行步数 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_task_id | NORMAL | task_id | 仿真任务查询索引 |
| idx_log_time | NORMAL | log_time | 日志时间排序索引 |

### 5.4 仿真结果数据表（biz_simulation_result）

存储仿真计算输出的结果数据，用于后处理分析和yt曲线展示。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| task_id | BIGINT | NOT NULL | — | 关联仿真任务ID |
| variable_name | VARCHAR(128) | NOT NULL | — | 变量名称 |
| variable_unit | VARCHAR(32) | NULL | NULL | 变量单位 |
| data_type | VARCHAR(32) | NOT NULL | 'time_series' | 数据类型：time_series/scalar/vector/matrix |
| data_content | LONGTEXT | NULL | NULL | 仿真数据完整结果，数据内容（JSON格式存储时序数据等） |
| data_file_url | VARCHAR(512) | NULL | NULL | 大规模数据文件存储路径（存云图？？） |
| data_size | BIGINT | NULL | NULL | 数据大小（字节） |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_task_id | NORMAL | task_id | 仿真任务查询索引 |
| idx_variable_name | NORMAL | variable_name | 变量名称查询索引 |

---

## 6. 后处理模块（暂时不考虑，可以先不做）

### 6.1 后处理图表配置表（biz_postprocess_chart）

存储后处理yt曲线图表的显示配置（单位、线型、线宽等）。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| project_id | BIGINT | NOT NULL | — | 关联项目ID |
| canvas_id | BIGINT | NULL | NULL | 关联画布ID |
| task_id | BIGINT | NULL | NULL | 关联仿真任务ID |
| chart_name | VARCHAR(128) | NULL | NULL | 图表名称 |
| chart_type | VARCHAR(32) | NOT NULL | 'yt_curve' | 图表类型：yt_curve/xy_scatter/bar 等 |
| x_axis_label | VARCHAR(64) | NULL | NULL | X轴标签 |
| x_axis_unit | VARCHAR(32) | NULL | NULL | X轴单位 |
| y_axis_label | VARCHAR(64) | NULL | NULL | Y轴标签 |
| y_axis_unit | VARCHAR(32) | NULL | NULL | Y轴单位 |
| zoom_level | DECIMAL(5,2) | NULL | 1.00 | 图表缩放级别 |
| display_config | JSON | NULL | NULL | 额外显示配置（JSON格式） |
| owner_id | BIGINT | NOT NULL | — | 创建者ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_project_id | NORMAL | project_id | 项目查询索引 |
| idx_task_id | NORMAL | task_id | 仿真任务查询索引 |

### 6.2 后处理曲线数据表（biz_postprocess_curve）

存储图表中每条曲线的数据来源与显示样式配置。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| chart_id | BIGINT | NOT NULL | — | 关联图表配置ID |
| result_id | BIGINT | NOT NULL | — | 关联仿真结果数据ID |
| curve_name | VARCHAR(128) | NULL | NULL | 曲线名称/图例标签 |
| line_style | VARCHAR(32) | NULL | 'solid' | 线型：solid/dashed/dotted/dash_dot |
| line_width | DECIMAL(4,1) | NULL | 1.0 | 线宽（像素） |
| line_color | VARCHAR(16) | NULL | NULL | 线颜色（十六进制色值） |
| show_data_points | TINYINT(1) | NULL | 0 | 是否显示数据点：0=否，1=是 |
| data_point_style | VARCHAR(32) | NULL | 'circle' | 数据点样式：circle/square/triangle/diamond |
| data_point_size | DECIMAL(4,1) | NULL | 4.0 | 数据点大小 |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_chart_id | NORMAL | chart_id | 图表查询索引 |
| idx_result_id | NORMAL | result_id | 仿真结果查询索引 |

---

## 7. 模型库管理模块

### 7.1 个人模型库文件夹表（biz_model_lib_folder）

> **待确认：** 此表与 4.1 模型库浏览器分类表（biz_model_category）在功能上是否存在重合？两者的区分如下：biz_model_category 是**平台内置模型的分类**（系统管理员维护，所有用户共享），biz_model_lib_folder 是**用户个人上传模型文件的目录结构**（每个用户独立维护）。如业务上确认两者职责不同，则需保留两张表。

用户自定义的模型库树结构，支持多级文件夹嵌套。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父文件夹ID，0表示根目录 |
| folder_name | VARCHAR(128) | NOT NULL | — | 文件夹名称 |
| folder_path | VARCHAR(1024) | NULL | NULL | 完整路径（自动生成，如 第一段/第二段/当前层级） |
| owner_id | BIGINT | NOT NULL | — | 所有者用户ID |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |

### 7.2 个人模型库文件表（biz_model_lib_file）

存储在个人模型库中的模型文件信息。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| folder_id | BIGINT | NOT NULL | — | 所属文件夹ID |
| file_name | VARCHAR(255) | NOT NULL | — | 文件名称 |
| file_size | BIGINT | NOT NULL | 0 | 文件大小（字节，前端自动换算单位） |
| file_type | VARCHAR(32) | NULL | NULL | 文件类型/扩展名 |
| file_url | VARCHAR(512) | NOT NULL | — | 文件存储路径/URL |
| file_md5 | VARCHAR(64) | NULL | NULL | 文件MD5校验值（用于去重和覆盖判断） |
| visibility | TINYINT(1) | NOT NULL | 0 | 可见范围：0=仅自己，1=已共享 |
| owner_id | BIGINT | NOT NULL | — | 创建者/所有者ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_folder_id | NORMAL | folder_id | 文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |
| idx_file_name | NORMAL | file_name | 文件名搜索索引 |

---

## 8. 仿真结果集管理模块

### 8.1 仿真结果集文件夹表（biz_result_set_folder）

用户自定义的仿真结果集树结构，支持多级文件夹嵌套。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父文件夹ID，0表示根目录 |
| folder_name | VARCHAR(128) | NOT NULL | — | 文件夹名称 |
| folder_path | VARCHAR(1024) | NULL | NULL | 完整路径 |
| owner_id | BIGINT | NOT NULL | — | 所有者用户ID |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |

### 8.2 仿真结果集文件表（biz_result_set_file）

存储保存到结果集中的仿真结果文件。默认存储30天，过期需自行下载。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| folder_id | BIGINT | NOT NULL | — | 所属文件夹ID |
| task_id | BIGINT | NULL | NULL | 关联仿真任务ID |
| file_name | VARCHAR(255) | NOT NULL | — | 文件名称 |
| file_size | BIGINT | NOT NULL | 0 | 文件大小（字节） |
| file_type | VARCHAR(32) | NULL | NULL | 文件类型 |
| file_url | VARCHAR(512) | NOT NULL | — | 文件存储路径/URL |
| visibility | TINYINT(1) | NOT NULL | 0 | 可见范围：0=仅自己，1=已共享 |
| expire_time | DATETIME | NULL | NULL | 过期时间（默认创建后30天） |
| owner_id | BIGINT | NOT NULL | — | 创建者ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_folder_id | NORMAL | folder_id | 文件夹查询索引 |
| idx_task_id | NORMAL | task_id | 仿真任务查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |
| idx_expire_time | NORMAL | expire_time | 过期时间索引（便于定时清理） |

---

## 9. 报告库管理模块

### 9.1 报告库文件夹表（biz_report_folder）

用户自定义的报告库树结构，支持多级文件夹嵌套。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父文件夹ID，0表示根目录 |
| folder_name | VARCHAR(128) | NOT NULL | — | 文件夹名称 |
| folder_path | VARCHAR(1024) | NULL | NULL | 完整路径 |
| owner_id | BIGINT | NOT NULL | — | 所有者用户ID |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |

### 9.2 报告表（biz_report）

存储用户创建的仿真分析报告。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| folder_id | BIGINT | NOT NULL | — | 所属文件夹ID |
| report_name | VARCHAR(255) | NOT NULL | — | 报告名称 |
| description | VARCHAR(512) | NULL | NULL | 报告简介 |
| template_id | BIGINT | NULL | NULL | 使用的报告模板ID |
| report_content | LONGTEXT | NULL | NULL | 报告正文内容（HTML/JSON格式） |
| report_status | TINYINT(1) | NOT NULL | 0 | 报告状态：0=草稿，1=已完成 |
| visibility | TINYINT(1) | NOT NULL | 0 | 可见范围：0=仅自己，1=已共享 |
| owner_id | BIGINT | NOT NULL | — | 创建者ID |
| last_edit_time | DATETIME | NULL | NULL | 最近编辑时间 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_folder_id | NORMAL | folder_id | 文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |
| idx_template_id | NORMAL | template_id | 模板查询索引 |
| idx_last_edit_time | NORMAL | last_edit_time | 最近编辑时间排序索引 |

### 9.3 报告模板表（biz_report_template）

定义报告生成使用的模板。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| folder_id | BIGINT | NULL | NULL | 所属文件夹ID |
| template_name | VARCHAR(255) | NOT NULL | — | 模板名称 |
| description | VARCHAR(512) | NULL | NULL | 模板简介 |
| template_content | LONGTEXT | NULL | NULL | 模板内容（HTML/JSON格式，含占位符） |
| template_type | VARCHAR(32) | NULL | NULL | 模板类型分类 |
| visibility | TINYINT(1) | NOT NULL | 0 | 可见范围：0=仅自己，1=已共享 |
| owner_id | BIGINT | NOT NULL | — | 创建者ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_folder_id | NORMAL | folder_id | 文件夹查询索引 |
| idx_owner_id | NORMAL | owner_id | 所有者查询索引 |

---

## 10. 共享管理模块

### 10.1 资源共享记录表（biz_share_record）

统一管理模型文件、仿真结果、报告等资源的共享记录，支持共享至组织或个人。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| resource_type | VARCHAR(32) | NOT NULL | — | 资源类型：model_file/result_file/report/report_template |
| resource_id | BIGINT | NOT NULL | — | 资源ID（对应各资源表的主键） |
| share_type | TINYINT(1) | NOT NULL | — | 共享目标类型：1=组织，2=用户 |
| target_id | BIGINT | NOT NULL | — | 共享目标ID（组织ID或用户ID） |
| permission | VARCHAR(32) | NOT NULL | 'read' | 权限类型：read=查看与使用 |
| sharer_id | BIGINT | NOT NULL | — | 共享发起人ID |
| expire_time | DATETIME | NULL | NULL | 共享过期时间（NULL表示永不过期） |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_resource | NORMAL | resource_type, resource_id | 资源查询联合索引 |
| idx_target | NORMAL | share_type, target_id | 共享目标查询联合索引 |
| idx_sharer_id | NORMAL | sharer_id | 共享发起人查询索引 |

---

## 11. 系统管理模块

### 11.1 帮助文档表（sys_help_doc）

存储平台使用帮助手册的内容，支持帮助中心页面展示。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 说明 |
|--------|----------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键ID |
| parent_id | BIGINT | NOT NULL | 0 | 父文档ID，0表示顶级，支持章节层级 |
| title | VARCHAR(255) | NOT NULL | — | 文档标题 |
| content | LONGTEXT | NULL | NULL | 文档内容（富文本/Markdown） |
| sort_order | INT | NOT NULL | 0 | 排序序号 |
| status | TINYINT(1) | NOT NULL | 1 | 状态：0=草稿，1=已发布 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标志 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | NULL | 创建人ID |

**索引设计：**

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| idx_parent_id | NORMAL | parent_id | 父文档查询索引 |

---

## 12. 数据字典

### 12.1 用户状态（sys_user.status）

| 值 | 含义 |
|----|------|
| 0 | 禁用 |
| 1 | 启用 |

### 12.2 登录操作类型（sys_login_log.operation_type）

| 值 | 含义 |
|----|------|
| 1 | 登录 |
| 2 | 注销登录 |

### 12.3 登录状态（sys_login_log.login_status）

| 值 | 含义 |
|----|------|
| 0 | 失败 |
| 1 | 成功 |

### 12.4 项目状态（biz_project.status）

| 值 | 含义 |
|----|------|
| 0 | 已完成 |
| 1 | 进行中 |

### 12.5 仿真任务状态（biz_simulation_task.task_status）

| 值 | 含义 |
|----|------|
| 0 | 待运行 |
| 1 | 运行中 |
| 2 | 已完成 |
| 3 | 失败 |
| 4 | 已取消 |

### 12.6 模型参数类型（biz_model_template_param.param_type）

| 值 | 含义 |
|----|------|
| number | 数值型参数 |
| file | 文件型参数 |
| matrix | 矩阵型参数 |
| select | 下拉选择型参数 |
| text | 文本型参数 |

### 12.7 接口方向（biz_model_template_port.port_direction）

| 值 | 含义 |
|----|------|
| input | 输入接口 |
| output | 输出接口 |

### 12.8 资源类型（biz_share_record.resource_type）

| 值 | 含义 |
|----|------|
| model_file | 模型文件 |
| result_file | 仿真结果文件 |
| report | 报告 |
| report_template | 报告模板 |

### 12.9 共享目标类型（biz_share_record.share_type）

| 值 | 含义 |
|----|------|
| 1 | 共享至组织 |
| 2 | 共享至用户 |

### 12.10 可见范围（visibility）

| 值 | 含义 |
|----|------|
| 0 | 仅自己可见 |
| 1 | 已共享（具体共享对象见 biz_share_record） |

### 12.11 逻辑删除标志（is_deleted）

| 值 | 含义 |
|----|------|
| 0 | 未删除 |
| 1 | 已删除 |

### 12.12 仿真工具类型（biz_model_template.model_type）

| 值 | 含义 |
|----|------|
| 0 | COMSOL |
| 1 | MATLAB |
| 2 | ANSYS |

### 12.13 参数方向（biz_model_template_param.param_direction）

| 值 | 含义 |
|----|------|
| 0 | IN（输入参数） |
| 1 | OUT（输出参数） |

---

## 13. E-R 关系说明

### 13.1 核心实体关系

```
sys_user (1) ──── (N) biz_project             用户拥有多个项目
sys_user (1) ──── (N) sys_login_log           用户拥有多条登录日志
sys_user (N) ──── (N) sys_role                用户与角色多对多（通过 sys_user_role）
sys_user (N) ──── (1) sys_organization        用户归属组织（多对一）

biz_project (1) ──── (N) biz_user_model        项目包含多个用户模型
biz_user_model (1) ── (1) biz_canvas            用户模型对应一个画布
biz_canvas (1) ──── (N) biz_canvas_component   画布包含多个组件实例
biz_canvas (1) ──── (N) biz_canvas_connection  画布包含多条连线
biz_canvas_component (1) ── (N) biz_user_model_param_value  组件实例拥有多个参数值

biz_model_category (1) ── (N) biz_model_template     分类包含多个模型模板
biz_model_template (1) ── (N) biz_model_template_param  模板定义多个参数
biz_model_template (1) ── (N) biz_model_template_port   模板定义多个接口
biz_canvas_component (N) ── (1) biz_model_template      组件实例引用模型模板

biz_canvas_component (1) ── (N) biz_simulation_config  组件实例关联多个仿真配置
biz_canvas_component (1) ── (N) biz_simulation_task    组件实例关联多次仿真执行
biz_simulation_task (1) ── (N) biz_simulation_log      任务包含多条日志
biz_simulation_task (1) ── (N) biz_simulation_result    任务产出多条结果数据

biz_postprocess_chart (1) ── (N) biz_postprocess_curve  图表包含多条曲线
biz_postprocess_curve (N) ── (1) biz_simulation_result  曲线关联仿真结果

biz_model_lib_folder (1) ── (N) biz_model_lib_file   文件夹包含多个模型文件
biz_result_set_folder (1) ── (N) biz_result_set_file  文件夹包含多个结果文件
biz_report_folder (1) ── (N) biz_report              文件夹包含多个报告

biz_share_record ──── 多态关联 ──── biz_model_lib_file / biz_result_set_file / biz_report
```

### 13.2 树结构关系

以下表使用 `parent_id` 实现自引用树结构：
- `sys_organization`：组织机构树（公司 → 部门 → 科室）
- `biz_model_category`：模型分类树
- `biz_model_lib_folder`：个人模型库文件夹树
- `biz_result_set_folder`：仿真结果集文件夹树
- `biz_report_folder`：报告库文件夹树
- `sys_help_doc`：帮助文档章节树

---

## 14. 附录：DDL建表脚本

以下为完整的MySQL建表SQL脚本，可直接执行创建所有数据表。

```sql
-- ============================================================
-- 工业仿真平台 MySQL 数据库建表脚本
-- 字符集: utf8mb4 | 引擎: InnoDB
-- ============================================================

-- -----------------------------------------------------------
-- 1. 用户管理模块
-- -----------------------------------------------------------

-- 1.1 用户基本信息表
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

-- -----------------------------------------------------------
-- 2. 项目管理模块
-- -----------------------------------------------------------

-- 2.1 项目表
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
-- 3. 建模管理模块
-- -----------------------------------------------------------

-- 3.1 模型库浏览器分类表
CREATE TABLE `biz_model_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级',
  `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模型库浏览器分类表';

-- 3.2 模型模板表
CREATE TABLE `biz_model_template` (
  `id` VARCHAR(128) NOT NULL COMMENT '主键ID（UUID）',
  `category_id` BIGINT NOT NULL COMMENT '所属分类ID',
  `template_name` VARCHAR(128) NOT NULL COMMENT '模型名称',
  `model_name` VARCHAR(128) NOT NULL COMMENT '模型真实名称（如 ***.mph）',
  `version` VARCHAR(32) DEFAULT '1.0.0' COMMENT '版本号',
  `model_type` BIGINT NOT NULL COMMENT '隶属仿真工具，0:COMSOL、1:MATLAB、2:ANSYS',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模型模板表';

-- 3.3 模型模板参数定义表
CREATE TABLE `biz_model_template_param` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` BIGINT NOT NULL COMMENT '所属模型模板ID',
  `param_name` VARCHAR(128) NOT NULL COMMENT '参数名称',
  `param_code` VARCHAR(64) NOT NULL COMMENT '参数编码',
  `param_type` VARCHAR(32) NOT NULL COMMENT '参数类型：number/file/matrix/select/text',
  `param_unit` VARCHAR(32) DEFAULT NULL COMMENT '参数单位',
  `default_value` TEXT DEFAULT NULL COMMENT '默认值（JSON格式）',
  `min_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最小值',
  `max_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最大值',
  `options` TEXT DEFAULT NULL COMMENT '可选值列表（JSON数组）',
  `is_required` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必填',
  `param_group` VARCHAR(64) DEFAULT NULL COMMENT '参数分组名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `param_direction` INT DEFAULT NULL COMMENT '输入输出方向，0:IN、1:OUT',
  `param_enname` VARCHAR(128) NOT NULL COMMENT '参数英文名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '参数描述',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_param` (`template_id`, `param_code`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模型模板参数定义表';

-- 3.4 模型模板接口定义表
CREATE TABLE `biz_model_template_port` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` BIGINT NOT NULL COMMENT '所属模型模板ID',
  `port_name` VARCHAR(64) NOT NULL COMMENT '接口名称',
  `port_code` VARCHAR(64) NOT NULL COMMENT '接口编码',
  `port_direction` VARCHAR(10) NOT NULL COMMENT '接口方向：input/output',
  `data_type` VARCHAR(64) DEFAULT NULL COMMENT '数据类型标识',
  `position` VARCHAR(16) DEFAULT NULL COMMENT '接口位置：top/bottom/left/right',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '接口描述',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模型模板接口定义表';

-- 3.5 用户模型表
CREATE TABLE `biz_user_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
  `model_name` VARCHAR(128) NOT NULL COMMENT '模型名称',
  `canvas_id` BIGINT DEFAULT NULL COMMENT '关联画布ID',
  `owner_id` BIGINT NOT NULL COMMENT '所有者用户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户模型表';

-- 3.6 用户画布组件实例参数值表
CREATE TABLE `biz_user_model_param_value` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `component_id` BIGINT NOT NULL COMMENT '画布组件实例ID',
  `param_id` BIGINT NOT NULL COMMENT '参数定义ID',
  `param_value` TEXT DEFAULT NULL COMMENT '参数值（JSON格式）',
  `file_url` VARCHAR(512) DEFAULT NULL COMMENT '文件型参数的文件路径',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_component_param` (`component_id`, `param_id`),
  KEY `idx_component_id` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户画布组件实例参数值表';

-- 3.7 画布表
CREATE TABLE `biz_canvas` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
  `user_model_id` BIGINT DEFAULT NULL COMMENT '关联用户模型ID',
  `canvas_name` VARCHAR(128) DEFAULT NULL COMMENT '画布名称',
  `zoom_level` DECIMAL(5,2) DEFAULT 1.00 COMMENT '缩放级别',
  `offset_x` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'X轴偏移量',
  `offset_y` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Y轴偏移量',
  `canvas_data` JSON DEFAULT NULL COMMENT '画布额外配置（JSON）',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_model_id` (`user_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='画布表';

-- 3.8 画布组件实例表（id由前端生成）
CREATE TABLE `biz_canvas_component` (
  `id` BIGINT NOT NULL COMMENT '主键ID（前端生成）',
  `canvas_id` BIGINT NOT NULL COMMENT '所属画布ID',
  `template_id` BIGINT NOT NULL COMMENT '引用的模型模板ID',
  `instance_name` VARCHAR(128) NOT NULL COMMENT '组件实例名称',
  `position_x` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'X坐标',
  `position_y` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Y坐标',
  `width` DECIMAL(10,2) DEFAULT NULL COMMENT '组件宽度',
  `height` DECIMAL(10,2) DEFAULT NULL COMMENT '组件高度',
  `rotation` INT NOT NULL DEFAULT 0 COMMENT '旋转角度',
  `flip_horizontal` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '水平翻转',
  `flip_vertical` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '竖直翻转',
  `z_index` INT NOT NULL DEFAULT 0 COMMENT '层级顺序',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_canvas_id` (`canvas_id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='画布组件实例表';

-- 3.9 画布组件连线表
CREATE TABLE `biz_canvas_connection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `canvas_id` BIGINT NOT NULL COMMENT '所属画布ID',
  `source_component_id` BIGINT NOT NULL COMMENT '源组件实例ID',
  `source_port_id` BIGINT NOT NULL COMMENT '源组件输出接口ID',
  `target_component_id` BIGINT NOT NULL COMMENT '目标组件实例ID',
  `target_port_id` BIGINT NOT NULL COMMENT '目标组件输入接口ID',
  `line_start` VARCHAR(32) DEFAULT NULL COMMENT '连线起始位置，用于前端识别',
  `line_end` VARCHAR(16) DEFAULT NULL COMMENT '连线结束位置，用于前端识别',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_canvas_id` (`canvas_id`),
  KEY `idx_source_component` (`source_component_id`),
  KEY `idx_target_component` (`target_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='画布组件连线表';

-- -----------------------------------------------------------
-- 4. 仿真管理模块
-- -----------------------------------------------------------

-- 4.1 仿真配置表
CREATE TABLE `biz_simulation_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `component_id` BIGINT NOT NULL COMMENT '关联画布组件实例ID',
  `config_name` VARCHAR(128) DEFAULT NULL COMMENT '配置名称',
  `solver_type` VARCHAR(64) DEFAULT NULL COMMENT '求解器/算法类型',
  `time_step` DECIMAL(20,10) DEFAULT NULL COMMENT '仿真步长',
  `total_steps` INT DEFAULT NULL COMMENT '仿真总步数',
  `start_time` DECIMAL(20,10) DEFAULT 0.0 COMMENT '仿真起始时间',
  `end_time` DECIMAL(20,10) DEFAULT NULL COMMENT '仿真结束时间',
  `tolerance` DECIMAL(20,15) DEFAULT NULL COMMENT '收敛容差',
  `max_iterations` INT DEFAULT NULL COMMENT '最大迭代次数',
  `extra_config` JSON DEFAULT NULL COMMENT '扩展配置参数（JSON）',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_component_id` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真配置表';

-- 4.2 仿真任务执行表
CREATE TABLE `biz_simulation_task` (
  `id` VARCHAR(36) NOT NULL COMMENT '主键ID（UUID）',
  `component_id` BIGINT NOT NULL COMMENT '关联画布组件实例ID',
  `task_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待运行，1=运行中，2=已完成，3=失败，4=已取消',
  `progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始执行时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束执行时间',
  `duration` INT DEFAULT NULL COMMENT '执行耗时（秒）',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `executor_id` BIGINT NOT NULL COMMENT '执行人用户ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_component_id` (`component_id`),
  KEY `idx_executor_id` (`executor_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真任务执行表';

-- 4.3 仿真日志表
CREATE TABLE `biz_simulation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '关联仿真任务ID',
  `log_level` VARCHAR(16) NOT NULL DEFAULT 'INFO' COMMENT '日志级别',
  `log_content` TEXT NOT NULL COMMENT '日志内容',
  `log_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '日志时间',
  `component_id` BIGINT DEFAULT NULL COMMENT '关联组件实例ID',
  `step_index` INT DEFAULT NULL COMMENT '当前执行步数',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_log_time` (`log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真日志表';

-- 4.4 仿真结果数据表
CREATE TABLE `biz_simulation_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '关联仿真任务ID',
  `variable_name` VARCHAR(128) NOT NULL COMMENT '变量名称',
  `variable_unit` VARCHAR(32) DEFAULT NULL COMMENT '变量单位',
  `data_type` VARCHAR(32) NOT NULL DEFAULT 'time_series' COMMENT '数据类型',
  `data_content` LONGTEXT DEFAULT NULL COMMENT '仿真数据完整结果（JSON格式）',
  `data_file_url` VARCHAR(512) DEFAULT NULL COMMENT '大规模数据文件存储路径',
  `data_size` BIGINT DEFAULT NULL COMMENT '数据大小（字节）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_variable_name` (`variable_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真结果数据表';

-- -----------------------------------------------------------
-- 5. 后处理模块（暂时不考虑，可以先不做）
-- -----------------------------------------------------------

-- 5.1 后处理图表配置表
CREATE TABLE `biz_postprocess_chart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '关联项目ID',
  `canvas_id` BIGINT DEFAULT NULL COMMENT '关联画布ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '关联仿真任务ID',
  `chart_name` VARCHAR(128) DEFAULT NULL COMMENT '图表名称',
  `chart_type` VARCHAR(32) NOT NULL DEFAULT 'yt_curve' COMMENT '图表类型',
  `x_axis_label` VARCHAR(64) DEFAULT NULL COMMENT 'X轴标签',
  `x_axis_unit` VARCHAR(32) DEFAULT NULL COMMENT 'X轴单位',
  `y_axis_label` VARCHAR(64) DEFAULT NULL COMMENT 'Y轴标签',
  `y_axis_unit` VARCHAR(32) DEFAULT NULL COMMENT 'Y轴单位',
  `zoom_level` DECIMAL(5,2) DEFAULT 1.00 COMMENT '图表缩放级别',
  `display_config` JSON DEFAULT NULL COMMENT '额外显示配置（JSON）',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后处理图表配置表';

-- 5.2 后处理曲线数据表
CREATE TABLE `biz_postprocess_curve` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `chart_id` BIGINT NOT NULL COMMENT '关联图表配置ID',
  `result_id` BIGINT NOT NULL COMMENT '关联仿真结果数据ID',
  `curve_name` VARCHAR(128) DEFAULT NULL COMMENT '曲线名称',
  `line_style` VARCHAR(32) DEFAULT 'solid' COMMENT '线型',
  `line_width` DECIMAL(4,1) DEFAULT 1.0 COMMENT '线宽',
  `line_color` VARCHAR(16) DEFAULT NULL COMMENT '线颜色',
  `show_data_points` TINYINT(1) DEFAULT 0 COMMENT '是否显示数据点',
  `data_point_style` VARCHAR(32) DEFAULT 'circle' COMMENT '数据点样式',
  `data_point_size` DECIMAL(4,1) DEFAULT 4.0 COMMENT '数据点大小',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_chart_id` (`chart_id`),
  KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后处理曲线数据表';

-- -----------------------------------------------------------
-- 6. 模型库管理模块
-- -----------------------------------------------------------

-- 6.1 个人模型库文件夹表
CREATE TABLE `biz_model_lib_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
  `folder_name` VARCHAR(128) NOT NULL COMMENT '文件夹名称',
  `folder_path` VARCHAR(1024) DEFAULT NULL COMMENT '完整路径',
  `owner_id` BIGINT NOT NULL COMMENT '所有者用户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人模型库文件夹表';

-- 6.2 个人模型库文件表
CREATE TABLE `biz_model_lib_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `folder_id` BIGINT NOT NULL COMMENT '所属文件夹ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` VARCHAR(32) DEFAULT NULL COMMENT '文件类型',
  `file_url` VARCHAR(512) NOT NULL COMMENT '文件存储路径',
  `file_md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5校验值',
  `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0=仅自己，1=已共享',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_file_name` (`file_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人模型库文件表';

-- -----------------------------------------------------------
-- 7. 仿真结果集管理模块
-- -----------------------------------------------------------

-- 7.1 仿真结果集文件夹表
CREATE TABLE `biz_result_set_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
  `folder_name` VARCHAR(128) NOT NULL COMMENT '文件夹名称',
  `folder_path` VARCHAR(1024) DEFAULT NULL COMMENT '完整路径',
  `owner_id` BIGINT NOT NULL COMMENT '所有者用户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真结果集文件夹表';

-- 7.2 仿真结果集文件表
CREATE TABLE `biz_result_set_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `folder_id` BIGINT NOT NULL COMMENT '所属文件夹ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '关联仿真任务ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` VARCHAR(32) DEFAULT NULL COMMENT '文件类型',
  `file_url` VARCHAR(512) NOT NULL COMMENT '文件存储路径',
  `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0=仅自己，1=已共享',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（默认30天）',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仿真结果集文件表';

-- -----------------------------------------------------------
-- 8. 报告库管理模块
-- -----------------------------------------------------------

-- 8.1 报告库文件夹表
CREATE TABLE `biz_report_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
  `folder_name` VARCHAR(128) NOT NULL COMMENT '文件夹名称',
  `folder_path` VARCHAR(1024) DEFAULT NULL COMMENT '完整路径',
  `owner_id` BIGINT NOT NULL COMMENT '所有者用户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报告库文件夹表';

-- 8.2 报告表
CREATE TABLE `biz_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `folder_id` BIGINT NOT NULL COMMENT '所属文件夹ID',
  `report_name` VARCHAR(255) NOT NULL COMMENT '报告名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '报告简介',
  `template_id` BIGINT DEFAULT NULL COMMENT '报告模板ID',
  `report_content` LONGTEXT DEFAULT NULL COMMENT '报告正文内容',
  `report_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0=草稿，1=已完成',
  `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0=仅自己，1=已共享',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `last_edit_time` DATETIME DEFAULT NULL COMMENT '最近编辑时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_last_edit_time` (`last_edit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报告表';

-- 8.3 报告模板表
CREATE TABLE `biz_report_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `folder_id` BIGINT DEFAULT NULL COMMENT '所属文件夹ID',
  `template_name` VARCHAR(255) NOT NULL COMMENT '模板名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '模板简介',
  `template_content` LONGTEXT DEFAULT NULL COMMENT '模板内容',
  `template_type` VARCHAR(32) DEFAULT NULL COMMENT '模板类型',
  `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0=仅自己，1=已共享',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报告模板表';

-- -----------------------------------------------------------
-- 9. 共享管理模块
-- -----------------------------------------------------------

-- 9.1 资源共享记录表
CREATE TABLE `biz_share_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型：model_file/result_file/report/report_template',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `share_type` TINYINT(1) NOT NULL COMMENT '共享目标类型：1=组织，2=用户',
  `target_id` BIGINT NOT NULL COMMENT '共享目标ID',
  `permission` VARCHAR(32) NOT NULL DEFAULT 'read' COMMENT '权限类型',
  `sharer_id` BIGINT NOT NULL COMMENT '共享发起人ID',
  `expire_time` DATETIME DEFAULT NULL COMMENT '共享过期时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resource` (`resource_type`, `resource_id`),
  KEY `idx_target` (`share_type`, `target_id`),
  KEY `idx_sharer_id` (`sharer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='资源共享记录表';

-- -----------------------------------------------------------
-- 10. 系统管理模块
-- -----------------------------------------------------------

-- 10.1 帮助文档表
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
```

---

## 15. 版本记录

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| V1.0 | 2026-03-09 | 初始版本，包含全部30张业务数据表设计 | — |
