# SynthraSim 工业仿真平台 - 项目架构文档

## 1. 技术架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                     前端（Vue/React）                         │
│              通过 HTTP + JWT Token 调用后端接口                │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP请求
                           │ Header: Authorization: Bearer {token}
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  synthrasim-admin（启动模块）                  │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │ Controller层 │  │ 验证码接口    │  │ 代码生成接口       │  │
│  │ (RESTful API)│  │ /captchaImage│  │ /tool/gen/*       │  │
│  └──────┬──────┘  └──────────────┘  └───────────────────┘  │
│         │                                                   │
│         │ 调用                                               │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │          synthrasim-framework（框架模块）              │    │
│  │                                                     │    │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────┐  │    │
│  │  │Security配置   │  │JWT过滤器     │  │权限切面    │  │    │
│  │  │(白名单/CORS)  │  │(Token解析)   │  │(@Requires │  │    │
│  │  │              │  │             │  │Permissions)│  │    │
│  │  └──────────────┘  └─────────────┘  └───────────┘  │    │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────┐  │    │
│  │  │MyBatisPlus配置│  │Redis配置     │  │Knife4j配置│  │    │
│  │  │(分页/自动填充) │  │(序列化)      │  │(API文档)  │  │    │
│  │  └──────────────┘  └─────────────┘  └───────────┘  │    │
│  └─────────────────────────┬───────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           synthrasim-system（系统模块）                │    │
│  │                                                     │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │    │
│  │  │ Domain层  │  │ Mapper层 │  │ Service层         │  │    │
│  │  │ (实体类)  │  │ (数据访问)│  │ (业务逻辑)        │  │    │
│  │  │          │  │          │  │                  │  │    │
│  │  │ SysUser  │  │ SysUser  │  │ ISysUserService  │  │    │
│  │  │ SysRole  │  │  Mapper  │  │ ISysRoleService  │  │    │
│  │  │ SysOrg   │  │ SysRole  │  │ ISysLoginLog     │  │    │
│  │  │ SysLogin │  │  Mapper  │  │  Service         │  │    │
│  │  │  Log     │  │  ...     │  │                  │  │    │
│  │  └──────────┘  └──────────┘  └──────────────────┘  │    │
│  └─────────────────────────┬───────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           synthrasim-common（公共模块）                │    │
│  │                                                     │    │
│  │  常量定义 │ 工具类 │ 异常处理 │ 基础实体 │ Redis工具   │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
         ┌────────┐  ┌────────┐  ┌──────────┐
         │ MySQL  │  │ Redis  │  │ 文件存储  │
         │ :8086  │  │ :6379  │  │ (预留)    │
         └────────┘  └────────┘  └──────────┘
```

## 2. 模块依赖关系

```
synthrasim-admin（启动入口）
  ├── synthrasim-framework（框架配置）
  │     ├── synthrasim-system（业务逻辑）
  │     │     └── synthrasim-common（公共工具）
  │     └── MySQL驱动 / Druid连接池
  └── synthrasim-generator（代码生成）
        └── synthrasim-framework
```

**依赖规则：上层可以依赖下层，下层不能反向依赖上层。**

---

## 3. 各模块详细说明

### 3.1 synthrasim-common（公共模块）

**职责：** 提供全项目共享的基础设施，不包含任何业务逻辑。

| 包路径 | 内容 | 说明 |
|--------|------|------|
| `annotation/` | `@RequiresPermissions`、`@Log` | 自定义注解，标注在Controller方法上 |
| `constant/` | `Constants`、`HttpStatus` | 系统常量（Token前缀、Redis键名、状态码等） |
| `core/domain/` | `AjaxResult`、`BaseEntity` | 统一响应封装、实体基类（公共字段自动填充） |
| `core/domain/model/` | `LoginBody`、`RegisterBody` | 请求体DTO（登录/注册表单数据） |
| `core/page/` | `TableDataInfo` | 分页查询统一响应结构 |
| `core/redis/` | `RedisCache` | Redis操作工具（set/get/delete/expire） |
| `enums/` | `UserStatus` | 用户状态枚举（启用/禁用） |
| `exception/` | `ServiceException`、`GlobalExceptionHandler` | 业务异常 + 全局异常拦截 |
| `utils/` | `SecurityUtils`、`StringUtils`、`ServletUtils`、`IpUtils` | 工具类集合 |

**关键类解释：**

**AjaxResult** — 所有接口的统一返回格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```
- `AjaxResult.success(data)` → code=200
- `AjaxResult.error("错误信息")` → code=500
- `AjaxResult.error(401, "未认证")` → 自定义状态码

**BaseEntity** — 所有业务实体的父类，自动管理这4个字段：
```
create_by   → INSERT时自动填充当前用户ID
create_time → INSERT时自动填充当前时间
update_by   → INSERT/UPDATE时自动填充
update_time → INSERT/UPDATE时自动填充
```

**RedisCache** — 对RedisTemplate的简化封装：
```java
redisCache.setCacheObject("key", value, 30, TimeUnit.MINUTES); // 写入，30分钟过期
redisCache.getCacheObject("key");                               // 读取
redisCache.deleteObject("key");                                 // 删除
```

---

### 3.2 synthrasim-system（系统模块）

**职责：** 核心业务实体定义、数据库访问、业务逻辑处理。

#### Domain层（实体类，映射数据库表）

| 实体类 | 数据库表 | 说明 |
|--------|----------|------|
| `SysUser` | sys_user | 用户信息（用户名、密码、姓名、邮箱、手机等） |
| `SysRole` | sys_role | 角色（admin超级管理员、user普通用户等） |
| `SysUserRole` | sys_user_role | 用户-角色多对多关联 |
| `SysOrganization` | sys_organization | 组织机构树（公司→部门→科室） |
| `SysLoginLog` | sys_login_log | 登录日志（操作类型、IP、时间等） |

#### Mapper层（数据访问，继承MyBatisPlus BaseMapper）

继承 `BaseMapper<T>` 后**自动获得**以下方法，无需手写SQL：
```java
insert(entity)              // 插入
deleteById(id)              // 按ID删除
updateById(entity)          // 按ID更新
selectById(id)              // 按ID查询
selectList(wrapper)         // 条件查询列表
selectPage(page, wrapper)   // 分页查询
```

自定义方法（用`@Select`注解写SQL）：
```java
// SysUserMapper - 支持用户名/手机号/邮箱三种方式登录
SysUser selectUserByUsername(String username);

// SysRoleMapper - 查询用户拥有的所有角色
List<SysRole> selectRolesByUserId(Long userId);
```

#### Service层（业务逻辑）

| Service | 核心方法 | 说明 |
|---------|----------|------|
| `ISysUserService` | `selectUserByUsername()` | 登录时查询用户 |
| | `checkUsernameUnique()` | 注册时校验用户名唯一 |
| | `registerUser()` | 注册新用户 |
| | `updateUserProfile()` | 修改个人资料 |
| | `resetUserPwd()` | 修改密码 |
| | `insertUserAuth()` | 给用户分配角色 |
| `ISysRoleService` | `selectRolesByUserId()` | 查询用户角色 |
| | `selectRoleCodesByUserId()` | 获取角色编码集合（权限判断用） |
| `ISysLoginLogService` | `insertLoginLog()` | 记录登录日志 |
| | `selectLoginLogPage()` | 分页查询登录日志 |

---

### 3.3 synthrasim-framework（框架模块）

**职责：** 安全认证、全局配置、基础设施。**这是整个项目最核心的模块。**

#### 3.3.1 认证流程（JWT + Redis）

```
【用户登录】
  ┌──────────┐    POST /login       ┌───────────────┐
  │  前端     │ ──────────────────→  │ SysLoginService│
  │          │    username/password  │               │
  │          │                      │  ①验证码校验    │
  │          │                      │  ②调用Security │
  │          │                      │   认证管理器    │
  │          │                      └───────┬───────┘
  │          │                              │
  │          │                              ▼
  │          │                      ┌───────────────┐
  │          │                      │UserDetailsSvc │
  │          │                      │               │
  │          │                      │ ③查询数据库    │
  │          │                      │ ④校验用户状态  │
  │          │                      │ ⑤加载角色权限  │
  │          │                      └───────┬───────┘
  │          │                              │ 认证成功
  │          │                              ▼
  │          │                      ┌───────────────┐
  │          │                      │ TokenService   │
  │          │                      │               │
  │          │    ← token ─────── │ ⑥生成UUID      │
  │          │                      │ ⑦存Redis      │
  │          │                      │ ⑧生成JWT返回   │
  └──────────┘                      └───────────────┘

【后续请求认证】
  ┌──────────┐  Header: Bearer xxx  ┌───────────────┐
  │  前端     │ ──────────────────→  │JwtAuthFilter  │
  │          │                      │               │
  │          │                      │ ①提取JWT      │
  │          │                      │ ②解析出UUID   │
  │          │                      │ ③Redis取用户   │
  │          │                      │ ④检查有效期    │
  │          │                      │ ⑤放入Security  │
  │          │                      │  Context      │
  │          │                      └───────┬───────┘
  │          │                              │
  │          │                              ▼
  │          │                      ┌───────────────┐
  │          │  ← JSON响应 ──────  │ Controller     │
  └──────────┘                      └───────────────┘
```

#### 3.3.2 Security配置（SecurityConfig.java）

**白名单接口（无需Token即可访问）：**

| 路径 | 说明 |
|------|------|
| `POST /login` | 用户登录 |
| `POST /register` | 用户注册 |
| `GET /captchaImage` | 获取验证码 |
| `/doc.html`、`/swagger-*`、`/v2/**`、`/webjars/**` | Knife4j接口文档 |
| `/druid/**` | Druid数据库监控 |

**其他所有接口 → 必须携带有效Token。**

#### 3.3.3 权限校验（PermissionAspect.java）

在Controller方法上标注注解即可控制权限：
```java
@RequiresPermissions("system:user:list")           // 需要用户列表权限
@RequiresPermissions("system:user:add")             // 需要用户新增权限
@RequiresPermissions(value = {"a", "b"}, logical = Logical.OR)  // 满足任一权限即可
```

校验逻辑：
- 从当前登录用户的 `permissions` 集合中检查是否包含所需权限
- admin角色拥有 `*:*:*` 通配权限，跳过所有校验
- 不具备权限 → 返回 `403 没有权限访问此资源`

#### 3.3.4 配置类汇总

| 配置类 | 功能 |
|--------|------|
| `SecurityConfig` | Security过滤器链、白名单、CORS跨域、密码编码器 |
| `MybatisPlusConfig` | 分页插件、createTime/updateTime自动填充 |
| `RedisConfig` | Redis序列化（Key用String，Value用JSON） |
| `Knife4jConfig` | API文档分组、Token认证配置 |
| `KaptchaConfig` | 验证码图片样式（尺寸、字体、字符数等） |

---

### 3.4 synthrasim-admin（启动模块）

**职责：** Controller层 + Application入口 + 配置文件。最终打包为可执行JAR。

#### API接口清单

##### 用户认证（无需Token）

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | `/login` | 用户登录 | `{"username":"admin","password":"admin123"}` |
| POST | `/register` | 用户注册 | `{"username":"test","password":"123456","realName":"测试"}` |
| GET | `/captchaImage` | 获取验证码 | 无 |
| POST | `/logout` | 退出登录 | 无（Header中带Token） |

##### 个人中心（需Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getInfo` | 获取当前用户信息+角色+权限 |
| GET | `/system/user/profile` | 获取个人资料详情 |
| PUT | `/system/user/profile` | 修改个人资料（邮箱/手机/办公电话/工作地） |
| PUT | `/system/user/profile/updatePwd` | 修改密码 |
| PUT | `/system/user/profile/avatar` | 修改头像 |

##### 用户管理（需Token + 权限）

| 方法 | 路径 | 所需权限 | 说明 |
|------|------|----------|------|
| GET | `/system/user/list` | system:user:list | 用户列表（分页） |
| GET | `/system/user/{userId}` | system:user:query | 用户详情 |
| POST | `/system/user` | system:user:add | 新增用户 |
| PUT | `/system/user` | system:user:edit | 修改用户 |
| DELETE | `/system/user/{userIds}` | system:user:remove | 删除用户 |
| PUT | `/system/user/resetPwd` | system:user:resetPwd | 重置密码 |
| PUT | `/system/user/authRole` | system:user:edit | 分配角色 |

##### 角色管理（需Token + 权限）

| 方法 | 路径 | 所需权限 | 说明 |
|------|------|----------|------|
| GET | `/system/role/list` | system:role:list | 角色列表（分页） |
| GET | `/system/role/all` | 无 | 查询所有角色 |
| GET | `/system/role/{roleId}` | system:role:query | 角色详情 |
| POST | `/system/role` | system:role:add | 新增角色 |
| PUT | `/system/role` | system:role:edit | 修改角色 |
| DELETE | `/system/role/{roleIds}` | system:role:remove | 删除角色 |

##### 登录日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/system/loginLog/list` | 查询当前用户登录日志（分页） |

##### 代码生成

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tool/gen/db/list` | 查询数据库所有表 |
| GET | `/tool/gen/column/{tableName}` | 查询表字段信息 |
| GET | `/tool/gen/preview/{tableName}` | 预览生成代码 |
| GET | `/tool/gen/download/{tableName}` | 下载生成代码ZIP |

---

### 3.5 synthrasim-generator（代码生成模块）

**职责：** 读取数据库表结构，通过Velocity模板自动生成CRUD代码。

**生成流程：**
```
数据库表 → 读取information_schema → 构建GenTable对象 → Velocity模板渲染 → 生成代码文件
```

**为每张表自动生成6个文件：**

| 模板 | 生成物 | 示例（biz_project表） |
|------|--------|----------------------|
| domain.java.vm | 实体类 | `Project.java` |
| mapper.java.vm | Mapper接口 | `ProjectMapper.java` |
| service.java.vm | Service接口 | `IProjectService.java` |
| serviceImpl.java.vm | Service实现 | `ProjectServiceImpl.java` |
| controller.java.vm | Controller | `ProjectController.java` |
| mapper.xml.vm | MyBatis XML | `ProjectMapper.xml` |

**使用方法：**
1. 访问 `GET /tool/gen/db/list` 查看所有数据库表
2. 访问 `GET /tool/gen/preview/biz_project` 预览生成的代码
3. 访问 `GET /tool/gen/download/biz_project` 下载ZIP压缩包
4. 解压后将代码复制到对应模块中

---

## 4. Knife4j 接口文档使用指南

### 4.1 访问地址

启动项目后浏览器打开：**http://localhost:8080/doc.html**

### 4.2 页面布局

```
┌──────────────────────────────────────────────────────┐
│  Authorize按钮                               搜索     │
├──────────────┬───────────────────────────────────────┤
│              │                                       │
│  左侧菜单     │         右侧接口详情                    │
│              │                                       │
│  ▸ 用户认证   │   接口地址、请求方式、参数说明             │
│    · 用户登录  │                                       │
│    · 用户注册  │   请求示例：                            │
│    · 获取信息  │   {                                   │
│              │     "username": "admin",               │
│  ▸ 个人中心   │     "password": "admin123"             │
│  ▸ 用户管理   │   }                                   │
│  ▸ 角色管理   │                                       │
│  ▸ 登录日志   │   [发送] 按钮                           │
│  ▸ 代码生成   │                                       │
│              │   响应结果：                             │
│              │   { "code": 200, "token": "xxx" }     │
│              │                                       │
└──────────────┴───────────────────────────────────────┘
```

### 4.3 测试接口的完整步骤

#### 第一步：测试无需Token的接口（登录/注册）

**登录和注册不需要填写任何请求头**，直接在请求体中填写参数即可。

1. 左侧菜单展开 **「用户认证」** → 点击 **「用户登录」**
2. 在右侧的请求参数区域，填写JSON：
```json
{
  "username": "admin",
  "password": "admin123"
}
```
> **注意：** `code` 和 `uuid` 字段是验证码相关的，当前验证码校验是**可选的**（code和uuid为空时会跳过校验），所以直接登录时**不需要填这两个字段**。

3. 点击 **「发送」** 按钮
4. 响应结果中会返回 `token` 值：
```json
{
  "code": 200,
  "msg": "登录成功",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleS..."
}
```
5. **复制这个token值**，下一步要用

#### 第二步：设置全局Token（解锁所有需认证的接口）

1. 点击页面顶部的 **「Authorize」** 按钮（或页面右上角的锁图标）
2. 在弹出的输入框中填入：
```
Bearer eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleS...
```
> **格式必须是：** `Bearer` + 空格 + token值（Bearer是固定前缀）

3. 点击 **「确认」/「Authorize」**
4. 此后文档中所有接口请求都会自动携带这个Token

#### 第三步：测试需要Token的接口

设置全局Token后，可以直接测试其他接口：
- 点击 **「个人中心」→「获取个人信息」→「发送」**，将返回当前登录用户资料
- 点击 **「用户管理」→「查询用户列表」→「发送」**，将返回用户分页数据

### 4.4 常见问题

| 现象 | 原因 | 解决方法 |
|------|------|----------|
| 返回 `{"code":401,"msg":"认证失败，请重新登录"}` | 没有设置Token / Token过期 | 重新登录获取新Token，在Authorize中填入 |
| 返回 `{"code":403,"msg":"没有权限访问此资源"}` | 当前用户没有该接口所需的权限 | 用admin账号登录（admin拥有所有权限） |
| 返回 `{"code":500,"msg":"系统内部错误"}` | 后端抛出异常 | 查看IDEA控制台的错误日志 |
| 登录时返回 `"验证码已失效"` | 填了code/uuid但Redis中验证码已过期 | code和uuid两个字段**留空不填**即可跳过验证码 |

### 4.5 默认账号

| 账号 | 密码 | 角色 | 权限 |
|------|------|------|------|
| admin | admin123 | 超级管理员(admin) | 所有权限（`*:*:*`） |
| zhangsan | admin123 | 普通用户(user) | 基本权限 |

---

## 5. 配置文件说明

主配置文件：`synthrasim-admin/src/main/resources/application.yml`

### 5.1 数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:8086/synthrasim_server?...
    username: root
    password: PSX18322002993
```

### 5.2 Redis

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password:          # 无密码留空
    database: 0
```

### 5.3 Token

```yaml
token:
  secret: synthrasimSecretKey2026ForIndustrialSimulation  # JWT签名密钥
  expireTime: 720                                         # Token有效期（分钟）=12小时
```

### 5.4 MyBatis Plus

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true    # 下划线 → 驼峰自动映射
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 控制台打印SQL
  global-config:
    db-config:
      logic-delete-field: isDeleted       # 逻辑删除字段
      logic-delete-value: 1               # 已删除标记值
      logic-not-delete-value: 0           # 未删除标记值
```

---

## 6. 开发规范

### 6.1 新增业务模块的标准步骤

以新增「项目管理」模块为例：

**方法一：使用代码生成器（推荐）**
1. 确保 `biz_project` 表已在数据库中创建
2. 访问 `GET /tool/gen/preview/biz_project` 预览代码
3. 访问 `GET /tool/gen/download/biz_project` 下载ZIP
4. 将生成的文件放入 `synthrasim-system` 对应目录

**方法二：手动创建**
1. 在 `system/domain/` 下创建 `BizProject.java`（继承BaseEntity，加@TableName注解）
2. 在 `system/mapper/` 下创建 `BizProjectMapper.java`（继承BaseMapper）
3. 在 `system/service/` 下创建 `IBizProjectService.java`（继承IService）
4. 在 `system/service/impl/` 下创建 `BizProjectServiceImpl.java`（继承ServiceImpl）
5. 在 `admin/.../controller/` 下创建 `BizProjectController.java`

### 6.2 接口返回规范

```java
// 成功
return AjaxResult.success();                    // {"code":200,"msg":"操作成功"}
return AjaxResult.success(data);                // {"code":200,"msg":"操作成功","data":{...}}
return AjaxResult.success("自定义消息", data);    // {"code":200,"msg":"自定义消息","data":{...}}

// 失败
return AjaxResult.error("错误描述");              // {"code":500,"msg":"错误描述"}
return AjaxResult.error(401, "未认证");           // {"code":401,"msg":"未认证"}

// 分页
return new TableDataInfo(list, total);           // {"code":200,"rows":[...],"total":100}
```

### 6.3 权限注解使用

```java
@RequiresPermissions("system:user:list")                                    // 单个权限
@RequiresPermissions(value = {"system:user:add", "system:user:edit"}, logical = Logical.OR)  // 任一即可
```

---

## 7. 数据库初始化

执行 `sql/init.sql` 脚本：

```bash
mysql -h 127.0.0.1 -P 8086 -u root -pPSX18322002993 < sql/init.sql
```

脚本内容：
- 创建 `synthrasim_server` 数据库
- 创建系统表（sys_user、sys_role、sys_user_role、sys_organization、sys_login_log、sys_help_doc）
- 创建项目表（biz_project）
- 创建代码生成辅助表（gen_table、gen_table_column）
- 插入初始数据（组织机构、角色、管理员账号）
